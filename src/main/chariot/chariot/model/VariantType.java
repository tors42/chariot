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

    record FromPosition(String fen) implements VariantType {}

}
