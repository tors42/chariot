package tests.model;

import static util.Assert.assertEquals;

import java.util.Map;
import chariot.model.Condition;
import util.Test;

public class TestCondition {

    @Test
    public void parseTeamNameToTeamId() {
        Map.of(
                "Simple",                  "simple",
                "Lichess.org",             "lichessorg",
                "Qe2+ Kxe2",               "qe2-kxe2",
                "That's",                  "thats",
                "Thor - Love and Blunder", "thor-love-and-blunder"
              )
            .forEach( (exampleTeamName, expectedTeamId) -> assertEquals(expectedTeamId, parseViaCondition(exampleTeamName)));
    }

    static String parseViaCondition(String teamName) {
        return switch (Condition.memberByTeamName(teamName)) {
            case Condition.Member(String teamId) -> teamId;
            default -> "Failed to parse [%s]".formatted(teamName);
        };
    }
}
