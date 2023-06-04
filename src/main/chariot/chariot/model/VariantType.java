package chariot.model;


public sealed interface VariantType {

    enum Variant implements VariantType {
        standard,
        chess960,
        crazyhouse,
        antichess,
        atomic,
        horde,
        kingOfTheHill,
        racingKings,
        threeCheck;
    }

    record FromPosition(Opt<String> fen) implements VariantType {}
}
