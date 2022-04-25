package chariot.model;

public sealed interface Model permits
    AccountEmail,
    AccountKid,
    AccountPreferences,
    Ack,
    Activity,
    Analysis,
    TournamentStatus,
    Arena,
    ArenaResult,
    Broadcast,
    Broadcast.Round,
    BulkPairing,
    BulkPairings,
    ChallengeResult,
    ChallengeTokens,
    ChatMessage,
    Crosstable,
    Err,
    ExploreResult,
    ExploreResult.OpeningDB,
    ExploreResult.OpeningPlayer,
    Game,
    GameImport,
    Leaderboard,
    NowPlaying,
    PageTeam,
    PendingChallenges,
    PerfStat,
    Puzzle,
    PuzzleActivity,
    PuzzleDashboard,
    RatingHistory,
    Simuls,
    StormDashboard,
    StreamEvent,
    StreamGame,
    StreamGameEvent,
    StreamMove,
    Swiss,
    SwissResult,
    TablebaseResult,
    Team,
    TeamBattleResults,
    TeamRequest,
    TokenBulkResult,
    TokenResult,
    Tournament,
    Trophy,
    TVChannels,
    TVFeed,
    UserPerformance,
    User,
    UserStatus,
    UserTopAll,
    Model.Unmapped {
        record Unmapped(String src) implements Model {}
        static Unmapped unmapped(String src) { return new Unmapped(src); }
}
