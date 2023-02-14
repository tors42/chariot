package chariot.model;

import chariot.internal.Util;
import chariot.model.Enums.Color;
import static chariot.internal.Util.orEmpty;

public sealed interface MoveInfo {

    public record GameSummary(
            String id,
            String speed,
            String perf,
            String initialFen,
            String fen,
            Status status,
            String source,
            String lastMove,
            int turns,
            int startedAtTurn,
            Long createdTime,
            boolean rated,
            Variant variant,
            Color player,
            Players players
            ) implements MoveInfo {

        public GameSummary {
            lastMove = orEmpty(lastMove);
            initialFen = orEmpty(initialFen);
        }

        public java.time.ZonedDateTime createdAt() {
            return Util.fromLong(createdTime());
        }
    }

    public record Move(String fen, String lm, int wc, int bc) implements MoveInfo {
        public Move {
            // first move has no lm (last move)
            lm = orEmpty(lm);
        }
    }

    public record Status(int id, String name) {}
    public record Players(Player white, Player black) { }
}
