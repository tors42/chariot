package chariot.model;

import module java.base;

public record FideRatingHistory(
        SortedMap<LocalDate, Integer> blitz,
        SortedMap<LocalDate, Integer> rapid,
        SortedMap<LocalDate, Integer> standard
        ) {}
