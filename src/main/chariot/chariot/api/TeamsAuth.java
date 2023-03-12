package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface TeamsAuth extends Teams {

    /**
     * Request to join team {@code teamId}
     */
    One<Ack> joinTeam(String teamId, Consumer<JoinParams> params);
    /**
     * Request to join team {@code teamId}
     */
    default One<Ack> joinTeam(String teamId) { return joinTeam(teamId, __ -> {}); }

    One<Ack> leaveTeam(String teamId);

    One<Ack> kickFromTeam(String teamId, String userId);

    One<Ack> messageTeam(String teamId, String message);

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
    One<Ack> requestAccept(String teamId, String userId);

    /**
     * Decline a join request to a team from a user
     * @param teamId
     * @param userId
     */
    One<Ack> requestDecline(String teamId, String userId);


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
