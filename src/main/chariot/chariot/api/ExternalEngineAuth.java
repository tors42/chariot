package chariot.api;

import java.util.List;
import java.util.Objects;

import chariot.model.*;

public interface ExternalEngineAuth extends ExternalEngine {

    Many<ExternalEngineInfo> list();
    One<ExternalEngineInfo>  create(ExternalEngineParams params);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    One<ExternalEngineInfo>  get(String engineId);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    One<ExternalEngineInfo>  update(String engineId, ExternalEngineParams params);
    /**
     * @param engineId The external engine id. Example: {@code eei_aTKImBJOnv6j}
     */
    One<Ack>                 delete(String engineId);


    public record ExternalEngineParams (
        /**
         * @param name Display name of the engine. [ 3 .. 200 ] characters
         */
        String name,
        /**
         * @param maxThreads Maximum number of available threads. [ 1 .. 65536 ]
         */
        int maxThreads,

        /**
         * @param maxHash Maximum available hash table size, in MiB. [ 1 .. 1048576 ]
         */
        int maxHash,
        /**
         * @param defaultDepth Estimated depth of normal search. [ 0 .. 246 ]
         */
        int defaultDepth,
        /**
         * @param variants List of supported chess variants. "chess" "crazyhouse" "antichess" "atomic" "horde" "kingofthehill" "racingkings" "3check"
         */
        List<String> variants,
        /**
         * @param providerSecret A random token that can be used to wait for analysis requests and provide analysis. The engine provider should securely generate a random string. The token will not be readable again, even by the user. The analysis provider can register multiple engines with the same token, even for different users, and wait for analysis requests from any of them. In this case, the request must not be made via CORS, so that the token is not revealed to any of the users. [ 16 .. 1024 ] characters
         */
        String providerSecret,
        /**
         * @param providerData Arbitrary data that the engine provider can use for identification or bookkeeping. Users can read this information, but updating it requires knowing or changing the {@code providerSecret}.
         */
        String providerData
            ) {

                public ExternalEngineParams {
                    variants = variants == null ? List.of() : variants;
                    providerData = Objects.toString(providerData, "");
                }
            }
}
