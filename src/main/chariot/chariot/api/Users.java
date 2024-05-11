package chariot.api;

import chariot.model.*;

import java.util.List;
import java.util.function.Consumer;

public interface Users extends UsersBase {

    /**
     * Get public user data
     *
     * @param userId
     */
    One<User> byId(String userId);


    Many<User> byIds(String ... userIds);

    /**
     * Get public user data
     *
     * @param userId
     * @param params
     */
    One<User> byId(String userId, Consumer<UserParams> params);


    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    Many<User> byIds(List<String> userIds);


    public interface UserParams {
        /**
         * Whether or not to include any trophies in the result
         */
        UserParams withTrophies(boolean withTrophies);

        default UserParams withTrophies() { return withTrophies(true); }
    }

    public interface CrosstableParams {
        /**
         * Whether or not to include matchup in the result
         */
        CrosstableParams matchup(boolean matchup);
        default CrosstableParams matchup() { return matchup(true); }
    }

    public interface UserStatusParams {
        /**
         * Whether or not to include game IDs in the result
         */
        UserStatusParams withGameIds(boolean withGameIds);
        default UserStatusParams withGameIds() { return withGameIds(true); }

        /**
         * Also return the network signal of the player, when available.
         * It ranges from 1 (poor connection, lag {@literal >} 500ms) to 4 (great connection, lag {@literal <} 150ms)
         * Defaults to `false` to preserve server resources.
         */
        UserStatusParams withSignal(boolean withSignal);
        default UserStatusParams withSignal() { return withSignal(true); }
     }
}
