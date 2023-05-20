package chariot.model;

import java.time.ZonedDateTime;

public record TeamMember(User user, String teamId, ZonedDateTime joinedTeamAt) {}
