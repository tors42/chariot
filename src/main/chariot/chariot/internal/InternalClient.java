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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import static java.util.function.Predicate.not;

import chariot.Client.Scope;

public class InternalClient {

    private final Config config;
    private final int retrySeconds = 60;

    private final ScheduledExecutorService requestExecutor = Executors.newSingleThreadScheduledExecutor(Util.tf);
    private final ScheduledExecutorService streamExecutor = Executors.newScheduledThreadPool(8, Util.tf);

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

        var executor = request.stream() ? streamExecutor : requestExecutor;
        HttpResponse<Stream<String>> httpResponse;
        try {
            httpResponse = sendWithRetry(executor, httpRequest, BodyHandlers.ofLines());
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

    private <T> HttpResponse<T> sendWithRetry(ScheduledExecutorService executor, HttpRequest httpRequest, BodyHandler<T> bodyHandler) throws Exception {
        var response = sendRequest(executor, httpRequest, bodyHandler, Optional.empty());

        if (response.statusCode() == 429) {
            // Perform a retry in a minute...
            config.logging().request().warning(() -> "%s".formatted(response));

            var builder = HttpRequest.newBuilder(httpRequest, (n, v) -> true);
            httpRequest.timeout().ifPresent(t ->
                    builder.timeout(t.plusSeconds(retrySeconds)));
            var retryHttpRequest = builder.build();

            return sendRequest(executor, retryHttpRequest, bodyHandler, Optional.of(retrySeconds));
        }

        return response;
    }

    private <T> HttpResponse<T> sendRequest(ScheduledExecutorService executor, HttpRequest httpRequest, BodyHandler<T> bodyHandler, Optional<Integer> delaySeconds) throws Exception {
        var future = delaySeconds.isEmpty() ?
            executor.submit(() -> {
                config.logging().request().fine(() -> "%s".formatted(httpRequest));
                return httpClient.send(httpRequest, bodyHandler);
            })
            :
            executor.schedule(() -> {
                config.logging().request().fine(() -> "Retry - %s".formatted(httpRequest));
                return httpClient.send(httpRequest, bodyHandler);
            }, delaySeconds.get(), TimeUnit.SECONDS);

        return future.get();
    }


    // Hmm, there should be a layer where "spacing"/throttling is implemented...
    // There's an "in-the-moment"-implementation of a single retry logic above,
    // on 429.
    // But maybe error codes should be modelled and exposed from this layer,
    // and the calling layer can make the retry and spacing decisions...?
    // Yah - design decisions... "Will fix that later"
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
            response = sendWithRetry(requestExecutor, httpRequest, BodyHandlers.discarding());
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

    public void shutdown() {
        requestExecutor.shutdownNow();
        streamExecutor.shutdownNow();
    }

}
