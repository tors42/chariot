package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.model.*;
import chariot.internal.*;

public class BotHandler extends ChallengesAuthCommonImpl implements BotApiAuth {

    public BotHandler(RequestHandler requestHandler) {
        super(requestHandler, Scope.bot_play);
    }

    @Override public Many<User> botsOnline(int nb) { return _botsOnline(request -> request.query(Map.of("nb", nb))); }
    @Override public Many<User> botsOnline() { return _botsOnline(request -> {}); }

    public Many<User> _botsOnline(Consumer<chariot.internal.RequestParameters.Params> consumer) {
        return Endpoint.botsOnline.newRequest(consumer)
            .process(requestHandler);
    }

    @Override
    public Ack upgradeToBotAccount() {
        return Endpoint.botAccountUpgrade.newRequest(request -> {})
            .process(requestHandler);
     }

    @Override
    public Many<GameStateEvent> connectToGame(String gameId) {
        return Endpoint.streamBotGameEvents.newRequest(request -> request
                .path(gameId)
                .stream())
            .process(requestHandler);
    }

    @Override
    public Ack move(String gameId, String move, boolean drawOffer) {
        return Endpoint.botMove.newRequest(request -> request
                .path(gameId, move)
                .query(Map.of("offeringDraw", drawOffer)))
            .process(requestHandler);
    }

    @Override
    public Ack move(String gameId, String move) {
        return Endpoint.botMove.newRequest(request -> request
                .path(gameId, move))
            .process(requestHandler);
    }

    @Override
    public Ack chat(String gameId, String text) {
        return _chat(gameId, text, "player");
    }

    @Override
    public Ack chatSpectators(String gameId, String text) {
        return _chat(gameId, text, "spectator");
    }

    private Ack _chat(String gameId, String text, String room) {
        return Endpoint.botChat.newRequest(request -> request
                .path(gameId)
                .body(Map.of("text", text, "room", room)))
            .process(requestHandler);
    }

    @Override
    public Ack abort(String gameId) {
        return Endpoint.botAbort.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
     }

    @Override
    public Ack resign(String gameId) {
        return Endpoint.botResign.newRequest(request -> request
            .path(gameId))
            .process(requestHandler);
    }

    @Override
    public Ack handleDrawOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.botDraw.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }

    @Override
    public Ack handleTakebackOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.botTakeback.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }

    @Override
    public Ack claimVictory(String gameId) {
        return Endpoint.botClaimVictory.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    @Override
    public Ack claimDraw(String gameId) {
        return Endpoint.botClaimDraw.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

    @Override
    public Many<ChatMessage> fetchChat(String gameId) {
        return Endpoint.botFetchChat.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

}
