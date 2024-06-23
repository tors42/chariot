package it.broadcastapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.net.URI;
import java.time.*;
import java.util.List;
import java.util.Locale;

public class BroadcastAuth {

    static ClientAuth client = IT.diego();
    static ClientAuth superadmin = IT.superadmin();

    @IntegrationTest
    public void createSimpleBroadcast() {
        String name = "Simple Broadcast";
        String descrShort = "Simple short description";
        String descrLong = "Simple long description";

        ZonedDateTime create = ZonedDateTime.now();
        var broadcastRequest = client.broadcasts().create(p -> p
                .name(name)
                .shortDescription(descrShort)
                .longDescription(descrLong));

        if (! (broadcastRequest instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastRequest);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();
        String markup = "<p>" + descrLong + "</p>\n";

        assertTrue(Duration.between(create, broadcast.tour().createdAt()).toMillis() < 100, "Created At off");

        int tier = 0;
        boolean teamTable = false;
        boolean leaderboard = false;

        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        descrShort,
                        createdAt,
                        tier,
                        markup,
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
        String descrShort = "Advanced short description";
        String descrLong = "Advanced long description";
        int tier = 4;
        boolean autoLeaderboard = true;
        boolean teamLeaderboard = true;

        ZonedDateTime create = ZonedDateTime.now();
        var broadcastRequest = superadmin.broadcasts().create(p -> p
                .name(name)
                .shortDescription(descrShort)
                .longDescription(descrLong)
                .tier(tier)
                .autoLeaderboard(autoLeaderboard)
                .teamTable(teamLeaderboard));

        if (! (broadcastRequest instanceof Entry(Broadcast broadcast))) {
            fail("Couldn't create broadcast " + broadcastRequest);
            return;
        }

        String id = broadcast.id();
        ZonedDateTime createdAt = broadcast.tour().createdAt();

        String slug = name.toLowerCase(Locale.ROOT).replace(' ', '-');
        Opt<URI> image = Opt.empty();
        String markup = "<p>" + descrLong + "</p>\n";

        assertTrue(Duration.between(create, broadcast.tour().createdAt()).toMillis() < 100, "Created At off");


        List<Broadcast.Round> rounds = List.of();
        Opt<Broadcast.Group> group = Opt.empty();

        Broadcast expected = new Broadcast(
                new Broadcast.Tour(
                        id,
                        name,
                        slug,
                        descrShort,
                        createdAt,
                        tier,
                        markup,
                        IT.lilaURI().resolve("/broadcast/" + slug + "/" + id),
                        image,
                        teamLeaderboard,
                        autoLeaderboard),
                rounds,
                group);

        assertEquals(expected, broadcast);
    }


    @IntegrationTest
    public void createSimpleRound() {
        var broadcastResult = client.broadcasts().create(p -> p
                .name("Broadcast IT")
                .shortDescription("Broadcast short description IT")
                .longDescription("Broadcast long description (markup) IT"));

        String roundName = "Simple Round";

        //client.logging(l -> l.response().all());
        var roundRequest = broadcastResult.map(broadcast -> client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName))).get();
        //client.logging(l -> l.response().off());

        unboxEquals(roundRequest, roundName, r -> r.round().name());
    }
}
