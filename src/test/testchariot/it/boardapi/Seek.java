package it.boardapi;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import chariot.ClientAuth;
import chariot.model.*;
import chariot.model.Event.*;
import util.*;
import static util.Assert.*;

public class Seek {

    record Colors(ClientAuth white, ClientAuth black) {}

    @IntegrationTest
    public void seekRealTimeGame() {
        if (! (findPlayers() instanceof Colors colors)) {
            fail("Couldn't find players to test Board API");
            return;
        }

        var whiteGameStart = new AtomicReference<GameStartEvent>();
        var blackGameStart = new AtomicReference<GameStartEvent>();

        var whiteListen = Thread.ofPlatform().start(() -> colors.white().board().connect().stream()
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(whiteGameStart::set));

        var blackListen = Thread.ofPlatform().start(() -> colors.black().board().connect().stream()
                .filter(GameStartEvent.class::isInstance)
                .map(GameStartEvent.class::cast)
                .findFirst().ifPresent(blackGameStart::set));

        var whiteSeek = Thread.ofPlatform().start(() -> colors.white()
                .board().seekRealTime(params -> params.clockRapid10m5s().color(c -> c.white()))
                .stream().findFirst());

        var blackSeek = Thread.ofPlatform().start(() -> colors.black()
                .board().seekRealTime(params -> params.clockRapid10m5s().color(c -> c.black()))
                .stream().findFirst());

        try {
            assertTrue(whiteSeek.join(Duration.ofSeconds(5)));
            assertTrue(blackSeek.join(Duration.ofSeconds(5)));
            assertTrue(whiteListen.join(Duration.ofSeconds(5)));
            assertTrue(blackListen.join(Duration.ofSeconds(5)));
        } catch (Exception e) {
            fail(e.getMessage());
            return;
        }

        assertNotNull(whiteGameStart.get());
        assertNotNull(blackGameStart.get());
        assertEquals(whiteGameStart.get().id(), blackGameStart.get().id());
    }

    private Colors findPlayers() {
        return IT.usersAuth()
            .mapMulti((ClientAuth candidate, Consumer<ClientAuth> mapper) -> {
                if (candidate.account().profile() instanceof Entry(var user)
                    && user.ratings().get(StatsPerfType.rapid) instanceof StatsPerf.StatsPerfGame stats
                    && stats.prov() == false
                    && stats.rating() >= 1000
                    && stats.rating() <= 1400) mapper.accept(candidate);
            })
            .limit(2)
            .toList() instanceof List<ClientAuth> list && list.size() == 2
            ? new Colors(list.getFirst(), list.getLast())
            : null;
    }
}
