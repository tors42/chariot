package chariot.api;

import chariot.api.Users.UserParams;
import chariot.model.*;

import java.util.*;
import java.util.function.Consumer;

public interface UsersAuth extends UsersBase {

    /**
     * Get public user data
     *
     * @param userId
     */
    One<UserAuth> byId(String userId, Consumer<UserParams> params);


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

    /**
     * Add a private note available only to you about this account.
     * @param userId the user to write a note about
     * @param text the note content
     */
    One<Void> writeNoteAboutUser(String userId, String text);

    /**
     * Gets the private notes you've written about an account.
     *
     * @param userId the user to read your notes about
     * @return any private notes you've written about the user
     */
    Many<Note> readNotesAboutUser(String userId);

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

    /**
     * Get public user data
     *
     * @param userId
     */
    default One<UserAuth> byId(String userId) {
        return byId(userId, __ -> {});
    }
}
