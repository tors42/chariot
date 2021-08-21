package chariot.api;

import chariot.model.Team;
import chariot.model.User;
import chariot.model.Tournament;
import chariot.model.PageTeam;
import chariot.model.Result;
import chariot.model.Swiss;

public interface Teams {

    Result<Team>        byTeamId(String teamId);
    Result<Team>        byUserId(String userId);
    Result<User>        usersByTeamId(String teamId);

    Result<Team>        search();
    Result<Team>        search(String text);

    Result<Team>        popularTeams();

    Result<Tournament>  arenaByTeamId(String teamId);
    Result<Tournament>  arenaByTeamId(String teamId, int max);

    Result<Swiss>       swissByTeamId(String teamId);
    Result<Swiss>       swissByTeamId(String teamId, int max);

    int numberOfTeams();

    Result<PageTeam>    searchByPage();
    Result<PageTeam>    searchByPage(int page);
    Result<PageTeam>    searchByPage(String text);
    Result<PageTeam>    searchByPage(int page, String text);

    Result<PageTeam>    popularTeamsByPage();
    Result<PageTeam>    popularTeamsByPage(int page);

}
