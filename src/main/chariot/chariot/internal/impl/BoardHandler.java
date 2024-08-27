package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class BoardHandler extends ChallengesAuthCommonImpl implements BoardApiAuth {

    public BoardHandler(RequestHandler requestHandler) {
        super(requestHandler, Scope.board_play);
    }

    @Override
    public Many<String> seekRealTime(Consumer<SeekRealTimeBuilder> consumer) {
        return Endpoint.boardSeekRealTime.newRequest(request -> request
                .stream()
                .body(seekRealTimeBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public One<SeekAck> seekCorrespondence(Consumer<SeekCorrespondenceBuilder> consumer) {
        return Endpoint.boardSeekCorr.newRequest(request -> request
                .stream()
                .body(seekCorrespondenceBuilderToMap(consumer)))
            .process(requestHandler);
    }

    @Override
    public Many<GameStateEvent> connectToGame(String gameId) {
        return Endpoint.streamBoardGameEvents.newRequest(request -> request
                .path(gameId)
                .stream())
            .process(requestHandler);
    }

    @Override
    public One<Void> move(String gameId, String move, boolean drawOffer) {
        return Endpoint.boardMove.newRequest(request -> request
                .path(gameId, move)
                .query(Map.of("offeringDraw", drawOffer)))
            .process(requestHandler);
    }

    @Override
    public One<Void> move(String gameId, String move) {
        return Endpoint.boardMove.newRequest(request -> request
                .path(gameId, move))
            .process(requestHandler);
    }

    @Override
    public One<Void> chat(String gameId, String text) {
        return _chat(gameId, text, "player");
    }

    @Override
    public One<Void> chatSpectators(String gameId, String text) {
        return _chat(gameId, text, "spectator");
    }

    private One<Void> _chat(String gameId, String text, String room) {
        return Endpoint.boardChat.newRequest(request -> request
                .path(gameId)
                .body(Map.of("text", text, "room", room)))
            .process(requestHandler);
    }

    @Override
    public One<Void> abort(String gameId) {
        return Endpoint.boardAbort.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
     }

    @Override
    public One<Void> resign(String gameId) {
        return Endpoint.boardResign.newRequest(request -> request
            .path(gameId))
            .process(requestHandler);
    }

    @Override
    public Many<ChatMessage> fetchChat(String gameId) {
        return Endpoint.boardFetchChat.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    @Override
    public One<Void> berserk(String gameId) {
        return Endpoint.boardBerserk.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    @Override
    public One<Void> handleDrawOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.boardDraw.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }

    @Override
    public One<Void> handleTakebackOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.boardTakeback.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }

    @Override
    public One<Void> claimVictory(String gameId) {
        return Endpoint.boardClaimVictory.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    private Map<String, Object> seekRealTimeBuilderToMap(Consumer<SeekRealTimeBuilder> consumer) {
        var builder = MapBuilder.of(SeekParams.class);
        var seekBuilder = new SeekRealTimeBuilder() {
            @Override
            public SeekParams clock(int initialMinutes, int incrementSeconds) {
                return builder
                    .add("time", initialMinutes)
                    .add("increment", incrementSeconds)
                    .proxy();
            }
        };
        consumer.accept(seekBuilder);
        return builder.toMap();
    }

    private Map<String, Object> seekCorrespondenceBuilderToMap(Consumer<SeekCorrespondenceBuilder> consumer) {
        var builder = MapBuilder.of(SeekParams.class);
        var seekBuilder = new SeekCorrespondenceBuilder() {
            @Override
            public SeekParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
            }

        };
        consumer.accept(seekBuilder);
        return builder.toMap();
    }
}
