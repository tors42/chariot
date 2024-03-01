package util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import chariot.Client;
import chariot.ClientAuth;

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


    static final Map<String, Client> userIdClientBasic = new ConcurrentHashMap<>();
    static final Map<String, ClientAuth> userIdClientAuth = new ConcurrentHashMap<>();

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
