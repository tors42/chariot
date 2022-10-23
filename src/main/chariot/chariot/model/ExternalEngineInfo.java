package chariot.model;

import java.util.List;
import java.util.Objects;

public record ExternalEngineInfo(
    String id,
    String name,
    String clientSecret,
    String userId,
    int maxThreads,
    int maxHash,
    int defaultDepth,
    List<String> variants,
    String providerData
        ) {

    public ExternalEngineInfo {
        providerData = Objects.toString(providerData, "");
    }

}
