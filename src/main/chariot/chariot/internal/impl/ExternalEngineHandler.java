package chariot.internal.impl;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class ExternalEngineHandler implements ExternalEngineApiAuth {

    private final RequestHandler requestHandler;

    public ExternalEngineHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }


    @Override
    public Many<ExternalEngineAnalysis> analyse(String engineId, Consumer<AnalysisParameters> params) {
        return Endpoint.externalEngineAnalyse.newRequest(request -> request
                .path(engineId)
                .body(toJson(params)))
            .process(requestHandler);
    }

    @Override
    public One<ExternalEngineRequest> acquire(String providerSecret) {
        return Endpoint.externalEngineAcquire.newRequest(request -> request
                .body("""
                    {"providerSecret":"%s"}
                    """.formatted(providerSecret)))
            .process(requestHandler);
    }

    @Override
    public One<Void> answer(String analysisId, InputStream inputStream) {
        return Endpoint.externalEngineAnswer.newRequest(request -> request
                .path(analysisId)
                .stream()
                .body(inputStream))
            .process(requestHandler);
    }

    String toJson(Consumer<AnalysisParameters> params) {

        var ap = new AnalysisParameters() {
            String clientSecret;
            ExternalEngineWork work;

            @Override
            public AnalysisParameters clientSecret(String clientSecret) {
                this.clientSecret = clientSecret;
                return this;
            }

            @Override
            public AnalysisParameters work(ExternalEngineWork work) {
                this.work = work;
                return this;
            }
        };
        params.accept(ap);

        return """
            {
                "clientSecret":"%s",
                "work": {
                    "sessionId":"%s",
                    "threads":%d,
                    "hash":%d,
                    "multiPv":%d,
                    "variant":"%s",
                    "initialFen":"%s",
                    "moves":[
                        %s
                    ],
                    "movetime":"%d",
                    "depth":"%d",
                    "nodes":"%d"
                }
            }
            """.formatted(
                     ap.clientSecret,
                     ap.work.sessionId(),
                     ap.work.threads(),
                     ap.work.hash(),
                     ap.work.multiPv(),
                     ap.work.variant(),
                     ap.work.initialFen(),
                     ap.work.moves().stream().map(m -> '"' + m + '"').collect(Collectors.joining(",")),
                     ap.work.movetime(),
                     ap.work.depth(),
                     ap.work.nodes());
    }


    @Override
    public Many<ExternalEngineInfo> list() {
        return Endpoint.externalEngineList.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<ExternalEngineInfo> create(ExternalEngineRegistration registration) {
        return Endpoint.externalEngineCreate.newRequest(request -> request
                .body(toJson(registration)))
            .process(requestHandler);
    }

    @Override
    public One<ExternalEngineInfo> get(String engineId) {
        return Endpoint.externalEngineGet.newRequest(request -> request
                .path(engineId))
            .process(requestHandler);
    }

    @Override
    public One<ExternalEngineInfo> update(String engineId, ExternalEngineRegistration registration) {
        return Endpoint.externalEngineUpdate.newRequest(request -> request
                .path(engineId)
                .body(toJson(registration)))
            .process(requestHandler);
    }

    @Override
    public One<Void> delete(String engineId) {
        return Endpoint.externalEngineDelete.newRequest(request -> request
                .path(engineId))
            .process(requestHandler);
    }

    private String toJson(ExternalEngineRegistration registration) {
        return """
        {
            "name": "%s",
            "maxThreads": %d,
            "maxHash": %d,
            "providerSecret": "%s"
            %s
            %s
        }
        """.formatted(
                registration.name(),
                registration.maxThreads(),
                registration.maxHash(),
                registration.providerSecret(),
                registration.variants().isEmpty() ? "" : ",\"variants\":[" + registration.variants().stream().map(v -> '"' + v + '"').collect(Collectors.joining(",")) + "]",
                registration.providerData().equals("") ? "" : ",\"providerData\":\"" + registration.providerData() + "\""
                );
    }
}
