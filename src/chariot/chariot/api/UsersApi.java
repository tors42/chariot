package chariot.api;

import module java.base;
import module chariot;

public interface UsersApi extends UsersApiBase {

    /**
     * Get public user data
     *
     * @param userId
     */
    One<? extends User> byId(String userId);

    /**
     * Get public user data
     *
     * @param userId
     * @param params
     */
    One<? extends User> byId(String userId, Consumer<UserParams> params);

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    Many<? extends User> byIds(String... userIds);

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    Many<? extends User> byIds(Collection<String> userIds);


    public interface UserParams {
        /**
         * Whether or not to include any trophies in the result
         */
        UserParams withTrophies(boolean withTrophies);

        default UserParams withTrophies() { return withTrophies(true); }

        /**
         * Whether or not to include if user accepts challenges in the result.
         */
        UserParams withChallengeable(boolean withChallengeable);
        default UserParams withChallengeable() { return withChallengeable(true); }
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


        /**
         * Also return the id, time control and variant of the game being played, if any, for each player, in a playing field.<br>
         * Defaults to false to preserve server resources.<br>
         * Disables withGameIds
         */
        UserStatusParams withGameMetas(boolean withGameMetas);
        default UserStatusParams withGameMetas() { return withGameMetas(true); }

     }
}
