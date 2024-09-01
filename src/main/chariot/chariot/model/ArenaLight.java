package chariot.model;

import java.time.Duration;
import java.util.List;

public record ArenaLight(
        TourInfo tourInfo,
        Duration duration,
        ConditionInfo<ArenaCondition> conditions,
        Opt<BattleLight> teamBattle,
        Opt<Winner> winner
        ) {

    public String id() { return tourInfo().id(); }

    public record BattleLight(List<String> teamIds, int nbLeaders) {}
    public record Winner(String id, String name, Opt<String> title) {}
}
