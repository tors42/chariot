package chariot.model;

import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

import chariot.internal.Util.PgnSpliterator;

public sealed interface Pgn {

    List<Tag> tags();
    List<Move> moveList();
    Map<String,String> tagMap();
    String moves();

    record Variation(List<Move> variation)  implements Move {}
    record Result(String result)            implements Move {}
    record NumBegin(int move, String san)   implements Move {}
    record NumEnd(int move, String san)     implements Move {}
    record Empty()                          implements Move {}
    record End(String san)                  implements Move {}
    record Comment(String comment)          implements Move {}

    sealed interface Move {

        static String render(Move move) {
            if (move instanceof Variation v) {
                return "(%s)".formatted(String.join(" ", v.variation().stream().map(Move::render).toList()));
            } else if (move instanceof Result r) {
                return r.result();
            } else if (move instanceof NumBegin nb) {
                return "%d. %s".formatted(nb.move(), nb.san());
            } else if (move instanceof NumEnd ne) {
                return "%d... %s".formatted(ne.move(), ne.san());
            } else if (move instanceof Empty e) {
                return "";
            } else if (move instanceof End e) {
                return e.san();
            } else if (move instanceof Comment c) {
                return "{%s}".formatted(c.comment());
            }
            return "{oops}"; // looking forward to preview-feature with exhaustive switch check, JEP-427
        }

        static List<Move> parse(String moves) {
            moves = moves.trim();
            if (moves.isEmpty()) {
                return List.of(new Empty());
            } else if(Set.of("*", "1-0", "0-1", "1/2-1/2").contains(moves)) {
                return List.of(new Result(moves));
            } else if(Character.isDigit(moves.charAt(0))) {
                int dotPos = moves.indexOf(".");
                int move = Integer.parseInt(moves.substring(0, dotPos));
                boolean manyDots = moves.charAt(dotPos+1) == '.';
                int sanBegin = moves.indexOf(" ", dotPos+1);
                while(Character.isWhitespace(moves.charAt(sanBegin))) sanBegin++;
                int sanEnd = indexOfOrEnd(" ", sanBegin, moves);
                String san = moves.substring(sanBegin, sanEnd);
                return Stream.concat(
                        Stream.of(manyDots ? new NumEnd(move, san) : new NumBegin(move, san)),
                        parse(moves.substring(sanEnd)).stream())
                    .toList();
            } else if ('(' == moves.charAt(0)) {
                boolean inComment = false;
                int nest = 1; int pos = 1;
                while (nest != 0) {
                    inComment = switch(moves.charAt(pos)) {
                        case '{' -> true;
                        case '}' -> false;
                        default -> inComment;
                    };
                    if (! inComment) {
                        nest = switch(moves.charAt(pos)) {
                            case '(' -> nest+1;
                            case ')' -> nest-1;
                            default -> nest;
                        };
                    }
                    pos++;
                }
                return Stream.concat(
                        Stream.of(new Variation(parse(moves.substring(1, pos-1)))),
                        parse(moves.substring(pos)).stream())
                    .toList();
            } else if('{' == moves.charAt(0)) {
                int end = moves.indexOf('}');
                return Stream.concat(
                        Stream.of(new Comment(moves.substring(1, end))),
                        parse(moves.substring(end+1)).stream())
                    .toList();
            } else {
                int sanEnd = indexOfOrEnd(" ", 0, moves);
                return Stream.concat(
                        Stream.of(new End(moves.substring(0, sanEnd))),
                        parse(moves.substring(sanEnd)).stream())
                    .toList();
            }
        }
    }

    private static int indexOfOrEnd(String target, int from, String source) {
        int index = source.indexOf(target, from);
        return index == -1 ? source.length() : index;
    }

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

        @Override
        public List<Move> moveList() {
            return Move.parse(moves());
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
