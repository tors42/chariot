package it.account;

import chariot.Client;
import chariot.ClientAuth;
import chariot.model.*;
import util.IntegrationTest;
import static util.Assert.*;

public class AccountAuth {

    static ClientAuth client = Client.auth(conf -> conf.api("http://lila:9663"), "lip_bobby");

    @IntegrationTest
    public void profile() {
        switch (client.account().profile()) {
            case Entry(var user) -> assertEquals("bobby", user.id());
            case NoEntry<?> fail -> fail(() -> fail);
        }
    }

    @IntegrationTest
    public void emailAddress() {
        switch(client.account().emailAddress()) {
            case Entry(var email) -> assertEquals("bobby@localhost", email);
            case NoEntry<?> fail  -> fail(fail);
        }
    }

    @IntegrationTest
    public void kidMode() {
        switch(client.account().kidMode()) {
            case Entry(var kid)  -> assertFalse(kid, "Expected kid mode disabled");
            case NoEntry<?> fail -> fail(fail);
        }

        client.account().kidMode(true);

        switch(client.account().kidMode()) {
            case Entry(var kid)  -> assertTrue(kid, "Expected kid mode enabled");
            case NoEntry<?> fail -> fail(fail);
        }
    }
}
