package chariot.model;

public record TeamMemberData(UserData _userData, String teamId) implements TeamMemberAuth {}
