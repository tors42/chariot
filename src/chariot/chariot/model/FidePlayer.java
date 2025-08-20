package chariot.model;

public record FidePlayer(
        int id,
        String name,
        String federation,
        Opt<String> title,
        Opt<Integer> year,
        Opt<Integer> standard,
        Opt<Integer> rapid,
        Opt<Integer> blitz,
        boolean inactive
        ) {}
