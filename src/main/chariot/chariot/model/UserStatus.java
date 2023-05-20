package chariot.model;

import java.util.Optional;

public sealed interface UserStatus extends UserCommon permits UStatus, PlayingStatus {

    private UStatus ustatus() {
        if (this instanceof UStatus ustatus) return ustatus;
        if (this instanceof PlayingStatus playingStatus) return playingStatus.status();
        return null;
    }

    default boolean          online() { return ustatus().online(); }
    default boolean          playing() { return ustatus().playing(); }
    default Optional<String> playingGameId() {
        if (this instanceof PlayingStatus playingStatus) {
            return Optional.of(playingStatus.gameId());
        }
        return Optional.empty();
    }
}
