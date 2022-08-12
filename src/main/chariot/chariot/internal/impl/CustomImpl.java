package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import chariot.Client.Scope;
import chariot.api.Custom;
import chariot.internal.*;
import chariot.internal.Endpoint.*;
import chariot.internal.RequestParameters.Params;
import chariot.model.Many;
import chariot.model.One;

public class CustomImpl extends Base implements Custom {

    public CustomImpl(InternalClient client) { super(client); }

    @Override
    public <T> EndpointBuilder<T> of(Function<String, T> mapper) {
        return new EndpointBuilder<T>() {
            Builder<T> builder = Endpoint.of(mapper);

            @Override
            public EndpointBuilder<T> path(String path) {
                builder = builder.endpoint(path);
                return this;
            }

            @Override
            public EndpointBuilder<T> accept(String contentType) {
                builder = builder.accept(contentType);
                return this;
            }

            @Override
            public EndpointBuilder<T> contentType(String contentType) {
                builder = builder.contentType(contentType);
                return this;
            }

            @Override
            public EndpointBuilder<T> scope(Scope scope) {
                builder = builder.scope(scope);
                return this;
            }

            @Override
            public EndpointBuilder<T> streamMapper(Function<Stream<String>, Stream<T>> mapper) {
                builder = builder.streamMapper(mapper);
                return this;
            }

            @Override
            public EndpointOne<T> toOne() {
                return new EndpointOne<>() {
                    final EPOne<T> ep = builder.toOne();
                    @Override
                    public One<T> request(Consumer<Request> request) {
                        return ep.newRequest(consumerAdapter.apply(request)).process(CustomImpl.this);
                    }
                };
            }

            @Override
            public EndpointMany<T> toMany() {
                return new EndpointMany<>() {
                    final EPMany<T> ep = builder.toMany();
                    @Override
                    public Many<T> request(Consumer<Request> request) {
                        return ep.newRequest(consumerAdapter.apply(request)).process(CustomImpl.this);
                    }
                };
            }
        };
    }

    Function<Consumer<Request>, Consumer<Params>> consumerAdapter =
        request -> params -> request.accept(new Request() {
            @Override public Request path(Object... parameters) { params.path(parameters); return this; }
            @Override public Request query(Map<String, Object> parameters) { params.query(parameters); return this; }
            @Override public Request post(String body) { params.post(body); return this; }
            @Override public Request post() { params.post(); return this; }
            @Override public Request post(Map<String, Object> parameters) { params.post(parameters); return this; }
            @Override public Request delete() { params.delete(); return this; }
            @Override public Request headers(Map<String, String> headers) { params.headers(headers); return this; }
            @Override public Request scope(Scope scope) { params.scope(scope); return this; }
            @Override public Request stream() { params.stream(); return this; }
            });
}
