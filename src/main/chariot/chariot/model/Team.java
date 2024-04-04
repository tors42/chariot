package chariot.model;

public record Team (
        String id,
        String name,
        String description,
        boolean open,
        UserCommon leader,
        java.util.List<UserCommon> leaders,
        Integer nbMembers,
        boolean joined,
        boolean requested,
        Opt<String> descriptionPrivate
        )  {}
