package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;

public class BoardHandler extends ChallengesAuthCommonImpl implements BoardAuth {

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
    public Many<GameEvent> connectToGame(String gameId) {
        return Endpoint.streamBoardGameEvents.newRequest(request -> request
                .path(gameId)
                .stream())
            .process(requestHandler);
    }

    @Override
    public One<Ack> move(String gameId, String move, boolean drawOffer) {
        return Endpoint.boardMove.newRequest(request -> request
                .path(gameId, move)
                .query(Map.of("offeringDraw", drawOffer)))
            .process(requestHandler);
    }

    @Override
    public One<Ack> move(String gameId, String move) {
        return Endpoint.boardMove.newRequest(request -> request
                .path(gameId, move))
            .process(requestHandler);
    }

    @Override
    public One<Ack> chat(String gameId, String text, Room room) {
        return Endpoint.boardChat.newRequest(request -> request
                .path(gameId)
                .body(Map.of("text", text, "room", room.name())))
            .process(requestHandler);
    }

    @Override
    public One<Ack> abort(String gameId) {
        return Endpoint.boardAbort.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
     }

    @Override
    public One<Ack> resign(String gameId) {
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
    public One<Ack> berserk(String gameId) {
        return Endpoint.boardBerserk.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    @Override
    public One<Ack> handleDrawOffer(String gameId, Offer accept) {
        return Endpoint.boardDraw.newRequest(request -> request
                .path(gameId, accept.name()))
            .process(requestHandler);
    }

    @Override
    public One<Ack> handleTakebackOffer(String gameId, Offer accept) {
        return Endpoint.boardTakeback.newRequest(request -> request
                .path(gameId, accept.name()))
            .process(requestHandler);
    }

    @Override
    public One<Ack> claimVictory(String gameId) {
        return Endpoint.boardClaimVictory.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    private Map<String, Object> seekRealTimeBuilderToMap(Consumer<SeekRealTimeBuilder> consumer) {
        var builder = MapBuilder.of(SeekParams.class);
        var seekBuilder = new SeekRealTimeBuilder() {
            @Override
            public SeekParams clock(float initialMinutes, int incrementSeconds) {
                return builder
                    .add("time", (int)initialMinutes)
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
