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
    public One<ExternalEngineInfo> create(ExternalEngineRegistration registration) {
        return Endpoint.externalEngineCreate.newRequest(request -> request
                .body(toJson(registration)))
            .process(this);
    }

    @Override
    public One<ExternalEngineInfo> get(String engineId) {
        return Endpoint.externalEngineGet.newRequest(request -> request
                .path(engineId))
            .process(this);
    }

    @Override
    public One<ExternalEngineInfo> update(String engineId, ExternalEngineRegistration registration) {
        return Endpoint.externalEngineUpdate.newRequest(request -> request
                .path(engineId)
                .body(toJson(registration)))
            .process(this);
    }

    @Override
    public One<Ack> delete(String engineId) {
        return Endpoint.externalEngineDelete.newRequest(request -> request
                .path(engineId))
            .process(this);
    }

    private String toJson(ExternalEngineRegistration registration) {
        return """
        {
            "name": "%s",
            "maxThreads": %d,
            "maxHash": %d,
            "defaultDepth": %d,
            "providerSecret": "%s"
            %s
            %s
        }
        """.formatted(
                registration.name(),
                registration.maxThreads(),
                registration.maxHash(),
                registration.defaultDepth(),
                registration.providerSecret(),
                registration.variants().isEmpty() ? "" : ",\"variants\":[" + registration.variants().stream().map(v -> '"' + v + '"').collect(Collectors.joining(",")) + "]",
                registration.providerData().equals("") ? "" : ",\"providerData\":\"" + registration.providerData() + "\""
                );
    }
}
