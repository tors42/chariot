package chariot.internal.impl;

import java.util.Set;

import chariot.Client.Scope;
import chariot.api.*;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.*;

public class AccountAuthImpl extends AccountImpl implements AccountAuth {
    public AccountAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<User> profile() {
        return Endpoint.accountProfile.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<AccountEmail> emailAddress() {
        return Endpoint.accountEmail.newRequest(request -> {})
            .process(this);
    }

    @Override
    public One<Boolean> getKidModeStatus() {
        var res = Endpoint.accountKid.newRequest(request -> {})
            .process(this);

        return res instanceof Entry<AccountKid> ak ?
            One.entry(ak.entry().kid()) : One.fail(-1, Err.from(res.toString()));
    }

    @Override
    public One<Ack> setKidModeStatus(boolean value) {
        return Endpoint.accountKidStatus.newRequest(request -> request
                .query(java.util.Map.of("v", value)))
            .process(this);
    }

    @Override
    public One<AccountPreferences> preferences() {
        return Endpoint.accountPreferences.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Many<User> following() {
        return Endpoint.relFollowing.newRequest(request -> {})
            .process(this);
    }

    @Override
    public Set<Scope> scopes() {
        return client.fetchScopes(Endpoint.accountProfile.endpoint());
    }

    @Override
    public One<Ack> revokeToken() {
        return Endpoint.apiTokenRevoke.newRequest(request -> {})
            .process(this);
    }
}

