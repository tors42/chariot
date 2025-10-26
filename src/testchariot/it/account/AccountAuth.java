package it.account;

import module chariot;

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
       bobby.account().kidMode(true);
       unboxEquals(bobby.account().kidMode(), true);
       bobby.account().kidMode(false);
       unboxEquals(bobby.account().kidMode(), false);
    }

    @IntegrationTest
    public void blocking() {
        bobby.users().unblockUser("yulia");
        yulia.users().unblockUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), false, UserAuth::blocked);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::blocked);

        bobby.users().blockUser("yulia");

        unboxEquals(bobby.users().byId("yulia"), true, UserAuth::blocked);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::blocked);

        yulia.users().blockUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), true, UserAuth::blocked);
        unboxEquals(yulia.users().byId("bobby"), true, UserAuth::blocked);

        bobby.users().unblockUser("yulia");
        yulia.users().unblockUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), false, UserAuth::blocked);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::blocked);
    }

    @IntegrationTest
    public void following() {
        bobby.account().following().stream().forEach(followed -> bobby.users().unfollowUser(followed.id()));
        yulia.account().following().stream().forEach(followed -> yulia.users().unfollowUser(followed.id()));

        unboxEquals(bobby.users().byId("yulia"), false, UserAuth::following);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::following);
        assertFalse(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        bobby.users().followUser("yulia");

        unboxEquals(bobby.users().byId("yulia"), true, UserAuth::following);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::following);
        assertTrue(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        yulia.users().followUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), true, UserAuth::following);
        unboxEquals(yulia.users().byId("bobby"), true, UserAuth::following);
        assertTrue(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertTrue(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));

        bobby.users().unfollowUser("yulia");
        yulia.users().unfollowUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), false, UserAuth::following);
        unboxEquals(yulia.users().byId("bobby"), false, UserAuth::following);
        assertFalse(bobby.account().following().stream().map(UserCommon::id).anyMatch("yulia"::equals));
        assertFalse(yulia.account().following().stream().map(UserCommon::id).anyMatch("bobby"::equals));
    }

    @IntegrationTest
    public void followable() {
        // "followable" is not changeable via the API.
        // It is configurable via,
        // https://lichess.org/account/preferences/privacy#letOtherPlayersFollowYou

        bobby.account().following().stream().forEach(followed -> bobby.users().unfollowUser(followed.id()));
        bobby.users().unblockUser("yulia");
        yulia.account().following().stream().forEach(followed -> yulia.users().unfollowUser(followed.id()));
        yulia.users().unblockUser("bobby");

        unboxEquals(bobby.users().byId("yulia"), true, UserAuth::followable);
        unboxEquals(yulia.users().byId("bobby"), true, UserAuth::followable);

        bobby.users().followUser("yulia");
        bobby.users().blockUser("yulia");
        bobby.account().kidMode(true);

        unboxEquals(yulia.users().byId("bobby"), true, UserAuth::followable);

        bobby.account().kidMode(false);
        bobby.users().unblockUser("yulia");
        bobby.users().unfollowUser("yulia");
    }
}
