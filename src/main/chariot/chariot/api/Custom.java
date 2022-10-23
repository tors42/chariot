package chariot.api;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import chariot.Client.Scope;
import chariot.internal.ModelMapper;
import chariot.model.Many;
import chariot.model.One;

public interface Custom {

    default <T> EndpointBuilder<T> of(Class<T> model) { return of(ModelMapper.mapper(model)); }
    <T> EndpointBuilder<T> of(Function<String, T> mapper);

    interface EndpointBuilder<T> {
        EndpointBuilder<T> path(String path);
        EndpointBuilder<T> accept(String contentType);
        EndpointBuilder<T> post();
        EndpointBuilder<T> post(String contentType);
        EndpointBuilder<T> put();
        EndpointBuilder<T> put(String contentType);
        EndpointBuilder<T> delete();
        EndpointBuilder<T> scope(Scope scope);
        EndpointBuilder<T> streamMapper(Function<Stream<String>, Stream<T>> mapper);

        EndpointOne<T> toOne();
        EndpointMany<T> toMany();
    }

    interface EndpointOne<T> {
        One<T> request(Consumer<Request> request);
    }

    interface EndpointMany<T> {
        Many<T> request(Consumer<Request> request);
    }

    interface Request {
        Request path(Object... parameters);
        Request query(Map<String, Object> parameters);
        Request body(String body);
        Request body(Map<String,Object> parameters);
        Request headers(Map<String, String> headers);
        Request scope(Scope scope);
        Request stream();
    }
}
