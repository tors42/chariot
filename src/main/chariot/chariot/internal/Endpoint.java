package chariot.internal;

import static chariot.internal.Util.MediaType.*;

import java.io.InputStream;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.Client.Scope;
import chariot.internal.Config.ServerType;
import chariot.internal.RequestParameters.*;
import chariot.internal.Util.Method;
import chariot.internal.modeladapter.TimelineAdapter.Timeline;
import chariot.model.*;

import static chariot.internal.ModelMapper.mapper;
import static chariot.internal.ModelMapper.mapperArr;

public sealed interface Endpoint<T> {

    public record EPOne<T>(EP ep, Function<Stream<String>, One<T>> mapper) implements Endpoint<T> {

        public ReqOne<T> newRequest(Consumer<Params> params) {
            return RequestParameters.one(toBuilder(params), result -> switch(result) {
                case null                    -> mapper.apply(Stream.of());
                case RequestResult.Success s -> mapper.apply(s.stream());
                case RequestResult.Failure f -> One.fail(f.code(), Err.from(f.body()));
            });
         }
    }

    public record EPMany<T>(EP ep, Function<Stream<String>, Many<T>> mapper) implements Endpoint<T> {
        public ReqMany<T> newRequest(Consumer<Params> params) {
            return RequestParameters.many(toBuilder(params), result -> switch(result) {
                case null                    -> mapper.apply(Stream.of());
                case RequestResult.Success s -> mapper.apply(s.stream());
                case RequestResult.Failure f -> Many.fail(f.code(), Err.from(f.body()));
            });
        }
    }

    public record EP(
            String endpoint,
            String accept,
            String contentType,
            Method method,
            Scope scope,
            ServerType target) {}

    EP ep();
    default String endpoint()    { return ep().endpoint(); }
    default String accept()      { return ep().accept(); }
    default String contentType() { return ep().contentType(); }
    default Method method()      { return ep().method(); }
    default Scope scope()        { return ep().scope(); }
    default ServerType target()  { return ep().target(); }

    public static EPOne<UserAuth> accountProfile =
        Endpoint.of(mapper(UserData.class).andThen(UserData::toUserAuth)).endpoint("/api/account").scope(Scope.any).toOne();

    public static EPOne<AccountPreferences> accountPreferences =
        Endpoint.of(AccountPreferences.class).endpoint("/api/account/preferences").scope(Scope.preference_read).toOne();

    public static EPOne<String> accountEmail =
        Endpoint.of(mapper(AccountEmail.class).andThen(AccountEmail::email)).endpoint("/api/account/email").scope(Scope.email_read).toOne();

    public static EPOne<Boolean> accountKid =
        Endpoint.of(mapper(AccountKid.class).andThen(AccountKid::kid)).endpoint("/api/account/kid").scope(Scope.preference_read).toOne();

    public static EPOne<Void> accountKidStatus =
        Endpoint.of(Void.class).endpoint("/api/account/kid").post().scope(Scope.preference_write).toOne();

    public static EPMany<GameInfo> accountNowPlaying =
        Endpoint.of(GameInfo.class).endpoint("/api/account/playing")
        .streamMapper(stream -> stream.map(mapper(PlayingWrapper.class)).filter(Objects::nonNull).flatMap(pw -> pw.nowPlaying().stream()))
        .scope(Scope.any).toMany();

    public static EPMany<TimelineEntry> timeline =
        Endpoint.of(TimelineEntry.class).streamMapper(stream -> stream.map(mapper(Timeline.class)).flatMap(Timeline::toTimelineEntries)).endpoint("/api/timeline").scope(Scope.any).toMany();

    public static EPOne<Void> accountOAuthToken =
        Endpoint.of(Void.class).endpoint("/account/oauth/token").toOne();

    public static EPOne<TokenResult> apiToken =
        Endpoint.of(TokenResult.class).endpoint("/api/token").post(wwwform).toOne();

    public static EPOne<Void> apiTokenRevoke =
        Endpoint.of(Void.class).endpoint("/api/token").delete().scope(Scope.any).toOne();

    public static EPOne<TokenBulkResult> apiTokenBulkTest =
        Endpoint.of(TokenBulkResult.class).endpoint("/api/token/test").post(plain).toOne();

    public static EPOne<ChallengeTokens> apiAdminChallengeTokens =
        Endpoint.of(ChallengeTokens.class).endpoint("/api/token/admin-challenge").post(wwwform).scope(Scope.web_mod).toOne();

    public static EPOne<Void> sendMessage =
        Endpoint.of(Void.class).endpoint("/inbox/%s").post(wwwform).scope(Scope.msg_write).toOne();

    public static EPOne<Crosstable> crosstableByUserIds =
        Endpoint.of(Crosstable.class).endpoint("/api/crosstable/%s/%s").toOne();

    public static EPOne<UserData> userById =
        Endpoint.of(UserData.class).endpoint("/api/user/%s").toOne();

    public static EPMany<RatingHistory> ratingHistoryById =
        Endpoint.ofArr(RatingHistory.class).endpoint("/api/user/%s/rating-history").toMany();

    public static EPOne<PerformanceStatistics> perfStatByIdAndType =
        Endpoint.of(PerformanceStatistics.class).endpoint("/api/user/%s/perf/%s").toOne();

    public static EPMany<Activity> activityById =
        Endpoint.ofArr(Activity.class).endpoint("/api/user/%s/activity").toMany();

    public static EPOne<Game> gameCurrentByUserId =
        Endpoint.of(Game.class).endpoint("/api/user/%s/current-game").toOne();

    public static EPOne<Pgn> gameCurrentByUserIdPgn =
        Endpoint.of(Pgn.class).endpoint("/api/user/%s/current-game")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toOne();

    public static EPMany<UserAuth> relFollowing =
        Endpoint.of(mapper(UserData.class).andThen(UserData::toUserAuth))
        .endpoint("/api/rel/following").scope(Scope.follow_read).accept(jsonstream).toMany();

    public static EPOne<Void> followUser =
        Endpoint.of(Void.class).endpoint("/api/rel/follow/%s").post().scope(Scope.follow_write).toOne();

    public static EPOne<Void> unfollowUser =
        Endpoint.of(Void.class).endpoint("/api/rel/unfollow/%s").post().scope(Scope.follow_write).toOne();

    public static EPOne<Void> blockUser =
        Endpoint.of(Void.class).endpoint("/api/rel/block/%s").post().scope(Scope.follow_write).toOne();

    public static EPOne<Void> unblockUser =
        Endpoint.of(Void.class).endpoint("/api/rel/unblock/%s").post().scope(Scope.follow_write).toOne();

    public static EPMany<ArenaLight> tournamentArenaCreatedByUser =
        Endpoint.of(ArenaLight.class).endpoint("/api/user/%s/tournament/created").accept(jsonstream).toMany();

    public static EPMany<UserStatus> userStatusByIds =
        Endpoint.ofArr(UserStatus.class).endpoint("/api/users/status").toMany();

    public static EPMany<UserData> usersByIds =
        Endpoint.ofArr(UserData.class).endpoint("/api/users").post(plain).toMany();

    public static EPMany<String> usersNamesAutocomplete =
        Endpoint.ofArr(s -> s.substring(1, s.length()-1).replaceAll("\"", "").split(","))
        .endpoint("/api/player/autocomplete").accept(plain).toMany();

    public static EPMany<UserStatus> usersStatusAutocomplete =
        Endpoint.ofArr(UserStatus.class).endpoint("/api/player/autocomplete")
        .streamMapper(stream -> stream.map(mapper(AutocompleteWrapper.class))
                .filter(Objects::nonNull)
                .flatMap(wrapper -> wrapper.result().stream())
                .map(UserData::toUserStatus))
        .toMany();

    public static EPOne<Void> writeNote =
        Endpoint.of(Void.class).endpoint("/api/user/%s/note").post(wwwform).scope(Scope.any).toOne();

    public static EPMany<Note> readNotes =
        Endpoint.ofArr(Note.class).endpoint("/api/user/%s/note").scope(Scope.any).toMany();

    public static EPMany<LiveStreamer> liveStreamers =
        Endpoint.ofArr(LiveStreamer.class).endpoint("/api/streamer/live").toMany();

    public static EPOne<Team> teamById =
        Endpoint.of(Team.class).endpoint("/api/team/%s").toOne();

    public static EPMany<Team> teamsByUserId =
        Endpoint.ofArr(Team.class).endpoint("/api/team/of/%s").toMany();

    public static EPMany<TeamMember> teamUsersById =
        Endpoint.of(mapper(UserData.class).andThen(UserData::toTeamMember)).endpoint("/api/team/%s/users")
        .accept(jsonstream).scope(Scope.team_read).toMany();

    public static EPMany<TeamMemberFull> teamUsersFullById =
        Endpoint.of(mapper(UserData.class).andThen(UserData::toTeamMemberFull)).endpoint("/api/team/%s/users")
        .accept(jsonstream).scope(Scope.team_read).toMany();

    public static EPOne<PageTeam> popularTeamsByPage =
        Endpoint.of(PageTeam.class).endpoint("/api/team/all").toOne();

    public static EPOne<PageTeam> teamsSearch =
        Endpoint.of(PageTeam.class).endpoint("/api/team/search").toOne();

    public static EPMany<ArenaLight> teamArenaById =
        Endpoint.of(ArenaLight.class).endpoint("/api/team/%s/arena").toMany();

    public static EPMany<Swiss> teamSwissById =
        Endpoint.of(Swiss.class).endpoint("/api/team/%s/swiss").toMany();

    public static EPOne<Void> teamJoin =
        Endpoint.of(Void.class).endpoint("/team/%s/join").post(wwwform).scope(Scope.team_write).toOne();

    public static EPOne<Void> teamQuit =
        Endpoint.of(Void.class).endpoint("/team/%s/quit").post().scope(Scope.team_write).toOne();

    public static EPOne<Void> teamKick =
        Endpoint.of(Void.class).endpoint("/api/team/%s/kick/%s").post().scope(Scope.team_lead).toOne();

    public static EPOne<Void> teamMessage =
        Endpoint.of(Void.class).endpoint("/team/%s/pm-all").post(wwwform).scope(Scope.team_lead).toOne();

    public static EPMany<TeamRequest> teamRequests =
        Endpoint.ofArr(TeamRequest.class).endpoint("/api/team/%s/requests").scope(Scope.team_read).toMany();

    public static EPOne<Void> teamAcceptJoin =
        Endpoint.of(Void.class).endpoint("/api/team/%s/request/%s/accept").post().scope(Scope.team_lead).toOne();

    public static EPOne<Void> teamDeclineJoin =
        Endpoint.of(Void.class).endpoint("/api/team/%s/request/%s/decline").post().scope(Scope.team_lead).toOne();

    public static EPOne<GameImport> gameImport =
        Endpoint.of(GameImport.class).endpoint("/api/import").post(wwwform).toOne();

    public static EPMany<GameMeta> streamGamesByUsers =
        Endpoint.of(GameMeta.class).endpoint("/api/stream/games-by-users").post(plain).accept(jsonstream).toMany();

    public static EPMany<GameMeta> streamGamesByStreamIds =
        Endpoint.of(GameMeta.class).endpoint("/api/stream/games/%s").post(plain).accept(jsonstream).toMany();

    public static EPOne<Void> addGameIdsToStream =
        Endpoint.of(Void.class).endpoint("/api/stream/games/%s/add").post(plain).accept(json).toOne();

    public static EPMany<MoveInfo> streamMoves =
        Endpoint.of(MoveInfo.class).endpoint("/api/stream/game/%s").accept(jsonstream).toMany();

    public static EPMany<Event> streamEvents =
        Endpoint.of(Event.class).endpoint("/api/stream/event").accept(jsonstream).scope(Scope.challenge_read).toMany();

    public static EPOne<Arena> createArenaTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Void> joinArenaTournament =
        Endpoint.of(Void.class).endpoint("/api/tournament/%s/join").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Void> withdrawArenaTournament =
        Endpoint.of(Void.class).endpoint("/api/tournament/%s/withdraw").post().scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> updateArenaTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament/%s").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> updateTeamBattleTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament/team-battle/%s").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Void> terminateArenaTournament =
        Endpoint.of(Void.class).endpoint("/api/tournament/%s/terminate").post().scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> tournamentArenaById =
        Endpoint.of(Arena.class).endpoint("/api/tournament/%s").toOne();

    public static EPMany<ArenaResult> tournamentArenaResultsById =
        Endpoint.of(mapper(ArenaResultWrappedSheet.class).andThen(ArenaResultWrappedSheet::toArenaResult))
        .endpoint("/api/tournament/%s/results").accept(jsonstream).toMany();

    public static EPMany<Arena.TeamStanding> tournamentTeamBattleResultsById =
        Endpoint.of(Arena.TeamStanding.class).endpoint("/api/tournament/%s/teams")
        .streamMapper(stream -> stream.map(mapper(TeamStandingWrapper.class)).filter(Objects::nonNull).flatMap(wrapper -> wrapper.teams().stream()))
        .toMany();

    public static EPOne<TournamentStatus> tournamentArenas =
        Endpoint.of(TournamentStatus.class).endpoint("/api/tournament").toOne();

    public static EPMany<Game> gamesByArenaId =
        Endpoint.of(Game.class).endpoint("/api/tournament/%s/games").accept(jsonstream).toMany();

    public static EPMany<Pgn> gamesByArenaIdPgn =
        Endpoint.of(Pgn.class).endpoint("/api/tournament/%s/games")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Game> gamesByUserId =
        Endpoint.of(Game.class).endpoint("/api/games/user/%s").accept(jsonstream).toMany();

    public static EPMany<Pgn> gamesByUserIdPgn =
        Endpoint.of(Pgn.class).endpoint("/api/games/user/%s")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPOne<Game> gameById =
        Endpoint.of(Game.class).endpoint("/game/export/%s").toOne();

    public static EPOne<Pgn> gameByIdPgn =
        Endpoint.of(Pgn.class).endpoint("/game/export/%s")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toOne();

    public static EPMany<Game> gamesByIds =
        Endpoint.of(Game.class).endpoint("/api/games/export/_ids").post(plain).accept(jsonstream).toMany();

    public static EPMany<Pgn> gamesByIdsPgn =
        Endpoint.of(Pgn.class).endpoint("/api/games/export/_ids").post(plain)
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> gamesImportedPgn =
        Endpoint.of(Pgn.class).endpoint("/api/games/export/imports")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPOne<ExploreResult.OpeningDB> exploreMasters =
        Endpoint.of(ExploreResult.OpeningDB.class).endpoint("/masters").target(ServerType.explorer).toOne();

    public static EPOne<ExploreResult.OpeningDB> exploreLichess =
        Endpoint.of(ExploreResult.OpeningDB.class).endpoint("/lichess").target(ServerType.explorer).toOne();

    public static EPOne<ExploreResult.OpeningPlayer> explorePlayers =
        Endpoint.of(ExploreResult.OpeningPlayer.class).endpoint("/player").target(ServerType.explorer).toOne();

    public static EPOne<Pgn> exploreMasterOTB =
        Endpoint.of(Pgn.class).endpoint("/master/pgn/%s")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).target(ServerType.explorer).toOne();

    public static EPOne<TablebaseResult> tablebaseLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/standard").target(ServerType.tablebase).toOne();

    public static EPOne<TablebaseResult> tablebaseAtomicLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/atomic").target(ServerType.tablebase).toOne();

    public static EPOne<TablebaseResult> tablebaseAntichessLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/antichess").target(ServerType.tablebase).toOne();

    public static EPOne<TVChannels> gameTVChannels =
        Endpoint.of(TVChannels.class).endpoint("/api/tv/channels").toOne();

    public static EPMany<TVFeedEvent> gameTVFeed =
        Endpoint.of(TVFeedEvent.class).endpoint("/api/tv/feed").accept(jsonstream).toMany();

    public static EPMany<TVFeedEvent> gameTVFeedChannel =
        Endpoint.of(TVFeedEvent.class).endpoint("/api/tv/%s/feed").accept(jsonstream).toMany();


    public static EPMany<Game> gamesTVChannel =
        Endpoint.of(Game.class).endpoint("/api/tv/%s").accept(jsonstream).toMany();

    public static EPMany<Pgn> gamesTVChannelPgn =
        Endpoint.of(Pgn.class).endpoint("/api/tv/%s")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();


    public static EPOne<UserTopAll> usersTopAll =
        Endpoint.of(UserTopAll.class).endpoint("/api/player").accept(lichessjson).toOne();

    public static EPOne<Leaderboard> usersLeaderboard =
        Endpoint.of(Leaderboard.class).endpoint("/api/player/top/%s/%s").accept(lichessjson).toOne();

    public static EPOne<Puzzle> dailyPuzzle =
        Endpoint.of(Puzzle.class).endpoint("/api/puzzle/daily").toOne();

    public static EPOne<Puzzle> puzzleById =
        Endpoint.of(Puzzle.class).endpoint("/api/puzzle/%s").toOne();

    public static EPMany<PuzzleActivity> puzzleActivity =
        Endpoint.of(PuzzleActivity.class).endpoint("/api/puzzle/activity").accept(jsonstream).scope(Scope.puzzle_read).toMany();

    public static EPOne<PuzzleDashboard> puzzleDashboard =
        Endpoint.of(PuzzleDashboard.class).endpoint("/api/puzzle/dashboard/%s").scope(Scope.puzzle_read).toOne();

    public static EPOne<PuzzleRace> puzzleRace =
        Endpoint.of(PuzzleRace.class).endpoint("/api/racer").post().scope(Scope.racer_write).toOne();

    public static EPOne<StormDashboard> stormDashboard =
        Endpoint.of(StormDashboard.class).endpoint("/api/storm/dashboard/%s").toOne();

    public static EPOne<CurrentSimuls> simuls =
        Endpoint.of(CurrentSimuls.class).endpoint("/api/simul").toOne();

    public static EPOne<CloudEvalCacheEntry> cloudEval =
        Endpoint.of(CloudEvalCacheEntry.class).endpoint("/api/cloud-eval").toOne();

    public static EPOne<PageStudy> studyPage =
        Endpoint.of(mapper(PageStudyWrapper.class).andThen(PageStudyWrapper::paginator))
        .endpoint("/study").scope(Scope.study_read).toOne();

    public static EPMany<Pgn> exportChapter =
        Endpoint.of(Pgn.class).endpoint("/api/study/%s/%s.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportChapters =
        Endpoint.of(Pgn.class).endpoint("/api/study/%s.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPOne<ZonedDateTime> lastModifiedStudy  =
        Endpoint.of(ZonedDateTime.class).endpoint("/api/study/%s.pgn")
        .head().toOne();

    public static EPMany<StudyMeta> listStudiesByUser =
        Endpoint.of(StudyMeta.class).endpoint("/api/study/by/%s")
        .accept(jsonstream).scope(Scope.study_read).toMany();

    public static EPMany<Pgn> exportStudies =
        Endpoint.of(Pgn.class).endpoint("/study/by/%s/export.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).scope(Scope.any).toMany();

    public static EPMany<ChapterMeta> importStudyChapters =
        Endpoint.of(ChapterMeta.class).endpoint("/api/study/%s/import-pgn")
        .streamMapper(stream -> stream.map(mapper(WrappedChapters.class)).filter(Objects::nonNull).flatMap(wc -> wc.chapters().stream()))
        .scope(Scope.study_write)
        .post(wwwform).toMany();

    public static EPOne<Void> deleteStudyChapter =
        Endpoint.of(Void.class).endpoint("/api/study/%s/%s").delete().toOne();

    public static EPOne<BulkPairing> bulkPairingGet =
        Endpoint.of(BulkPairing.class).endpoint("/api/bulk-pairing/%s")
        .scope(Scope.challenge_bulk).toOne();

    public static EPMany<BulkPairing> bulkPairingGetMany =
        Endpoint.of(BulkPairing.class).endpoint("/api/bulk-pairing")
        .streamMapper(stream -> stream.map(mapper(BulkPairingWrapper.class)).filter(Objects::nonNull).flatMap(w -> w.bulks().stream()))
        .scope(Scope.challenge_bulk).toMany();

    public static EPOne<BulkPairing> bulkPairingCreate =
        Endpoint.of(BulkPairing.class).endpoint("/api/bulk-pairing").post(wwwform).scope(Scope.challenge_bulk).toOne();

    public static EPOne<Void> bulkPairingStart =
        Endpoint.of(Void.class).endpoint("/api/bulk-pairing/%s/start-clocks").post().scope(Scope.challenge_bulk).toOne();

    public static EPOne<Void> bulkPairingCancel =
        Endpoint.of(Void.class).endpoint("/api/bulk-pairing/%s").delete().scope(Scope.challenge_bulk).toOne();

    public static EPMany<Game> bulkPairingGamesByBulkId =
        Endpoint.of(Game.class).endpoint("/api/bulk-pairing/%s/games").accept(jsonstream).scope(Scope.challenge_bulk).toMany();

    public static EPMany<Pgn> bulkPairingGamesByBulkIdPgn =
        Endpoint.of(Pgn.class).endpoint("/api/bulk-pairing/%s/games")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).scope(Scope.challenge_bulk).toMany();



    public static EPOne<PendingChallenges> challenges =
        Endpoint.of(PendingChallenges.class).endpoint("/api/challenge").scope(Scope.challenge_read).toOne();

    public static EPOne<Challenge> challengeCreate =
        Endpoint.of(Challenge.class).endpoint("/api/challenge/%s")
        .post(wwwform).scope(Scope.challenge_write).toOne();

    public static EPMany<Challenge> challengeCreateKeepAlive =
        Endpoint.of(Challenge.class).endpoint("/api/challenge/%s")
        .post(wwwform).scope(Scope.challenge_write).toMany();

    public static EPOne<ChallengeInfo> challengeShow =
        Endpoint.of(ChallengeInfo.class).endpoint("/api/challenge/%s/show")
        .scope(Scope.challenge_read).toOne();

    public static EPOne<ChallengeAI> challengeAI =
        Endpoint.of(ChallengeAI.class).endpoint("/api/challenge/ai")
        .post(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<ChallengeOpenEnded> challengeOpenEnded =
        Endpoint.of(ChallengeOpenEnded.class).endpoint("/api/challenge/open").post(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<Void> challengeCancel =
        Endpoint.of(Void.class).endpoint("/api/challenge/%s/cancel").post().scope(Scope.challenge_write).toOne();

    public static EPOne<Void> challengeAccept =
        Endpoint.of(Void.class).endpoint("/api/challenge/%s/accept").post().scope(Scope.challenge_write).toOne();

    public static EPOne<Void> challengeDecline =
        Endpoint.of(Void.class).endpoint("/api/challenge/%s/decline").post(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<Void> startClocksOfGame =
        Endpoint.of(Void.class).endpoint("/api/challenge/%s/start-clocks").post().scope(Scope.challenge_write).toOne();

    public static EPOne<Void> addTimeToGame =
        Endpoint.of(Void.class).endpoint("/api/round/%s/add-time/%s").post().scope(Scope.challenge_write).toOne();

    public static EPOne<Swiss> createSwiss =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/new/%s").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Swiss> tournamentSwissById =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/%s").toOne();

    public static EPOne<Swiss> updateSwissTournament =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/%s/edit").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Void> scheduleNextRoundSwiss =
        Endpoint.of(Void.class).endpoint("/api/swiss/%s/schedule-next-round").post(wwwform).scope(Scope.tournament_write).toOne();


    public static EPOne<Void> joinSwissTournament =
        Endpoint.of(Void.class).endpoint("/api/swiss/%s/join").post(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Void> terminateSwiss =
        Endpoint.of(Void.class).endpoint("/api/swiss/%s/terminate").post().scope(Scope.tournament_write).toOne();

    public static EPOne<Void> withdrawSwiss =
        Endpoint.of(Void.class).endpoint("/api/swiss/%s/withdraw").post().scope(Scope.tournament_write).toOne();

    public static EPMany<SwissResult> swissResults =
        Endpoint.of(SwissResult.class).endpoint("/api/swiss/%s/results").accept(jsonstream).toMany();

    public static EPMany<String> swissTRF =
        Endpoint.of(Function.identity()).endpoint("/swiss/%s.trf").accept(plain).toMany();

    public static EPMany<Game> gamesBySwissId =
        Endpoint.of(Game.class).endpoint("/api/swiss/%s/games").accept(jsonstream).toMany();

    public static EPMany<Pgn> gamesBySwissIdPgn =
        Endpoint.of(Pgn.class).endpoint("/api/swiss/%s/games")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Broadcast> officialBroadcasts =
        Endpoint.of(Broadcast.class).endpoint("/api/broadcast").accept(jsonstream).toMany();


    public static EPMany<Broadcast.TourWithLastRound> broadcastsTopActive =
        Endpoint.of(Broadcast.TourWithLastRound.class).endpoint("/api/broadcast/top")
        .streamMapper(stream -> stream.map(mapper(ActiveAndUpcoming.class)).flatMap(au -> au.active().stream()))
        .toMany();

    public static EPMany<Broadcast.TourWithLastRound> broadcastsTopUpcoming =
        Endpoint.of(Broadcast.TourWithLastRound.class).endpoint("/api/broadcast/top")
        .streamMapper(stream -> stream.map(mapper(ActiveAndUpcoming.class)).flatMap(au -> au.upcoming().stream()))
        .toMany();

    public static EPOne<PageBroadcast> broadcastsTopPastPage =
        Endpoint.of(mapper(PageBroadcastWrapper.class).andThen(PageBroadcastWrapper::past))
        .endpoint("/api/broadcast/top").toOne();

    public static EPOne<Broadcast> createBroadcast =
        Endpoint.of(Broadcast.class).endpoint("/broadcast/new").post(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<MyRound> createRound =
        Endpoint.of(mapper(MyRoundConvert.class).andThen(MyRoundConvert::toMyRound)).endpoint("/broadcast/%s/new").post(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Broadcast> broadcastById =
        Endpoint.of(Broadcast.class).endpoint("/api/broadcast/%s").scope(Scope.study_read).toOne();

    public static EPOne<PageBroadcast> broadcastPageByUser =
        Endpoint.of(PageBroadcast.class)
        .endpoint("/api/broadcast/by/%s").scope(Scope.study_read).toOne();

    public static EPOne<RoundInfo> roundById =
        Endpoint.of(RoundInfo.class).endpoint("/api/broadcast/-/-/%s").scope(Scope.study_read).toOne();

    public static EPOne<Void> updateBroadcast =
        Endpoint.of(Void.class).endpoint("/broadcast/%s/edit").post(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Broadcast.Round> updateRound =
        Endpoint.of(Broadcast.Round.class).endpoint("/broadcast/round/%s/edit").post(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Void> resetRound =
        Endpoint.of(Void.class).endpoint("/api/broadcast/round/%s/reset")
        .post().scope(Scope.study_write).toOne();

    public static EPMany<LeaderboardEntry> broadcastLeaderboard =
        Endpoint.ofArr(LeaderboardEntry.class).endpoint("/broadcast/%s/leaderboard").toMany();

    public static EPMany<PushResult> pushPGNbyRoundId =
        Endpoint.of(PushResult.class).endpoint("/api/broadcast/round/%s/push")
        .streamMapper(stream -> stream.map(mapper(PushWrapper.class)).flatMap(PushWrapper::result))
        .post(plain).scope(Scope.study_write).toMany();

    public static EPMany<Pgn> streamBroadcast =
        Endpoint.of(Pgn.class).endpoint("/api/stream/broadcast/round/%s.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportBroadcastOneRoundPgn =
        Endpoint.of(Pgn.class).endpoint("/api/broadcast/round/%s.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportBroadcastAllRoundsPgn =
        Endpoint.of(Pgn.class).endpoint("/api/broadcast/%s.pgn")
        .streamMapper(Util::toPgnStream)
        .accept(chesspgn).toMany();

    public static EPMany<MyRound> streamMyRounds = Endpoint.of(mapper(MyRoundConvert.class).andThen(MyRoundConvert::toMyRound)).endpoint("/api/broadcast/my-rounds").accept(jsonstream).scope(Scope.study_read).toMany();

    public static EPMany<String> boardSeekRealTime =
        Endpoint.of(Function.identity()).endpoint("/api/board/seek").post(wwwform).accept(plain).scope(Scope.board_play).toMany();

    public static EPOne<SeekAck> boardSeekCorr =
        Endpoint.of(SeekAck.class).endpoint("/api/board/seek").post(wwwform).accept(plain).scope(Scope.board_play).toOne();

    public static EPMany<GameStateEvent> streamBoardGameEvents =
        Endpoint.of(GameStateEvent.class).endpoint("/api/board/game/stream/%s").accept(jsonstream).scope(Scope.board_play).toMany();

    public static EPOne<Void> boardMove =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/move/%s").post().scope(Scope.board_play).toOne();

    public static EPOne<Void> boardChat =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/chat").post(wwwform).scope(Scope.board_play).toOne();

    public static EPOne<Void> boardAbort =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/abort").post().scope(Scope.board_play).toOne();

    public static EPOne<Void> boardResign =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/resign").post().scope(Scope.board_play).toOne();

    public static EPOne<Void> boardDraw =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/draw/%s").post().scope(Scope.board_play).toOne();

    public static EPOne<Void> boardTakeback =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/takeback/%s").post().scope(Scope.board_play).toOne();

    public static EPOne<Void> boardClaimVictory =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/claim-victory").post().scope(Scope.board_play).toOne();

    public static EPMany<ChatMessage> boardFetchChat =
        Endpoint.ofArr(ChatMessage.class).endpoint("/api/board/game/%s/chat").accept(jsonstream).scope(Scope.board_play).toMany();

    public static EPOne<Void> boardBerserk =
        Endpoint.of(Void.class).endpoint("/api/board/game/%s/berserk").scope(Scope.board_play).toOne();

    public static EPMany<User> botsOnline =
        Endpoint.of(mapper(UserData.class).andThen(UserData::toUser)).endpoint("/api/bot/online").accept(jsonstream).toMany();

    public static EPOne<Void> botAccountUpgrade =
        Endpoint.of(Void.class).endpoint("/api/bot/account/upgrade").post().scope(Scope.bot_play).toOne();

    public static EPMany<GameStateEvent> streamBotGameEvents =
        Endpoint.of(GameStateEvent.class).endpoint("/api/bot/game/stream/%s").accept(jsonstream).scope(Scope.bot_play).toMany();

    public static EPOne<Void> botMove =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/move/%s").post().scope(Scope.bot_play).toOne();

    public static EPOne<Void> botChat =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/chat").post(wwwform).scope(Scope.bot_play).toOne();

    public static EPOne<Void> botAbort =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/abort").post().scope(Scope.bot_play).toOne();

    public static EPOne<Void> botResign =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/resign").post().scope(Scope.bot_play).toOne();

    public static EPOne<Void> botDraw =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/draw/%s").post().scope(Scope.bot_play).toOne();

    public static EPOne<Void> botTakeback =
        Endpoint.of(Void.class).endpoint("/api/bot/game/%s/takeback/%s").post().scope(Scope.bot_play).toOne();


    public static EPMany<ChatMessage> botFetchChat =
        Endpoint.ofArr(ChatMessage.class).endpoint("/api/bot/game/%s/chat").accept(jsonstream).scope(Scope.bot_play).toMany();


    public static EPMany<ExternalEngineInfo> externalEngineList =
        Endpoint.ofArr(ExternalEngineInfo.class).endpoint("/api/external-engine").scope(Scope.engine_read).toMany();

    public static EPOne<ExternalEngineInfo> externalEngineCreate =
        Endpoint.of(ExternalEngineInfo.class).endpoint("/api/external-engine").post(json).scope(Scope.engine_write).toOne();

    public static EPOne<ExternalEngineInfo> externalEngineGet =
        Endpoint.of(ExternalEngineInfo.class).endpoint("/api/external-engine/%s").scope(Scope.engine_read).toOne();

    public static EPOne<ExternalEngineInfo> externalEngineUpdate =
        Endpoint.of(ExternalEngineInfo.class).endpoint("/api/external-engine/%s").put(json).scope(Scope.engine_write).toOne();

    public static EPOne<Void> externalEngineDelete =
        Endpoint.of(Void.class).endpoint("/api/external-engine/%s").delete().scope(Scope.engine_write).toOne();


    public static EPMany<ExternalEngineAnalysis> externalEngineAnalyse =
        Endpoint.of(ExternalEngineAnalysis.class).endpoint("/api/external-engine/%s/analyse").post(json).accept(jsonstream).target(ServerType.engine).toMany();

    public static EPOne<ExternalEngineRequest> externalEngineAcquire =
        Endpoint.of(ExternalEngineRequest.class).endpoint("/api/external-engine/work").post(json).target(ServerType.engine).toOne();

    public static EPOne<Void> externalEngineAnswer =
        Endpoint.of(Void.class).endpoint("/api/external-engine/work/%s").post(plain).target(ServerType.engine).toOne();


    static record AutocompleteWrapper(List<UserData> result) {}
    static record BulkPairingWrapper(List<BulkPairing> bulks) {}
    static record PlayingWrapper(List<GameInfo> nowPlaying)  {}
    static record AccountEmail(String email)  {}
    static record AccountKid(boolean kid)  {}
    static record WrappedSheet(String scores) {}
    static record ArenaResultWrappedSheet(int rank, int score, int rating, String username, Opt<String> title, Opt<String> flair, Opt<Integer> performance, Opt<String> team, Opt<WrappedSheet> sheet) {
        ArenaResult toArenaResult() { return new ArenaResult(rank, score, rating, username, title, flair, performance, team, sheet.map(WrappedSheet::scores)); }
    }
    static record WrappedChapters(List<ChapterMeta> chapters) {}
    static record PushAck(int moves) {}

    static record MyRoundConvert(MyRound.Tour tour, RoundConvert round, MyRound.Study study) {
        MyRound toMyRound() { return new MyRound(tour, round.toRound(), study); }
    }
    static record RoundConvert(String id, String slug, String name, ZonedDateTime createdAt, boolean startsAfterPrevious, Opt<ZonedDateTime> startsAt, boolean ongoing, boolean finished, java.net.URI url, Integer delay) {
        MyRound.Round toRound() {
            return new MyRound.Round(id, slug, name, createdAt, startsAfterPrevious, startsAt, ongoing, finished, url, delay == null ? Duration.ZERO : Duration.ofSeconds(delay));
        }
    }
    static record PushWrapper(List<PushResult> games) {
        Stream<PushResult> result() {
            return games.stream();
        }
    }

    static record TeamStandingWrapper(String id, List<Arena.TeamStanding> teams) {}

    static record ActiveAndUpcoming(List<Broadcast.TourWithLastRound> active, List<Broadcast.TourWithLastRound> upcoming) {}

    static record PageBroadcastWrapper(PageBroadcast past) {}
    static record PageStudyWrapper(PageStudy paginator) {}

    public static record PageBroadcast(
            Integer currentPage,
            Integer maxPerPage,
            List<Broadcast.TourWithLastRound> currentPageResults,
            Integer nbResults,
            Integer previousPage,
            Integer nextPage,
            Integer nbPages) implements Page<Broadcast.TourWithLastRound> {}


    public static class Builder<T> {
        private String endpoint = "";
        Function<Stream<String>, One<T>> mapOne;
        Function<Stream<String>, Many<T>> mapMany;

        private ServerType target = ServerType.api;
        private String accept = json;
        private String contentType;
        private Method method = Method.GET;
        private Scope scope;

        public Builder<T> elementMapper(Function<String, T> mapper) {
            Objects.requireNonNull(mapper);
            this.mapOne = stream -> stream.map(mapper).filter(Objects::nonNull).findFirst().map(One::entry).orElse(One.none());
            this.mapMany = stream -> Many.entries(stream.map(mapper).filter(Objects::nonNull));
            return this;
        }

        public Builder<T> endpoint(String endpoint) {
            this.endpoint = Objects.requireNonNull(endpoint);
            return this;
        }

        public Builder<T> streamMapper(Function<Stream<String>, Stream<T>> mapper) {
            Objects.requireNonNull(mapper);
            this.mapOne = stream -> mapper.apply(stream).findFirst().map(One::entry).orElse(One.none());
            this.mapMany = stream -> Many.entries(mapper.apply(stream).filter(Objects::nonNull));
            return this;
        }

        public Builder<T> accept(String accept) {
            this.accept = Objects.requireNonNull(accept);
            return this;
        }

        public Builder<T> post() {
            this.method = Method.POST;
            return this;
        }

        public Builder<T> post(String contentType) {
            this.contentType = Objects.requireNonNull(contentType);
            return post();
        }

        public Builder<T> put() {
            this.method = Method.PUT;
            return this;
        }

        public Builder<T> put(String contentType) {
            this.contentType = Objects.requireNonNull(contentType);
            return put();
        }

        public Builder<T> head() {
            this.method = Method.HEAD;
            return this;
        }

        public Builder<T> delete() {
            this.method = Method.DELETE;
            return this;
        }

        public Builder<T> scope(Scope scope) {
            this.scope = Objects.requireNonNull(scope);
            return this;
        }

        public Builder<T> target(ServerType target) {
            this.target = Objects.requireNonNull(target);
            return this;
        }

        public EPOne<T> toOne() {
            return Endpoint.one(this);
        }

        public EPMany<T> toMany() {
            return Endpoint.many(this);
        }
    }

    static <T> EPOne<T> one(Builder<T> builder) {
        return new EPOne<>(new EP(builder.endpoint, builder.accept, builder.contentType, builder.method, builder.scope, builder.target), builder.mapOne);
    }
    static <T> EPMany<T> many(Builder<T> builder) {
        return new EPMany<>(new EP(builder.endpoint, builder.accept, builder.contentType, builder.method, builder.scope, builder.target), builder.mapMany);
    }

    public static <T> Builder<T> of(Class<T> clazz) {
        return of(mapper(clazz));
    }

    public static <T> Builder<T> of(Function<String, T> elementMapper) {
        return new Builder<T>()
            .elementMapper(elementMapper);
    }

    public static <T> Builder<T> ofArr(Class<T> clazz) {
        return ofArr(mapperArr(clazz));
    }

    public static <T> Builder<T> ofArr(Function<String, T[]> elementMapper) {
        return new Builder<T>()
            .streamMapper(stream -> stream.map(elementMapper).flatMap(Arrays::stream));
    }

    default ParamsBuilder toBuilder(Consumer<Params> consumer) {
        record KeyValue(String key, String value) {}
        var headers = Stream.of(
                new KeyValue("accept", accept()),
                new KeyValue("content-type", contentType())
                )
            .filter(e -> e.value() != null)
            .collect(Collectors.toMap(KeyValue::key, KeyValue::value));

        var builder = new ParamsBuilder(endpoint(), method())
            .headers(headers)
            .scope(scope())
            .serverType(target());

        var params = new Params() {
            public Params path(Object... pathParameters)             { builder.path(pathParameters); return this; }
            public Params query(Map<String, Object> queryParameters) { builder.query(queryParameters); return this;}
            public Params body(InputStream inputStream)              { builder.body(inputStream); return this; }
            public Params body(String data)                          { builder.body(data); return this; }
            public Params body(Map<String, ?> dataMap)               { builder.body(dataMap); return this; }
            public Params timeout(Duration timeout)                  { builder.timeout(timeout); return this; }
            public Params headers(Map<String, String> headers)       { builder.headers(headers); return this; }
            public Params scope(Scope scope)                         { builder.scope(scope); return this; }
            public Params serverType(ServerType serverType)          { builder.serverType(serverType); return this; }
            public Params stream()                                   { builder.stream(); return this; }
        };
        consumer.accept(params);
        return builder;
    }
}
