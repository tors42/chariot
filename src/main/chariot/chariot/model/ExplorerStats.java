package chariot.model;

import java.util.Map;

public record ExplorerStats(Stats master, Map<String, Stats> lichess) implements Model {

    public record Stats(Long games, Long uniquePositions) {}

}
