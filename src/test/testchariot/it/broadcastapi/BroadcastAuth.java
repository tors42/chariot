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
        String markup = "Simple long description";

        ZonedDateTime create = ZonedDateTime.now();
        var broadcastRequest = client.broadcasts().create(p -> p
                .name(name)
                .markup(markup));

        if (! (broadcastRequest instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastRequest);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();
        String markupResult = "<p>" + markup + "</p>\n";

        assertTrue(aboutSameTime(create, createdAt), "Created At off");

        int tier = 0;
        boolean teamTable = false;
        boolean leaderboard = false;

        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();
        List<ZonedDateTime> dates = List.of();
        Broadcast.Info info = new Broadcast.Info(Opt.empty(), Opt.empty(), Opt.empty());

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        createdAt,
                        dates,
                        info,
                        tier,
                        markupResult,
                        IT.lilaURI().resolve("/broadcast/" + slug + "/" + id),
                        image,
                        teamTable,
                        leaderboard),
                rounds,
                group
                );

        assertEquals(expected, broadcast);
    }

    @IntegrationTest
    public void createBroadcastWithAllParameters() {
        String name = "Advanced Broadcast";
        String markup = "Advanced long description";
        int tier = 4;
        boolean autoLeaderboard = true;
        boolean teamLeaderboard = true;
        String infoTimeControl = "Classical";
        String infoTournamentFormat = "Swiss 3-round";
        String infoPlayers = "Anna,Beatrice,Claire,Diana";

        ZonedDateTime create = ZonedDateTime.now();
        var broadcastResult = superadmin.broadcasts().create(p -> p
                .name(name)
                .markup(markup)
                .infoTimeControl(infoTimeControl)
                .infoTournamentFormat(infoTournamentFormat)
                .infoFeaturedPlayers(infoPlayers)
                .tier(tier)
                .autoLeaderboard(autoLeaderboard)
                .teamTable(teamLeaderboard));

        if (! (broadcastResult instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastResult);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        assertTrue(aboutSameTime(create, createdAt), "Created At off");

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();
        String markupResult = "<p>" + markup + "</p>\n";
        Broadcast.Info info = new Broadcast.Info(Opt.some(infoTournamentFormat), Opt.some(infoTimeControl), Opt.some(infoPlayers));

        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();
        List<ZonedDateTime> dates = List.of();

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        createdAt,
                        dates,
                        info,
                        tier,
                        markupResult,
                        IT.lilaURI().resolve("/broadcast/" + slug + "/" + id),
                        image,
                        teamLeaderboard,
                        autoLeaderboard),
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
                    broadcast.tour().markup(),
                    broadcast.tour().url(),
                    broadcast.tour().image(),
                    broadcast.tour().teamTable(),
                    broadcast.tour().leaderboard()
                    ),
                expectedRounds,
                broadcast.group()
                );

        unboxEquals(broadcastAfterRoundHaveBeenCreated, expectedWithRounds);
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
                .markup("Broadcast long description (markup) IT"));

        String roundName = "Simple Round";

        var roundRequest = broadcastResult.map(broadcast -> client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName))).get();

        unboxEquals(roundRequest, roundName, r -> r.round().name());
    }
}
