package chariot.model;

import module java.base;

public record FidePlayer(
        int id,
        String name,
        String federation,
        Opt<String> title,
        Opt<Integer> year,
        Opt<Integer> standard,
        Opt<Integer> rapid,
        Opt<Integer> blitz,
        Opt<Photo> photo,
        boolean inactive
        ) {
    public record Photo(String credit, URI small, URI medium) {}
}
