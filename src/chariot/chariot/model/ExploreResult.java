package chariot.model;

import java.util.*;

import chariot.model.Enums.Color;
import chariot.model.Enums.Speed;

public sealed interface ExploreResult {

    record OpeningDB (
            long white,
            long draws,
            long black,
            List<DBMove> moves,
            List<DBGame> topGames,
            List<DBGame> recentGames,
            List<ERStats> history,
            Optional<EROpening> opening) implements ExploreResult {}
    record ERStats(String month, long black, long draws, long white) {}

    record OpeningPlayer(
            long white,
            long draws,
            long black,
            List<PlayerMove> moves,
            List<PlayerGame> topGames,
            List<PlayerGame> recentGames,
            Optional<EROpening> opening,
            Optional<Integer> queuePosition) implements ExploreResult {}

    record DBMove(
            String uci,
            String san,
            long white,
            long draws,
            long black,
            int averageRating,
            Optional<DBGame> game) {}

    record DBGame(
            String uci,
            String id,
            Optional<Color> winner,
            Speed speed,
            String mode,
            ERPlayer white,
            ERPlayer black,
            int year,
            String month) {}

    record PlayerMove(
            String uci,
            String san,
            long white,
            long draws,
            long black,
            int averageOpponentRating,
            int performance,
            Optional<PlayerGame> game) {}

    record PlayerGame(
            String uci,
            String id,
            Optional<Color> winner,
            String speed,
            String mode,
            ERPlayer white,
            ERPlayer black,
            int year,
            String month) {}

    record ERPlayer(String name, int rating) {}
    record EROpening(String eco, String name) {}
}
