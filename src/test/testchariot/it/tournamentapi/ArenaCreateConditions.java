package it.tournamentapi;

import chariot.api.TournamentsApiAuth.ArenaBuilder;
import chariot.api.TournamentsApiAuth.ArenaParams;
import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public class ArenaCreateConditions {

    final static Clock clock = new Clock(Duration.ofMinutes(10), Duration.ofSeconds(5));
    final static Speed speed = Speed.fromClock(clock);

    @IntegrationTest
    public void noConditions() {
        unboxEquals(_withConditions(_ -> {}),
                List.of(Condition.bots(false)),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void allowList() {
        unboxEquals(_withConditions(p -> p.allowList(List.of("someone"))),
                List.of(Condition.allowList(List.of("someone")), Condition.bots(false)),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void entryCode() {
        unboxEquals(_withConditions(p -> p.entryCode("The Entry Code")),
                List.of(Condition.bots(false), Condition.entryCode()),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void titled() {
        unboxEquals(_withConditions(p -> p.conditionTitled()),
                List.of(Condition.bots(false), Condition.titled()),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void bots() {
        unboxEquals(_withConditions(p -> p.conditionBots(false)),
                List.of(Condition.bots(false)),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionBots(true)),
                List.of(Condition.bots(true)),
                arena -> arena.conditions().list());
    }


    @IntegrationTest
    public void maxRating() {
        unboxEquals(_withConditions(p -> p.conditionMaxRating(1000)),
                List.of(Condition.bots(false), Condition.maxRating(1000, speed)),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void minRating() {
        unboxEquals(_withConditions(p -> p.conditionMinRating(1000)),
                List.of(Condition.bots(false), Condition.minRating(1000, speed)),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void minRatedGames() {
        unboxEquals(_withConditions(p -> p.conditionMinRatedGames(30)),
                List.of(Condition.bots(false), Condition.minRatedGames(30, speed)),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void member() {
        AtomicReference<String> teamId = new AtomicReference<>();
        Consumer<String> teamIdConsumer = teamId::set;
        Consumer<ArenaParams> paramsConsumer = params -> params.conditionTeam(teamId.get());

        unboxEquals(_withConditions(paramsConsumer, teamIdConsumer, false),
                List.of(Condition.bots(false), Condition.member(teamId.get())),
                arena -> arena.conditions().list());
    }

    @IntegrationTest
    public void minAccountAge() {

        unboxEquals(_withConditions(p -> p.conditionAccountAge(1)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofDays(1))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(3)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofDays(3))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(7)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofDays(7))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(14)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofDays(14))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(30)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofMonths(1))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(60)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofMonths(2))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(90)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofMonths(3))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(180)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofMonths(6))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(365)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofYears(1))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(730)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofYears(2))),
                arena -> arena.conditions().list());

        unboxEquals(_withConditions(p -> p.conditionAccountAge(1095)),
                List.of(Condition.bots(false), Condition.minAccountAge(Period.ofYears(3))),
                arena -> arena.conditions().list());
    }

    One<Arena> _withConditions(Consumer<ArenaParams> paramsConsumer) {
        return _withConditions(paramsConsumer, false);
    }

    One<Arena> _withConditions(Consumer<ArenaParams> paramsConsumer, boolean debug) {
        return _withConditions(paramsConsumer, _ -> {}, debug);
    }

    One<Arena> _withConditions(Consumer<ArenaParams> paramsConsumer, Consumer<String> teamIdConsumer, boolean debug) {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, _, var teamId)))) {
            return One.fail(-1, Err.from("Couldn't find team leader for creating a arena"));
        }

        teamIdConsumer.accept(teamId);

        Consumer<ArenaBuilder> builderConsumer = arenaBuilder ->
            paramsConsumer.accept(arenaBuilder.clock(clock));


        if (debug) client.logging(l -> l.request().all().response().all());

        var result = client.tournaments().createArena(builderConsumer);

        if (debug) client.logging(l -> l.request().warning().response().warning());

        return result;
    }

    // Just here to perform exhaustive check on Condition,
    // so tests can be added/removed when Condition are added/removed.
    void covered(ArenaCondition condition) {
        switch(condition) {
            case Condition.MinRatedGames(int games, Speed speed) -> minRatedGames();
            case Condition.MaxRating(int rating, Speed speed)    -> maxRating();
            case Condition.MinRating(int rating, Speed speed)    -> minRating();
            case Condition.Titled()                              -> titled();
            case Condition.Bots(boolean allowed)                 -> bots();
            case Condition.MinAccountAge(Period age)             -> minAccountAge();
            case Condition.AllowList(List<String> users)         -> allowList();
            case Condition.AllowListHidden()                     -> allowList();
            case Condition.EntryCode()                           -> entryCode();
            case Condition.Member(String teamId)                 -> member();

            case Condition.Generic(String description)           -> {}
        };
    }
}
