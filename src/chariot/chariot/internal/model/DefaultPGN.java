package chariot.internal.model;

import module java.base;

import chariot.model.PGN;

public record DefaultPGN(SequencedMap<String,String> tags, CharSequence text) implements PGN {

    public DefaultPGN() { this(new LinkedHashMap<>(), ""); }

    public DefaultPGN {
        text = Objects.requireNonNullElseGet(text, () -> "");
        tags = Collections.unmodifiableSequencedMap(tags);
    }

    public static PGN of(String tagsSection, String text) {
        return new DefaultPGN(tagsSection.lines()
                .<Map.Entry<String,String>>mapMulti((line, downstream) -> { try {
                    downstream.accept(Map.entry(
                                line.substring(1, line.indexOf(' ')),
                                line.substring(line.indexOf('"')+1, line.length()-2)));
                } catch (Exception e) {}
                }).collect(sequencedMap),
                text);
    }

    private static final Collector<Map.Entry<String, String>, ?, SequencedMap<String, String>> sequencedMap =
        Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                (_, newValue) -> newValue,
                LinkedHashMap::new);

    @Override
    public String toString() {
        return tags.isEmpty()
            ?                          text + "\n"
            : tagsSection() + "\n\n" + text + "\n";
    }

    @Override
    public String tagsSection() {
        return tags.entrySet().stream()
            .map(entry -> """
                    [%s "%s"]""".formatted(entry.getKey(), entry.getValue()))
            .collect(Collectors.joining("\n"));
    }

    @Override
    public String textSection() {
        return String.valueOf(text);
    }

    @Override
    public DefaultPGN withTags(Map<String, String> _tags) {
        return new DefaultPGN(_tags.entrySet().stream().collect(sequencedMap), text);
    }

    public DefaultPGN withTags(UnaryOperator<Stream<Map.Entry<String, String>>> _tags) {
        return new DefaultPGN(_tags.apply(tags.entrySet().stream()).collect(sequencedMap), text);
    }

    @Override
    public PGN withText(CharSequence _text) {
        return new DefaultPGN(tags, _text);
    }

    @Override
    public PGN filterTags(BiPredicate<String, String> filter) {
        return withTags(stream -> stream
                .filter(entry -> filter.test(entry.getKey(), entry.getValue())));
    }

    @Override
    public PGN replaceTags(BiFunction<String, String, String> replace) {
        return withTags(stream -> stream
                .map(entry -> Map.entry(entry.getKey(), replace.apply(entry.getKey(), entry.getValue()))));
    }

    @Override
    public PGN addTags(Map<String, String> add) {
        return withTags(s -> Stream.concat(s, add.entrySet().stream()));
    }

    public static String render(Text text) { return switch(text) {
        case Text.Move m when m.num().move() == 0                -> m.san();
        case Text.Move(String san, Text.Num(int move, int dots)) -> "%d%s %s".formatted(move, ".".repeat(dots), san);
        case Text.Variation(List<Text> v)                        -> "(%s)".formatted(String.join(" ", v.stream().map(Text::render).toList()));
        case Text.Result(String result)                          -> result;
        case Text.Empty()                                        -> "";
        case Text.Comment(String comment)                        -> "{%s}".formatted(comment); };
    }

    public static Stream<Text> parse(String moves) { return switch(moves.trim()) {
        case "" -> Stream.of(new Text.Empty());
        case String s when result(s) instanceof Text.Result r -> Stream.of(r);
        case String s when Character.isDigit(s.charAt(0)) -> {
            int dotPos = s.indexOf(".");
            int moveNum = Integer.parseInt(s.substring(0, dotPos));
            int dots = s.charAt(dotPos+1) == '.' ? 3 : 1;
            int sanBegin = s.indexOf(" ", dotPos+1);
            while(Character.isWhitespace(s.charAt(sanBegin))) sanBegin++;
            int sanEnd = indexOfOrEnd(" ", sanBegin, s);
            String san = s.substring(sanBegin, sanEnd);
            yield Stream.concat(
                    Stream.of(new Text.Move(san, moveNum, dots)),
                    DefaultPGN.parse(s.substring(sanEnd)));
        }
        case String s when '(' == s.charAt(0) -> {
            boolean inComment = false;
            int nest = 1; int pos = 1;
            while (nest != 0) {
                inComment = switch(s.charAt(pos)) {
                    case '{' -> true;
                    case '}' -> false;
                    default -> inComment;
                };
                if (! inComment) {
                    nest = switch(s.charAt(pos)) {
                        case '(' -> nest+1;
                        case ')' -> nest-1;
                        default -> nest;
                    };
                }
                pos++;
            }
            yield Stream.concat(
                    Stream.of(new Text.Variation(DefaultPGN.parse(s.substring(1, pos-1)).toList())),
                    DefaultPGN.parse(s.substring(pos)));
        }
        case String s when '{' == s.charAt(0) -> Stream.concat(
                Stream.of(new Text.Comment(s.substring(1, s.indexOf('}')))),
                DefaultPGN.parse(s.substring(s.indexOf('}')+1)));
        case String s -> Stream.concat(
                Stream.of(new Text.Move(s.substring(0, indexOfOrEnd(" ", 0, s)))),
                DefaultPGN.parse(s.substring(indexOfOrEnd(" ", 0, s)))); };
    }

    static Text.Result result(String s) {
        return switch(s) {
            case "*",
                 "1-0",
                 "0-1",
                 "1/2-1/2" -> new Text.Result(s);
            default -> null;
        };
    }

    private static int indexOfOrEnd(String target, int from, String source) {
        int index = source.indexOf(target, from);
        return index == -1 ? source.length() : index;
    }

}
