package chariot.util;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import chariot.util.Board.*;

public record ConsoleRenderer() {

    static String noframeTemplate = """
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s
        %s %s %s %s %s %s %s %s""";

    static String frameTemplate = """
        ┌───┬───┬───┬───┬───┬───┬───┬───┐
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        ├───┼───┼───┼───┼───┼───┼───┼───┤
        │ %s│ %s│ %s│ %s│ %s│ %s│ %s│ %s│
        └───┴───┴───┴───┴───┴───┴───┴───┘""";

    public interface Config {
        Config frame(boolean frame);
        Config letter(boolean letter);
        Config coordinates(boolean coordinates);
        Config flipped(boolean flipped);

        default Config frame() { return frame(true); }
        default Config letter() { return letter(true); }
        default Config coordinates() { return coordinates(true); }
        default Config flipped() { return flipped(true); }
    }

    public static String render(Board board) {
        return render(board, c -> {});
    }

    public static String render(Board board, Consumer<Config> config) {
        var toConsume = new Config() {
            Data mutate = new Data(false, false, false, false);
            @Override public Config frame(boolean frame)             { mutate = mutate.with(new Data.Frame(frame));             return this; }
            @Override public Config letter(boolean letter)           { mutate = mutate.with(new Data.Letter(letter));           return this; }
            @Override public Config coordinates(boolean coordinates) { mutate = mutate.with(new Data.Coordinates(coordinates)); return this; }
            @Override public Config flipped(boolean flipped)         { mutate = mutate.with(new Data.Flipped(flipped));         return this; }
        };
        config.accept(toConsume);
        Data data = toConsume.mutate;
        return render(board, data);
    }

    record Data(boolean frame, boolean letter, boolean coordinates, boolean flipped) {

        sealed interface Component {}

        record Frame(boolean value)       implements Component {}
        record Letter(boolean value)      implements Component {}
        record Coordinates(boolean value) implements Component {}
        record Flipped(boolean value)     implements Component {}

        Data with(Component component) {
            return new Data(
                    component instanceof Frame       f ? f.value : frame,
                    component instanceof Letter      u ? u.value : letter,
                    component instanceof Coordinates c ? c.value : coordinates,
                    component instanceof Flipped     f ? f.value : flipped
                    );
        }

        Data with(Component... components) {
            var copy = this;
            for (var component : components) copy = copy.with(component);
            return copy;
        }

    }

    private static String render(Board board, Data config) {
        Function<Piece, String> render = p -> (config.letter() ? p.letter() : p.unicode()) + (config.frame() ? " " : "");
        Supplier<String> empty = () -> config.frame() ? "  " : " ";
        String template = config.frame() ? frameTemplate : noframeTemplate;

        if (config.coordinates()) {
            var withoutCoordinates = template.lines().toList();
            var withCoordinates = new ArrayList<String>();

            Comparator<String> rankComparator = config.flipped() ? Comparator.naturalOrder() : Comparator.reverseOrder();
            Comparator<String> fileComparator = config.flipped() ? Comparator.reverseOrder() : Comparator.naturalOrder();

            var ranks = Arrays.stream("12345678".split("")).sorted(rankComparator).iterator();
            var files = Stream.concat(Stream.of(" "), Arrays.stream("abcdefgh".split("")).sorted(fileComparator)).toList();

            for (int line = 0; line < withoutCoordinates.size(); line++) {
                boolean renderRank = (!config.frame()) || line % 2 != 0;
                String prefix = renderRank ? ranks.next() : " ";
                withCoordinates.add(prefix + " " + withoutCoordinates.get(line) + "\n");
            }
            withCoordinates.add(String.join("%1$s", files).formatted(config.frame() ? "   " : " "));
            template = withCoordinates.stream().collect(Collectors.joining());
        }

        var pieces = new ArrayList<String>();
        if (! config.flipped()) {
            for (int row = 7; row >= 0; row--)
                for (int col = 0; col <= 7; col++)
                    pieces.add((board.get(row, col) != null ? (render.apply(board.get(row, col))) : empty.get()));
        } else {
            for (int row = 0; row <= 7; row++)
                for (int col = 7; col >= 0; col--)
                    pieces.add((board.get(row, col) != null ? (render.apply(board.get(row, col))) : empty.get()));
        }
        return template.formatted(pieces.toArray());
    }
}
