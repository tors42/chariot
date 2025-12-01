package it.tournamentapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.StructuredTaskScope.Subtask;
import java.util.stream.Collectors;
import java.util.stream.Gatherers;

public class RunArena {

    static List<String> usersForTwoSizedArena = List.of("yulia", "diego");

    @IntegrationTest(expectedSeconds = 60)
    public void runArenas() {
        try (var scope = StructuredTaskScope.open()) {
            var tasks = List.of(
                    scope.fork(() -> runTwoSizedArena()),
                    scope.fork(() -> runTeamBattleArena()));
            try {
                scope.join();
            } catch (InterruptedException ie) {}

            tasks.stream()
                .filter(task -> task.state() == Subtask.State.SUCCESS)
                .map(Subtask::get)
                .forEach(callable -> { try { callable.call(); } catch (Exception e) {} });
        }
    }



    public Callable<?> runTwoSizedArena() {

        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, _, _)))) {
            return () -> {
                fail("Couldn't find team leader for creating arena");
                return null;
            };
        }

        One<Arena> createRes = client.tournaments().createArena(params -> params
                .clockRapid10m0s() // Board API fastest allowed timecontrol is Rapid
                .name("Integration Test Arena")
                .minutes(60)
                .startTime(ZonedDateTime.now().plusSeconds(5))
                .description("Just testing to run an Arena in Integration Tests")
                //.conditionTeam(teamId)
                );

        if (! (createRes instanceof Some(Arena arena))) {
            return () -> {
                fail("Failed to create Arena - %s".formatted(createRes));
                return null;
            };
        }

        var participants = participants(usersForTwoSizedArena, Opt.of());

        var runner = new ArenaRunner(arena, client, participants, Opt.of(Duration.ofMinutes(1)));

        runner.run();

        List<ArenaResult> results = client.tournaments().resultsByArenaId(arena.id(), p -> p.sheet()).stream().toList();

        return () -> {
            assertEquals(participants.size(), results.size(), "Expected results for all participants");
            return null;
        };
    }

    public Callable<?> runTeamBattleArena() {

        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, _, _)))) {
            return () -> {
                fail("Couldn't find team leader for creating arena");
                return null;
            };
        }

        One<Arena> createRes = client.tournaments().createArena(params -> params
                .clockRapid10m0s() // Board API fastest allowed timecontrol is Rapid
                .name("IT Team Battle Arena")
                .minutes(60)
                .startTime(ZonedDateTime.now().plusSeconds(5))
                .description("Just testing to run an Team Battle Arena in Integration Tests with many characters".repeat(3))
                );

        if (! (createRes instanceof Some(Arena arena))) {
            return () -> {
                fail("Failed to create Team Battle Arena - %s".formatted(createRes));
                return null;
            };
        }

        int nbTeams = 3;
        int nbLeaders = 2;
        int nbMembers = nbLeaders + 1;

        var users = IT.userIds.stream()
            .filter(userId -> !usersForTwoSizedArena.contains(userId))
            .gather(Gatherers.windowFixed(nbMembers))
            .iterator();

        record TeamAndMembers(Team team, List<ArenaRunner.Participant> members) {}

        List<TeamAndMembers> teams = client.teams().popularTeams().stream()
            .filter(t -> !t.id().startsWith("lichess-"))
            .takeWhile(_ -> users.hasNext())
            .map(t -> new TeamAndMembers(t, participants(users.next(), Opt.of(t.id()))))
            .filter(t -> t.members().stream().noneMatch(member -> member.client().teams().joinTeam(t.team().id()) instanceof Fail))
            .limit(nbTeams)
            .toList();

        if (teams.size() <3) {
            return () -> {
                System.err.println("Couldn't find 3 teams where 3 users could join");
                // skip();
                return null;
            };
        }

        if (client.tournaments().updateTeamBattle(
                    arena.id(),
                    nbLeaders,
                    teams.stream()
                        .map(TeamAndMembers::team)
                        .map(Team::id)
                        .collect(Collectors.toSet()))
                instanceof Fail fail) {
            return () -> {
                System.err.println("Couldn't find teams with members for team battle - " + fail);
                // skip();
                return null;
            };
        }

        List<ArenaRunner.Participant> participants = teams.stream()
            .map(TeamAndMembers::members)
            .flatMap(List::stream)
            .toList();

        var runner = new ArenaRunner(arena, client, participants, Opt.of(Duration.ofMinutes(1)));

        runner.run();

        List<ArenaResult> results = client.tournaments().resultsByArenaId(arena.id(), p -> p.sheet()).stream().toList();
        List<Arena.TeamStanding> teamStandings = switch(client.tournaments().teamBattleResultsById(arena.id()).stream().toList()) {
            case List<Arena.TeamStanding> list when list.size() == teams.size() -> list;
            default -> Util.delayedSupplier(Duration.ofSeconds(2), () -> client.tournaments().teamBattleResultsById(arena.id()).stream().toList()).get();
        };

        return () -> {
            assertEquals(participants.size(), results.size(), "Expected results for all participants");
            assertEquals(teams.size(), teamStandings.size(), "Expected team standings for all teams\n" + String.join("\n", teamStandings.stream().map(Arena.TeamStanding::toString).toList()));
            return null;
        };
    }

    static List<ArenaRunner.Participant> participants(List<String> userIds, Opt<String> teamIdOpt) {
        return userIds.stream()
            .<ArenaRunner.Participant>mapMulti( (userId, mapper) -> {
                ClientAuth client = IT.clientAuthByUserId(userId);
                switch (client.account().profile()) {
                    case Some(var account) -> {
                        Opt<Team> teamOpt = switch(teamIdOpt) {
                            case Some(var teamId) -> switch (client.teams().byTeamId(teamId)) {
                                case Some(var team) -> Opt.of(team);
                                case Fail<?> f -> { IO.println(f); yield Opt.empty(); }
                            };
                            case Empty() -> Opt.empty();
                        };
                        mapper.accept(new ArenaRunner.Participant(client, account, teamOpt));
                    }
                    case Fail<?> f -> IO.println(f);
                }
            })
        .toList();
    }
}
