package chariot.model;

public record TVChannels (
        TVChannel bot,
        TVChannel blitz,
        TVChannel racingKings,
        TVChannel ultraBullet,
        TVChannel bullet,
        TVChannel classical,
        TVChannel threeCheck,
        TVChannel antichess,
        TVChannel computer,
        TVChannel horde,
        TVChannel rapid,
        TVChannel atomic,
        TVChannel crazyhouse,
        TVChannel chess960,
        TVChannel kingOfTheHill,
        TVChannel topRated
        )  {

    public record TVChannel(UserCommon user, Integer rating, String gameId) {}
}
