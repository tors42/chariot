package chariot.model;

import chariot.internal.Util;

public record PuzzleActivity(String id, long date, boolean win, int puzzleRating)  {

    public java.time.ZonedDateTime dateAt() {
        return Util.fromLong(date());
    }

}
