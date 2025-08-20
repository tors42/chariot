package chariot.internal.modeladapter;

import java.util.*;

import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;

public interface CrosstableAdapter {
    /**
     * <pre>
     * {
     *   "users": {
     *     "neio": 201.5,
     *     "thibault": 144.5
     *   },
     *   "nbGames": 346,
     *   "matchup": {
     *     "users": {
     *       "neio": 44,
     *       "thibault": 43
     *     },
     *     "nbGames": 87
     *   }
     * }
     * </pre>
     *
     * matchup is present only if requested (and available?)
     */
    static Crosstable nodeToCrosstable(YayNode node, YayMapper mapper) {
        return switch(node) {
            case YayObject yo when nodeToCrosstableResults(yo) instanceof Crosstable.Results results ->
                switch(nodeToCrosstableResults(yo.value().get("matchup"))) {
                    case Crosstable.Results matchup -> new Crosstable(results, Opt.of(matchup));
                    case null                       -> new Crosstable(results, Opt.empty());
                };
            case null, default -> null;
        };
    }

    /**
     * <pre>
     * {
     *   "users": {
     *     "neio": 201.5,
     *     "thibault": 144.5
     *   },
     *   "nbGames": 346
     * }
     * </pre>
     */
    private static Crosstable.Results nodeToCrosstableResults(YayNode node) {
        return switch(node) {
            case YayObject yo
                when yo.value().get("nbGames") instanceof YayNumber(var games)
                &&   nodeToCrosstableResultsResult(yo.value().get("users")) instanceof List<Crosstable.Result> results
                -> new Crosstable.Results(results.get(0), results.get(1), games.intValue());
            case null, default -> null;
        };
    }

    /**
     * <pre>
     * {
     *   "neio": 201.5,
     *   "thibault": 144.5
     * }
     * </pre>
     */
    private static List<Crosstable.Result> nodeToCrosstableResultsResult(YayNode node) {
        return switch(node) {
            case YayObject yo
                when yo.filterCastMap(number -> number.value().doubleValue(), YayNumber.class) instanceof Map<String,Double> results
                &&   results.size() == 2
                -> results.entrySet().stream().map(entry -> new Crosstable.Result(entry.getKey(), entry.getValue())).toList();
            case null, default -> null;
        };
    }
}
