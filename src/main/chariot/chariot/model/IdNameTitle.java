package chariot.model;

public record IdNameTitle(String id, String name, String title) implements UserInfo {

    @Override
    public String toString() {
        return title != null
            ? "[%s] %s (%s)".formatted(title, name, id)
            :      "%s (%s)".formatted(name, id);
    }

}
