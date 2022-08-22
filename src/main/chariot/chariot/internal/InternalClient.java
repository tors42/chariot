package chariot.internal;

import java.net.URI;
import java.net.http.*;
import java.net.http.HttpClient.*;
import java.net.http.HttpRequest.*;
import java.net.http.HttpResponse.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.concurrent.locks.*;
import java.util.function.*;
import java.util.logging.Level;
import java.util.stream.*;

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

    public RequestResult request(RequestParameters request) {

        String baseUri = switch(request.target()) {
            case api -> config.servers().api().get();
            case explorer -> config.servers().explorer().get();
            case tablebase -> config.servers().tablebase().get();
        };

        var uri = URI.create(joinUri(baseUri, request.path()));

        var builder = HttpRequest.newBuilder()
            .uri(uri);

        String postData = request.postData();
        if (postData == null) {
            if (request.delete()) builder.DELETE();
        } else
            builder.POST(postData.isEmpty() ?
                    BodyPublishers.noBody() :
                    BodyPublishers.ofString(postData));

        if (config instanceof Config.Auth auth) {
            var scope = request.scope() != null ? request.scope() : Scope.any;
            auth.type().getToken(scope).ifPresent(token ->
                    builder.header("authorization", "Bearer " + String.valueOf(token.get())));
        }
        builder.header("user-agent", "%s %s".formatted(Util.javaVersion, Util.clientVersion));
        request.headers().forEach((k,v) -> builder.header(k,v));

        var httpRequest = builder.build();

        config.logging().request().info(() -> "### Request: %s%nBody:%n%s".formatted(uri, postData));

        HttpResponse<Stream<String>> httpResponse;
        try {
            httpResponse = sendWithRetry(request.stream(), httpRequest, BodyHandlers.ofLines(), config.retries());
        } catch(Exception e) {
            config.logging().request().log(Level.SEVERE, "%s".formatted(httpRequest), e);
            return new RequestResult.Failure(-1, e.getMessage());
        }

        var statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode <= 299) {
            config.logging().request().info(() -> "%s".formatted(httpResponse));

            var stream = httpResponse.body()
                .peek(string -> config.logging().responsebodyraw().info(() -> string))
                .filter(Predicate.not("{}"::equals)); // Filter out any keep-alive messages

            return new RequestResult.Success(stream);
        } else {
            var body = httpResponse.body().collect(Collectors.joining());

            Supplier<String> msg = () -> {
                var headers = httpResponse.headers().map().entrySet().stream().map(e -> "[%s] [%s]".formatted(e.getKey(), e.getValue())).collect(Collectors.joining("\n"));
                return "### Response: %s%nBody:%n%s%nHeaders:%n%s".formatted(httpResponse, body.isEmpty() ? "<no body>" : body, headers);
            };

            if (statusCode >= 500)
                config.logging().request().warning(msg);
            else
                config.logging().request().info(msg);

            config.logging().responsebodyraw().info(() -> body);

            return new RequestResult.Failure(statusCode, body);
        }
    }

    private <T> HttpResponse<T> sendWithRetry(boolean stream, HttpRequest httpRequest, BodyHandler<T> bodyHandler, int retries) throws Exception {

        var response = sendRequest(stream, httpRequest, bodyHandler);

        if (response.statusCode() == 429) {

            // Perform a retry in a minute...
            throttle429.set(true);

            config.logging().request().warning(() -> "%s".formatted(response));

            if (retries > 0) {
                var builder = HttpRequest.newBuilder(httpRequest, (n, v) -> true);
                httpRequest.timeout().ifPresent(t -> builder.timeout(t.plusMillis(retryMillis)));
                var retryHttpRequest = builder.build();
                return sendWithRetry(stream, retryHttpRequest, bodyHandler, retries-1);
            }
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

    public Set<Scope> fetchScopes(String endpointPath) {
        return config instanceof Config.Auth auth ?
            auth.type().getToken(Scope.any)
            .map(supplier -> fetchScopes(endpointPath, supplier))
            .orElse(Set.of()) :
            Set.of();
    }

    public Set<Scope> fetchScopes(String endpointPath, Supplier<char[]> tokenSupplier) {
        var uri = URI.create(joinUri(config.servers().api().get(), endpointPath));
        var builder = HttpRequest.newBuilder()
            .uri(uri)
            .method("HEAD", BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(15));
        builder.header("authorization", "Bearer " + new String(tokenSupplier.get()));

        var httpRequest = builder.build();

        HttpResponse<Void> response;
        try {
            response = sendWithRetry(false, httpRequest, BodyHandlers.discarding(), config().retries());
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

    private String joinUri(String baseUri, String path) {
        if (!baseUri.endsWith("/") && !path.startsWith("/")) {
            return baseUri + "/" + path;
        } else if (baseUri.endsWith("/") && path.startsWith("/")) {
            return baseUri + path.substring(1);
        } else {
            return baseUri + path;
        }
    }
}
