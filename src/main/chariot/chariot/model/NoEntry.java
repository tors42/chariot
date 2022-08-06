package chariot.model;
public sealed interface NoEntry<T> extends One<T> permits
    None,
    Fail {}
