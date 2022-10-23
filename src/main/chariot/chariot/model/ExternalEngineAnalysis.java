package chariot.model;

import java.util.List;

public record ExternalEngineAnalysis(
    int time,
    int depth,
    int nodes,
    List<Pvs> pvs
        ) {

    public record Pvs(
            int depth,
            int cp,
            int mate,
            List<String> moves
            ) {}
}
