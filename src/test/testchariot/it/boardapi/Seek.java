package it.boardapi;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import chariot.model.Entries;
import chariot.model.Event.*;
import util.*;
import static util.Assert.*;

public class Seek {

    @IntegrationTest
    public void seekRealTimeGame() {
        var white = IT.bobby();
        var black = IT.diego();

        if (! (black.board().connect() instanceof Entries(var blackEvents)
            && white.board().connect() instanceof Entries(var whiteEvents))) {
            fail("Couldn't connect");
            return;
        }

        var whiteGameStart = new AtomicReference<GameStartEvent>();
        var blackGameStart = new AtomicReference<GameStartEvent>();

        var whiteListen = Thread.ofPlatform().start(() -> whiteEvents
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(whiteGameStart::set));

        var blackListen = Thread.ofPlatform().start(() -> blackEvents
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(blackGameStart::set));

        var whiteSeek = Thread.ofPlatform().start(() -> white.board().seekRealTime(params -> params
                    .clockRapid10m5s()
                    .color(c -> c.white()))
                .stream().findFirst());

        var blackSeek = Thread.ofPlatform().start(() -> black.board().seekRealTime(params -> params
                    .clockRapid10m5s()
                    .color(c -> c.black()))
                .stream().findFirst());

        List.of(whiteSeek, blackSeek, whiteListen, blackListen)
            .forEach(thread -> assertTrue(() -> thread.join(Duration.ofSeconds(5))));

        assertNotNull(whiteGameStart.get());
        assertNotNull(blackGameStart.get());
        assertEquals(whiteGameStart.get().id(), blackGameStart.get().id());
    }
}
