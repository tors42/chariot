package chariot.internal;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Redirect;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.function.Predicate.not;

import chariot.Client.Scope;

public class InternalClient {

    private final Config config;
    private final int retryMillis = 60_000;
    private final int burstRefillAfterInactivityMillis = 10_000;
    private final int requestSpacingMillis = 1_000;

    private final int NUMBER_OF_PARALLEL_REQUESTS = 1;
    private final int NUMBER_OF_BURST_REQUESTS = 4;
    private final int NUMBER_OF_STREAM_REQUESTS = 8;

    private final Semaphore singleSemaphore = new Semaphore(NUMBER_OF_PARALLEL_REQUESTS);
    private final Semaphore burstSemaphore = new Semaphore(NUMBER_OF_BURST_REQUESTS);
    private final Semaphore streamSemaphore = new Semaphore(NUMBER_OF_STREAM_REQUESTS);
    private final AtomicLong previousRequestTS = new AtomicLong();
    private final AtomicBoolean throttle429 = new AtomicBoolean();
    private final Lock throttleLock = new ReentrantLock();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .version(Version.HTTP_2)
        .connectTimeout(Duration.ofSeconds(5))
        .followRedirects(Redirect.NORMAL)
        .build();

    public InternalClient(Config config) {
        this.config = config;
    }

    public Config config() {
        return config;
    }

    public synchronized <T> InternalResult<T> fetch(Request<T> request) {

        String host = switch(request.target()) {
            case api -> config.servers().api().get();
            case explorer -> config.servers().explorer().get();
            case tablebase -> config.servers().tablebase().get();
        };

        // :  <-- comment to fix treesitter syntax highlighter, not supporting switch-expressions

        var uri = URI.create(host + request.path());

        var builder = HttpRequest.newBuilder()
            .uri(uri);

        String postData = request.postData();

        var bodyPublisher = postData == null ?
            null :
            postData.isEmpty() ?
                BodyPublishers.noBody() :
                BodyPublishers.ofString(postData);

        // todo,
        // find nicer way to specify HTTP Method...
        if (bodyPublisher == null) {
            if (request.delete()) {
                builder.DELETE();
            } else {
                builder.GET();
            }
        } else {
            builder.POST(bodyPublisher);
        }

        request.headers().put("user-agent", "%s %s".formatted(Util.javaVersion, Util.clientVersion));
        request.headers().forEach((k,v) -> builder.header(k,v));

        var optScope = Optional.ofNullable(request.scope());
        optScope.ifPresent(scope -> {
            if (config instanceof Config.Auth auth) {
                auth.type().getToken(scope).ifPresent(token ->
                        builder.header("authorization", "Bearer " + String.valueOf(token.get())));
            }
        });

        var httpRequest = builder.build();

        HttpResponse<Stream<String>> httpResponse;
        try {
            httpResponse = sendWithRetry(request.stream(), httpRequest, BodyHandlers.ofLines());
        } catch(Exception e) {
            config.logging().request().log(Level.SEVERE, "%s".formatted(httpRequest), e);
            return new InternalResult.Error<>(e.getMessage());
        }

        var statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode <= 299) {
            config.logging().request().info(() -> "%s".formatted(httpResponse));

            var stream = httpResponse.body();

            var mapped = stream
                .map(string -> { config.logging().responsebodyraw().info(() -> string); return string; } )
                .filter(not("{}"::equals)) // Filter out any keep-alive messages
                .map(request.mapper())
                .filter(Objects::nonNull);

            var streamWithCloseHandler = mapped.onClose(() -> config.logging().request().fine(() -> "Closing body stream for %s%n".formatted(httpResponse.request().uri())));
            return new InternalResult.Success<T>(streamWithCloseHandler);

        } else {

            var body = httpResponse.body().collect(Collectors.joining());

            Supplier<String> msg = () -> {
                var headers = httpResponse.headers().map().entrySet().stream().map(e -> "[%s] [%s]".formatted(e.getKey(), e.getValue())).collect(Collectors.joining("\n"));
                return "### %s%nBody:%n%s%nHeaders:%n%s".formatted(httpResponse, body.isEmpty() ? "<no body>" : body, headers);
            };

            // Which status codes to log...?
            // 404 is ok? Not worth logging? At least not as warning?
            // Maybe only 5xx as warning?
            if (statusCode >= 500)
                config.logging().request().warning(msg);
            else
                config.logging().request().info(msg);

            config.logging().responsebodyraw().info(() -> body);

            var mappedError = request.errorMapper().apply(body);

            return new InternalResult.Failure<>(statusCode, mappedError);
        }
    }

    private <T> HttpResponse<T> sendWithRetry(boolean stream, HttpRequest httpRequest, BodyHandler<T> bodyHandler) throws Exception {

        var response = sendRequest(stream, httpRequest, bodyHandler);

        if (response.statusCode() == 429) {

            // Perform a retry in a minute...
            throttle429.set(true);

            config.logging().request().warning(() -> "%s".formatted(response));

            var builder = HttpRequest.newBuilder(httpRequest, (n, v) -> true);
            httpRequest.timeout().ifPresent(t -> builder.timeout(t.plusMillis(retryMillis)));
            var retryHttpRequest = builder.build();

            return sendRequest(stream, retryHttpRequest, bodyHandler);
        }

        return response;
    }

    private void sleep(long millis) {
        try{Thread.sleep(millis);}catch(InterruptedException ie){}
    }

    private <T> HttpResponse<T> sendRequest(
            boolean stream,
            HttpRequest httpRequest,
            BodyHandler<T> bodyHandler) throws Exception {

        throttleLock.lock();
        try {
            if (throttle429.get()) {
                long elapsedSince429 = System.currentTimeMillis() - previousRequestTS.get();
                long wait = retryMillis - elapsedSince429;
                if (wait > 0) {
                    sleep(wait);
                }
                throttle429.set(false);
            }
        } finally {
            throttleLock.unlock();
        }

        Semaphore semaphore = stream ? streamSemaphore : singleSemaphore;

        boolean burst = false;
        if ( ! stream ) {
            burst = burstSemaphore.tryAcquire();
        }

        try {
            if ( ! burst ) {
                semaphore.acquire();

                long elapsedSincePreviousRequest = System.currentTimeMillis() - previousRequestTS.get();

                if (! stream) {
                    if (elapsedSincePreviousRequest > burstRefillAfterInactivityMillis) {
                        burstSemaphore.release(NUMBER_OF_BURST_REQUESTS);
                    }
                }

                if (elapsedSincePreviousRequest < requestSpacingMillis) {
                    long wait = requestSpacingMillis - elapsedSincePreviousRequest;
                    sleep(wait);
                }
            }

            config.logging().request().fine(() -> "%s".formatted(httpRequest));

            var response = httpClient.send(httpRequest, bodyHandler);

            previousRequestTS.set(System.currentTimeMillis());
            return response;
        } finally {
            if ( ! burst ) {
                semaphore.release();
            }
        }
    }

    public synchronized Set<Scope> fetchScopes(String endpointPath, Supplier<char[]> tokenSupplier) {
        String host = config.servers().api().get();
        var uri = URI.create(host + endpointPath);
        var builder = HttpRequest.newBuilder()
            .uri(uri)
            .method("HEAD", BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(15));
        builder.header("authorization", "Bearer " + new String(tokenSupplier.get()));

        var httpRequest = builder.build();

        HttpResponse<Void> response;
        try {
            response = sendWithRetry(false, httpRequest, BodyHandlers.discarding());
        } catch (Exception e) {
            config.logging().request().log(Level.SEVERE, "%s".formatted(httpRequest), e);
            return Set.of();
        }

        var statusCode = response.statusCode();

        Supplier<String> log = () -> {
            var headers = response.headers().map().entrySet().stream()
                .map(e -> "[%s] [%s]".formatted(e.getKey(), e.getValue()))
                .collect(Collectors.joining("\n"));
            return "*** %s%n%nHeaders:%n%s".formatted(response, headers);
        };

        if (statusCode >= 200 && statusCode <= 299) {
            config.logging().auth().info(log);

            return response.headers().allValues("x-oauth-scopes").stream()
                .flatMap(s -> Arrays.stream(s.split(",")))
                .map(String::trim)
                .map(s -> Scope.fromString(s))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
        } else {
            config.logging().request().warning(log);
            return Set.of();
        }
    }

}
