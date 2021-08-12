package chariot.internal.impl;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.Util;
import chariot.internal.InternalClient;
import chariot.model.ChallengeResult;
import chariot.model.ChallengeResult.ChallengeOpenEnded;
import chariot.model.Result;

public class ChallengesImpl extends Base implements Internal.ChallengesInternal {

    public ChallengesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<ChallengeOpenEnded> challengeOpenEnded(ChallengeOpenEndedParameters parameters) {
        var parameterString = Util.urlEncode(parameters.toMap());

        var request = Endpoint.challengeOpenEnded.newRequest()
            .post(parameterString)
            .build();

        var result = fetchOne(request);

        if (result instanceof Result.One<ChallengeResult> o && o.entry() instanceof ChallengeResult.ChallengeOpenEnded open) {
            return Result.one(open);
        } else {
            return Result.fail(result.error());
        }
     }
}
