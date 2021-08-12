package chariot.model;

import chariot.internal.Util;
import chariot.model.Enums.Color;

public sealed interface StreamMove extends Model {

    public record Info(
            String id,
            String speed,
            String perf,
            String initialFen,
            String fen,
            Status status,
            String source,
            String swissId,
            String lastMove,
            Long createdTime,
            boolean rated,
            Variant variant,
            Color player
            ) implements StreamMove {

        public record Status(int id, String name) {}

        public java.time.ZonedDateTime createdAt() {
            return Util.fromLong(createdTime());
        }
    }

    public record Move(String fen, String lm, int wc, int bc) implements StreamMove {}
}
