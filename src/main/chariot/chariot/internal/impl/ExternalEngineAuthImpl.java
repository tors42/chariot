package chariot.internal.impl;

import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class ExternalEngineAuthImpl extends ExternalEngineImpl implements ExternalEngineAuth {

    public ExternalEngineAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<ExternalEngineInfo> list() {
        return Endpoint.externalEngineList.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<ExternalEngineInfo> create(ExternalEngineParams params) {
        return Endpoint.externalEngineCreate.newRequest(request -> request
                .body(toJson(params)))
            .process(this);
    }

    @Override
    public One<ExternalEngineInfo> get(String engineId) {
        return Endpoint.externalEngineGet.newRequest(request -> request
                .path(engineId))
            .process(this);
    }

    @Override
    public One<ExternalEngineInfo> update(String engineId, ExternalEngineParams params) {
        return Endpoint.externalEngineUpdate.newRequest(request -> request
                .path(engineId)
                .body(toJson(params)))
            .process(this);
    }

    @Override
    public One<Ack> delete(String engineId) {
        return Endpoint.externalEngineDelete.newRequest(request -> request
                .path(engineId))
            .process(this);
    }

    private String toJson(ExternalEngineParams params) {
        return """
        {
            "name": "%s",
            "maxThreads": %d,
            "maxHash": %d,
            "shallowDepth": %d,
            "deepDepth": %d,
            "providerSecret": "%s"
            %s
            %s
        }
        """.formatted(
                params.name(),
                params.maxThreads(),
                params.maxHash(),
                params.defaultDepth(),
                params.providerSecret(),
                params.variants().isEmpty() ? "" : ",\"variants\":[" + params.variants().stream().map(v -> '"' + v + '"').collect(Collectors.joining(",")),
                params.providerSecret().equals("") ? "" : ",\"providerSecret\":\"" + params.providerSecret() + "\""
                );
    }
}
