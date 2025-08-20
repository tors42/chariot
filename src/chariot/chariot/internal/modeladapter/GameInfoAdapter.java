package chariot.internal.modeladapter;

import java.time.Duration;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import chariot.model.Enums.*;


public interface GameInfoAdapter {

    static GameInfo nodeToInfo(YayNode node) {
        if (! (node instanceof YayObject gameYo)) return null;
        var gameId = gameYo.getString("gameId");
        var fullId = gameYo.getString("fullId");
        var fen = gameYo.getString("fen");
        var color = Color.valueOf(gameYo.getString("color"));
        String lm = gameYo.getString("lastMove");
        Opt<String> lastMove = (lm != null && ! lm.isBlank()) ? Opt.of(lm) : Opt.empty();
        var status = Status.valueOf(((YayObject) gameYo.value().get("status")).getInteger("id"));

        Variant variantType = Variant.Basic.standard;
        if (gameYo.value().get("variant") instanceof YayObject varYo) {
            String key = varYo.getString("key");
            Opt<String> initialFen = Opt.of(gameYo.getString("initialFen"));
            variantType = switch(key) {
                case "chess960"     -> new Variant.Chess960(initialFen);
                case "fromPosition" -> new Variant.FromPosition(initialFen);
                default             -> Variant.Basic.valueOf(key);
            };
        }

        var speed = Speed.valueOf(gameYo.getString("speed"));
        var timeInfo = new GameInfo.TimeInfo(speed, Opt.of(gameYo.getInteger("secondsLeft")).map(Duration::ofSeconds));

        var rated = gameYo.getBool("rated");

        var hasMoved = gameYo.getBool("hasMoved");
        var isMyTurn = gameYo.getBool("isMyTurn");

        GameInfo.Opponent opponent = null;
        if (gameYo.value().get("opponent") instanceof YayObject oppYo) {
            Integer aiLevel = oppYo.getInteger("ai");
            String id = oppYo.getString("id");
            String username = oppYo.getString("username");
            if (aiLevel != null) {
                opponent = username == null
                    ? new AI(aiLevel, "Level %d".formatted(aiLevel))
                    : new AI(aiLevel, username);
            } else if(id == null) {
                opponent = new Anonymous();
            } else {
                int rating = oppYo.getInteger("rating");
                opponent = new GameInfo.Account(id, username, rating, Opt.of(oppYo.getInteger("ratingDiff")));
            }
        }
        var source = gameYo.getString("source");

        TournamentId tournamentInfo = null;
        var swissId = gameYo.getString("swissId");
        var arenaId = gameYo.getString("tournamentId");
        if (swissId != null) {
            tournamentInfo = TournamentId.swiss(swissId);
        } else if(arenaId != null) {
            tournamentInfo = TournamentId.arena(arenaId);
        }

        var gameInfo = new GameInfo(fullId, gameId, fen,
                color, status, variantType, timeInfo,
                rated, hasMoved, isMyTurn, opponent,
                source, lastMove,
                Opt.of(gameYo.getInteger("ratingDiff")), Opt.of(tournamentInfo));

        return gameInfo;
    }
}
