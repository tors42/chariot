package it.tournamentapi;

import chariot.ClientAuth;
import chariot.model.*;
import util.*;
import static util.Assert.*;

import java.time.*;
import java.util.List;

public class RunArena {

    @IntegrationTest(expectedSeconds = 60)
    public void runAnArena() {

        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, _, _)))) {
            fail("Couldn't find team leader for creating arena");
            return;
        }

        One<Arena> createRes = client.tournaments().createArena(params -> params
                .clockRapid10m0s() // Board API fastest allowed timecontrol is Rapid
                .name("Integration Test Arena")
                .minutes(60)
                .startTime(ZonedDateTime.now().plusSeconds(5))
                .description("Just testing to run an Arena in Integration Tests")
                //.conditionTeam(teamId)
                );

        if (! (createRes instanceof Entry(Arena arena))) {
            fail("Failed to create Arena - %s".formatted(createRes));
            return;
        }

        var participants = participants(List.of("yulia", "diego"));

        var runner = new ArenaRunner(arena, client, participants, Opt.of(Duration.ofMinutes(1)));

        runner.run();

        List<ArenaResult> results = client.tournaments().resultsByArenaId(arena.id(), p -> p.sheet()).stream().toList();

        assertEquals(participants.size(), results.size(), "Expected results for all participants");
    }

    static List<ArenaRunner.Participant> participants(List<String> userIds) {
        return userIds.stream()
            .map(userId -> {
                ClientAuth client = IT.clientAuthByUserId(userId);
                UserAuth account = client.account().profile().get();
                Opt<Team> teamOpt = Opt.of();
                return new ArenaRunner.Participant(client, account, teamOpt);
            })
            .toList();
    }
}
