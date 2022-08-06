package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface Teams {

    One<Team>         byTeamId(String teamId);
    Many<Team>        byUserId(String userId);
    Many<User>        usersByTeamId(String teamId);

    Many<Team>        search();
    Many<Team>        search(String text);

    Many<Team>        popularTeams();

    Many<Tournament>  arenaByTeamId(String teamId);
    Many<Tournament>  arenaByTeamId(String teamId, int max);

    Many<Swiss>       swissByTeamId(String teamId);
    Many<Swiss>       swissByTeamId(String teamId, int max);

    default int numberOfTeams() {
        return searchByPage() instanceof Entry<PageTeam> page ?
            page.entry().nbResults() : 0;
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
}
