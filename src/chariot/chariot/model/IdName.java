package chariot.model;

public record IdName(String id, String name) implements UserInfo {

    @Override
    public String toString() {
        return "%s (%s)".formatted(name, id);
    }

}
