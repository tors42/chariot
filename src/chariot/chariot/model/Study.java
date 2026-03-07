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

    public enum UserSelection {
        nobody,
        owner,
        contributor,
        member,
        everyone,
        ;
        public interface Provider {
            default UserSelection nobody()      { return nobody; }
            default UserSelection owner()       { return owner; }
            default UserSelection contributor() { return contributor; }
            default UserSelection member()      { return member; }
            default UserSelection everyone()    { return everyone; }
        }
        public static Provider provider() {return new Provider(){};}
    }
}
