package chariot.model;

public record Analysis(int inaccuracy, int mistake, int blunder, int acpl, Opt<Integer> accuracy) {}
