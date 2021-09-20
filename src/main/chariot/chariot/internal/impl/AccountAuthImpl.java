package chariot.internal.impl;

import java.util.Set;
import java.util.function.Supplier;

import chariot.Client.Scope;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.AccountEmail;
import chariot.model.AccountKid;
import chariot.model.AccountPreferences;
import chariot.model.Ack;
import chariot.model.Result;
import chariot.model.User;

public class AccountAuthImpl extends AccountImpl implements chariot.api.AccountAuth {
    public AccountAuthImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<User> profile() {
        var request = Endpoint.accountProfile.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<AccountEmail> emailAddress() {
        var request = Endpoint.accountEmail.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<Boolean> getKidModeStatus() {
        var request = Endpoint.accountKid.newRequest()
            .build();
        var kid = fetchOne(request);
        if (kid instanceof Result.One<AccountKid> k) {
            return Result.one(Boolean.valueOf(k.entry().kid()));
        } else {
            return Result.fail(kid.error());
        }
    }

    @Override
    public Result<Ack> setKidModeStatus(boolean value) {
        var request = Endpoint.accountKidStatus.newRequest()
            .query(java.util.Map.of("v", value))
            .post()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<AccountPreferences> preferences() {
        var request = Endpoint.accountPreferences.newRequest()
            .build();
        return fetchOne(request);
    }

    @Override
    public Result<User> following() {
        var request = Endpoint.relFollowing.newRequest()
            .build();
        return fetchMany(request);
    }


    @Override
    public Set<Scope> scopes(Supplier<char[]> token) {
        var scopes = client.fetchScopes(Endpoint.accountProfile.endpoint(), token);
        return scopes;
    }

    @Override
    public Result<Ack> revokeToken() {
        var request = Endpoint.apiTokenRevoke.newRequest()
            .delete()
            .build();
        return fetchOne(request);
    }

}

