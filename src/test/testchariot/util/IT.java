package util;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import chariot.*;
import chariot.model.*;
import chariot.model.StatsPerf.StatsPerfGame;

public class IT {

    public static Stream<Client> usersBasic() {
        return userIds.stream().map(IT::clientBasicByUserId);
    }

    public static Stream<ClientAuth> usersAuth() {
        return userIds.stream().map(IT::clientAuthByUserId);
    }

    public static Client clientBasicByUserId(String userId) {
        return userIdClientBasic.computeIfAbsent(userId, id -> Client.basic(conf -> conf.api(Main.itApi())));
    }

    public static ClientAuth clientAuthByUserId(String userId) {
        return userIdClientAuth.computeIfAbsent(userId, id -> Client.auth(conf -> conf.api(Main.itApi()), "lip_" + id));
    }

    public static ClientAuth admin() { return clientAuthByUserId("admin"); }

    public static ClientAuth bobby() { return clientAuthByUserId("bobby"); }
    public static ClientAuth boris() { return clientAuthByUserId("boris"); }
    public static ClientAuth diego() { return clientAuthByUserId("diego"); }
    public static ClientAuth yulia() { return clientAuthByUserId("yulia"); }

    static final Map<String, Client> userIdClientBasic = new ConcurrentHashMap<>();
    static final Map<String, ClientAuth> userIdClientAuth = new ConcurrentHashMap<>();

    public record Players(ClientAuth white, ClientAuth black) {}
    public static Players findPlayers() {
        record IdAndRating(String id, int rating) {}
        var candidates = admin().users().byIds(userIds).stream()
            .map(user -> switch(user) {
                case User u when u.ratings().get(StatsPerfType.rapid) instanceof StatsPerfGame rapidPerf
                    && rapidPerf.prov() == false -> new IdAndRating(u.id(), rapidPerf.rating());
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
        return bestMatch.map(pairing -> new Players(
                    clientAuthByUserId(pairing.first().id()),
                    clientAuthByUserId(pairing.second().id())))
            .orElse(null);
    }

    static final List<String> userIds = """
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
