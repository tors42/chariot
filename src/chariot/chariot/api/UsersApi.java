package chariot.api;

import module java.base;
import module chariot;

public interface UsersApi extends UsersApiBase {

    /// Get public user data
    default One<? extends User> byId(String userId) { return byId(userId, _ -> {}); }

    /// Get public user data
    One<? extends User> byId(String userId, Consumer<UserParams> params);

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    default Many<? extends User> byIds(Consumer<UsersParams> params, String... userIds) {
        return byIds(Arrays.asList(userIds), params);
    }

    /// See {@link #byIds(java.util.function.Consumer, String...)}
    default Many<? extends User> byIds(String... userIds) { return byIds(_ -> {}, userIds); }

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    Many<? extends User> byIds(Collection<String> userIds, Consumer<UsersParams> params);

    /// See {@link #byIds(java.util.Collection, java.util.function.Consumer)}
    default Many<? extends User> byIds(Collection<String> userIds) { return byIds(userIds, _ -> {}); }

    interface ProfileRank<T> {
        /// Whether or not to include user profile in the result - Default `true`
        T profile(boolean profile);
        /// Whether or not to include user profile in the result - Default `true`
        default T profile() { return profile(true); }

        /// Whether or not to include user rank in the stats in the result - Default `false`
        T rank(boolean rank);
        /// Whether or not to include user rank in the stats in the result - Default `false`
        default T rank() { return rank(true); }
    }

    interface UsersParams extends ProfileRank<UsersParams> {}

    interface UserParams extends ProfileRank<UserParams> {

        /// Whether or not to include any trophies in the result - Default `false`
        UserParams trophies(boolean withTrophies);
        /// Whether or not to include any trophies in the result - Default `false`
        default UserParams trophies() { return trophies(true); }

        /// Whether or not to include if user accepts challenges in the result - Default `false`
        UserParams challenge(boolean challenge);
        /// Whether or not to include if user accepts challenges in the result - Default `false`
        default UserParams challenge() { return challenge(true); }



        /// Whether or not to include any trophies in the result - Default `false`
        /// @deprecated
        @Deprecated
        default UserParams withTrophies(boolean withTrophies) { return trophies(withTrophies); }
        /// Whether or not to include any trophies in the result - Default `false`
        /// @deprecated
        @Deprecated
        default UserParams withTrophies() { return trophies(true); }

        /// Whether or not to include if user accepts challenges in the result - Default `false`
        /// @deprecated
        @Deprecated
        default UserParams withChallengeable(boolean withChallengeable) { return challenge(withChallengeable); }
        /// Whether or not to include if user accepts challenges in the result - Default `false`
        /// @deprecated
        @Deprecated
        default UserParams withChallengeable() { return challenge(true); }
    }

    interface CrosstableParams {
        /**
         * Whether or not to include matchup in the result
         */
        CrosstableParams matchup(boolean matchup);
        default CrosstableParams matchup() { return matchup(true); }
    }

    interface UserStatusParams {
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
