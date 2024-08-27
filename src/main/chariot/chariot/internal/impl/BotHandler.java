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
    public One<Void> upgradeToBotAccount() {
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
    public One<Void> move(String gameId, String move, boolean drawOffer) {
        return Endpoint.botMove.newRequest(request -> request
                .path(gameId, move)
                .query(Map.of("offeringDraw", drawOffer)))
            .process(requestHandler);
    }

    @Override
    public One<Void> move(String gameId, String move) {
        return Endpoint.botMove.newRequest(request -> request
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
        return Endpoint.botChat.newRequest(request -> request
                .path(gameId)
                .body(Map.of("text", text, "room", room)))
            .process(requestHandler);
    }

    @Override
    public One<Void> abort(String gameId) {
        return Endpoint.botAbort.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
     }

    @Override
    public One<Void> resign(String gameId) {
        return Endpoint.botResign.newRequest(request -> request
            .path(gameId))
            .process(requestHandler);
    }

    @Override
    public One<Void> handleDrawOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.botDraw.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }

    @Override
    public One<Void> handleTakebackOffer(String gameId, boolean offerOrAccept) {
        return Endpoint.botTakeback.newRequest(request -> request
                .path(gameId, offerOrAccept ? "yes" : "no"))
            .process(requestHandler);
    }


    @Override
    public Many<ChatMessage> fetchChat(String gameId) {
        return Endpoint.botFetchChat.newRequest(request -> request
                .path(gameId))
            .process(requestHandler);
    }

}
