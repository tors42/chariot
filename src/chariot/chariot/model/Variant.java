package chariot.model;

import java.util.Locale;

public sealed interface Variant {

    default String key() {
        return switch(this) {
            case Basic basic -> basic.name();
            case Chess960 _ -> "chess960";
            case FromPosition _ -> "fromPosition";
        };
    }

    enum Basic implements Variant {
        standard,
        crazyhouse,
        antichess,
        atomic,
        horde,
        kingOfTheHill,
        racingKings,
        threeCheck;
    }

    record Chess960(Opt<String> fen) implements Variant {}
    record FromPosition(Opt<String> fen, Opt<String> name) implements Variant {
        public FromPosition(Opt<String> fen) {
            this(fen, Opt.empty());
        }
    }

    static Variant fromString(String variant) {
        return switch(variant.toLowerCase(Locale.ROOT)) {
            case "standard"      -> Basic.standard;
            case "crazyhouse"    -> Basic.crazyhouse;
            case "antichess"     -> Basic.antichess;
            case "atomic"        -> Basic.atomic;
            case "horde"         -> Basic.horde;
            case "kingofthehill" -> Basic.kingOfTheHill;
            case "racingkings"   -> Basic.racingKings;
            case "threecheck"    -> Basic.threeCheck;
            case "chess960"      -> new Chess960(Opt.empty());
            case "fromposition"  -> new FromPosition(Opt.empty());
            default -> null;
        };
    }

    interface Provider {
        default Variant standard()      { return Basic.standard; }
        default Variant crazyhouse()    { return Basic.crazyhouse; }
        default Variant antichess()     { return Basic.antichess; }
        default Variant atomic()        { return Basic.atomic; }
        default Variant horde()         { return Basic.horde; }
        default Variant kingOfTheHill() { return Basic.kingOfTheHill; }
        default Variant racingKings()   { return Basic.racingKings; }
        default Variant threeCheck()    { return Basic.threeCheck; }

        default Variant chess960()               { return new Chess960(Opt.of()); }
        default Variant chess960(String fen)     { return new Chess960(Opt.of(fen)); }
        default Variant fromPosition(String fen) { return new FromPosition(Opt.of(fen)); }
        default Variant standard(String fen)     { return new FromPosition(Opt.of(fen), Opt.of("workaround-standard")); }
    }
    static Provider provider() {return new Provider(){};}
}
