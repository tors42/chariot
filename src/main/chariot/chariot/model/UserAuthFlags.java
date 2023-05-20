package chariot.model;

public record UserAuthFlags(boolean followable, boolean following, boolean followsYou, boolean blocking) {}
