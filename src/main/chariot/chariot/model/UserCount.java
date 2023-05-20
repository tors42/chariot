package chariot.model;

public record UserCount(
        int all, int rated, int ai,
        int draw, int drawH,
        int loss, int lossH,
        int win, int winH,
        int bookmark, int playing,
        int imported, // "import"
        int me) {}
