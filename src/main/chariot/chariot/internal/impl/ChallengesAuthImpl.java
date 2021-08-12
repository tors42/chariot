package chariot.internal.impl;

import java.util.Map;

import chariot.Client.Scope;
import chariot.internal.Endpoint;
import chariot.internal.Util;
import chariot.internal.InternalClient;
import chariot.model.Ack;
import chariot.model.BulkPairing;
import chariot.model.BulkPairings;
import chariot.model.PendingChallenges;
import chariot.model.Result;

public class ChallengesAuthImpl extends ChallengesAuthCommonImpl implements Internal.ChallengeAuth {

    public ChallengesAuthImpl(InternalClient client) {
        super(client, Scope.challenge_write);
    }

    @Override
    public Result<PendingChallenges> challenges() {
        var request = Endpoint.challenges.newRequest()
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> startClocksOfGame(String gameId, String token1, String token2) {
        var request = Endpoint.startClocksOfGame.newRequest()
            .path(gameId)
            .query(Map.of("token1", token1, "token2", token2))
            .post()
            .build();

        return fetchOne(request);
    }


    @Override
    public Result<Ack> addTimeToGame(String gameId, int seconds) {
        if (seconds < 1 || seconds > 86400) {
            return Result.fail("Seconds to add must be [ 1 .. 86400 ]");
        }
        var request = Endpoint.addTimeToGame.newRequest()
            .post()
            .path(gameId, seconds)
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<BulkPairing> bulks() {
        var request = Endpoint.bulkPairingGet.newRequest()
            .build();

        var result = fetchOne(request);

        if (result instanceof Result.One<BulkPairings> o) {
            return Result.many(o.entry().bulks().stream());
        } else {
            return Result.fail(result.error());
        }
    }

    @Override
    public Result<BulkPairing> createBulk(InternalBulkParameters parameters) {

        if (parameters instanceof InternalBulkParameters.Parameters p) {
            var parameterString = Util.urlEncode(p.params());

            var request = Endpoint.bulkPairingCreate.newRequest()
                .post(parameterString)
                .build();

            var result = fetchOne(request);

            return result;
        }

        return Result.fail("Unknown parameters " + parameters);
    }

    @Override
    public Result<Ack> startBulk(String bulkId) {
        var request = Endpoint.bulkPairingStart.newRequest()
            .path(bulkId)
            .post()
            .build();

        return fetchOne(request);
    }

    @Override
    public Result<Ack> cancelBulk(String bulkId) {
        var request = Endpoint.bulkPairingCancel.newRequest()
            .path(bulkId)
            .delete()
            .build();

        return fetchOne(request);
    }

}
