package it.account;

import chariot.ClientAuth;
import chariot.model.User;
import util.IntegrationTest;
import util.IT;
import static util.Assert.unboxEquals;

public class AccountAuth {

    static ClientAuth client = IT.bobby();

    @IntegrationTest
    public void profile() {
        var profile = client.account().profile();
        unboxEquals(profile, "bobby", User::id);
        unboxEquals(profile, "Bobby", User::name);
        unboxEquals(profile, IT.lilaURI().resolve("/@/Bobby"), User::url);
    }

    @IntegrationTest
    public void emailAddress() {
        unboxEquals(client.account().emailAddress(), "bobby@localhost");
    }

    @IntegrationTest
    public void kidMode() {
        unboxEquals(client.account().kidMode(), false);
        client.account().kidMode(true);
        unboxEquals(client.account().kidMode(), true);
    }
}
