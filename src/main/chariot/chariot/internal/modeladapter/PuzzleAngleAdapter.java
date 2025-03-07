package chariot.internal.modeladapter;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;

public interface PuzzleAngleAdapter {

    static PuzzleAngle nodeToPuzzleAngle(YayNode node) {
        return switch (node) {
            case YayString(var name) -> switch(name) {
                case "long" -> PuzzleAngle.Theme.long_;
                case "short" -> PuzzleAngle.Theme.short_;
                case String str -> {
                    try {
                        yield PuzzleAngle.Theme.valueOf(str);
                    } catch (Exception e) {}

                    yield new PuzzleAngle.Custom(str);
                }
            };
            default -> PuzzleAngle.Theme.mix;
        };
    }
}
