package chariot.internal.impl;

import chariot.api.*;
import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.model.*;

public class AccountHandler implements AccountAuth {

    private final RequestHandler requestHandler;

    public AccountHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<User> profile() {
        return Endpoint.accountProfile.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<AccountEmail> emailAddress() {
        return Endpoint.accountEmail.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<AccountKid> getAccountKidMode() {
        return Endpoint.accountKid.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Ack> setAccountKidMode(boolean value) {
        return Endpoint.accountKidStatus.newRequest(request -> request
                .query(java.util.Map.of("v", value)))
            .process(requestHandler);
    }

    @Override
    public One<AccountPreferences> preferences() {
        return Endpoint.accountPreferences.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public Many<User> following() {
        return Endpoint.relFollowing.newRequest(request -> {})
            .process(requestHandler);
    }

}
