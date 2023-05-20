package chariot.api;

import chariot.model.*;

import java.util.*;
import java.util.function.Consumer;

public interface UsersAuth extends UsersBase {

    /**
     * Get public user data
     *
     * @param userId
     */
    One<UserAuth> byId(String userId); //, Consumer<UserParams> params);


    /**
     * Get public user data
     *
     * @param userIds
     */
    Many<UserAuth> byIds(String ... userIds);


    /**
     * Get public user data
     *
     * @param userIds A list of up to 300 user ids
     */
    Many<UserAuth> byIds(List<String> userIds);

    One<Void> sendMessageToUser(String userId, String text);

    One<Void> followUser(String userId);

    One<Void> unfollowUser(String userId);

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friend If true, returns followed players matching {@code term} if any, else returns other players.
     */
    Many<String> autocompleteNames(String term, boolean friend);

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friend If true, returns followed players matching {@code term} if any, else returns other players.
     */
    Many<UserStatus> autocompleteUsers(String term, boolean friend);




    ///**
    // * Get public user data
    // *
    // * @param userId
    // */
    //default One<UserAuth> byId(String userId) {
    //    return byId(userId, __ -> {});
    //}

}
