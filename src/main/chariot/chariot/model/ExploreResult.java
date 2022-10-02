package chariot.model;

import java.util.*;

import chariot.model.Enums.Color;

public sealed interface ExploreResult {

    record OpeningDB (
            int white,
            int draws,
            int black,
            List<DBMove> moves,
            List<DBGame> topGames,
            List<DBGame> recentGames,
            Optional<EROpening> opening) implements ExploreResult {}

    record OpeningPlayer(
            int white,
            int draws,
            int black,
            List<PlayerMove> moves,
            List<PlayerGame> topGames,
            List<PlayerGame> recentGames,
            Optional<EROpening> opening) implements ExploreResult {}

    public record DBMove(
            String uci,
            String san,
            int white,
            int draws,
            int black,
            int averageRating,
            Optional<DBGame> game) {}

    public record DBGame(
            String uci,
            String id,
            Optional<Color> winner,
            ERPlayer white,
            ERPlayer black,
            int year,
            String month) {}

    public record PlayerMove(
            String uci,
            String san,
            int white,
            int draws,
            int black,
            int averageOpponentRating,
            int performance,
            Optional<PlayerGame> game) {}

    public record PlayerGame(
            String uci,
            String id,
            Optional<Color> winner,
            String speed,
            String mode,
            ERPlayer white,
            ERPlayer black,
            int year,
            String month) {}

    public record ERPlayer(String name, Integer rating) {}
    public record EROpening(String eco, String name) {}

    public record HistoryStats(List<ERStats> history, EROpening opening) {}
    public record ERStats(String month, int black, int draws, int white) {}
}
