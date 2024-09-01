package it.tournamentapi;

import chariot.api.TournamentsApiAuth.SwissBuilder;
import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import util.*;
import static util.Assert.*;

import java.util.function.Consumer;

public class SwissCreateClock {

    @IntegrationTest
    public void bullet() {
        Clock clock = Clock.ofMinutes(0).withIncrementSeconds(1);
        var res = _withClock(params -> params.clock(clock));
        unboxEquals(res, clock, swiss -> swiss.tourInfo().clock());
        unboxEquals(res, Speed.bullet, swiss -> swiss.tourInfo().speed());
    }

    @IntegrationTest
    public void blitz() {
        Clock clock = Clock.ofMinutes(0).withIncrementSeconds(5);
        var res = _withClock(params -> params.clock(clock));
        unboxEquals(res, clock, swiss -> swiss.tourInfo().clock());
        unboxEquals(res, Speed.blitz, swiss -> swiss.tourInfo().speed());
    }

    @IntegrationTest
    public void rapid() {
        Clock clock = Clock.ofMinutes(10);
        var res = _withClock(params -> params.clock(clock));
        unboxEquals(res, clock, swiss -> swiss.tourInfo().clock());
        unboxEquals(res, Speed.rapid, swiss -> swiss.tourInfo().speed());
    }

    @IntegrationTest
    public void classical() {
        Clock clock = Clock.ofMinutes(10).withIncrementSeconds(120);
        var res = _withClock(params -> params.clock(clock));
        unboxEquals(res, clock, swiss -> swiss.tourInfo().clock());
        unboxEquals(res, Speed.classical, swiss -> swiss.tourInfo().speed());
    }

    One<Swiss> _withClock(Consumer<SwissBuilder> builderConsumer) {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            return One.fail(-1, Err.from("Couldn't find team leader for creating a swiss"));
        }

        return client.tournaments().createSwiss(teamId, builderConsumer);
    }
}
