package chariot.internal;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import chariot.Client.Scope;
import chariot.internal.Config.ServerType;
import chariot.model.Err;

public record Request<T>(
        String path,
        String postData,
        Duration timeout,
        Map<String, String> headers,
        Function<String, T> mapper,
        Function<String, Err> errorMapper,
        Scope scope,
        boolean delete,
        ServerType target,
        boolean stream) {

    public static<T> Request<T> of(Builder<T> builder) {
        return new Request<>(builder.path, builder.postData, builder.timeout, builder.headers, builder.mapper, builder.errorMapper, builder.scope, builder.delete, builder.target, builder.stream);
    }

    public static class Builder<T> {
        private final String endpoint;
        private final Function<String, T> mapper;

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
        private Function<String, Err> errorMapper = ModelMapper.mapper(Err.class);

        public Builder(String endpoint, Function<String, T> mapper) {
            this.endpoint = endpoint;
            this.mapper = mapper;
        }

        public Builder<T> path(Object... pathParameters) {
            Objects.requireNonNull(pathParameters);
            this.pathParameters = List.of(pathParameters);
            return this;
        }

        public Builder<T> query(Map<String, Object> queryParameters) {
            Objects.requireNonNull(queryParameters);
            this.queryParameters = queryParameters;
            return this;
        }

        public Builder<T> post(String postData) {
            this.postData = postData;
            return this;
        }
        public Builder<T> post(Map<String, ?> postMap) {
            this.postData = Util.urlEncode(postMap);
            return this;
        }
        public Builder<T> post() {
            this.postData = "";
            return this;
        }

        public Builder<T> delete() {
            this.delete = true;
            return this;
        }

        public Builder<T> timeout(Duration timeout) {
            this.timeout = timeout;
            return this;
        }

        public Builder<T> headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder<T> scope(Scope scope) {
            this.scope = scope;
            return this;
        }

        public Builder<T> serverType(ServerType serverType) {
            this.target = serverType;
            return this;
        }

        public Builder<T> stream() {
            this.stream = true;
            return this;
        }

        public Builder<T> errorMapper(Function<String, Err> errorMapper) {
            this.errorMapper = errorMapper;
            return this;
        }

        public Request<T> build() {

            var withQueryParameters = Util.urlEncodeWithWorkaround(queryParameters);

            path = endpoint.formatted(pathParameters.toArray()) + (
                    withQueryParameters.isEmpty() ?
                    "" :
                    "?" + withQueryParameters
                    );

            return Request.of(this);
        }
    }
}
