package chariot.api;

import chariot.model.Team;
import chariot.model.User;
import chariot.model.Tournament;
import chariot.model.Result;
import chariot.model.PageTeam;
import chariot.model.Swiss;

public interface Teams {

    Result<Team>        byTeamId(String teamId);
    Result<Team>        byUserId(String userId);
    Result<User>        usersByTeamId(String teamId);
    Result<PageTeam>    byPage(int page);
    Result<PageTeam>    searchPage(String text, int page);

    Result<Tournament>  arenaByTeamId(String teamId);
    Result<Tournament>  arenaByTeamId(String teamId, int max);

    Result<Swiss>       swissByTeamId(String teamId);
    Result<Swiss>       swissByTeamId(String teamId, int max);

    default Result<PageTeam> searchPage(String text) { return searchPage(text, 1); }

    // Meta
    default Integer numberOfTeams() {
        var page = byPage(1);
        return page.isPresent() ?
            page.get().nbResults() :
            0;
    }

}
