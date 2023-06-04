package chariot.model;

import java.time.Duration;

import chariot.model.Enums.*;

public record GameInfo(
        String fullId,
        String gameId,
        String fen,
        Color color,
        Status status,
        VariantType variant,
        TimeInfo time,
        boolean rated,
        boolean hasMoved,
        boolean isMyTurn,
        Opponent opponent,
        String source,
        Opt<String> lastMove,
        Opt<Integer> ratingDiff,
        Opt<TournamentId> tournament
        ) {

    public record TimeInfo(Speed speed, Opt<Duration> timeLeft) {}

    public sealed interface Opponent permits AI, Anonymous, Account {
        String id();
        String name();
    }

    public record Account(String id, String name, int rating, Opt<Integer> ratingDiff)  implements Opponent {}
}
