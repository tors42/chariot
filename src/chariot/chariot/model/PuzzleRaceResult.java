package chariot.model;

import module java.base;

public record PuzzleRaceResult(String id, String owner, List<Racer> players, List<Puzzle> puzzles, ZonedDateTime startsAt, ZonedDateTime finishesAt) {

    public sealed interface Racer {
        String name();
        int score();
        default boolean patron() { return this instanceof User u && u.patronColor.isPresent(); }
    }

    public record User(String id, String name, int score, Opt<Integer> patronColor, Opt<String> flair) implements Racer {}
    public record Anon(String name, int score) implements Racer {}

    public record Puzzle(String id, String fen, String line, int rating) {}

    @Override
    public List<Racer> players() {
        return players.stream()
            .sorted(Comparator.comparing(Racer::score).reversed()
                    .thenComparing(User.class::isInstance).reversed())
            .toList();
    }
}
