package util;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import chariot.*;
import chariot.model.*;
import chariot.model.StatsPerf.StatsPerfGame;

public class IT {

    public static java.net.URI lilaURI() {
        return superadmin().account().profile().maybe().map(u -> u.url().resolve("/")).orElse(Main.itApi());
    }

    public static Stream<Client> usersBasic() {
        return userIds.stream().map(IT::clientBasicByUserId);
    }

    public static Stream<ClientAuth> usersAuth() {
        return userIds.stream().map(IT::clientAuthByUserId);
    }

    public static Client clientBasicByUserId(String userId) {
        return userIdClientBasic.computeIfAbsent(userId, _ -> Client.basic(conf -> conf.api(Main.itApi()).spacing(Duration.ZERO)));
    }

    public static ClientAuth clientAuthByUserId(String userId) {
        return userIdClientAuth.computeIfAbsent(userId, id -> Client.auth(conf -> conf.api(Main.itApi()).spacing(Duration.ZERO), "lip_" + id));
    }

    public static ClientAuth admin()      { return clientAuthByUserId("admin"); }
    public static ClientAuth superadmin() { return clientAuthByUserId("superadmin"); }

    public static ClientAuth bobby() { return clientAuthByUserId("bobby"); }
    public static ClientAuth boris() { return clientAuthByUserId("boris"); }
    public static ClientAuth diego() { return clientAuthByUserId("diego"); }
    public static ClientAuth yulia() { return clientAuthByUserId("yulia"); }

    static final Map<String, Client> userIdClientBasic = new ConcurrentHashMap<>();
    static final Map<String, ClientAuth> userIdClientAuth = new ConcurrentHashMap<>();

    public record Players(ClientAuth white, String whiteId, ClientAuth black, String blackId) {}
    public static Opt<Players> findPlayers() {
        return findPlayers(StatsPerfType.rapid);
    }
    public static Opt<Players> findPlayers(StatsPerfType statsType) {
        record IdAndRating(String id, int rating) {}
        var candidates = admin().users().byIds(userIds).stream()
            .map(user -> switch(user) {
                case User u when u.ratings().get(statsType) instanceof StatsPerfGame statsPerf
                    && statsPerf.prov() == false -> new IdAndRating(u.id(), statsPerf.rating());
                default -> new IdAndRating(user.id(), -1); })
            .filter(r -> r.rating() != -1)
            .sorted(Comparator.comparingInt(IdAndRating::rating))
            .toList();

        record Candidates(IdAndRating first, IdAndRating second) {}
        var bestMatch = IntStream.range(0, candidates.size()-1)
            .mapToObj(i -> new Candidates(
                        candidates.get(i),
                        candidates.get(i+1)))
            .min(Comparator.comparingInt(pair -> Math.abs(pair.first().rating() - pair.second().rating())));
        return bestMatch.map(pairing -> Opt.of(new Players(
                    clientAuthByUserId(pairing.first().id()), pairing.first().id(),
                    clientAuthByUserId(pairing.second().id()), pairing.second().id())))
            .orElse(Opt.of());
    }

    public record TeamLeader(ClientAuth client, String userId, String teamId) {}

    public static Opt<TeamLeader> findTeamLeader() {
        return findTeamLeader(Set.of());
    }
    public static Opt<TeamLeader> findTeamLeader(Set<String> excludingUserIds) {
        record IdAndTeamList(String userId, List<Team> teams) {}
        return userIds.stream()
            .filter(userId -> !excludingUserIds.contains(userId))
            .map(userId -> new IdAndTeamList(
                        userId,
                        admin().teams().byUserId(userId).stream()
                            .filter(team -> team.leader().id().equals(userId))
                            .sorted(Comparator.comparing(t -> t.name().length()))
                            .limit(1)
                            .toList()))
            .filter(idAndList -> ! idAndList.teams().isEmpty())
            .findFirst()
            .map(idAndList -> new TeamLeader(clientAuthByUserId(idAndList.userId()), idAndList.userId(), idAndList.teams().getFirst().id()))
            .map(Opt::of)
            .orElse(Opt.of());
    }

    public static final List<String> userIds = """
        li
        ana
        ivan
        diego
        lola
        angel
        mei
        yaroslava
        dmitry
        elena
        milena
        dae
        boris
        yulia
        mateo
        yevgeny
        iryna
        bobby
        elizabeth
        marcel
        yunel
        akeem
        abdul
        veer
        rudra
        sai
        yarah
        gabriela
        ikem
        idris
        frances
        hans
        pedro
        emmanuel
        svetlana
        qing
        ekaterina
        yun
        xioyan
        monica
        david
        guang
        nushi
        mohammed
        jose
        luis
        mary
        hui
        fatima
        aleksandr
        jiang
        vera
        anthony
        ramesh
        suresh
        aaron
        jacob
        salma
        margarita
        benjamin
        abubakar
        kenneth
        adriana
        patricia""".lines().toList();
}
