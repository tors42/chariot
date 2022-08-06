package chariot.internal;

import static chariot.internal.Util.MediaType.*;

import java.time.Duration;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.Client.Scope;
import chariot.api.Many;
import chariot.api.One;
import chariot.api.Err;
import chariot.internal.Config.ServerType;
import chariot.internal.RequestParameters.*;
import chariot.model.*;
import chariot.model.ChallengeResult.ChallengeInfo;

import static chariot.internal.ModelMapper.mapper;
import static chariot.internal.ModelMapper.mapperArr;

public sealed interface Endpoint<T> {

    public record EPOne<T>(EP ep, Function<Stream<String>, One<T>> mapper) implements Endpoint<T> {

        public ReqOne<T> newRequest(Consumer<Params> params) {
            return RequestParameters.one(toBuilder(params), result -> {
                if (result instanceof RequestResult.Success s) return mapper.apply(s.stream());
                if (result instanceof RequestResult.Failure f) return One.fail(f.code(), Err.from(f.body()));
                return mapper.apply(Stream.of());
            });
        }
    }

    public record EPMany<T>(EP ep, Function<Stream<String>, Many<T>> mapper) implements Endpoint<T> {
        public ReqMany<T> newRequest(Consumer<Params> params) {
            return RequestParameters.many(toBuilder(params), result -> {
                if (result instanceof RequestResult.Success s) return mapper.apply(s.stream());
                if (result instanceof RequestResult.Failure f) return Many.fail(f.code(), Err.from(f.body()));
                return mapper.apply(Stream.of());
            });
        }
    }

    public record EP(
            String endpoint,
            String accept,
            String contentType,
            Scope scope,
            ServerType target) {}

    EP ep();
    default String accept()      { return ep().accept(); }
    default String contentType() { return ep().contentType(); }
    default String endpoint()    { return ep().endpoint(); }
    default Scope scope()        { return ep().scope(); }
    default ServerType target()  { return ep().target(); }

    public static EPOne<User> accountProfile =
        Endpoint.of(User.class).endpoint("/api/account").scope(Scope.any).toOne();

    public static EPOne<AccountPreferences> accountPreferences =
        Endpoint.of(AccountPreferences.class).endpoint("/api/account/preferences").scope(Scope.preference_read).toOne();

    public static EPOne<AccountEmail> accountEmail =
        Endpoint.of(AccountEmail.class).endpoint("/api/account/email").scope(Scope.email_read).toOne();

    public static EPOne<AccountKid> accountKid =
        Endpoint.of(AccountKid.class).endpoint("/api/account/kid").scope(Scope.preference_read).toOne();

    public static EPOne<Ack> accountKidStatus =
        Endpoint.of(Ack.class).endpoint("/api/account/kid").scope(Scope.preference_write).toOne();

    public static EPMany<GameInfo> accountNowPlaying =
        Endpoint.of(GameInfo.class).endpoint("/api/account/playing")
        .streamMapper(stream -> stream.map(mapper(PlayingWrapper.class)).filter(Objects::nonNull).flatMap(pw -> pw.nowPlaying().stream()))
        .scope(Scope.any).toMany();

    public static EPOne<Ack> accountOAuthToken =
        Endpoint.of(Ack.class).endpoint("/account/oauth/token").toOne();

    public static EPOne<TokenResult> apiToken =
        Endpoint.of(TokenResult.class).endpoint("/api/token").contentType(wwwform).toOne();

    public static EPOne<Ack> apiTokenRevoke =
        Endpoint.of(Ack.class).endpoint("/api/token").scope(Scope.any).toOne();

    public static EPOne<TokenBulkResult> apiTokenBulkTest =
        Endpoint.of(TokenBulkResult.class).endpoint("/api/token/test").contentType(plain).toOne();

    public static EPOne<ChallengeTokens> apiAdminChallengeTokens =
        Endpoint.of(ChallengeTokens.class).endpoint("/api/token/admin-challenge").contentType(wwwform).scope(Scope.web_mod).toOne();

    public static EPOne<Ack> sendMessage =
        Endpoint.of(Ack.class).endpoint("/inbox/%s").contentType(wwwform).scope(Scope.msg_write).toOne();

    public static EPOne<Crosstable> crosstableByUserIds =
        Endpoint.of(Crosstable.class).endpoint("/api/crosstable/%s/%s").toOne();

    public static EPOne<User> userById =
        Endpoint.of(User.class).endpoint("/api/user/%s").toOne();

    public static EPMany<RatingHistory> ratingHistoryById =
        Endpoint.ofArr(RatingHistory.class).endpoint("/api/user/%s/rating-history").toMany();

    public static EPOne<PerfStat> perfStatByIdAndType =
        Endpoint.of(PerfStat.class).endpoint("/api/user/%s/perf/%s").toOne();

    public static EPMany<Activity> activityById =
        Endpoint.ofArr(Activity.class).endpoint("/api/user/%s/activity").toMany();

    public static EPOne<Game> gameCurrentByUserId =
        Endpoint.of(Game.class).endpoint("/api/user/%s/current-game").toOne();

    public static EPMany<User> relFollowing =
        Endpoint.of(User.class).endpoint("/api/rel/following").scope(Scope.follow_read).accept(jsonstream).toMany();

    public static EPOne<Ack> followUser =
        Endpoint.of(Ack.class).endpoint("/api/rel/follow/%s").scope(Scope.follow_write).toOne();

    public static EPOne<Ack> unfollowUser =
        Endpoint.of(Ack.class).endpoint("/api/rel/unfollow/%s").scope(Scope.follow_write).toOne();

    public static EPMany<Tournament> tournamentArenaCreatedByUser =
        Endpoint.of(Tournament.class).endpoint("/api/user/%s/tournament/created").accept(jsonstream).toMany();

    public static EPMany<UserStatus> userStatusByIds =
        Endpoint.ofArr(UserStatus.class).endpoint("/api/users/status").toMany();

    public static EPMany<User> usersByIds =
        Endpoint.ofArr(User.class).endpoint("/api/users").contentType(plain).toMany();

    public static EPMany<StreamerStatus> liveStreamers =
        Endpoint.ofArr(StreamerStatus.class).endpoint("/streamer/live").toMany();

    public static EPOne<Team> teamById =
        Endpoint.of(Team.class).endpoint("/api/team/%s").toOne();

    public static EPMany<Team> teamsByUserId =
        Endpoint.ofArr(Team.class).endpoint("/api/team/of/%s").toMany();

    public static EPMany<User> teamUsersById =
        Endpoint.of(User.class).endpoint("/api/team/%s/users").accept(jsonstream).scope(Scope.team_read).toMany();

    public static EPOne<PageTeam> popularTeamsByPage =
        Endpoint.of(PageTeam.class).endpoint("/api/team/all").toOne();

    public static EPOne<PageTeam> teamsSearch =
        Endpoint.of(PageTeam.class).endpoint("/api/team/search").toOne();

    public static EPMany<Tournament> teamArenaById =
        Endpoint.of(Tournament.class).endpoint("/api/team/%s/arena").toMany();

    public static EPMany<Swiss> teamSwissById =
        Endpoint.of(Swiss.class).endpoint("/api/team/%s/swiss").toMany();

    public static EPOne<Ack> teamJoin =
        Endpoint.of(Ack.class).endpoint("/team/%s/join").contentType(wwwform).scope(Scope.team_write).toOne();

    public static EPOne<Ack> teamQuit =
        Endpoint.of(Ack.class).endpoint("/team/%s/quit").scope(Scope.team_write).toOne();

    public static EPOne<Ack> teamKick =
        Endpoint.of(Ack.class).endpoint("/team/%s/kick/%s").scope(Scope.team_write).toOne();

    public static EPOne<Ack> teamMessage =
        Endpoint.of(Ack.class).endpoint("/team/%s/pm-all").contentType(wwwform).scope(Scope.team_write).toOne();

    public static EPMany<TeamRequest> teamRequests =
        Endpoint.ofArr(TeamRequest.class).endpoint("/api/team/%s/requests").scope(Scope.team_read).toMany();

    public static EPOne<Ack> teamAcceptJoin =
        Endpoint.of(Ack.class).endpoint("/api/team/%s/request/%s/accept").scope(Scope.team_write).toOne();

    public static EPOne<Ack> teamDeclineJoin =
        Endpoint.of(Ack.class).endpoint("/api/team/%s/request/%s/decline").scope(Scope.team_write).toOne();

    public static EPOne<GameImport> gameImport =
        Endpoint.of(GameImport.class).endpoint("/api/import").contentType(wwwform).toOne();

    public static EPMany<StreamGame> streamGamesByUsers =
        Endpoint.of(StreamGame.class).endpoint("/api/stream/games-by-users").accept(jsonstream).contentType(plain).toMany();

    public static EPMany<StreamMove> streamMoves=
        Endpoint.of(StreamMove.class).endpoint("/api/stream/game/%s").accept(jsonstream).toMany();

    public static EPMany<StreamEvent> streamEvents =
        Endpoint.of(StreamEvent.class).endpoint("/api/stream/event").accept(jsonstream).scope(Scope.challenge_read).toMany();

    public static EPOne<Arena> createArenaTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Ack> joinArenaTournament =
        Endpoint.of(Ack.class).endpoint("/api/tournament/%s/join").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Ack> withdrawArenaTournament =
        Endpoint.of(Ack.class).endpoint("/api/tournament/%s/withdraw").scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> updateArenaTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament/%s").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> updateTeamBattleTournament =
        Endpoint.of(Arena.class).endpoint("/api/tournament/team-battle/%s").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Ack> terminateArenaTournament =
        Endpoint.of(Ack.class).endpoint("/api/tournament/%s/terminate").scope(Scope.tournament_write).toOne();

    public static EPOne<Arena> tournamentArenaById =
        Endpoint.of(Arena.class).endpoint("/api/tournament/%s").toOne();

    public static EPMany<ArenaResult> tournamentArenaResultsById =
        Endpoint.of(ArenaResult.class).endpoint("/api/tournament/%s/results").accept(jsonstream).toMany();

    public static EPOne<TeamBattleResults> tournamentTeamBattleResultsById =
        Endpoint.of(TeamBattleResults.class).endpoint("/api/tournament/%s/teams").toOne();

    public static EPOne<TournamentStatus> tournamentArenas =
        Endpoint.of(TournamentStatus.class).endpoint("/api/tournament").toOne();

    public static EPMany<Game> gamesByArenaId =
        Endpoint.of(Game.class).endpoint("/api/tournament/%s/games").accept(jsonstream).toMany();

    public static EPMany<Game> gamesByUserId =
        Endpoint.of(Game.class).endpoint("/api/games/user/%s").accept(jsonstream).toMany();

    public static EPOne<Game> gameById =
        Endpoint.of(Game.class).endpoint("/game/export/%s").toOne();

    public static EPMany<Game> gamesByIds =
        Endpoint.of(Game.class).endpoint("/api/games/export/_ids").accept(jsonstream).contentType(plain).toMany();

    public static EPOne<ExploreResult.OpeningDB> exploreMasters =
        Endpoint.of(ExploreResult.OpeningDB.class).endpoint("/masters").target(ServerType.explorer).toOne();

    public static EPOne<ExploreResult.OpeningDB> exploreLichess =
        Endpoint.of(ExploreResult.OpeningDB.class).endpoint("/lichess").target(ServerType.explorer).toOne();

    public static EPOne<ExploreResult.OpeningPlayer> explorePlayers =
        Endpoint.of(ExploreResult.OpeningPlayer.class).endpoint("/player").target(ServerType.explorer).toOne();

    public static EPOne<Pgn> exploreMasterOTB =
        Endpoint.of(Pgn.class).endpoint("/master/pgn/%s")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).target(ServerType.explorer).toOne();

    public static EPOne<TablebaseResult> tablebaseLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/standard").target(ServerType.tablebase).toOne();

    public static EPOne<TablebaseResult> tablebaseAtomicLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/atomic").target(ServerType.tablebase).toOne();

    public static EPOne<TablebaseResult> tablebaseAntichessLookup =
        Endpoint.of(TablebaseResult.class).endpoint("/antichess").target(ServerType.tablebase).toOne();

    public static EPOne<TVChannels> gameTVChannels =
        Endpoint.of(TVChannels.class).endpoint("/api/tv/channels").toOne();

    public static EPMany<TVFeed> gameTVFeed =
        Endpoint.of(TVFeed.class).endpoint("/api/tv/feed").accept(jsonstream).toMany();

    public static EPMany<Game> gamesTVChannel =
        Endpoint.of(Game.class).endpoint("/api/tv/%s").accept(jsonstream).toMany();

    public static EPOne<UserTopAll> usersTopAll =
        Endpoint.of(UserTopAll.class).endpoint("/player").accept(lichessjson).toOne();

    public static EPOne<Leaderboard> usersLeaderboard =
        Endpoint.of(Leaderboard.class).endpoint("/player/top/%s/%s").accept(lichessjson).toOne();

    public static EPOne<Puzzle> dailyPuzzle =
        Endpoint.of(Puzzle.class).endpoint("/api/puzzle/daily").toOne();

    public static EPMany<PuzzleActivity> puzzleActivity =
        Endpoint.of(PuzzleActivity.class).endpoint("/api/puzzle/activity").accept(jsonstream).scope(Scope.puzzle_read).toMany();

    public static EPOne<PuzzleDashboard> puzzleDashboard =
        Endpoint.of(PuzzleDashboard.class).endpoint("/api/puzzle/dashboard/%s").scope(Scope.puzzle_read).toOne();

    public static EPOne<PuzzleRace> puzzleRace =
        Endpoint.of(PuzzleRace.class).endpoint("/api/racer").scope(Scope.racer_write).toOne();

    public static EPOne<StormDashboard> stormDashboard =
        Endpoint.of(StormDashboard.class).endpoint("/api/storm/dashboard/%s").toOne();

    public static EPOne<Simuls> simuls =
        Endpoint.of(Simuls.class).endpoint("/api/simul").toOne();

    public static EPOne<Analysis> cloudEval =
        Endpoint.of(Analysis.class).endpoint("/api/cloud-eval").toOne();

    public static EPMany<Pgn> exportChapter =
        Endpoint.of(Pgn.class).endpoint("/study/%s/%s.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportChapters =
        Endpoint.of(Pgn.class).endpoint("/api/study/%s.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportStudies =
        Endpoint.of(Pgn.class).endpoint("/study/by/%s/export.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).scope(Scope.any).toMany();

    public static EPMany<BulkPairing> bulkPairingGet =
        Endpoint.of(BulkPairing.class).endpoint("/api/bulk-pairing")
        .streamMapper(stream -> stream.map(mapper(BulkPairingWrapper.class)).filter(Objects::nonNull).flatMap(w -> w.bulks().stream()))
        .scope(Scope.challenge_bulk).toMany();

    public static EPOne<BulkPairing> bulkPairingCreate =
        Endpoint.of(BulkPairing.class).endpoint("/api/bulk-pairing").contentType(wwwform).scope(Scope.challenge_bulk).toOne();

    public static EPOne<Ack> bulkPairingStart =
        Endpoint.of(Ack.class).endpoint("/api/bulk-pairing/%s/start-clocks").scope(Scope.challenge_bulk).toOne();

    public static EPOne<Ack> bulkPairingCancel =
        Endpoint.of(Ack.class).endpoint("/api/bulk-pairing/%s").scope(Scope.challenge_bulk).toOne();

    public static EPOne<PendingChallenges> challenges =
        Endpoint.of(PendingChallenges.class).endpoint("/api/challenge").scope(Scope.challenge_read).toOne();

    public static EPOne<Challenge> challengeCreate =
        Endpoint.of(Challenge.class).endpoint("/api/challenge/%s")
        .streamMapper(stream -> stream.map(mapper(ChallengeResult.class))
                .filter(ChallengeInfo.class::isInstance)
                .map(ChallengeInfo.class::cast)
                .map(ChallengeInfo::challenge))
        .contentType(wwwform).scope(Scope.challenge_write).toOne();

    public static EPMany<Challenge> challengeCreateKeepAlive =
        Endpoint.of(Challenge.class).endpoint("/api/challenge/%s")
        .streamMapper(stream -> stream.map(mapper(ChallengeResult.class))
                .filter(ChallengeInfo.class::isInstance)
                .map(ChallengeInfo.class::cast)
                .map(ChallengeInfo::challenge))
         .contentType(wwwform).scope(Scope.challenge_write).toMany();

    public static EPOne<ChallengeAI> challengeAI =
        Endpoint.of(ChallengeAI.class).endpoint("/api/challenge/ai")
        .streamMapper(stream -> stream.map(mapper(ChallengeResult.class))
                .filter(ChallengeAI.class::isInstance)
                .map(ChallengeAI.class::cast))
        .contentType(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<ChallengeOpenEnded> challengeOpenEnded =
        Endpoint.of(ChallengeOpenEnded.class).endpoint("/api/challenge/open").contentType(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<Ack> challengeCancel =
        Endpoint.of(Ack.class).endpoint("/api/challenge/%s/cancel").scope(Scope.challenge_write).toOne();

    public static EPOne<Ack> challengeAccept =
        Endpoint.of(Ack.class).endpoint("/api/challenge/%s/accept").scope(Scope.challenge_write).toOne();

    public static EPOne<Ack> challengeDecline =
        Endpoint.of(Ack.class).endpoint("/api/challenge/%s/decline").contentType(wwwform).scope(Scope.challenge_write).toOne();

    public static EPOne<Ack> startClocksOfGame =
        Endpoint.of(Ack.class).endpoint("/api/challenge/%s/start-clocks").scope(Scope.challenge_write).toOne();

    public static EPOne<Ack> addTimeToGame =
        Endpoint.of(Ack.class).endpoint("/api/round/%s/add-time/%s").scope(Scope.challenge_write).toOne();

    public static EPOne<Swiss> createSwiss =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/new/%s").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Swiss> tournamentSwissById =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/%s").toOne();

    public static EPOne<Swiss> updateSwissTournament =
        Endpoint.of(Swiss.class).endpoint("/api/swiss/%s/edit").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Ack> joinSwissTournament =
        Endpoint.of(Ack.class).endpoint("/api/swiss/%s/join").contentType(wwwform).scope(Scope.tournament_write).toOne();

    public static EPOne<Ack> terminateSwiss =
        Endpoint.of(Ack.class).endpoint("/api/swiss/%s/terminate").scope(Scope.tournament_write).toOne();

    public static EPMany<SwissResult> swissResults =
        Endpoint.of(SwissResult.class).endpoint("/api/swiss/%s/results").accept(jsonstream).toMany();

    public static EPMany<String> swissTRF =
        Endpoint.of(Function.identity()).endpoint("/swiss/%s.trf").accept(plain).toMany();

    public static EPMany<Game> gamesBySwissId =
        Endpoint.of(Game.class).endpoint("/api/swiss/%s/games").accept(jsonstream).toMany();

    public static EPMany<Broadcast> officialBroadcasts =
        Endpoint.of(Broadcast.class).endpoint("/api/broadcast").accept(jsonstream).toMany();

    public static EPOne<Broadcast> createBroadcast =
        Endpoint.of(Broadcast.class).endpoint("/broadcast/new").contentType(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Broadcast.Round> createRound =
        Endpoint.of(Broadcast.Round.class).endpoint("/broadcast/%s/new").contentType(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Broadcast> broadcastById =
        Endpoint.of(Broadcast.class).endpoint("/broadcast/-/%s").scope(Scope.study_read).toOne();

    public static EPOne<Broadcast.Round> roundById =
        Endpoint.of(Broadcast.Round.class).endpoint("/broadcast/-/-/%s").scope(Scope.study_read).toOne();

    public static EPOne<Ack> updateBroadcast =
        Endpoint.of(Ack.class).endpoint("/broadcast/%s/edit").contentType(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Broadcast.Round> updateRound =
        Endpoint.of(Broadcast.Round.class).endpoint("/broadcast/round/%s/edit").contentType(wwwform).scope(Scope.study_write).toOne();

    public static EPOne<Ack> pushPGNbyRoundId =
        Endpoint.of(Ack.class).endpoint("/broadcast/round/%s/push").contentType(plain).scope(Scope.study_write).toOne();

    public static EPMany<Pgn> streamBroadcast =
        Endpoint.of(Pgn.class).endpoint("/api/stream/broadcast/round/%s.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportBroadcastOneRoundPgn =
        Endpoint.of(Pgn.class).endpoint("/api/broadcast/round/%s.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).toMany();

    public static EPMany<Pgn> exportBroadcastAllRoundsPgn =
        Endpoint.of(Pgn.class).endpoint("/api/broadcast/%s.pgn")
        .streamMapper(stream -> Util.toPgnStream(stream))
        .accept(chesspgn).toMany();

    public static EPMany<String> boardSeekRealTime =
        Endpoint.of(Function.identity()).endpoint("/api/board/seek").contentType(wwwform).accept(plain).scope(Scope.board_play).toMany();

    public static EPOne<SeekAck> boardSeekCorr =
        Endpoint.of(SeekAck.class).endpoint("/api/board/seek").contentType(wwwform).accept(plain).scope(Scope.board_play).toOne();

    public static EPMany<StreamGameEvent> streamBoardGameEvents =
        Endpoint.of(StreamGameEvent.class).endpoint("/api/board/game/stream/%s").accept(jsonstream).scope(Scope.board_play).toMany();

    public static EPOne<Ack> boardMove =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/move/%s").scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardChat =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/chat").contentType(wwwform).scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardAbort =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/abort").scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardResign =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/resign").scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardDraw =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/draw/%s").scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardTakeback =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/takeback/%s").scope(Scope.board_play).toOne();

    public static EPOne<Ack> boardClaimVictory =
        Endpoint.of(Ack.class).endpoint("/api/board/game/%s/claim-victory").scope(Scope.board_play).toOne();

    public static EPMany<ChatMessage> boardFetchChat =
        Endpoint.ofArr(ChatMessage.class).endpoint("/api/board/game/%s/chat").accept(jsonstream).scope(Scope.board_play).toMany();

    public static EPMany<User> botsOnline =
        Endpoint.of(User.class).endpoint("/api/bot/online").accept(jsonstream).toMany();

    public static EPOne<Ack> botAccountUpgrade =
        Endpoint.of(Ack.class).endpoint("/api/bot/account/upgrade").scope(Scope.bot_play).toOne();

    public static EPMany<StreamGameEvent> streamBotGameEvents =
        Endpoint.of(StreamGameEvent.class).endpoint("/api/bot/game/stream/%s").accept(jsonstream).scope(Scope.bot_play).toMany();

    public static EPOne<Ack> botMove =
        Endpoint.of(Ack.class).endpoint("/api/bot/game/%s/move/%s").scope(Scope.bot_play).toOne();

    public static EPOne<Ack> botChat =
        Endpoint.of(Ack.class).endpoint("/api/bot/game/%s/chat").contentType(wwwform).scope(Scope.bot_play).toOne();

    public static EPOne<Ack> botAbort =
        Endpoint.of(Ack.class).endpoint("/api/bot/game/%s/abort").scope(Scope.bot_play).toOne();

    public static EPOne<Ack> botResign =
        Endpoint.of(Ack.class).endpoint("/api/bot/game/%s/resign").scope(Scope.bot_play).toOne();

    public static EPMany<ChatMessage> botFetchChat =
        Endpoint.ofArr(ChatMessage.class).endpoint("/api/bot/game/%s/chat").accept(jsonstream).scope(Scope.bot_play).toMany();

    public static class Builder<T> {
        private String endpoint;
        Function<Stream<String>, One<T>> mapOne;
        Function<Stream<String>, Many<T>> mapMany;

        private ServerType target = ServerType.api;
        private String accept = json;
        private String contentType;
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

        public Builder<T> contentType(String contentType) {
            this.contentType = Objects.requireNonNull(contentType);
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
        return new EPOne<>(new EP(builder.endpoint, builder.accept, builder.contentType, builder.scope, builder.target), builder.mapOne);
    }
    static <T> EPMany<T> many(Builder<T> builder) {
        return new EPMany<>(new EP(builder.endpoint, builder.accept, builder.contentType, builder.scope, builder.target), builder.mapMany);
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

        var builder = new ParamsBuilder(endpoint())
            .headers(headers)
            .scope(scope())
            .serverType(target());

        var params = new Params() {
            public Params path(Object... pathParameters)             { builder.path(pathParameters); return this; }
            public Params query(Map<String, Object> queryParameters) { builder.query(queryParameters); return this;}
            public Params post(String postData)                      { builder.post(postData); return this; }
            public Params post(Map<String, ?> postMap)               { builder.post(postMap); return this; }
            public Params post()                                     { builder.post(); return this; }
            public Params delete()                                   { builder.delete(); return this; }
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
