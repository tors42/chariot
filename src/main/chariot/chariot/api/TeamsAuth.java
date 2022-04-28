package chariot.api;

import java.util.function.Consumer;

import chariot.model.Ack;
import chariot.model.Result;
import chariot.model.TeamRequest;

public interface TeamsAuth extends Teams {

    /**
     * Request to join team {@code teamId}
     */
    Result<Ack> joinTeam(String teamId, Consumer<JoinParams> params);
    /**
     * Request to join team {@code teamId}
     */
    default Result<Ack> joinTeam(String teamId) { return joinTeam(teamId, __ -> {}); }

    @Deprecated default Result<Ack> joinTeam(String teamId, String message) { return joinTeam(teamId, p -> p.message(message)); }
    @Deprecated default Result<Ack> joinTeamPW(String teamId, String password) { return joinTeam(teamId, p -> p.password(password)); }
    @Deprecated default Result<Ack> joinTeamPW(String teamId, String password, String message) { return joinTeam(teamId, p -> p.password(password).message(message)); }

    Result<Ack> leaveTeam(String teamId);

    Result<Ack> kickFromTeam(String teamId, String userId);

    Result<Ack> messageTeam(String teamId, String message);

    /**
     * Get pending join requests of your team
     */
    Result<TeamRequest> requests(String teamId);

    /**
     * Accept a join request to a team from a user
     * @param teamId
     * @param userId
     */
    Result<Ack> requestAccept(String teamId, String userId);

    /**
     * Decline a join request to a team from a user
     * @param teamId
     * @param userId
     */
    Result<Ack> requestDecline(String teamId, String userId);


    interface JoinParams {

        /**
         * Optional request message, if the team requires one.
         */
        JoinParams message(String message);
        /**
         * Optional password, if the team requires one.
         */
        JoinParams password(String password);

    }
}
