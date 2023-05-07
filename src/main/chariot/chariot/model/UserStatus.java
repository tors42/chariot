package chariot.model;

import java.util.Optional;

public record UserStatus(UserData _userData) implements UserCommon {
    public boolean          online()        { return _userData()._online().orElse(false); }
    public boolean          playing()       { return _userData()._isPlaying().orElse(false); }
    public Optional<String> playingGameId() { return _userData()._playingGameId(); }
}
