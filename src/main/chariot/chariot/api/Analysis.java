package chariot.api;

import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.Enums.VariantName;
import chariot.model.Result;

public interface Analysis {

    Result<chariot.model.Analysis> cloudEval(String fen, Consumer<Params> params);
    default Result<chariot.model.Analysis> cloudEval(String fen) {
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
