package chariot.internal.impl;

import java.util.Map;
import java.util.Optional;

import chariot.Client.Scope;
import chariot.model.Enums.*;
import chariot.internal.Endpoint;
import chariot.internal.Util;
import chariot.internal.InternalClient;
import chariot.model.Ack;
import chariot.model.Result;
import chariot.model.StreamGameEvent;
import chariot.model.User;

public class BotAuthImpl extends ChallengesAuthCommonImpl implements Internal.BotAuth {

    public BotAuthImpl(InternalClient client) {
        super(client, Scope.bot_play);
    }

    @Override
    public Result<User> botsOnline(Optional<Integer> nb) {
        var builder = Endpoint.botsOnline.newRequest();
        nb.ifPresent(max -> builder.query(Map.of("nb", max)));
        var request = builder.build();
        return fetchMany(request);
    }

    @Override
    public Result<Ack> upgradeToBotAccount() {
        var request = Endpoint.botAccountUpgrade.newRequest()
            .post()
            .build();
        return fetchOne(request);
     }

    @Override
    public Result<StreamGameEvent> streamGameState(String gameId) {
        var request = Endpoint.streamBotGameEvents.newRequest()
            .path(gameId)
            .stream()
            .build();
        return fetchMany(request);
    }

    @Override
    public Result<Ack> move(String gameId, String move, Optional<Boolean> drawOffer) {
        var builder = Endpoint.botMove.newRequest()
            .path(gameId, move)
            .post();

        drawOffer.ifPresent(draw -> builder.query(Map.of("offeringDraw", draw)));

        var request = builder.build();
        return fetchOne(request);
    }

    @Override
    public Result<Ack> chat(String gameId, String text, Room room) {
        var map = Map.of("text", text, "room", room.name());
        var postData = Util.urlEncode(map);

        var request = Endpoint.botChat.newRequest()
            .path(gameId)
            .post(postData)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> abort(String gameId) {
        var request = Endpoint.botAbort.newRequest()
            .path(gameId)
            .post()
            .build();
        return fetchOne(request);
     }

    @Override
    public Result<Ack> resign(String gameId) {
        var request = Endpoint.botResign.newRequest()
            .path(gameId)
            .post()
            .build();
        return fetchOne(request);
    }

}
