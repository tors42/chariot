package chariot.model;

import java.util.Optional;

public interface UserCommon {
    UserData _userData();
    default String id() { return _userData()._id(); }
    default String name() { return _userData()._name(); }
    default Optional<String> title() { return _userData()._title(); }
    default boolean patron() { return _userData()._patron().orElse(false); }
}
