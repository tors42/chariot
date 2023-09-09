package chariot.model;

import java.time.ZonedDateTime;

public record Note(
        UserCommon from,
        UserCommon to,
        ZonedDateTime date,
        String text
        )  {}
