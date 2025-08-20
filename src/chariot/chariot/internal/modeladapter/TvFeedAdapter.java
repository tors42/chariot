package chariot.internal.modeladapter;

import java.time.Duration;
import java.util.List;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import chariot.model.Enums.Color;

public interface TvFeedAdapter {

    static TVFeedEvent nodeToEvent(YayNode node) {
        TVFeedEvent event = null;
        if (! (node instanceof YayObject nodeYo)) return event;
        var data = nodeYo.value().get("d");
        event = switch(nodeYo.getString("t")) {
            case "featured" -> nodeToFeatured(data);
            case "fen"      -> nodeToFen(data);
            default         -> null;
        };
        return event;
    }

    private static TVFeedEvent.Fen nodeToFen(YayNode node) {
        if (! (node instanceof YayObject yo)) return null;
        var fen = yo.getString("fen");
        var lastMove = yo.getString("lm");
        var whiteTime = Duration.ofSeconds(yo.getLong("wc"));
        var blackTime = Duration.ofSeconds(yo.getLong("bc"));
        return new TVFeedEvent.Fen(fen, lastMove, whiteTime, blackTime);
    }

    private static TVFeedEvent.Featured nodeToFeatured(YayNode node) {
        if (! (node instanceof YayObject yo)) return null;

        String id = yo.getString("id");
        Color orientation = Color.valueOf(yo.getString("orientation"));
        List<TVFeedEvent.PlayerInfo> players = yo.value().get("players") instanceof YayArray yarr
            ? yarr.value().stream()
            .filter(YayObject.class::isInstance)
            .map(YayObject.class::cast)
            .map(playerYo -> {
                if (! (playerYo.value().get("user") instanceof YayObject userYo)) return null;
                UserInfo userInfo = UserInfo.of(
                        userYo.getString("id"),
                        userYo.getString("name"),
                        userYo.getString("title"));
                Color color = Color.valueOf(playerYo.getString("color"));
                var rating = playerYo.getInteger("rating");
                var seconds = Duration.ofSeconds(playerYo.getInteger("seconds"));
                var pi = new TVFeedEvent.PlayerInfo(userInfo, color, rating, seconds);
                return pi;
            }).toList()
        : List.of();
        String fen = yo.getString("fen");
        return new TVFeedEvent.Featured(id, orientation, players, fen);
    }
}
