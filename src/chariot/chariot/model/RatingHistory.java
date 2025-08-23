package chariot.model;

import java.util.List;
import java.time.LocalDate;

public record RatingHistory(String name, List<DateResult> results)  {
    public record DateResult(LocalDate date, Integer points) {};
}
