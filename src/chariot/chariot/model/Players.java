package chariot.model;

public sealed interface Players {
    Player white();
    Player black();

    record Pair(Player white, Player black) implements Players {}
    record Analyzed(Player white, Player black, Analysis whiteAnalysis, Analysis blackAnalysis) implements Players {}

    default Pair pair() {
        return switch(this) {
            case Pair p -> p;
            case Analyzed(var w,var b,_,_) -> new Pair(w, b);
        };
    }
    default Opt<Analysis> whiteAnalysisOpt() { return this instanceof Analyzed a ? Opt.of(a.whiteAnalysis()) : Opt.of(); }
    default Opt<Analysis> blackAnalysisOpt() { return this instanceof Analyzed a ? Opt.of(a.blackAnalysis()) : Opt.of(); }
}
