package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.Client.Scope;
import chariot.model.Enums.*;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Ack;
import chariot.model.ChatMessage;
import chariot.model.Result;
import chariot.model.StreamGameEvent;

public class BoardAuthImpl extends ChallengesAuthCommonImpl implements Internal.BoardAuth {

    public BoardAuthImpl(InternalClient client) {
        super(client, Scope.board_play);
    }

    @Override
    public Result<Ack> seek(SeekParameters parameters) {
        var map = parameters.toMap();

        var requestBuilder = Endpoint.boardSeek.newRequest()
            .post(map);

        if ( ! map.containsKey("days") ){
            requestBuilder.stream();
        }

        var request = requestBuilder.build();

        return fetchOne(request);
    }

    @Override
    public Result<StreamGameEvent> streamGameState(String gameId) {
        var request = Endpoint.streamBoardGameEvents.newRequest()
            .path(gameId)
            .stream()
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<Ack> move(String gameId, String move, Optional<Boolean> drawOffer) {

        var builder = Endpoint.boardMove.newRequest()
            .path(gameId, move)
            .post();

        drawOffer.ifPresent(draw -> builder.query(Map.of("offeringDraw", draw)));

        var request = builder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> chat(String gameId, String text, Room room) {
        var map = Map.of("text", text, "room", room.name());

        var request = Endpoint.boardChat.newRequest()
            .path(gameId)
            .post(map)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> abort(String gameId) {
        var request = Endpoint.boardAbort.newRequest()
            .path(gameId)
            .post()
            .build();
        return fetchOne(request);
     }

    @Override
    public Result<Ack> resign(String gameId) {
        var request = Endpoint.boardResign.newRequest()
            .path(gameId)
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> handleDrawOffer(String gameId, Offer accept) {
        var request = Endpoint.boardDraw.newRequest()
            .path(gameId, accept.name())
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> handleTakebackOffer(String gameId, Offer accept) {
        var request = Endpoint.boardTakeback.newRequest()
            .path(gameId, accept.name())
            .post()
            .build();
        return fetchOne(request);
    }


    @Override
    public Result<Ack> claimVictory(String gameId) {
        var request = Endpoint.boardClaimVictory.newRequest()
            .path(gameId)
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<ChatMessage> fetchChat(String gameId) {
        var request = Endpoint.boardFetchChat.newRequest()
            .path(gameId)
            .build();
        return fetchArr(request);
    }


}
