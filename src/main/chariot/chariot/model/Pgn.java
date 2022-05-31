package chariot.model;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import chariot.internal.Util.PgnSpliterator;

public sealed interface Pgn extends Model {

    List<Tag> tags();
    Map<String,String> tagMap();
    String moves();

    static Pgn of(List<Tag> tags, String moves) {
        return new BasicPgn(List.copyOf(tags), moves);
    }

    static List<Pgn> readFromFile(Path file) {
        try (var stream = Files.lines(file)) {
            return StreamSupport.stream(new PgnSpliterator(stream.iterator()), false).toList();
        } catch(Exception ex) {
            return List.of();
        }
    }

    record BasicPgn(List<Tag> tags, String moves) implements Pgn {
        @Override
        public String toString() {
            return String.join("\n\n",
                    String.join("\n", tags.stream().map(Object::toString).toList()),
                    moves);
        }

        @Override
        public Map<String, String> tagMap() {
            return tags.stream().collect(Collectors.toMap(Tag::name, Tag::value));
        }
    }

    sealed interface Tag {
        String name();
        String value();

        static Tag parse(String line) {
            return  of(line.substring(1, line.indexOf(' ')),
                       line.substring(line.indexOf('"')+1, line.length()-2));
        }

        static Tag of(String name, String value) { return new BasicTag(name, value); }

        record BasicTag(String name, String value) implements Tag {
            @Override
            public String toString() { return "[%s \"%s\"]".formatted(name, value); }
        }
    }
}
