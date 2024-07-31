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
import chariot.model.*;

public class InternalClient {

    private final Config config;
    private final int retryMillis = 60_000;
    private final int burstRefillAfterInactivityMillis = 10_000;

    private final int NUMBER_OF_PARALLEL_REQUESTS = 1;
    private final int NUMBER_OF_BURST_REQUESTS = 4;
    private final int NUMBER_OF_STREAM_REQUESTS = 8;

    private final Semaphore singleSemaphore = new Semaphore(NUMBER_OF_PARALLEL_REQUESTS, true);
    private final Semaphore streamSemaphore = new Semaphore(NUMBER_OF_STREAM_REQUESTS, true);
    private final Semaphore burstSemaphore = new Semaphore(NUMBER_OF_BURST_REQUESTS);
    private final Semaphore waitingSemaphore = new Semaphore(0);
    private final AtomicLong previousRequestTS = new AtomicLong();
    private final AtomicBoolean throttle429 = new AtomicBoolean();
    private final Lock throttleLock = new ReentrantLock();

    private final HttpClient httpClient = HttpClient.newBuilder()
        .version(Version.HTTP_3)
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
            case api -> config.servers().api().toString();
            case explorer -> config.servers().explorer().toString();
            case tablebase -> config.servers().tablebase().toString();
            case engine -> config.servers().engine().toString();
        };

        var uri = URI.create(joinUri(baseUri, request.path()));

        var builder = HttpRequest.newBuilder(uri);

        String requestBody = Objects.toString(request.data(), "");
        var bodyPublisher = requestBody.isEmpty() ? BodyPublishers.noBody() : BodyPublishers.ofString(requestBody);

        if (request.dataInputStream() != null) {
            bodyPublisher = BodyPublishers.ofInputStream(() -> request.dataInputStream());
        }

        switch(request.method()) {
            case GET    -> builder.GET();
            case DELETE -> builder.DELETE();
            case POST   -> builder.POST(bodyPublisher);
            case PUT    -> builder.PUT(bodyPublisher);
            case HEAD   -> builder.HEAD();
        };

        if (config instanceof Config.Auth auth) {
            builder.header("authorization", "Bearer " + String.valueOf(auth.token().get()));
        }

        builder.header("user-agent", config.userAgent());
        request.headers().forEach((k,v) -> builder.header(k,v));

        var httpRequest = builder.build();

        config.logging().request().info(() -> "### Request: %s %s%nHeaders:%n%s%nBody:%n%s".formatted(
                    httpRequest.method(),
                    uri,
                    httpRequest.headers().map().entrySet().stream()
                        .flatMap(e -> e.getValue().stream().map(v -> Util.stripSensitive(e.getKey(), v)))
                        .sorted()
                        .collect(Collectors.joining("\n")),
                    requestBody.isEmpty() ? "<no body>" : requestBody)
                );

        HttpResponse<Stream<String>> httpResponse;
        try {
            httpResponse = sendWithRetry(request.stream(), httpRequest, BodyHandlers.ofLines(), config.retries());
        } catch(Exception e) {
            config.logging().request().log(Level.SEVERE, "%s".formatted(httpRequest), e);
            return new RequestResult.Failure(-1, e.getMessage());
        }

        var statusCode = httpResponse.statusCode();
        if (statusCode >= 200 && statusCode <= 299) {

            Supplier<String> msg = () -> {
                var headers = httpResponse.headers().map().entrySet().stream()
                        .map(e -> "[%s] [%s]".formatted(e.getKey(), e.getValue()))
                        .collect(Collectors.joining("\n"));
                return "### Response: %s%nHeaders:%n%s".formatted(httpResponse, headers);
            };

            config.logging().response().info(msg);

            var stream = httpResponse.body()
                .peek(string -> { if (! string.isEmpty()) config.logging().response().info(() -> string); })
                .filter(Predicate.not("{}"::equals)); // Filter out any keep-alive messages

            return new RequestResult.Success(stream);
        } else {
            var responseBody = httpResponse.body().collect(Collectors.joining());

            Supplier<String> msg = () -> {
                var headers = httpResponse.headers().map().entrySet().stream()
                    .map(e -> "[%s] [%s]".formatted(e.getKey(), e.getValue()))
                    .collect(Collectors.joining("\n"));
                return "### Response: %s%nBody:%n%s%nHeaders:%n%s".formatted(
                        httpResponse,
                        responseBody.isEmpty() ? "<no body>" : responseBody,
                        headers);
            };

            if (statusCode >= 500)
                config.logging().response().warning(msg);
            else
                config.logging().response().info(msg);

            config.logging().response().info(() -> responseBody);

            return new RequestResult.Failure(statusCode, responseBody);
        }
    }

    private <T> HttpResponse<T> sendWithRetry(boolean stream, HttpRequest httpRequest, BodyHandler<T> bodyHandler, int retries) throws Exception {

        var response = sendRequest(stream, httpRequest, bodyHandler);

        if (response.statusCode() == 429) {

            // Perform a retry in a minute...
            throttle429.set(true);

            config.logging().response().warning(() -> "%s".formatted(response));

            if (retries > 0) {
                var builder = HttpRequest.newBuilder(httpRequest, (n, v) -> true);
                httpRequest.timeout().ifPresent(t -> builder.timeout(t.plusMillis(retryMillis)));
                var retryHttpRequest = builder.build();
                return sendWithRetry(stream, retryHttpRequest, bodyHandler, retries-1);
            }
        }

        return response;
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
                    waitingSemaphore.tryAcquire(wait, TimeUnit.MILLISECONDS);
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

                if (elapsedSincePreviousRequest < config.spacing().toMillis()) {
                    long wait = config.spacing().toMillis() - elapsedSincePreviousRequest;
                    waitingSemaphore.tryAcquire(wait, TimeUnit.MILLISECONDS);
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

    public Many<Scope> fetchScopes(String endpointPath) {
        return config instanceof Config.Auth auth ?
            fetchScopes(endpointPath, auth.token()) : Many.fail(-1, Err.from("No token"));
    }

    public Many<Scope> fetchScopes(String endpointPath, Supplier<char[]> tokenSupplier) {
        return switch(fetchHeaders(endpointPath, tokenSupplier)) {
            case Entry(var headers) -> Many.entries(
                    headers.allValues("x-oauth-scopes").stream()
                        .flatMap(s -> Arrays.stream(s.split(",")))
                        .map(String::trim)
                        .map(s -> Scope.fromString(s))
                        .filter(Optional::isPresent)
                        .map(Optional::get));
            case Fail(int s, Err err) -> Many.fail(s, err);
            case None() -> Many.entries(Stream.of());
        };
    }

    public One<HttpHeaders> fetchHeaders(String endpointPath) {
        return fetchHeaders(headBuilder(endpointPath));
    }

    public One<HttpHeaders> fetchHeaders(String endpointPath, Supplier<char[]> tokenSupplier) {
        return fetchHeaders(headBuilder(endpointPath)
                .header("authorization", "Bearer " + new String(tokenSupplier.get())));
    }

    private HttpRequest.Builder headBuilder(String endpointPath) {
        var uri = URI.create(joinUri(config.servers().api().toString(), endpointPath));
        var builder = HttpRequest.newBuilder(uri)
            .method("HEAD", BodyPublishers.noBody())
            .timeout(Duration.ofSeconds(15));
        return builder;
    }

    public One<HttpHeaders> fetchHeaders(HttpRequest.Builder builder) {
        var httpRequest = builder.build();

        HttpResponse<Void> response;
        try {
            response = sendWithRetry(false, httpRequest, BodyHandlers.discarding(), config().retries());
        } catch (Exception e) {
            config.logging().request().log(Level.SEVERE, "%s".formatted(httpRequest), e);
            return One.fail(-1, Err.from(e.getMessage()));
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
            return One.entry(response.headers());
        } else {
            config.logging().response().warning(log);
            return One.fail(statusCode, Err.from(""));
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
