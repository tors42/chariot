package chariot.model;

public sealed interface UserStatus extends UserCommon permits UserStatusData {
    boolean       online();
    boolean       playing();
    Opt<String>   playingGameId();
    Opt<GameMeta> playingGameMeta();
    Opt<Integer>  signal();

    public record GameMeta(String id, String clock, Enums.GameVariant variant) {}
}
