package chariot.model;

import chariot.internal.Util;
import chariot.model.Enums.Color;
import static chariot.internal.Util.orEmpty;

public sealed interface StreamMove extends Model {

    public record Info(
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
            Color player
            ) implements StreamMove {

        public Info {
            lastMove = orEmpty(lastMove);
            initialFen = orEmpty(initialFen);
        }

        public record Status(int id, String name) {}

        public java.time.ZonedDateTime createdAt() {
            return Util.fromLong(createdTime());
        }
    }

    public record Move(String fen, String lm, int wc, int bc) implements StreamMove {
        public Move {
            // first move has no lm (last move)
            lm = orEmpty(lm);
        }
    }
}
