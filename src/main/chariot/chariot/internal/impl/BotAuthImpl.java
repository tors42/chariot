package chariot.internal.impl;

import java.util.Map;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.model.Enums.*;
import chariot.internal.*;

public class BotAuthImpl extends ChallengesAuthCommonImpl implements BotAuth {

    public BotAuthImpl(InternalClient client) {
        super(client, Scope.bot_play);
    }

    @Override
    public Many<User> botsOnline(int nb) {
        return Endpoint.botsOnline.newRequest(request -> request
                .query(Map.of("nb", nb)))
            .process(this);
    }

    @Override
    public Many<User> botsOnline() {
        return Endpoint.botsOnline.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<Ack> upgradeToBotAccount() {
        return Endpoint.botAccountUpgrade.newRequest(request -> request
                .post())
            .process(this);
     }

    @Override
    public Many<StreamGameEvent> streamGameState(String gameId) {
        return Endpoint.streamBotGameEvents.newRequest(request -> request
                .path(gameId)
                .stream())
            .process(this);
    }

    @Override
    public One<Ack> move(String gameId, String move, boolean drawOffer) {
        return Endpoint.botMove.newRequest(request -> request
                .path(gameId, move)
                .post()
                .query(Map.of("offeringDraw", drawOffer)))
            .process(this);
    }

    @Override
    public One<Ack> move(String gameId, String move) {
        return Endpoint.botMove.newRequest(request -> request
                .path(gameId, move)
                .post())
            .process(this);
    }


    @Override
    public One<Ack> chat(String gameId, String text, Room room) {
        return Endpoint.botChat.newRequest(request -> request
                .path(gameId)
                .post(Map.of("text", text, "room", room.name())))
            .process(this);
    }

    @Override
    public One<Ack> abort(String gameId) {
        return Endpoint.botAbort.newRequest(request -> request
                .path(gameId)
                .post())
            .process(this);
     }

    @Override
    public One<Ack> resign(String gameId) {
        return Endpoint.botResign.newRequest(request -> request
            .path(gameId)
            .post())
            .process(this);
    }

    @Override
    public Many<ChatMessage> fetchChat(String gameId) {
        return Endpoint.botFetchChat.newRequest(request -> request
                .path(gameId))
            .process(this);
    }
}
