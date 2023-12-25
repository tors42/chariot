package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.Endpoint;
import chariot.internal.RequestHandler;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class AccountHandler implements AccountAuth {

    private final RequestHandler requestHandler;

    public AccountHandler(RequestHandler requestHandler) {
        this.requestHandler = requestHandler;
    }

    @Override
    public One<UserAuth> profile() {
        return Endpoint.accountProfile.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<String> emailAddress() {
        return Endpoint.accountEmail.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Boolean> kidMode() {
        return Endpoint.accountKid.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public One<Void> kidMode(boolean value) {
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
    public Many<UserAuth> following() {
        return Endpoint.relFollowing.newRequest(request -> {})
            .process(requestHandler);
    }

    @Override
    public Many<TimelineEntry> timeline(Consumer<TimelineParams> params) {
        return Endpoint.timeline.newRequest(request -> request
                .query(MapBuilder.of(TimelineParams.class).toMap(params)))
            .process(requestHandler);
    }
}
