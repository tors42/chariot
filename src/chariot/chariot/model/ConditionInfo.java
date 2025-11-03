package chariot.model;

import java.util.Map;
import java.time.Period;
import java.util.List;

public record ConditionInfo<T extends Condition>(List<T> list, Map<T, String> unmet) {
    public ConditionInfo {
        list = List.copyOf(list);
        unmet = Map.copyOf(unmet);
    }

    public Opt<List<String>> allowList() {
        return find(Condition.AllowList.class)
            .map(Condition.AllowList::users);
    }

    public boolean hasEntryCode() { return find(Condition.EntryCode.class).isPresent(); }
    public boolean onlyForTitled() { return find(Condition.Titled.class).isPresent(); }
    public boolean allowsBots() { return find(Condition.Bots.class)
        .map(Condition.Bots::allowed).orElse(false); }

    public Opt<Integer> minRating() {
        return find(Condition.MinRating.class)
            .map(Condition.MinRating::rating);
    }

    public Opt<Integer> maxRating() {
        return find(Condition.MaxRating.class)
            .map(Condition.MaxRating::rating);
    }

    public Opt<Period> minAccountAge() {
        return find(Condition.MinAccountAge.class)
            .map(Condition.MinAccountAge::age);
    }

    public Opt<String> onlyForTeam() {
        return find(Condition.Member.class)
            .map(Condition.Member::teamId);
    }

    public boolean notMissedSwiss() { return find(Condition.NotMissedSwiss.class).isPresent(); }

    private <U> Opt<U> find(Class<U> typeClass) {
        return list().stream()
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
            .findFirst()
            .map(Opt::of)
            .orElse(Opt.of());
    }
}
