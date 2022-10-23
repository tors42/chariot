package chariot.api;

import java.io.InputStream;
import java.util.function.Consumer;

import chariot.model.*;

public interface ExternalEngine {
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    Many<ExternalEngineAnalysis> analyse(String engineId, Consumer<AnalysisParameters> params);

    /**
     * @param providerSecret Provider credentials.
     */
    One<ExternalEngineRequest> acquire(String providerSecret);

    /**
     * @param analysisId The id from the request, {@link ExternalEngineRequest#id()}. Example: {@code aingoohiJee2sius}
     */
    One<Void> answer(String analysisId, InputStream inputStream);

    interface AnalysisParameters {
        AnalysisParameters clientSecret(String clientSecret);
        AnalysisParameters work(ExternalEngineWork work);
    }

}
