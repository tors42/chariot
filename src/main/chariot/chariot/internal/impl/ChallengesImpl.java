package chariot.internal.impl;

import java.util.Map;
import java.util.function.Consumer;

import chariot.api.*;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.ChallengeOpenEnded;
import chariot.model.One;

public class ChallengesImpl extends Base implements Challenges {

    public ChallengesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public One<ChallengeOpenEnded> challengeOpenEnded(Consumer<OpenEndedBuilder> consumer) {
        return Endpoint.challengeOpenEnded.newRequest(request -> request
            .post(openEndedBuilderToMap(consumer)))
            .process(this);
     }

    private Map<String,Object> openEndedBuilderToMap(Consumer<OpenEndedBuilder> consumer) {
        var builder = MapBuilder.of(OpenEndedParams.class);
        var openEndedBuilder = new OpenEndedBuilder() {
            @Override
            public OpenEndedParams clock(int initial, int increment) {
                return builder
                    .add("clock.limit", initial)
                    .add("clock.increment", increment)
                    .proxy();
            }
            @Override
            public OpenEndedParams daysPerTurn(int daysPerTurn) {
                return builder
                    .add("days", daysPerTurn)
                    .proxy();
            }
        };
        consumer.accept(openEndedBuilder);
        return builder.toMap();
    }
}
