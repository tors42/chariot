package chariot.internal.impl;

import java.io.InputStream;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.*;
import chariot.internal.*;
import chariot.model.*;

public class ExternalEngineImpl extends Base implements ExternalEngine {

    ExternalEngineImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<ExternalEngineAnalysis> analyse(String engineId, Consumer<AnalysisParameters> params) {
        return Endpoint.externalEngineAnalyse.newRequest(request -> request
                .path(engineId)
                .body(toJson(params)))
            .process(this);
    }

    @Override
    public One<ExternalEngineRequest> acquire(String providerSecret) {
        return Endpoint.externalEngineAcquire.newRequest(request -> request
                .body("""
                    {"providerSecret":"%s"}
                    """.formatted(providerSecret)))
            .process(this);
    }

    @Override
    public One<Void> answer(String analysisId, InputStream inputStream) {
        return Endpoint.externalEngineAnswer.newRequest(request -> request
                .path(analysisId)
                .body(inputStream))
            .process(this);
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
                    "infinite":%b,
                    "multiPv":%d,
                    "variant":"%s",
                    "initialFen":"%s",
                    "moves":[
                        %s
                    ]
                }
            }
            """.formatted(
                    ap.clientSecret,
                     ap.work.sessionId(),
                     ap.work.threads(),
                     ap.work.hash(),
                     ap.work.infinite(),
                     ap.work.multiPv(),
                     ap.work.variant(),
                     ap.work.initialFen(),
                     ap.work.moves().stream().map(m -> '"' + m + '"').collect(Collectors.joining(",")));
    }

}
