package chariot.model;

import java.time.*;

public record UserTimes(ZonedDateTime created, ZonedDateTime seen, Duration played, Duration featured) {}

