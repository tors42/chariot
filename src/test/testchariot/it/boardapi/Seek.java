package it.boardapi;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import chariot.model.*;
import chariot.model.Event.*;
import util.*;
import util.IT.Players;
import static util.Assert.*;

public class Seek {

    @IntegrationTest
    public void seekRealTimeGame() {
        if (! (IT.findPlayers() instanceof Some(Players(var first, var __, var second, var ___)))) {
            fail("Couldn't find similarly rated players");
            return;
        }

        if (! (second.board().connect() instanceof Entries(var secondEvents)
            && first.board().connect() instanceof Entries(var firstEvents))) {
            fail("Couldn't connect");
            return;
        }

        var firstGameStart = new AtomicReference<GameStartEvent>();
        var secondGameStart = new AtomicReference<GameStartEvent>();

        var firstListen = Thread.ofPlatform().start(() -> firstEvents
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(firstGameStart::set));

        var secondListen = Thread.ofPlatform().start(() -> secondEvents
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(secondGameStart::set));

        var firstSeek = Thread.ofPlatform().start(() -> first.board().seekRealTime(params -> params
                    .clockRapid10m5s())
                .stream().findFirst());

        var secondSeek = Thread.ofPlatform().start(() -> second.board().seekRealTime(params -> params
                    .clockRapid10m5s())
                .stream().findFirst());

        List.of(firstSeek, secondSeek, firstListen, secondListen)
            .forEach(thread -> assertTrue(() -> thread.join(Duration.ofSeconds(5))));

        assertNotNull(firstGameStart.get());
        assertNotNull(secondGameStart.get());
        assertEquals(firstGameStart.get().id(), secondGameStart.get().id());
    }
}
