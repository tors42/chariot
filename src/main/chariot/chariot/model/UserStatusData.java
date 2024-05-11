package chariot.model;

public record UserStatusData(
        UserCommon common,
        boolean online,
        boolean playing,
        Opt<String> playingGameId,
        Opt<Integer> signal
        ) implements UserStatus {}
