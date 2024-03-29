package it.broadcastapi;

import chariot.ClientAuth;
import util.IntegrationTest;
import util.IT;

import static util.Assert.assertTrue;
import static util.Assert.unboxEquals;

public class BroadcastAuth {

    static ClientAuth client = IT.diego();

    @IntegrationTest
    public void createSimpleBroadcast() {
        String name = "Simple Broadcast";
        String descrShort = "Simple short description";
        String descrLong = "Simple long description";

        var broadcastRequest = client.broadcasts().create(p -> p
                .name(name)
                .shortDescription(descrShort)
                .longDescription(descrLong));

        unboxEquals(broadcastRequest, name,       b -> b.tour().name());
        unboxEquals(broadcastRequest, descrShort, b -> b.tour().description());
        // "<p>Simple long description<p>\n"
        assertTrue(broadcastRequest.map(b -> b.tour().markup()).orElse("").contains(descrLong));
    }

    @IntegrationTest
    public void createSimpleRound() {
        var broadcastResult = client.broadcasts().create(p -> p
                .name("Broadcast IT")
                .shortDescription("Broadcast short description IT")
                .longDescription("Broadcast long description (markup) IT"));

        String roundName = "Simple Round";
        var roundRequest = broadcastResult.map(broadcast -> client.broadcasts().createRound(broadcast.id(), p -> p.name(roundName))).get();
        unboxEquals(roundRequest, roundName, r -> r.round().name());
    }
}
