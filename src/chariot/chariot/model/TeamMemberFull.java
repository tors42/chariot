package chariot.model;

import java.time.ZonedDateTime;

public record TeamMemberFull(User user, String teamId, ZonedDateTime joinedTeamAt) {}
