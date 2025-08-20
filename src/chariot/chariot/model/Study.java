package chariot.model;

import java.util.List;
import java.time.ZonedDateTime;

public record Study(
        String id,
        String name,
        boolean liked,
        int likes,
        ZonedDateTime updatedAt,
        UserCommon owner,
        List<String> chapters,
        List<String> topics,
        List<StudyMember> members
        ) {
    public record StudyMember(UserCommon user, String role) {}
}
