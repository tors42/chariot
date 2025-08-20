package chariot.internal;

import java.io.InputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import chariot.Client.Scope;
import chariot.model.Ack;
import chariot.model.Many;
import chariot.model.One;
import chariot.internal.Config.ServerType;
import chariot.internal.Util.Method;

public sealed interface RequestParameters {

    public record Parameters(
            String path,
            String data,
            InputStream dataInputStream,
            Method method,
            Duration timeout,
            Map<String, String> headers,
            Scope scope,
            ServerType target,
            boolean stream) {}

    public record ReqAck(
            Parameters parameters,
            Function<RequestResult, Ack> mapper
            ) implements RequestParameters {

        public Ack process(RequestHandler handler) {
            return mapper.apply(handler.request(this));
        }
    }

    public record ReqOne<T>(
            Parameters parameters,
            Function<RequestResult, One<T>> mapper
            ) implements RequestParameters {

        public One<T> process(RequestHandler handler) {
            return mapper.apply(handler.request(this));
        }
    }

    public record ReqMany<T>(
            Parameters parameters,
            Function<RequestResult, Many<T>> mapper
            ) implements RequestParameters {

        public Many<T> process(RequestHandler handler) {
            return mapper.apply(handler.request(this));
        }
    }

    public Parameters parameters();
    default String path() { return parameters().path(); }
    default String data() { return parameters().data(); }
    default InputStream dataInputStream() { return parameters().dataInputStream(); }
    default Method method() { return parameters().method(); }
    default Duration timeout() { return parameters().timeout(); }
    default Map<String, String> headers() { return parameters().headers(); }
    default Scope scope() { return parameters().scope(); }
    default ServerType target() { return parameters().target(); }
    default boolean stream() { return parameters().stream(); }

    public static ReqAck ack(ParamsBuilder builder, Function<RequestResult, Ack> mapper) {
        return new ReqAck(builder.build(), mapper);
    }

    public static <T> ReqOne<T> one(ParamsBuilder builder, Function<RequestResult, One<T>> mapper) {
        return new ReqOne<T>(builder.build(), mapper);
    }

    public static <T> ReqMany<T> many(ParamsBuilder builder, Function<RequestResult, Many<T>> mapper) {
        return new ReqMany<T>(builder.build(), mapper);
    }

    public interface Params {
        Params path(Object... pathParameters);
        Params query(Map<String, Object> queryParameters);
        Params body(InputStream inputStream);
        Params body(String data);
        Params body(Map<String, ?> map);
        Params timeout(Duration timeout);
        Params headers(Map<String, String> headers);
        Params scope(Scope scope);
        Params serverType(ServerType serverType);
        Params stream();
    }

    public static class ParamsBuilder {
        private final String endpoint;
        private final Method method;
        private String path;
        private String data;
        private InputStream dataInputStream;
        private Map<String, ?> dataMap;

        private Duration timeout = Duration.ofSeconds(60);
        private Map<String, String> headers = Map.of();
        private List<Object> pathParameters = List.of();
        private Map<String, Object> queryParameters = Map.of();
        private Scope scope;
        private ServerType target;
        private boolean stream;

        ParamsBuilder(String endpoint, Method method) {
            this.endpoint = Objects.requireNonNull(endpoint);
            this.method = Objects.requireNonNull(method);
        }

        public ParamsBuilder path(Object... pathParameters) { this.pathParameters = List.of(Objects.requireNonNull(pathParameters)); return this; }
        public ParamsBuilder query(Map<String, Object> queryParameters) { this.queryParameters = Objects.requireNonNull(queryParameters); return this; }
        public ParamsBuilder body(InputStream inputStream) { this.dataInputStream = inputStream; return this; }
        public ParamsBuilder body(String data) { this.data = data; return this; }
        public ParamsBuilder body(Map<String, ?> dataMap) { this.dataMap = dataMap; return this; }
        public ParamsBuilder timeout(Duration timeout) { this.timeout = timeout; return this; }
        public ParamsBuilder headers(Map<String, String> headers) { this.headers = headers; return this; }
        public ParamsBuilder scope(Scope scope) { this.scope = scope; return this; }
        public ParamsBuilder serverType(ServerType serverType) { this.target = serverType; return this; }
        public ParamsBuilder stream() { this.stream = true; return this; }

        public Parameters build() {
            var withQueryParameters = Util.urlEncodeWithWorkaround(queryParameters);
            path = endpoint.formatted(pathParameters.toArray()) + (
                    withQueryParameters.isEmpty() ?
                    "" :
                    "?" + withQueryParameters
                    );

            // todo, encode by content-type...
            // currently the only used content-type for "Map<String, ?>" is url-encoding
            if (dataMap != null) {
                data = Util.urlEncode(dataMap);
            }

            return new Parameters(path, data, dataInputStream, method, timeout, headers, scope, target, stream);
        }
    }


}
