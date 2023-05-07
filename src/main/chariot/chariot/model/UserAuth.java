package chariot.model;

public interface UserAuth extends User {
    // only available if OAuth

    default boolean followable() { return _userData()._followable().orElse(false); }
    default boolean following()  { return _userData()._following().orElse(false); }
    default boolean followsYou() { return _userData()._followsYou().orElse(false); }
    default boolean blocking()   { return _userData()._blocking().orElse(false); }
    default boolean streaming()  { return _userData()._streaming().orElse(false); }
}
