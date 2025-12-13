package chariot.internal.modeladapter;

import java.time.Duration;
import java.time.ZonedDateTime;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.model.GameStateEvent.Side;
import chariot.model.GameStateEvent.State;

public interface GameStateAdapter {

    static GameStateEvent nodeToEvent(YayNode node) {
        if (! (node instanceof YayObject yo)) return null;
        String eventType = yo.getString("type");
        GameStateEvent event = switch(eventType) {
            case "gameFull" -> {
                String id = yo.getString("id");
                GameType gameType = GameTypeAdapter.nodeToGameType(yo);
                ZonedDateTime createdAt = Util.fromLong(yo.getLong("createdAt"));
                GameStateEvent.Side white = nodeToSide(yo.value().get("white"));
                GameStateEvent.Side black = nodeToSide(yo.value().get("black"));
                Opt<TournamentId> tournamentId = Opt.of(yo.getString("tournamentId")).map(TournamentId.ArenaId::new);
                var state = nodeToState(yo.value().get("state"));

                yield new GameStateEvent.Full(id, gameType, createdAt, white, black, tournamentId, state);
            }
            case "gameState" -> nodeToState(yo);
            case "chatLine" -> new GameStateEvent.Chat(yo.getString("username"), yo.getString("text"), yo.getString("room"));
            case "opponentGone" -> {
                boolean gone = yo.getBool("gone");
                GameStateEvent.Claim claim = new GameStateEvent.No();
                Integer claimWinInSeconds = yo.getInteger("claimWinInSeconds");
                if (claimWinInSeconds != null) {
                    claim = claimWinInSeconds <= 0
                        ? new GameStateEvent.Yes()
                        : new GameStateEvent.Soon(Duration.ofSeconds(claimWinInSeconds));
                }
                yield new GameStateEvent.OpponentGone(gone, claim);
            }
            default -> null;
        };
        return event;
    }

    private static State nodeToState(YayNode node) {
        if (! (node instanceof YayObject stateYo)) return null;
        return new State(
                stateYo.getString("moves"),
                Duration.ofMillis(stateYo.getLong("wtime")),
                Duration.ofMillis(stateYo.getLong("btime")),
                Duration.ofMillis(stateYo.getLong("winc")),
                Duration.ofMillis(stateYo.getLong("binc")),
                Status.valueOf(stateYo.getString("status")),
                Opt.of(stateYo.getString("winner")).map(Color::valueOf),
                stateYo.getBool("wdraw")
                    ? Opt.of(Color.white)
                    : stateYo.getBool("bdraw")
                      ? Opt.of(Color.black)
                      : Opt.empty(),
                stateYo.getBool("wtakeback")
                    ? Opt.of(Color.white)
                    : stateYo.getBool("btakeback")
                      ? Opt.of(Color.black)
                      : Opt.empty(),
                Opt.of(stateYo.getString("rematch")),
                stateYo.value().get("expiration") instanceof YayObject expYo
                    && expYo.getLong("idleMillis") instanceof Long idle
                    && expYo.getLong("millisToMove") instanceof Long toMove
                    ? Opt.of(new GameStateEvent.Expiration(Duration.ofMillis(idle), Duration.ofMillis(toMove)))
                    : Opt.empty()
                );
    }

    private static Side nodeToSide(YayNode node) {
        if (! (node instanceof YayObject yo)) return new Anonymous();
        Integer aiLevel = yo.getInteger("aiLevel");
        if (aiLevel != null) {
            return new AI(aiLevel);
        }
        String id = yo.getString("id");
        if (id == null) return new Anonymous();
        String name = yo.getString("name");
        String title = yo.getString("title");
        var userInfo = UserInfo.of(id, name, title);
        int rating = yo.getInteger("rating");
        boolean provisional = yo.getBool("provisional");
        return new GameStateEvent.Account(userInfo, rating, provisional);
    }
}
