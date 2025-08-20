package chariot.model;

import java.util.Map;

public record UserStats(Map<StatsPerfType, StatsPerf> ratings, UserCount counts) {}

