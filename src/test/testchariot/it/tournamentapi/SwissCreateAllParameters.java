package it.tournamentapi;

import chariot.api.TournamentsApiAuth.SwissParams.Pairing;
import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.*;

public class SwissCreateAllParameters {

    record TestData(InputParameters parameters, IT.TeamLeader teamLeader) {}

    record InputParameters(
            Clock clock,
            Variant variant,
            String name,
            boolean rated,
            Enums.ChatFor chatFor,
            int nbRounds,
            ZonedDateTime startsAt,
            String entryCode,
            String description,
            Set<String> allowList,
            List<Pairing> forbiddenPairings) {}


    @IntegrationTest
    public void createSwiss() {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            fail("Couldn't find team leader for creating a swiss");
            return;
        }

        var inputParameters = new InputParameters(
                new Clock(Duration.ofMinutes(3), Duration.ofSeconds(2)),
                Variant.Basic.standard,
                "SwissAPI Test",
                true,
                Enums.ChatFor.onlyTeamMembers,
                3,
                ZonedDateTime.now(ZoneId.systemDefault()).plusMinutes(5).withNano(0),
                "TheEntryCode",
                "The description of the SwissAPI test tournament",
                Set.of("user1", "user2", "user3", "user4", userId),
                List.of(new Pairing("user1", "user4"))
                );


        boolean debug = false;
        if (debug) client.logging(l -> l.request().all().response().all());

        One<Swiss> createRes = client.tournaments().createSwiss(teamId, params -> params
                .clock(inputParameters.clock())
                .variant(inputParameters.variant())
                .name(inputParameters.name())
                .rated(inputParameters.rated())
                .chatFor(inputParameters.chatFor())
                .nbRounds(inputParameters.nbRounds())
                .startsAt(inputParameters.startsAt())
                .entryCode(inputParameters.entryCode())
                .description(inputParameters.description())
                .allowList(inputParameters.allowList())
                .addForbiddenPairings(inputParameters.forbiddenPairings())
                );

        if (debug) client.logging(l -> l.request().warning().response().warning());

        if (! (createRes instanceof Entry(Swiss createdSwiss))) {
            fail("Failed to create Swiss - %s".formatted(createRes));
            return;
        }

        Swiss expectedSwiss = expectedSwissFromInputAndServerSideGeneratedValues(
                inputParameters,
                userId,
                createdSwiss.id(),
                createdSwiss.nextRoundIn());

        assertEquals(expectedSwiss, createdSwiss);

        One<Swiss> fetchRes = client.tournaments().swissById(createdSwiss.id());

        if (! (fetchRes instanceof Entry(Swiss fetchedSwiss))) {
            fail("Failed to fetch Swiss - %s".formatted(fetchRes));
            return;
        }
    }

    static Swiss expectedSwissFromInputAndServerSideGeneratedValues(
            InputParameters inputParameters,
            String creatorUserId,
            String generatedSwissId,
            Opt<Duration> generatedNextRoundIn) {
        // Notes
        //  - allowList
        //      A list of users are sent as parameter,
        //      but users are not visible in response - only that a "allowList" condition exists is visible
        //  - forbiddenPairings
        //      A list of users are sent as parameter,
        //      but users are not visible in response - and not that a forbiddenPairings list exists
        //      (maybe verification could be possible by playing the created Swiss,
        //      and then check that the forbidden pairings weren't paired.)
        //  - entryCode
        //      An entry code is sent as parameter,
        //      but the code is not visible in response - only that entry code exists
        //  - desacription
        //      description is not visible in response

        int expectedNbPlayers = 0;
        int expectedRound = 0;
        int expectedNbOngoing = 0;

        TourInfo.Status expectedStatus = TourInfo.Status.created;

        Opt<Swiss.Stats> expectedStats = Opt.empty();
        Opt<String> expectedDescription = Opt.empty();

        List<SwissCondition> expectedConditions = !inputParameters.allowList().isEmpty() && !inputParameters.entryCode().isBlank()
            ? List.of((SwissCondition)Condition.allowListHidden(), (SwissCondition) Condition.entryCode())
            : List.of();
        Map<SwissCondition, String> expectedUnmetConditions = !inputParameters.allowList().contains(creatorUserId)
            ? Map.of((SwissCondition)Condition.allowListHidden(), "Your name is not in the tournament line-up.")
            : Map.of();

        ConditionInfo<SwissCondition> expectedConditionInfo = new ConditionInfo<>(expectedConditions, expectedUnmetConditions);


        TourInfo expectedTourInfo = new TourInfo(
                generatedSwissId,
                creatorUserId,
                inputParameters.startsAt(),
                inputParameters.name(),
                inputParameters.clock(),
                Speed.fromClock(inputParameters.clock()),
                inputParameters.variant(),
                expectedNbPlayers,
                inputParameters.rated(),
                expectedStatus,
                expectedDescription,
                Opt.of() // no freq
                );

        Swiss expectedSwiss = new Swiss(
                expectedTourInfo,
                expectedRound,
                inputParameters.nbRounds(),
                expectedNbOngoing,
                expectedConditionInfo,
                Opt.of(inputParameters.startsAt()),
                closeEnough(Duration.ofMinutes(5), generatedNextRoundIn),
                expectedStats
                );

        return expectedSwiss;
    }

    static Opt<Duration> closeEnough(Duration target, Opt<Duration> actual) {
        if (actual instanceof Some(Duration dur)
            && Math.abs(Math.abs(target.toSeconds()) -
                        Math.abs(dur.toSeconds())) < 2) {
            return actual;
        } else {
            return Opt.empty();
        }
    }

    @IntegrationTest
    public void createAndDeleteSimpleSwiss() {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            fail("Couldn't find team leader for creating a swiss");
            return;
        }

        Clock clock = new Clock(Duration.ofMinutes(3), Duration.ofSeconds(2));
        One<Swiss> createRes = client.tournaments().createSwiss(teamId, params -> params.clock(clock));
        unboxEquals(createRes, clock, swiss -> swiss.tourInfo().clock());
        createRes.map(s -> s.tourInfo().id()).ifPresent(id -> client.tournaments().terminateSwiss(id));
    }

}
