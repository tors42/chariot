package chariot.api;

import module java.base;
import module chariot;

public interface UsersApiAuth extends UsersApi {

    /**
     * Get public user data
     *
     * @param userId
     */
    default One<UserAuth> byId(String userId) {
        return byId(userId, _ -> {});
    }


    /**
     * Get public user data
     *
     * @param userId
     */
    One<UserAuth> byId(String userId, Consumer<UserParams> params);

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    Many<UserAuth> byIds(String ... userIds);

    /// Get public user data
    ///
    /// Note,  
    /// use only up to 300 ids at a time for full error handling.  
    /// If more than 300 ids are provided, additional requests will
    /// be made as needed and, for any successful response, the results
    /// are concatenated into the initial response stream.
    /// @param userIds A list of up to 300 user ids
    Many<UserAuth> byIds(Collection<String> userIds);

    Ack sendMessageToUser(String userId, String text);

    /**
     * Add a private note available only to you about this account.
     * @param userId the user to write a note about
     * @param text the note content
     */
    Ack writeNoteAboutUser(String userId, String text);

    /**
     * Gets the private notes you've written about an account.
     *
     * @param userId the user to read your notes about
     * @return any private notes you've written about the user
     */
    Many<Note> readNotesAboutUser(String userId);

    Ack followUser(String userId);

    Ack unfollowUser(String userId);

    Ack blockUser(String userId);

    Ack unblockUser(String userId);

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

}
