package chariot.model;

public record IdNameTitle(String id, String name, String title) implements UserInfo {

    @Override
    public String toString() {
        return "[%s] %s (%s)".formatted(title, name, id);
    }

}
