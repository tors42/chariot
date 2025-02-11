package chariot.model;

import chariot.model.Condition.*;
import chariot.model.Condition.Member;

public sealed interface ArenaCondition extends Condition permits
    MinRatedGames,
    MaxRating,
    MinRating,
    Titled,
    MinAccountAge,
    AllowList,
    AllowListHidden,
    EntryCode,
    Generic,
    Member,
    Bots
{}
