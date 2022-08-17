package chariot.internal.impl;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.Enums.*;
import chariot.model.*;

public class TournamentsAuthImpl extends TournamentsImpl implements TournamentsAuth {

    public TournamentsAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<Arena> createArena(Consumer<ArenaBuilder> consumer) {
        return Endpoint.createArenaTournament.newRequest(request -> request
                .post(arenaBuilderToMap(consumer)))
            .process(this);
    }

    @Override
    public One<Arena> updateArena(String id, Consumer<ArenaBuilder> consumer) {
        return Endpoint.updateArenaTournament.newRequest(request -> request
                .path(id)
                .post(arenaBuilderToMap(consumer)))
            .process(this);
    }

    @Override
    public One<Arena> updateTeamBattle(String id, int nbLeaders, Set<String> teamIds) {
        return Endpoint.updateTeamBattleTournament.newRequest(request -> request
                .path(id)
                .post(Map.ofEntries(
                        Map.entry("teams", teamIds.stream().collect(Collectors.joining(","))),
                        Map.entry("nbLeaders", String.valueOf(nbLeaders))
                        )))
            .process(this);
    }

    @Override
    public One<Ack> terminateArena(String id) {
        return Endpoint.terminateArenaTournament.newRequest(request -> request
                .path(id)
                .post())
            .process(this);
    }

    @Override
    public One<Ack> joinArena(String id, Consumer<JoinArenaParams> consumer) {
        return Endpoint.joinArenaTournament.newRequest(request -> request
            .path(id)
            .post(MapBuilder.of(JoinArenaParams.class)
                .rename("entryCode", "password").toMap(consumer)))
            .process(this);
    }

    @Override
    public One<Ack> withdrawArena(String id) {
        return Endpoint.withdrawArenaTournament.newRequest(request -> request
            .path(id)
            .post())
            .process(this);
    }

    @Override
    public One<Swiss> createSwiss(String teamId, Consumer<SwissBuilder> consumer) {
        return Endpoint.createSwiss.newRequest(request -> request
                .path(teamId)
                .post(swissBuilderToMap(consumer)))
            .process(this);
    }

    @Override
    public One<Swiss> updateSwiss(String id, Consumer<SwissBuilder> consumer) {
        return Endpoint.updateSwissTournament.newRequest(request -> request
                .path(id)
                .post(swissBuilderToMap(consumer)))
            .process(this);
    }

    @Override
    public One<Ack> terminateSwiss(String swissId) {
        return Endpoint.terminateSwiss.newRequest(request -> request
                .path(swissId)
                .post())
            .process(this);
    }

    @Override
    public One<Ack> joinSwiss(String id, Consumer<JoinSwissParams> consumer) {
        return Endpoint.joinSwissTournament.newRequest(request -> request
                .path(id)
                .post(MapBuilder.of(JoinSwissParams.class)
                    .rename("entryCode", "password").toMap(consumer)))
            .process(this);
    }


    private Map<String, Object> arenaBuilderToMap(Consumer<ArenaBuilder> consumer) {
        var builder = MapBuilder.of(ArenaParams.class)
            .addCustomHandler("startTime", (args, map) -> {
                @SuppressWarnings("unchecked")
                var startTime = (Function<ArenaParams.StartTime.Provider, ArenaParams.StartTime>) args[0];
                var time = startTime.apply(ArenaParams.StartTime.provider());
                if (time instanceof ArenaParams.StartTime.InMinutes m) {
                    map.put("waitMinutes", m.waitMinutes());
                } else if (time instanceof ArenaParams.StartTime.AtDate d) {
                    map.put("startDate", d.startDate());
                }
            })
            .rename("entryCode", "password")
            .rename("conditionTeam", "conditions.teamMember.teamId")
            .rename("conditionMinRating", "conditions.minRating.rating")
            .rename("conditionMaxRating", "conditions.maxRating.rating")
            .rename("conditionMinRatedGames", "conditions.nbRatedGames.nb");

        var arenaBuilder = new ArenaBuilder() {
            @Override
            public ArenaParams clock(float initial, int increment) {
                return builder
                    .add("clockTime", initial)
                    .add("clockIncrement", increment)
                    .add("minutes", 60 + 40)
                    .proxy();
            }
        };
        consumer.accept(arenaBuilder);
        return builder.toMap();
    }

    private Map<String, Object> swissBuilderToMap(Consumer<SwissBuilder> consumer) {
        var builder = MapBuilder.of(SwissParams.class)
            .rename("entryCode", "password")
            .addCustomHandler("chatFor", (args, map) -> map.put("chatFor", ChatFor.class.cast(args[0]).id))
            .addCustomHandler("forbiddenPairings", (args, map) -> {
                @SuppressWarnings("unchecked")
                var pairings = (Collection<SwissParams.ForbiddenPairing>) args[0];

                String forbiddenPairings = pairings.stream()
                    .map(pairing -> String.join(" ", pairing.player1(), pairing.player2()))
                    .collect(Collectors.joining("\n"));

                var existingPairings = (String) map.get("forbiddenPairings");

                if (existingPairings != null) {
                    forbiddenPairings = String.join("\n", existingPairings, forbiddenPairings);
                }
                map.put("forbiddenPairings", forbiddenPairings);
            });

        var swissBuilder = new SwissBuilder() {
            @Override
            public SwissParams clock(int initial, int increment) {
                return builder
                .add("clock.limit", initial)
                .add("clock.increment", increment)
                .add("nbRounds", 9)
                .proxy();
            }
        };
        consumer.accept(swissBuilder);
        return builder.toMap();
    }
}
