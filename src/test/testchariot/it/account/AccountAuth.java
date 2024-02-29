package it.account;

import chariot.ClientAuth;
import chariot.model.User;
import util.IntegrationTest;
import static util.Assert.assertResult;

public class AccountAuth {

    static ClientAuth client = ClientAuth.auth(conf -> conf.api("http://lila:9663"), "lip_bobby");

    @IntegrationTest
    public void profile() {
        assertResult(client.account().profile(), "bobby", User::id);
    }

    @IntegrationTest
    public void emailAddress() {
        assertResult(client.account().emailAddress(), "bobby@localhost");
    }

    @IntegrationTest
    public void kidMode() {
        assertResult(client.account().kidMode(), false);
        client.account().kidMode(true);
        assertResult(client.account().kidMode(), true);
    }
}
