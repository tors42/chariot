package chariot.internal.impl;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import chariot.api.OpeningExplorerApi;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.ExploreResult.*;
import chariot.model.*;
import chariot.model.Enums.*;

public class OpeningExplorerHandler implements OpeningExplorerApi {

    private final RequestHandler requestHandler;

    public OpeningExplorerHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }


    @Override
    public One<Pgn> pgnByMastersGameId(String gameId) {
        return Endpoint.exploreMasterOTB.newRequest(request -> request
                .path(gameId))
                .process(requestHandler);
    }

    @Override
    public One<OpeningDB> masters(Consumer<MastersBuilder> params) {
        return Endpoint.exploreMasters.newRequest(request -> request
                .query(MapBuilder.of(MastersBuilder.class).toMap(params)))
                .process(requestHandler);
    }

    @Override
    public One<OpeningDB> lichess(Consumer<LichessBuilder> params) {
        return Endpoint.exploreLichess.newRequest(request -> request
                .query(MapBuilder.of(LichessBuilder.class)
                        .addCustomHandler("speeds", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var speeds = (Set<Speed>) args[0];
                            if (!speeds.isEmpty()) {
                                map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                            }
                        })
                        .addCustomHandler("ratings", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var ratings = (Set<RatingGroup>) args[0];
                            if (!ratings.isEmpty()) {
                                map.put("ratings", ratings.stream().map(RatingGroup::asString)
                                        .collect(Collectors.joining(",")));
                            }
                        }).toMap(params)))
                .process(requestHandler);
    }

    @Override
    public One<OpeningPlayer> player(String userId, Consumer<PlayerBuilder> params) {
        return Endpoint.explorePlayers.newRequest(request -> request
                .query(MapBuilder.of(PlayerBuilder.class)
                        .add("player", userId)
                        .add("color", chariot.model.Enums.Color.white)
                        .addCustomHandler("speeds", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var speeds = (Set<Speed>) args[0];
                            if (!speeds.isEmpty()) {
                                map.put("speeds", speeds.stream().map(Speed::name).collect(Collectors.joining(",")));
                            }
                        })
                        .addCustomHandler("ratings", (args, map) -> {
                            @SuppressWarnings("unchecked")
                            var modes = (Set<PlayerBuilder.Mode>) args[0];
                            if (!modes.isEmpty()) {
                                map.put("modes",
                                        modes.stream().map(PlayerBuilder.Mode::name).collect(Collectors.joining(",")));
                            }
                        })
                        .toMap(params)))
                .process(requestHandler);
    }

}
