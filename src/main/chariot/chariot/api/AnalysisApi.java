package chariot.api;

import java.util.function.*;

import chariot.model.*;
import chariot.model.Enums.VariantName;

public interface AnalysisApi {

    One<CloudEvalCacheEntry> cloudEval(String fen, Consumer<Params> params);
    default One<CloudEvalCacheEntry> cloudEval(String fen) {
        return cloudEval(fen, __ -> {});
    }

    interface Params {
        /**
         * @param multiPv Number of variations
         * Default: 1
         */
        Params multiPv(int multiPv);

        /**
         * @param variant Variant
         * Default: standard
         */
        Params variant(VariantName variant);
        default Params variant(Function<VariantName.Provider,VariantName> variant) {
            return variant(variant.apply(VariantName.provider()));
        }
    }

}
