package it.tournamentapi;

import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.*;

public class ArenaCreateAllParameters {

    record TestData(InputParameters parameters, IT.TeamLeader teamLeader) {}

    record InputParameters(
            Clock clock,
            Variant variant,
            String name,
            boolean rated,
            boolean withChat,
            int minutes,
            ZonedDateTime startsAt,
            String entryCode,
            String description,
            List<String> allowList,
            String team,
            boolean berserkable) {}


    @IntegrationTest
    public void createArenaWithinTeam() {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            fail("Couldn't find team leader for creating a arena within team");
            return;
        }

        var inputParameters = new InputParameters(
                new Clock(Duration.ofMinutes(3), Duration.ofSeconds(2)),
                Variant.Basic.standard,
                "Arena API within team Test",
                true,
                true,
                20,
                ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(5).withNano(0),
                "TheEntryCode",
                "The description of the Arena API within team test tournament",
                List.of("user1", "user2", "user3", "user4", userId),
                teamId,
                true
                );


        boolean debug = false;
        if (debug) client.logging(l -> l.request().all().response().all());

        One<Arena> createRes = client.tournaments().createArena(params -> params
                .clock(inputParameters.clock())
                .variant(inputParameters.variant())
                .name(inputParameters.name())
                .withChat(inputParameters.withChat())
                .minutes(inputParameters.minutes())
                .startTime(s -> s.atDate(inputParameters.startsAt()))
                .entryCode(inputParameters.entryCode())
                .description(inputParameters.description())
                .conditionTeam(teamId)
                .allowList(inputParameters.allowList())
                );

        if (debug) client.logging(l -> l.request().warning().response().warning());

        if (! (createRes instanceof Entry(Arena createdArena))) {
            fail("Failed to create Arena - %s".formatted(createRes));
            return;
        }

        Arena expectedArena = expectedArenaFromInputAndServerSideGeneratedValues(
                inputParameters,
                userId,
                createdArena.id(),
                createdArena.quote());

        assertEquals(expectedArena, createdArena);

        if (debug) client.logging(l -> l.request().all().response().all());

        One<Arena> fetchRes = client.tournaments().arenaById(createdArena.id());

        if (debug) client.logging(l -> l.request().warning().response().warning());

        if (! (fetchRes instanceof Entry(Arena fetchedArena))) {
            fail("Failed to fetch Arena - %s".formatted(fetchRes));
            return;
        }
    }

    static Arena expectedArenaFromInputAndServerSideGeneratedValues(
            InputParameters inputParameters,
            String creatorUserId,
            String generatedArenaId,
            Opt<Arena.Quote> generatedArenaQuote
            ) {
        // Notes
        //  - entryCode
        //      An entry code is sent as parameter,
        //      but the code is not visible in response - only that entry code exists

        int expectedNbPlayers = 0;

        TourInfo.Status expectedStatus = TourInfo.Status.created;

        Opt<Arena.Stats> expectedStats = Opt.empty();



        List<ArenaCondition> expectedConditions = !inputParameters.allowList().isEmpty()
                                             && !inputParameters.entryCode().isBlank()
                                             && !inputParameters.team().isBlank()
                ? List.of(
                        (ArenaCondition) Condition.allowList(inputParameters.allowList()),
                        (ArenaCondition) Condition.entryCode(),
                        (ArenaCondition) Condition.member(inputParameters.team))
                : List.of();


        Map<ArenaCondition, String> expectedUnmetConditions = !inputParameters.allowList().contains(creatorUserId)
            ? Map.of((ArenaCondition) Condition.allowListHidden(), "Your name is not in the tournament line-up.")
            : Map.of();

        ConditionInfo<ArenaCondition> expectedConditionInfo = new ConditionInfo<>(expectedConditions, expectedUnmetConditions);

        TourInfo expectedTourInfo = new TourInfo(
                generatedArenaId,
                creatorUserId,
                inputParameters.startsAt(),
                inputParameters.name() + " Arena", // huh? " Arena" added...
                inputParameters.clock(),
                Speed.fromClock(inputParameters.clock()),
                inputParameters.variant(),
                expectedNbPlayers,
                inputParameters.rated(),
                expectedStatus,
                Opt.of(inputParameters.description()),
                Opt.of() // no freq
                );

        Arena expectedArena = new Arena(
                expectedTourInfo,
                Duration.ofMinutes(inputParameters.minutes()),
                inputParameters.berserkable(),
                expectedConditionInfo,
                List.of(), // No standings
                List.of(), // No podium
                List.of(), // No team standings
                List.of(), // No top games
                false,    // pairings are not closed
                false,    // not recently finished
                Opt.of(), // no spotlight headline
                Opt.of(), // no battle
                generatedArenaQuote, // we get a quote!
                Opt.of(), // no great player info
                Opt.of(), // no featured game
                expectedStats
                );

        return expectedArena;
    }

    @IntegrationTest
    public void createAndDeleteSimpleArena() {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            fail("Couldn't find team leader for creating a arena");
            return;
        }

        Clock clock = new Clock(Duration.ofMinutes(3), Duration.ofSeconds(2));
        One<Arena> createRes = client.tournaments().createArena(params -> params.clock(clock));
        unboxEquals(createRes, clock, arena -> arena.tourInfo().clock());
        createRes.map(s -> s.tourInfo().id()).ifPresent(id -> client.tournaments().terminateArena(id));
    }

}
