package chariot.model;

import chariot.model.Enums.Speed;

public sealed interface TimeControl permits RealTime, Correspondence, Unlimited {
    default Speed speed() { return this instanceof RealTime rt ? rt.speed() : Speed.correspondence; }
    default String show() { return this instanceof RealTime rt ? rt.show()
        : this instanceof Correspondence c ? "%d day%s per move".formatted(c.daysPerTurn(), c.daysPerTurn() > 1 ? "s" : "")
        : "Unlimited"; }
}

