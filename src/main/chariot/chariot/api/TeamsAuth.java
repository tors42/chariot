package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface TeamsAuth extends Teams<TeamMemberAuth> {

    /**
     * Request to join team {@code teamId}
     */
    One<Void> joinTeam(String teamId, Consumer<JoinParams> params);
    /**
     * Request to join team {@code teamId}
     */
    default One<Void> joinTeam(String teamId) { return joinTeam(teamId, __ -> {}); }

    One<Void> leaveTeam(String teamId);

    One<Void> kickFromTeam(String teamId, String userId);

    One<Void> messageTeam(String teamId, String message);

    /**
     * Get pending join requests of your team
     */
    Many<TeamRequest> requests(String teamId);

    /**
     * Get the declined join requests of your team
     */
    Many<TeamRequest> requestsDeclined(String teamId);

    /**
     * Accept a join request to a team from a user
     * @param teamId
     * @param userId
     */
    One<Void> requestAccept(String teamId, String userId);

    /**
     * Decline a join request to a team from a user
     * @param teamId
     * @param userId
     */
    One<Void> requestDecline(String teamId, String userId);


    interface JoinParams {
        /**
         * Optional request message, if the team requires one.
         */
        JoinParams message(String message);
        /**
         * Optional entry code, if the team requires one.
         */
        JoinParams entryCode(String entryCode);
    }
}
