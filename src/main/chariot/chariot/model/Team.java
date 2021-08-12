package chariot.model;

public record Team (
        String id,
        String name,
        String description,
        boolean open,
        LightUser leader,
        java.util.List<LightUser> leaders,
        Integer nbMembers
        ) implements Model {}
