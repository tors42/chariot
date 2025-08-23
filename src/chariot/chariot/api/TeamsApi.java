package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface TeamsApi {

    One<Team>         byTeamId(String teamId);
    Many<Team>        byUserId(String userId);

    ///  Members are sorted by reverse chronological order of joining the team (most recent first).  
    ///  OAuth is only required if the list of members is private.  
    ///  Up to 5,000 users.
    Many<TeamMember>  usersByTeamId(String teamId);

    /// Members are sorted by reverse chronological order of joining the team (most recent first).  
    /// OAuth is only required if the list of members is private.  
    /// Includes performance ratings.  
    /// Up to 1,000 users.  
    /// If looking for more users and more speed, see the "lighter" method [#usersByTeamId(String)]
    Many<TeamMemberFull>  usersByTeamIdFull(String teamId);


    Many<Team>        search();
    Many<Team>        search(String text);

    Many<Team>        popularTeams();

    Many<ArenaLight>  arenaByTeamId(String teamId, Consumer<TournamentParams> params);
    default Many<ArenaLight>  arenaByTeamId(String teamId) { return arenaByTeamId(teamId, __ -> {}); }
    @Deprecated
    default Many<ArenaLight>  arenaByTeamId(String teamId, int max) { return arenaByTeamId(teamId, p -> p.max(max)); }

    Many<Swiss>         swissByTeamId(String teamId, Consumer<TournamentParams> params);
    default Many<Swiss> swissByTeamId(String teamId) { return swissByTeamId(teamId, __ -> {}); }
    @Deprecated
    default Many<Swiss> swissByTeamId(String teamId, int max) { return swissByTeamId(teamId, p -> p.max(max)); }

    default int numberOfTeams() {
        return searchByPage() instanceof Some<PageTeam> page ?
            page.value().nbResults() : 0;
    }

    One<PageTeam>     searchByPage(Consumer<PageParams> params);

    default One<PageTeam> searchByPage() { return searchByPage(__ -> {}); }
    default One<PageTeam> searchByPage(int page) { return searchByPage(p -> p.page(page)); }
    default One<PageTeam> searchByPage(String text) { return searchByPage(p -> p.text(text)); }
    default One<PageTeam> searchByPage(int page, String text) { return searchByPage(p -> p.page(page).text(text)); }

    One<PageTeam>     popularTeamsByPage();
    One<PageTeam>     popularTeamsByPage(int page);

    interface PageParams {
        PageParams page(int page);
        PageParams text(String text);
    }

    interface TournamentParams {
        TournamentParams max(int max);
        TournamentParams status(TourInfo.Status status);
        default TournamentParams statusStarted() { return status(TourInfo.Status.started); }
        default TournamentParams statusCreated() { return status(TourInfo.Status.created); }
        default TournamentParams statusFinished() { return status(TourInfo.Status.finished); }
        TournamentParams createdBy(String name);
        TournamentParams name(String name);
    }
}
