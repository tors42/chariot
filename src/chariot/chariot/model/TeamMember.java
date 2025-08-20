package chariot.model;

import java.time.ZonedDateTime;

public record TeamMember(UserCommon user, String teamId, ZonedDateTime joinedTeamAt) {}
