package util;

import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sun.net.httpserver.*;

import util.Scenario.*;
import static util.Assert.filterCast;

public class StubServer implements AutoCloseable {

    private final String src;
    private final Consumer<String> log;
    private final HttpServer server;
    private final Set<String> ignoredHeaderNames = Set.of(
            "connection", "http2-settings", "host", "upgrade", "user-agent");


    public static StubServer start(Scenario scenario) {

        var log = filterCast(scenario.properties().stream(), SLogging.class)
            .map(l -> l.log())
            .findFirst()
            .orElse(text -> {});

        boolean verbose = false;
        if (verbose) {
            final Consumer<String> orig = log;
            log = text -> {
                orig.accept(text);
                System.out.println(text);
            };
        }

        var stubServer = new StubServer(Assert.sourceTestCase(), log);

        stubServer.initializeScenario(scenario);

        //var ot = scenario.properties().stream()
        //    .filter(STimeout.class::isInstance).map(STimeout.class::cast)
        //    .findFirst();
        stubServer.start();

        return stubServer;
    }


    void initializeScenario(Scenario scenario) {

        var scriptIterator = scenario.scripts().iterator();

        HttpHandler handler = exchange -> {
            if (! scriptIterator.hasNext()) {
                respond(exchange, 503, "No script for request!");
                return;
            }
            var script = scriptIterator.next();

            var requestPath = exchange.getRequestURI().getPath();
            var requestQueryParams = exchange.getRequestURI().getQuery();
            var params = requestQueryParams == null
                ? List.<String>of()
                : Arrays.stream(requestQueryParams.split("&"))
                .map(str -> str.split("="))
                .map(arr -> new SQueryParam(arr[0], arr[1]))
                .toList();

            var requestBody = new String(exchange.getRequestBody().readAllBytes());
            var requestHeaders = exchange.getRequestHeaders().entrySet().stream()
                .map(entry -> Map.entry(entry.getKey().toLowerCase(Locale.ROOT), entry.getValue()))
                .sorted(Comparator.comparing(Map.Entry::getKey))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

            log.accept("""
                    Test: %s
                    >>> Request Path
                    %s
                    """
                    .formatted(src, requestPath)
                    +
                    "%s"
                    .formatted(
                        requestQueryParams == null
                        ? ">>> No Query Parameters\n"
                        : """
                        >>> Query Parameters
                        %s
                        """
                        .formatted(requestQueryParams)
                        )
                    +
                    "%s"
                    .formatted(requestBody.isEmpty()
                        ? ">>> No Request Body\n"
                        : """
                        >>> Request Body
                        %s
                        """
                        .formatted(requestBody)
                        )
                    +
                    """
                    >>> Request Headers
                    %s
                    """
                    .formatted(String.join("\n",
                            requestHeaders.entrySet().stream()
                            .sorted(Comparator.comparing((Function<Map.Entry<String, List<String>>, Boolean>)
                                    entry -> ignoredHeaderNames.contains(entry.getKey().toLowerCase(Locale.ROOT)))
                                .thenComparing(Comparator.comparing(Map.Entry::getKey)))
                            .map(entry -> "%-20s: %s".formatted("["+entry.getKey()+"]", entry.getValue()))
                            .toList())));

            var expectedPath = filterCast(script.req().stream(), SPath.class).findFirst();
            if (expectedPath.isPresent()) {
                String expected = expectedPath.get().value();
                if (! Objects.equals(expected, requestPath)) {
                    String message = Util.formatExpectedActual(expected, requestPath);
                    respond(exchange, 503, "Unexpected path!\n%s".formatted(message));
                    return;
                }
            }

            var expectedQueryParams = filterCast(script.req().stream(), SQueryParam.class).toList();
            if (! expectedQueryParams.isEmpty()) {
                var expected = String.join("&",
                        expectedQueryParams.stream()
                        .map(qp -> "%s=%s".formatted(qp.key(), qp.value()))
                        .toList());

                if (! Objects.equals(expected, requestQueryParams)) {
                    String message = Util.formatExpectedActual(expected, requestQueryParams);
                    respond(exchange, 503, "Unexpected Query Params!\n%s".formatted(message));
                    return;
                }
            }


            var expectedBody = filterCast(script.req().stream(), SBody.class).findFirst();
            if (expectedBody.isPresent()) {
                String expected = new String(expectedBody.get().value());
                if (! Objects.equals(expected, requestBody)) {
                    String message = Util.formatExpectedActual(expected, requestBody);
                    respond(exchange, 503, "Unexpected body!\n%s".formatted(message));
                    return;
                }
            }

            var expectedHeaders = filterCast(script.req().stream(), SHeader.class).toList();
            if (! expectedHeaders.isEmpty()) {
                var expected = expectedHeaders.stream()
                    .sorted(Comparator.comparing(SHeader::key))
                    .collect(Collectors.toMap(SHeader::key, SHeader::value));

                var subsetActualHeaders = requestHeaders.entrySet().stream()
                        .filter(entry -> ! ignoredHeaderNames.contains(entry.getKey()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

                if (! Objects.equals(expected, subsetActualHeaders)) {
                    String message = Util.formatExpectedActual(expected, subsetActualHeaders);
                    respond(exchange, 503, "Unexpected Headers!\n%s".formatted(message));
                    return;
                }
            }

            // Response
            int responseStatus = filterCast(script.res().stream(), SStatus.class).findFirst()
                .map(SStatus::value)
                .orElse(200);

            byte[] responseBody = filterCast(script.res().stream(), SBody.class).findFirst()
                .map(SBody::value).orElse(null);

            List<byte[]> responseBodies = filterCast(script.res().stream(), SStreamBody.class).findFirst()
                .map(SStreamBody::value).orElse(null);

            var responseHeaders = filterCast(script.res().stream(), SHeader.class)
                .collect(Collectors.toMap(SHeader::key, SHeader::value));

            log.accept("""
                    <<< Status Code
                    %d
                    <<< Response Headers
                    %s
                    <<< Response Body
                    %s
                     """
                    .formatted(
                        responseStatus,
                        String.join("\n",
                            responseHeaders.entrySet().stream()
                            .map(entry -> "%-20s: %s".formatted("["+entry.getKey()+"]", "["+entry.getValue()+"]"))
                            .sorted()
                            .toList()),
                        responseBody == null
                            ? responseBodies.stream()
                                .map(String::new)
                                .collect(Collectors.joining())
                            : new String(responseBody)));

            exchange.getResponseHeaders().putAll(responseHeaders);

            if (responseBody == null) {
                if (responseBodies == null) {
                    respond(exchange, responseStatus);
                } else {
                    respond(exchange, responseStatus, responseBodies);
                }
            } else {
                respond(exchange, responseStatus, responseBody);
            }
        };

        addHandler(handler);
    }

    void respond(HttpExchange exchange, int status, String body) throws IOException {
        respond(exchange, status, body.getBytes());
    }

    void respond(HttpExchange exchange, int status, byte[] body) throws IOException {
        exchange.sendResponseHeaders(status, body.length);
        exchange.getResponseBody().write(body);
        exchange.getResponseBody().flush();
    }

    void respond(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
    }

    void respond(HttpExchange exchange, int status, List<byte[]> bodies) throws IOException {
        exchange.sendResponseHeaders(status, 0);
        for (var body : bodies) {
            exchange.getResponseBody().write(body);
            exchange.getResponseBody().flush();
        }
        exchange.close();
    }

    private StubServer(String src, Consumer<String> log) {
        this.src = src;
        this.log = log;
        try {
            server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
            server.setExecutor(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private StubServer(String src) {
        this(src, text -> {});
    }


    @Override
    public void close() {
        try {
            server.stop(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void start() {
        server.start();
    }

    private HttpContext addHandler(HttpHandler handler) {
        return server.createContext("/", handler);
    }

    public String hostAndPort() {
        var inetSocketAddress = server.getAddress();
        return inetSocketAddress.getHostString() + ":" + inetSocketAddress.getPort();
    }

    public URI uri() {
        return URI.create("http://%s:%d".formatted(
                    server.getAddress().getHostString(),
                    server.getAddress().getPort()));
    }

}
