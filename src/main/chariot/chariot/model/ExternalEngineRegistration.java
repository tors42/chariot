package chariot.model;

import java.util.*;

public record ExternalEngineRegistration (
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

    public ExternalEngineRegistration(String name, int maxThreads, int maxHash, int defaultDepth, List<String> variants, String providerSecret) {
        this(name, maxThreads, maxHash, defaultDepth, variants, providerSecret, "");
    }

    public ExternalEngineRegistration {
        variants = variants == null ? List.of() : variants;
        providerData = Objects.toString(providerData, "");
    }
}
