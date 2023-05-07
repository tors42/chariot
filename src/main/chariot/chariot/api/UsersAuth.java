package chariot.api;

import chariot.model.*;

public interface UsersAuth extends Users<UserAuth> {

    One<Void> sendMessageToUser(String userId, String text);

    One<Void> followUser(String userId);
    One<Void> unfollowUser(String userId);

    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friends If true, returns followed players matching {@code term} if any, else returns other players.
     */
    Many<String>     autocompleteNames(String term, boolean friends);
    /**
     * Autocomplete names given the starting 3 or more characters of a username
     * @param term The beginning of a username >= 3 characters
     * @param friends If true, returns followed players matching {@code term} if any, else returns other players.
     */
    Many<UserStatus> autocompleteUsers(String term, boolean friends);

}
