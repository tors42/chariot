package chariot.model;

import module java.base;

import chariot.internal.Util;
import chariot.internal.model.DefaultPGN;

public interface PGN {

    /// Parses a string of PGN into a `PGN`
    static PGN read(CharSequence sequence) {
        try (var stream = PGN.stream(sequence)) {
            return stream.findFirst().orElseGet(DefaultPGN::new);
        }
    }

    /// Parses a file of PGN into a `PGN`
    static PGN read(Path file) {
        try (var stream = PGN.stream(file)) {
            return stream.findFirst().orElseGet(DefaultPGN::new);
        }
    }

    /// Parses a string of PGNs into a `Stream<PGN>`
    static Stream<PGN> stream(CharSequence sequence) {
        return Util.pgnStream(sequence);
    }

    /// Parses a file of PGNs into a `Stream<PGN>`
    static Stream<PGN> stream(Path file) {
        return Util.pgnStream(file);
    }

    /// Retrieves a mapping of PGN tag names to values
    Map<String,String> tags();

    /// Retrieves the list of moves from the mainline of the PGN (omitting comments, variations and result)
    default List<String> movesList() {
        return textList().stream()
            .<String>mapMulti((text, mapper) -> {
                if (text instanceof Text.Move move) mapper.accept(move.san());
            }).toList();
    }

    /// Retrieves a string of moves from the mainline of the PGN (omitting comments, variations and result)
    default String moves() {
        return String.join(" ", movesList());
    }

    default List<Text> textList() {
        return Text.parse(textSection()).toList();
    }

    /// Retrieves a string of the PGN tags
    String tagsSection();
    /// Retrieves a string of the PGN move text
    String textSection();

    /// A copy of this `PGN` with tags replaced with `tags`
    PGN withTags(Map<String, String> tags);
    /// A copy of this `PGN` with move text replaced with `text`
    PGN withText(CharSequence text);

    /// @return a copy of this `PGN` with tags yielded from applying provided `tags` operator on existing tags.
    PGN withTags(UnaryOperator<Stream<Map.Entry<String, String>>> tags);
    /// @return a copy of this `PGN` with tags yielded from applying provided `filter` on existing tags.
    PGN filterTags(BiPredicate<String, String> filter);
    /// @return a copy of this `PGN` with tags yielded from applying provided `mapper` on existing tags.
    PGN replaceTags(BiFunction<String, String, String> mapper);
    /// @return a copy of this `PGN` with tags yielded from adding (overwrites duplicates) `tags` to existing tags.
    PGN addTags(Map<String, String> tags);

    sealed interface Text {
        record Move(String san, Num num) implements Text {
            public Move(String san) { this(san,0,0); }
            public Move(String san, int move, int dots) { this(san, new Num(move,dots)); }
            public Move {
                san = Objects.requireNonNull(san);
                num = Objects.requireNonNull(num);
            }
        }
        record Variation(List<Text> variation)     implements Text {
            public Variation { variation = List.copyOf(Objects.requireNonNull(variation)); }
        }
        record Comment(String comment)             implements Text {
            public Comment { comment = Objects.requireNonNull(comment); }
        }
        record Result(String result)               implements Text {
            public Result { result = Objects.requireNonNull(result); }
        }
        record Empty()                             implements Text {}

        record Num(int move, int dots) {
            public Num {
                move = Math.max(0, move);
                dots = Math.clamp(dots, 0, 3);
            }
        }

        static String render(Text text) {
            return DefaultPGN.render(text);
        }

        static Stream<Text> parse(String moves) {
            return DefaultPGN.parse(moves);
        }
    }
}
