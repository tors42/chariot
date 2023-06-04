package chariot.model;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;

import chariot.model.Enums.Color;
import chariot.model.Enums.Status;

public sealed interface GameStateEvent {

    enum Type { gameFull, gameState, chatLine, opponentGone }

    default Type type() {
        if (this instanceof Full) return Type.gameFull;
        if (this instanceof State) return Type.gameState;
        if (this instanceof Chat) return Type.chatLine;
        return Type.opponentGone;
    }

    public sealed interface Side permits Anonymous, AI, Account {
        String name();
    }

    public record Account(UserInfo user, int rating, boolean provisional) implements Side {
        @Override public String name() { return user.name(); }
    }

    record Full(
            String id,
            GameType gameType,
            ZonedDateTime createdAt,
            Side white,
            Side black,
            Opt<TournamentId> tournament,
            State state
            ) implements GameStateEvent {
    }

    record State(
            String moves,
            Duration wtime,
            Duration btime,
            Duration winc,
            Duration binc,
            Status status,
            Opt<Color> winner,
            Opt<Color> drawOffer,
            Opt<Color> takebackOffer,
            Opt<String> rematch
            ) implements GameStateEvent {

        public State {
            moves = moves == null ? "" : moves;
        }

        public List<String> moveList() {
            return Arrays.stream(moves.split(" ")).filter(s -> ! s.isEmpty()).toList();
        }
    }

    record Chat(String username, String text, String room) implements GameStateEvent {}

    record OpponentGone(boolean gone, Claim claimable) implements GameStateEvent {
        public boolean canClaim() {
            return claimable() instanceof Yes;
        }
    }

    sealed interface Claim permits No, Soon, Yes {}
    record No() implements Claim {}
    record Soon(Duration timeUntilClaimable) implements Claim {}
    record Yes() implements Claim {}

}
