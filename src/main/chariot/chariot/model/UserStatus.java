package chariot.model;

public sealed interface UserStatus extends UserCommon permits UserStatusData {
    boolean      online();
    boolean      playing();
    Opt<String>  playingGameId();
    Opt<Integer> signal();
}
