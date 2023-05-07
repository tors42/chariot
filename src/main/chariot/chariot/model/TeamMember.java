package chariot.model;

import java.time.ZonedDateTime;

public interface TeamMember extends User {
    String teamId();
    default ZonedDateTime joinedTeamAt() { return _userData()._joinedTeamAt().orElse(null); }
}
