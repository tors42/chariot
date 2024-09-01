package chariot.model;

import chariot.model.Condition.*;

public sealed interface SwissCondition extends Condition permits
    MinRatedGames,
    MaxRating,
    MinRating,
    Titled,
    MinAccountAge,
    AllowList,
    AllowListHidden,
    EntryCode,
    Generic,
    NotMissedSwiss
{}
