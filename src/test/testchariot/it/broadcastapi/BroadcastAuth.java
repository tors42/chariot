package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.net.URI;
import java.time.*;
import java.util.*;

public class BroadcastAuth {

    static ClientAuth client = IT.diego();
    static ClientAuth superadmin = IT.superadmin();

    @IntegrationTest
    public void createSimpleBroadcast() {
        String name = "Simple Broadcast";
        String description = "Simple long description";

        ZonedDateTime create = ZonedDateTime.now();
        var broadcastRequest = client.broadcasts().create(p -> p
                .name(name)
                .description(description));

        if (! (broadcastRequest instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastRequest);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();

        assertTrue(aboutSameTime(create, createdAt), "Created At off");

        int tier = 0;
        boolean teamTable = false;

        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();
        List<ZonedDateTime> dates = List.of();
        Broadcast.Info info = new Broadcast.Info();

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        createdAt,
                        dates,
                        info,
                        tier,
                        description,
                        IT.lilaURI().resolve("/broadcast/" + slug + "/" + id),
                        image,
                        teamTable),
                rounds,
                group
                );

        assertEquals(expected, broadcast);


        // Test that create result also matches fetch result
        unboxEquals(client.broadcasts().broadcastById(id), expected);


        // Fetching allows specifying rendering of description field,
        // as the original markdown (default),
        // or as rendered HTML.

        // Test that description field gets rendered as HTML when specified
        String expectedHtmlDescription = "<p>" + description + "</p>\n";


        // In future with withers (ish):
        // expectedWithHtmlDescription = expected with { tour with { description = htmlDescription } }
        // For now, lots of repeated fields...:
        Broadcast expectedWithHtmlDescription = new Broadcast(
                new Broadcast.Tour(
                        expected.tour().id(),
                        expected.tour().name(),
                        expected.tour().slug(),
                        expected.tour().createdAt(),
                        expected.tour().dates(),
                        expected.tour().info(),
                        expected.tour().tier(),
                        expectedHtmlDescription,
                        expected.tour().url(),
                        expected.tour().image(),
                        expected.tour().teamTable()),
                expected.rounds(),
                expected.group());

        unboxEquals(client.broadcasts().broadcastById(id, p -> p.html()), expectedWithHtmlDescription);
    }

    @IntegrationTest
    public void createBroadcastWithAllParameters() {
        String name = "Advanced Broadcast";
        String description = "Advanced long description";
        int tier = 4;
        boolean showScores = true;
        boolean showRatingDiffs = true;
        boolean teamLeaderboard = true;
        FideTC infoTimeControlFIDE = FideTC.standard;
        String infoTimeControl = "Classical";
        String infoTournamentFormat = "Swiss 3-round";
        String infoPlayers = "Anna,Beatrice,Claire,Diana";
        String infoLocation = "Utopia";
        URI infoWebsite = URI.create("https://localhost/tournament");
        URI infoStandings = URI.create("https://localhost/tournament/results");

        ZonedDateTime create = ZonedDateTime.now();

        boolean debug = false;
        if (debug) superadmin.logging(l -> l.request().all().response().all());

        var broadcastResult = superadmin.broadcasts().create(p -> p
                .name(name)
                .description(description)
                .infoTimeControlFIDE(infoTimeControlFIDE)
                .infoTimeControl(infoTimeControl)
                .infoTournamentFormat(infoTournamentFormat)
                .infoFeaturedPlayers(infoPlayers)
                .infoLocation(infoLocation)
                .infoWebsite(infoWebsite)
                .infoStandings(infoStandings)
                .tier(tier)
                .showScores(showScores)
                .showRatingDiffs(showRatingDiffs)
                .teamTable(teamLeaderboard));

        if (debug) superadmin.logging(l -> l.request().warning().response().warning());

        if (! (broadcastResult instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastResult);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        assertTrue(aboutSameTime(create, createdAt), "Created At off");

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();
        Broadcast.Info info = new Broadcast.Info(
                Opt.some(infoTournamentFormat),
                Opt.some(infoTimeControl),
                Opt.some(infoPlayers),
                Opt.some(infoTimeControlFIDE),
                Opt.of(infoLocation),
                Opt.of(infoWebsite),
                Opt.of(infoStandings));

        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();
        List<ZonedDateTime> dates = List.of();

        // Note,
        //  It is not possible to query the values for, so no obvious way to verify them...
        //  - showScores
        //  - showRatingDiffs

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        createdAt,
                        dates,
                        info,
                        tier,
                        description,
                        IT.lilaURI().resolve("/broadcast/" + slug + "/" + id),
                        image,
                        teamLeaderboard),
                rounds,
                group);

        assertEquals(expected, broadcast);

        var broadcastById = superadmin.broadcasts().broadcastById(broadcast.id());

        unboxEquals(broadcastById, expected);

        var myRound1 = createAndVerifyRound(broadcast, "Round 1", 1);
        var myRound2 = createAndVerifyRound(broadcast, "Round 2", 2);
        var myRound3 = createAndVerifyRound(broadcast, "Round 3", 3);

        var broadcastAfterRoundHaveBeenCreated = superadmin.broadcasts().broadcastById(broadcast.id());

        List<Broadcast.Round> expectedRounds = List.of(
                myRoundToRound(myRound1),
                myRoundToRound(myRound2),
                myRoundToRound(myRound3));

        List<ZonedDateTime> expectedDates = List.of(myRound1.round().startsAt().get(), myRound3.round().startsAt().get());

        // In future with withers (ish):
        // expectedWithRounds = expected with { tour with { dates = expectedDates } } with { rounds = expectedRounds };

        // For now, lots of repeated fields...:
        Broadcast expectedWithRounds = new Broadcast(
                new Broadcast.Tour(
                    broadcast.tour().id(),
                    broadcast.tour().name(),
                    broadcast.tour().slug(),
                    broadcast.tour().createdAt(),
                    expectedDates,
                    broadcast.tour().info(),
                    broadcast.tour().tier(),
                    broadcast.tour().description(),
                    broadcast.tour().url(),
                    broadcast.tour().image(),
                    broadcast.tour().teamTable()
                    ),
                expectedRounds,
                broadcast.group()
                );

        unboxEquals(broadcastAfterRoundHaveBeenCreated, expectedWithRounds);
    }


    @IntegrationTest
    public void createWithStartsAfterPrevious() {
        var broadcastResult = superadmin.broadcasts().create(p -> p
                .name("startsAfterPrevious test")
                .description("startsAfterPrevious description")
                );
        if (! (broadcastResult instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastResult);
            return;
        }
        var myRoundResult = superadmin.broadcasts().createRound(broadcast.id(), p -> p
                .name("startsAfterPrevious name")
                .startsAfterPrevious());
        if (! (myRoundResult instanceof Entry(MyRound myRound))) {
            fail("Couldn't create round: " +  myRoundResult);
            return;
        }
        assertTrue(myRound.round().startsAfterPrevious(), "Should have startsAfterPrevious");
        One<Broadcast> broadcastFetchRes = superadmin.broadcasts().broadcastById(broadcast.id());
        unboxEquals(broadcastFetchRes, true, b -> ! b.rounds().isEmpty() && b.rounds().getFirst().startsAfterPrevious());
    }

    static MyRound createAndVerifyRound(Broadcast broadcast, String roundName, int plusDays) {
        ZonedDateTime createRound = ZonedDateTime.now();
        ZonedDateTime roundStartsAt = createRound.plusDays(plusDays).withNano(0);
        Duration roundDelay = Duration.ofMinutes(30);
        var myRoundResult = superadmin.broadcasts().createRound(broadcast.id(), p -> p
                .name(roundName)
                .startsAt(roundStartsAt)
                .delay(roundDelay)
                );

        if (! (myRoundResult instanceof Entry(MyRound myRound))) {
            fail("Couldn't create " + roundName + " - " + myRoundResult);
            return null;
        }

        String roundId = myRound.round().id();
        URI roundUrl = myRound.round().url();

        ZonedDateTime roundCreatedAt = myRound.round().createdAt();
        assertTrue(aboutSameTime(createRound, roundCreatedAt), roundName + " Created At off");

        MyRound.Tour expectedRoundTour = new MyRound.Tour(
                broadcast.tour().id(),
                broadcast.tour().slug(),
                broadcast.tour().name(),
                broadcast.tour().info(),
                broadcast.tour().createdAt(),
                broadcast.tour().tier(),
                broadcast.tour().image());

        MyRound.Round expectedRoundRound = new MyRound.Round(
                roundId,
                roundName.toLowerCase(Locale.ROOT).replace(' ', '-'),
                roundName,
                roundCreatedAt,
                false, // startsAfterPrevious
                Opt.of(roundStartsAt),
                false,
                false,
                roundUrl,
                roundDelay
                );
        MyRound.Study expectedRoundStudy = new MyRound.Study(true);

        MyRound expectedRound = new MyRound(expectedRoundTour, expectedRoundRound, expectedRoundStudy);

        assertEquals(expectedRound, myRound);
        return myRound;
    }

    static Broadcast.Round myRoundToRound(MyRound myRound) {
        var round = myRound.round();
        return new Broadcast.Round(
                round.id(),
                round.name(),
                round.slug(),
                round.createdAt(),
                round.ongoing(),
                round.finished(),
                round.startsAfterPrevious(),
                round.startsAt(),
                Opt.empty(),
                round.url()
                );
    }

    static boolean aboutSameTime(ZonedDateTime time1, ZonedDateTime time2) {
        return Duration.between(time1, time2).toMillis() < 500;
    }


    @IntegrationTest
    public void createSimpleRound() {
        var broadcastResult = client.broadcasts().create(p -> p
                .name("Broadcast IT")
                .description("Broadcast long description (markup) IT"));

        String roundName = "Simple Round";

        var roundRequest = broadcastResult.map(broadcast -> client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName))).get();

        unboxEquals(roundRequest, roundName, r -> r.round().name());
    }
}
