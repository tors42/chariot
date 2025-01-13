package it.account;

import chariot.ClientAuth;
import chariot.model.User;
import chariot.model.UserAuth;
import chariot.model.UserCommon;
import util.IntegrationTest;
import util.IT;
import static util.Assert.assertFalse;
import static util.Assert.assertTrue;
import static util.Assert.unboxEquals;

public class AccountAuth {

    static ClientAuth bobby = IT.bobby();
    static ClientAuth yulia = IT.yulia();

    @IntegrationTest
    public void profile() {
        var profile = bobby.account().profile();
        unboxEquals(profile, "bobby", User::id);
        unboxEquals(profile, "Bobby", User::name);
        unboxEquals(profile, IT.lilaURI().resolve("/@/Bobby"), User::url);
    }

    @IntegrationTest
    public void emailAddress() {
        unboxEquals(bobby.account().emailAddress(), "bobby@localhost");
    }

    @IntegrationTest
    public void kidMode() {
        unboxEquals(bobby.account().kidMode(), false);
        bobby.account().kidMode(true);
        unboxEquals(bobby.account().kidMode(), true);
    }

    @IntegrationTest
    public void blocking() {
        bobby.usersAuth().unblockUser("yulia");
        yulia.usersAuth().unblockUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), false, UserAuth::blocked);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::blocked);

        bobby.usersAuth().blockUser("yulia");

        unboxEquals(bobby.usersAuth().byId("yulia"), true, UserAuth::blocked);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::blocked);

        yulia.usersAuth().blockUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), true, UserAuth::blocked);
        unboxEquals(yulia.usersAuth().byId("bobby"), true, UserAuth::blocked);

        bobby.usersAuth().unblockUser("yulia");
        yulia.usersAuth().unblockUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), false, UserAuth::blocked);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::blocked);
    }

    @IntegrationTest
    public void following() {
        bobby.account().following().stream().forEach(followed -> bobby.usersAuth().unfollowUser(followed.id()));
        yulia.account().following().stream().forEach(followed -> yulia.usersAuth().unfollowUser(followed.id()));

        unboxEquals(bobby.usersAuth().byId("yulia"), false, UserAuth::following);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::following);
        assertFalse(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        bobby.usersAuth().followUser("yulia");

        unboxEquals(bobby.usersAuth().byId("yulia"), true, UserAuth::following);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::following);
        assertTrue(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        yulia.usersAuth().followUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), true, UserAuth::following);
        unboxEquals(yulia.usersAuth().byId("bobby"), true, UserAuth::following);
        assertTrue(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertTrue(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        bobby.usersAuth().unfollowUser("yulia");
        yulia.usersAuth().unfollowUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), false, UserAuth::following);
        unboxEquals(yulia.usersAuth().byId("bobby"), false, UserAuth::following);
        assertFalse(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));
    }

    @IntegrationTest
    public void followable() {
        // "followable" is not changeable via the API.
        // It is configurable via,
        // https://lichess.org/account/preferences/privacy#letOtherPlayersFollowYou

        bobby.account().following().stream().forEach(followed -> bobby.usersAuth().unfollowUser(followed.id()));
        bobby.usersAuth().unblockUser("yulia");
        yulia.account().following().stream().forEach(followed -> yulia.usersAuth().unfollowUser(followed.id()));
        yulia.usersAuth().unblockUser("bobby");

        unboxEquals(bobby.usersAuth().byId("yulia"), true, UserAuth::followable);
        unboxEquals(yulia.usersAuth().byId("bobby"), true, UserAuth::followable);

        bobby.usersAuth().followUser("yulia");
        bobby.usersAuth().blockUser("yulia");
        bobby.account().kidMode(true);

        unboxEquals(yulia.usersAuth().byId("bobby"), true, UserAuth::followable);

        bobby.account().kidMode(false);
        bobby.usersAuth().unblockUser("yulia");
        bobby.usersAuth().unfollowUser("yulia");
    }
}
