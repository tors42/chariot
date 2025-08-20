package chariot.model;

import java.util.Optional;

public sealed interface UserInfo permits IdName, IdNameTitle {
    String id();
    String name();
    default Optional<String> titleOpt() { return this instanceof IdNameTitle info
        ? Optional.of(info.title())
        : Optional.empty();
    }

    static UserInfo of(String id, String name) { return of(id, name, null); }
    static UserInfo of(String id, String name, String title) {
        return title == null ? new IdName(id, name) : new IdNameTitle(id, name, title);
    }
}
