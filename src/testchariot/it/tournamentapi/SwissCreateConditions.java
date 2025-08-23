package it.tournamentapi;

import chariot.api.TournamentsApiAuth.SwissBuilder;
import chariot.api.TournamentsApiAuth.SwissParams;
import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.*;
import java.util.function.Consumer;

public class SwissCreateConditions {

    final static Clock clock = new Clock(Duration.ofMinutes(10), Duration.ofSeconds(5));
    final static Speed speed = Speed.fromClock(clock);

    @IntegrationTest
    public void noConditions() {
        unboxEquals(_withConditions(_ -> {}),
                List.of(),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void allowListHidden() {
        unboxEquals(_withConditions(p -> p.allowList(List.of("someone"))),
                List.of(Condition.allowListHidden()),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void entryCode() {
        unboxEquals(_withConditions(p -> p.entryCode("The Entry Code")),
                List.of(Condition.entryCode()),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void titled() {
        unboxEquals(_withConditions(p -> p.conditionTitled()),
                List.of(Condition.titled()),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void maxRating() {
        unboxEquals(_withConditions(p -> p.conditionMaxRating(1000), false),
                List.of(Condition.maxRating(1000, speed)),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void minRating() {
        unboxEquals(_withConditions(p -> p.conditionMinRating(1000)),
                List.of(Condition.minRating(1000, speed)),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void minRatedGames() {
        unboxEquals(_withConditions(p -> p.conditionMinRatedGames(30)),
                List.of(Condition.minRatedGames(30, speed)),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void playYourGames() {
        unboxEquals(_withConditions(p -> p.conditionPlayYourGames(), false),
                List.of(Condition.notMissedSwiss()),
                swiss -> swiss.conditions().list());
    }

    @IntegrationTest
    public void minAccountAge() {

        unboxEquals(_withConditions(p -> p.conditionAccountAge(1)),
                List.of(Condition.minAccountAge(Period.ofDays(1))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(3)),
                List.of(Condition.minAccountAge(Period.ofDays(3))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(7)),
                List.of(Condition.minAccountAge(Period.ofDays(7))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(14)),
                List.of(Condition.minAccountAge(Period.ofDays(14))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(30)),
                List.of(Condition.minAccountAge(Period.ofMonths(1))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(60)),
                List.of(Condition.minAccountAge(Period.ofMonths(2))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(90)),
                List.of(Condition.minAccountAge(Period.ofMonths(3))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(180)),
                List.of(Condition.minAccountAge(Period.ofMonths(6))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(365)),
                List.of(Condition.minAccountAge(Period.ofYears(1))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(730)),
                List.of(Condition.minAccountAge(Period.ofYears(2))),
                swiss -> swiss.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(1095)),
                List.of(Condition.minAccountAge(Period.ofYears(3))),
                swiss -> swiss.conditions().list());
    }

    One<Swiss> _withConditions(Consumer<SwissParams> paramsConsumer) {
        return _withConditions(paramsConsumer, false);
    }

    One<Swiss> _withConditions(Consumer<SwissParams> paramsConsumer, boolean debug) {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, _, var teamId)))) {
            return One.fail(-1, "Couldn't find team leader for creating a swiss");
        }

        Consumer<SwissBuilder> builderConsumer = swissBuilder ->
            paramsConsumer.accept(swissBuilder.clock(clock));


        if (debug) client.logging(l -> l.request().all().response().all());

        var result = client.tournaments().createSwiss(teamId, builderConsumer);

        if (debug) client.logging(l -> l.request().warning().response().warning());

        return result;
    }

    // Just here to perform exhaustive check on Condition,
    // so tests can be added/removed when Condition are added/removed.
    void covered(SwissCondition condition) {
        switch(condition) {
            case Condition.MinRatedGames(_, _) -> minRatedGames();
            case Condition.MaxRating(_, _)     -> maxRating();
            case Condition.MinRating(_ , _)    -> minRating();
            case Condition.Titled()            -> titled();
            case Condition.MinAccountAge(_)    -> minAccountAge();
            case Condition.AllowList(_)        -> allowListHidden();
            case Condition.AllowListHidden()   -> allowListHidden();
            case Condition.EntryCode()         -> entryCode();
            case Condition.NotMissedSwiss()    -> playYourGames();
            case Condition.Generic(_)          -> {}
        };
    }
}
