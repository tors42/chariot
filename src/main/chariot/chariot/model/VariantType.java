package chariot.model;


public sealed interface VariantType {

    enum Variant implements VariantType {
        standard,
        crazyhouse,
        antichess,
        atomic,
        horde,
        kingOfTheHill,
        racingKings,
        threeCheck;
    }

    record Chess960(Opt<String> fen) implements VariantType {}
    record FromPosition(Opt<String> fen) implements VariantType {}
}
