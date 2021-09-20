package chariot.internal;

import static chariot.internal.Util.MediaType.*;

import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.Client.Scope;
import chariot.internal.Config.ServerType;
import chariot.model.*;

public record Endpoint<T> (
        String endpoint,
        Function<String, T> mapper,
        String accept,
        String content,
        Scope scope,
        ServerType target) {

    public static Endpoint<User> accountProfile =
        Endpoint.of("/api/account", User.class).scope(Scope.any).build();

    public static Endpoint<AccountPreferences> accountPreferences =
        Endpoint.of("/api/account/preferences", AccountPreferences.class).scope(Scope.preference_read).build();

    public static Endpoint<AccountEmail> accountEmail =
        Endpoint.of("/api/account/email", AccountEmail.class).scope(Scope.email_read).build();

    public static Endpoint<AccountKid> accountKid =
        Endpoint.of("/api/account/kid", AccountKid.class).scope(Scope.preference_read).build();

    public static Endpoint<Ack> accountKidStatus =
        Endpoint.of("/api/account/kid", Ack.class).scope(Scope.preference_write).build();

    public static Endpoint<NowPlaying> accountNowPlaying =
        Endpoint.of("/api/account/playing", NowPlaying.class).scope(Scope.any).build();

    public static Endpoint<Ack> accountOAuthToken =
        Endpoint.of("/account/oauth/token", Ack.class).build();

    public static Endpoint<TokenResult> apiToken =
        Endpoint.of("/api/token", TokenResult.class).content(wwwform).build();

    public static Endpoint<Ack> apiTokenRevoke =
        Endpoint.of("/api/token", Ack.class).scope(Scope.any).build();

    public static Endpoint<ChallengeTokens> apiAdminChallengeTokens =
        Endpoint.of("/api/token/admin-challenge", ChallengeTokens.class).content(wwwform).scope(Scope.web_mod).build();

    public static Endpoint<Ack> sendMessage =
        Endpoint.of("/inbox/%s", Ack.class).content(wwwform).scope(Scope.msg_write).build();

    public static Endpoint<Crosstable> crosstableByUserIds =
        Endpoint.of("/api/crosstable/%s/%s", Crosstable.class).build();

    public static Endpoint<User> userById =
        Endpoint.of("/api/user/%s", User.class).build();

    public static Endpoint<RatingHistory[]> ratingHistoryById =
     Endpoint.ofArr("/api/user/%s/rating-history", RatingHistory.class).build();

    public static Endpoint<PerfStat> perfStatByIdAndType =
        Endpoint.of("/api/user/%s/perf/%s", PerfStat.class).build();

    public static Endpoint<Activity[]> activityById =
     Endpoint.ofArr("/api/user/%s/activity", Activity.class).build();

    public static Endpoint<Game> gameCurrentByUserId =
        Endpoint.of("/api/user/%s/current-game", Game.class).build();

    public static Endpoint<User> usersFollowingById =
        Endpoint.of("/api/user/%s/following", User.class).accept(jsonstream).build();

    public static Endpoint<Ack> followUser =
        Endpoint.of("/api/rel/follow/%s", Ack.class).scope(Scope.follow_write).build();

    public static Endpoint<Ack> unfollowUser =
        Endpoint.of("/api/rel/unfollow/%s", Ack.class).scope(Scope.follow_write).build();

    public static Endpoint<Tournament> tournamentArenaCreatedByUser =
        Endpoint.of("/api/user/%s/tournament/created", Tournament.class).accept(jsonstream).build();

    public static Endpoint<UserStatus[]> userStatusByIds =
     Endpoint.ofArr("/api/users/status", UserStatus.class).build();

    public static Endpoint<User[]> usersByIds =
     Endpoint.ofArr("/api/users", User.class).content(plain).build();

    public static Endpoint<UserStatus[]> liveStreamers =
     Endpoint.ofArr("/streamer/live", UserStatus.class).build();

    public static Endpoint<Team> teamById =
        Endpoint.of("/api/team/%s", Team.class).build();

    public static Endpoint<Team[]> teamsByUserId =
     Endpoint.ofArr("/api/team/of/%s", Team.class).build();

    public static Endpoint<User> teamUsersById =
        Endpoint.of("/api/team/%s/users", User.class).accept(jsonstream).scope(Scope.team_read).build();

    public static Endpoint<PageTeam> popularTeamsByPage =
        Endpoint.of("/api/team/all", PageTeam.class).build();

    public static Endpoint<PageTeam> teamsSearch =
        Endpoint.of("/api/team/search", PageTeam.class).build();

    public static Endpoint<Tournament> teamArenaById =
        Endpoint.of("/api/team/%s/arena", Tournament.class).build();

    public static Endpoint<Swiss> teamSwissById =
        Endpoint.of("/api/team/%s/swiss", Swiss.class).build();

    public static Endpoint<Ack> teamJoin =
        Endpoint.of("/team/%s/join", Ack.class).content(wwwform).scope(Scope.team_write).build();

    public static Endpoint<Ack> teamQuit =
        Endpoint.of("/team/%s/quit", Ack.class).scope(Scope.team_write).build();

    public static Endpoint<Ack> teamKick =
        Endpoint.of("/team/%s/kick/%s", Ack.class).scope(Scope.team_write).build();

    public static Endpoint<Ack> teamMessage =
        Endpoint.of("/team/%s/pm-all", Ack.class).content(wwwform).scope(Scope.team_write).build();

    public static Endpoint<GameImport> gameImport =
        Endpoint.of("/api/import", GameImport.class).content(wwwform).build();

    public static Endpoint<StreamGame> streamGamesByUsers =
        Endpoint.of("/api/stream/games-by-users", StreamGame.class).accept(jsonstream).content(plain).build();

    public static Endpoint<StreamMove> streamMoves=
        Endpoint.of("/api/stream/game/%s", StreamMove.class).accept(jsonstream).build();

    public static Endpoint<StreamEvent> streamEvents =
        Endpoint.of("/api/stream/event", StreamEvent.class).accept(jsonstream).scope(Scope.challenge_read).build();

    public static Endpoint<Arena> createArenaTournament =
        Endpoint.of("/api/tournament", Arena.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Ack> joinArenaTournament =
        Endpoint.of("/api/tournament/%s/join", Ack.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Arena> updateArenaTournament =
        Endpoint.of("/api/tournament/%s", Arena.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Arena> updateTeamBattleTournament =
        Endpoint.of("/api/tournament/team-battle/%s", Arena.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Ack> terminateArenaTournament =
        Endpoint.of("/api/tournament/%s/terminate", Ack.class).scope(Scope.tournament_write).build();

    public static Endpoint<Arena> tournamentArenaById =
        Endpoint.of("/api/tournament/%s", Arena.class).build();

    public static Endpoint<ArenaResult> tournamentArenaResultsById =
        Endpoint.of("/api/tournament/%s/results", ArenaResult.class).accept(jsonstream).build();

    public static Endpoint<TeamBattleResults> tournamentTeamBattleResultsById =
        Endpoint.of("/api/tournament/%s/teams", TeamBattleResults.class).build();

    public static Endpoint<TournamentStatus> tournamentArenas =
        Endpoint.of("/api/tournament", TournamentStatus.class).build();

    public static Endpoint<Game> gamesByArenaId =
        Endpoint.of("/api/tournament/%s/games", Game.class).accept(jsonstream).build();

    public static Endpoint<Game> gamesByUserId =
        Endpoint.of("/api/games/user/%s", Game.class).accept(jsonstream).build();

    public static Endpoint<Game> gameById =
        Endpoint.of("/game/export/%s", Game.class).build();

    public static Endpoint<Game> gamesByIds =
        Endpoint.of("/games/export/_ids", Game.class).accept(jsonstream).content(plain).build();

    public static Endpoint<ExploreResult> exploreMaster =
        Endpoint.of("/master", ExploreResult.class).target(ServerType.explorer).build();

    public static Endpoint<ExploreResult> exploreLichess =
        Endpoint.of("/lichess", ExploreResult.class).target(ServerType.explorer).build();

    public static Endpoint<String> exploreMasterOTB =
        Endpoint.of("/master/pgn/%s", Function.identity()).accept(chesspgn).target(ServerType.explorer).build();

    public static Endpoint<ExplorerStats> explorerStats =
        Endpoint.of("/stats", ExplorerStats.class).target(ServerType.explorer).build();

    public static Endpoint<TablebaseResult> tablebaseLookup =
        Endpoint.of("/standard", TablebaseResult.class).target(ServerType.tablebase).build();

    public static Endpoint<TablebaseResult> tablebaseAtomicLookup =
        Endpoint.of("/atomic", TablebaseResult.class).target(ServerType.tablebase).build();

    public static Endpoint<TablebaseResult> tablebaseAntichessLookup =
        Endpoint.of("/antichess", TablebaseResult.class).target(ServerType.tablebase).build();

    public static Endpoint<TVChannels> gameTVChannels =
        Endpoint.of("/api/tv/channels", TVChannels.class).build();

    public static Endpoint<TVFeed> gameTVFeed =
        Endpoint.of("/api/tv/feed", TVFeed.class).accept(jsonstream).build();

    public static Endpoint<Game> gamesTVChannel =
        Endpoint.of("/api/tv/%s", Game.class).accept(jsonstream).build();

    public static Endpoint<UserTopAll> usersTopAll =
        Endpoint.of("/player", UserTopAll.class).accept(lichessjson).build();

    public static Endpoint<Leaderboard> usersLeaderboard =
        Endpoint.of("/player/top/%s/%s", Leaderboard.class).accept(lichessjson).build();

    public static Endpoint<Puzzle> dailyPuzzle =
        Endpoint.of("/api/puzzle/daily", Puzzle.class).build();

    public static Endpoint<PuzzleActivity> puzzleActivity =
        Endpoint.of("/api/puzzle/activity", PuzzleActivity.class).accept(jsonstream).scope(Scope.puzzle_read).build();

    public static Endpoint<PuzzleDashboard> puzzleDashboard =
        Endpoint.of("/api/puzzle/dashboard/%s", PuzzleDashboard.class).scope(Scope.puzzle_read).build();

    public static Endpoint<StormDashboard> stormDashboard =
        Endpoint.of("/api/storm/dashboard/%s", StormDashboard.class).build();

    public static Endpoint<Simuls> simuls =
        Endpoint.of("/api/simul", Simuls.class).build();

    public static Endpoint<Analysis> cloudEval =
        Endpoint.of("/api/cloud-eval", Analysis.class).build();

    public static Endpoint<String> exportChapter =
        Endpoint.of("/study/%s/%s.pgn", Function.identity()).accept(chesspgn).build();

    public static Endpoint<String> exportChapters =
        Endpoint.of("/api/study/%s.pgn", Function.identity()).accept(chesspgn).build();

    public static Endpoint<String> exportStudies =
        Endpoint.of("/study/by/%s/export.pgn", Function.identity()).accept(chesspgn).scope(Scope.any).build();

    public static Endpoint<BulkPairings> bulkPairingGet =
        Endpoint.of("/api/bulk-pairing", BulkPairings.class).scope(Scope.challenge_bulk).build();

    public static Endpoint<BulkPairing> bulkPairingCreate =
        Endpoint.of("/api/bulk-pairing", BulkPairing.class).content(wwwform).scope(Scope.challenge_bulk).build();

    public static Endpoint<Ack> bulkPairingStart =
        Endpoint.of("/api/bulk-pairing/%s/start-clocks", Ack.class).scope(Scope.challenge_bulk).build();

    public static Endpoint<Ack> bulkPairingCancel =
        Endpoint.of("/api/bulk-pairing/%s", Ack.class).scope(Scope.challenge_bulk).build();

    public static Endpoint<PendingChallenges> challenges =
        Endpoint.of("/api/challenge", PendingChallenges.class).scope(Scope.challenge_read).build();

    public static Endpoint<ChallengeResult> challengeCreate =
        Endpoint.of("/api/challenge/%s", ChallengeResult.class).content(wwwform).scope(Scope.challenge_write).build();

    public static Endpoint<ChallengeResult> challengeAI =
        Endpoint.of("/api/challenge/ai", ChallengeResult.class).content(wwwform).scope(Scope.challenge_write).build();

    public static Endpoint<ChallengeResult> challengeOpenEnded =
        Endpoint.of("/api/challenge/open", ChallengeResult.class).content(wwwform).scope(Scope.challenge_write).build();

    public static Endpoint<Ack> challengeCancel =
        Endpoint.of("/api/challenge/%s/cancel", Ack.class).scope(Scope.challenge_write).build();

    public static Endpoint<Ack> challengeAccept =
        Endpoint.of("/api/challenge/%s/accept", Ack.class).scope(Scope.challenge_write).build();

    public static Endpoint<Ack> challengeDecline =
        Endpoint.of("/api/challenge/%s/decline", Ack.class).content(wwwform).scope(Scope.challenge_write).build();

    public static Endpoint<Ack> startClocksOfGame =
        Endpoint.of("/api/challenge/%s/start-clocks", Ack.class).scope(Scope.challenge_write).build();

    public static Endpoint<Ack> addTimeToGame =
        Endpoint.of("/api/round/%s/add-time/%s", Ack.class).scope(Scope.challenge_write).build();

    public static Endpoint<Swiss> createSwiss =
        Endpoint.of("/api/swiss/new/%s", Swiss.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Ack> joinSwissTournament =
        Endpoint.of("/api/swiss/%s/join", Ack.class).content(wwwform).scope(Scope.tournament_write).build();

    public static Endpoint<Ack> terminateSwiss =
        Endpoint.of("/api/swiss/%s/terminate", Ack.class).scope(Scope.tournament_write).build();

    public static Endpoint<SwissResult> swissResults =
        Endpoint.of("/api/swiss/%s/results", SwissResult.class).accept(jsonstream).build();

    public static Endpoint<String> swissTRF =
        Endpoint.of("/swiss/%s.trf", Function.identity()).accept(plain).build();

    public static Endpoint<Game> gamesBySwissId =
        Endpoint.of("/api/swiss/%s/games", Game.class).accept(jsonstream).build();

    public static Endpoint<Broadcast> officialBroadcasts =
        Endpoint.of("/api/broadcast", Broadcast.class).accept(jsonstream).build();

    public static Endpoint<Broadcast> createBroadcast =
        Endpoint.of("/broadcast/new", Broadcast.class).content(wwwform).scope(Scope.study_write).build();

    public static Endpoint<Broadcast.Round> createRound =
        Endpoint.of("/broadcast/%s/new", Broadcast.Round.class).content(wwwform).scope(Scope.study_write).build();

    public static Endpoint<Broadcast> broadcastById =
        Endpoint.of("/broadcast/-/%s", Broadcast.class).scope(Scope.study_read).build();

    public static Endpoint<Broadcast.Round> roundById =
        Endpoint.of("/broadcast/-/-/%s", Broadcast.Round.class).scope(Scope.study_read).build();

    public static Endpoint<Ack> updateBroadcast =
        Endpoint.of("/broadcast/%s/edit", Ack.class).content(wwwform).scope(Scope.study_write).build();

    public static Endpoint<Ack> updateRound =
        Endpoint.of("/broadcast/round/%s/edit", Ack.class).content(wwwform).scope(Scope.study_write).build();

    public static Endpoint<Ack> pushPGNbyRoundId =
        Endpoint.of("/broadcast/round/%s/push", Ack.class).content(plain).scope(Scope.study_write).build();

    public static Endpoint<Ack> boardSeek =
        Endpoint.of("/api/board/seek", Ack.class).content(wwwform).accept(plain).scope(Scope.board_play).build();

    public static Endpoint<StreamGameEvent> streamBoardGameEvents =
        Endpoint.of("/api/board/game/stream/%s", StreamGameEvent.class).accept(jsonstream).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardMove =
        Endpoint.of("/api/board/game/%s/move/%s", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardChat =
        Endpoint.of("/api/board/game/%s/chat", Ack.class).content(wwwform).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardAbort =
        Endpoint.of("/api/board/game/%s/abort", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardResign =
        Endpoint.of("/api/board/game/%s/resign", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardDraw =
        Endpoint.of("/api/board/game/%s/draw/%s", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardTakeback =
        Endpoint.of("/api/board/game/%s/takeback/%s", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<Ack> boardClaimVictory =
        Endpoint.of("/api/board/game/%s/claim-victory", Ack.class).scope(Scope.board_play).build();

    public static Endpoint<ChatMessage[]> boardFetchChat =
     Endpoint.ofArr("/api/board/game/%s/chat", ChatMessage.class).accept(jsonstream).scope(Scope.board_play).build();

    public static Endpoint<User> botsOnline =
        Endpoint.of("/api/bot/online", User.class).accept(jsonstream).build();

    public static Endpoint<Ack> botAccountUpgrade =
        Endpoint.of("/api/bot/account/upgrade", Ack.class).scope(Scope.bot_play).build();

    public static Endpoint<StreamGameEvent> streamBotGameEvents =
        Endpoint.of("/api/bot/game/stream/%s", StreamGameEvent.class).accept(jsonstream).scope(Scope.bot_play).build();

    public static Endpoint<Ack> botMove =
        Endpoint.of("/api/bot/game/%s/move/%s", Ack.class).scope(Scope.bot_play).build();

    public static Endpoint<Ack> botChat =
        Endpoint.of("/api/bot/game/%s/chat", Ack.class).content(wwwform).scope(Scope.bot_play).build();

    public static Endpoint<Ack> botAbort =
        Endpoint.of("/api/bot/game/%s/abort", Ack.class).scope(Scope.bot_play).build();

    public static Endpoint<Ack> botResign =
        Endpoint.of("/api/bot/game/%s/resign", Ack.class).scope(Scope.bot_play).build();


    public static class EndpointBuilder<T> {
        private final String endpoint;
        private final Function<String, T> mapper;

        private ServerType target = ServerType.api;
        private String accept = json;
        private String content;
        private Scope scope;

        private EndpointBuilder(String endpoint, Function<String, T> mapper) {
            Objects.nonNull(endpoint);
            Objects.nonNull(mapper);
            this.endpoint = endpoint;
            this.mapper = mapper;
         }

        public EndpointBuilder<T> accept(String accept) {
            Objects.nonNull(accept);
            this.accept = accept;
            return this;
        }

        public EndpointBuilder<T> content(String content) {
            Objects.nonNull(content);
            this.content = content;
            return this;
        }

        public EndpointBuilder<T> scope(Scope scope) {
            Objects.nonNull(scope);
            this.scope = scope;
            return this;
        }

        public EndpointBuilder<T> target(ServerType target) {
            Objects.nonNull(target);
            this.target = target;
            return this;
        }

        public Endpoint<T> build() {
            return new Endpoint<>(this);
        }
    }

    public Endpoint(EndpointBuilder<T> builder) {
        this(builder.endpoint, builder.mapper, builder.accept, builder.content, builder.scope, builder.target);
    }

    public static <T extends Model> EndpointBuilder<T> of(
            String endpoint,
            Class<T> clazz) {
        return new EndpointBuilder<>(endpoint, ModelMapper.mapper(clazz));
    }

    public static <T extends Model> EndpointBuilder<T[]> ofArr(
            String endpoint,
            Class<T> clazz) {
        return new EndpointBuilder<>(endpoint, ModelMapper.mapperArr(clazz));
    }

    public static <T> EndpointBuilder<T> of(
            String endpoint,
            Function<String, T> mapper) {
        return new EndpointBuilder<>(endpoint, mapper);
    }

    public Request.Builder<T> newRequest() {
        record KeyValue(String key, String value) {}
        var map = Stream.of(
                new KeyValue("accept", accept),
                new KeyValue("content-type", content)
                )
            .filter(e -> e.value() != null)
            .collect(Collectors.toMap(KeyValue::key, KeyValue::value));

        return new Request.Builder<>(endpoint, mapper)
            .headers(map)
            .scope(scope)
            .serverType(target);
    }
}
