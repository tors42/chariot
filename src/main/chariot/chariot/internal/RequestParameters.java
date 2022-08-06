package chariot.internal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import chariot.Client.Scope;
import chariot.api.Many;
import chariot.api.One;
import chariot.internal.Config.ServerType;

public sealed interface RequestParameters {

    public record Parameters(
            String path,
            String postData,
            Duration timeout,
            Map<String, String> headers,
            Scope scope,
            boolean delete,
            ServerType target,
            boolean stream) {}

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

    Parameters parameters();
    default String path() { return parameters().path(); }
    default String postData() { return parameters().postData(); }
    default Duration timeout() { return parameters().timeout(); }
    default Map<String, String> headers() { return parameters().headers(); }
    default Scope scope() { return parameters().scope(); }
    default boolean delete() { return parameters().delete(); }
    default ServerType target() { return parameters().target(); }
    default boolean stream() { return parameters().stream(); }

    public static <T> ReqOne<T> one(ParamsBuilder builder, Function<RequestResult, One<T>> mapper) {
        return new ReqOne<T>(builder.build(), mapper);
    }

    public static <T> ReqMany<T> many(ParamsBuilder builder, Function<RequestResult, Many<T>> mapper) {
        return new ReqMany<T>(builder.build(), mapper);
    }

    public interface Params {
        Params path(Object... pathParameters);
        Params query(Map<String, Object> queryParameters);
        Params post(String postData);
        Params post(Map<String, ?> postMap);
        Params post();
        Params delete();
        Params timeout(Duration timeout);
        Params headers(Map<String, String> headers);
        Params scope(Scope scope);
        Params serverType(ServerType serverType);
        Params stream();
    }

    public static class ParamsBuilder {
        private final String endpoint;
        private String path;
        private String postData;
        private Duration timeout = Duration.ofSeconds(60);
        private Map<String, String> headers = Map.of();
        private List<Object> pathParameters = List.of();
        private Map<String, Object> queryParameters = Map.of();
        private Scope scope;
        private boolean delete;
        private ServerType target;
        private boolean stream;

        ParamsBuilder(String endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint);
        }

        public ParamsBuilder path(Object... pathParameters) { this.pathParameters = List.of(Objects.requireNonNull(pathParameters)); return this; }
        public ParamsBuilder query(Map<String, Object> queryParameters) { this.queryParameters = Objects.requireNonNull(queryParameters); return this; }
        public ParamsBuilder post(String postData) { this.postData = postData; return this; }
        public ParamsBuilder post(Map<String, ?> postMap) { this.postData = Util.urlEncode(postMap); return this; }
        public ParamsBuilder post() { this.postData = ""; return this; }
        public ParamsBuilder delete() { this.delete = true; return this; }
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
            return new Parameters(path, postData, timeout, headers, scope, delete, target, stream);
        }
    }


}
