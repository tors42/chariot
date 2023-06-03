package chariot.model;

import java.net.URI;
import java.util.Optional;

import chariot.model.Enums.Color;
import chariot.model.Enums.ColorPref;
import chariot.model.Enums.Speed;

public record ChallengeInfo(String id, URI url, Players players, GameType type, ColorInfo colorInfo) implements Challenge {

    public sealed interface Players permits OpenEnded, From, FromTo {
        default Optional<Player> challengerOpt() {
            return this instanceof OpenEnded ? Optional.empty() :
                Optional.of(this instanceof From from
                        ? from.challenger()
                        : ((FromTo) this).challenger());
        }
        default Optional<Player> challengedOpt() {
            return this instanceof FromTo t
                ? Optional.of(t.challenged())
                : Optional.empty();
        }
    }
    public record OpenEnded() implements Players {}
    public record From(Player challenger) implements Players {}
    public record FromTo(Player challenger, Player challenged) implements Players {}

    public record Player(UserCommon user, int rating, boolean provisional, boolean online) {}

    public record GameType(boolean rated, VariantType variant, TimeControl timeControl) {}

    public sealed interface TimeControl permits RealTime, Correspondence, Unlimited {
        default Speed speed() { return this instanceof RealTime rt ? rt.speed() : Speed.correspondence; }
        default String show() { return this instanceof RealTime rt ? rt.show()
            : this instanceof Correspondence c ? "%d day%s per move".formatted(c.daysPerTurn(), c.daysPerTurn() > 1 ? "s" : "")
            : "Unlimited"; }
    }
    public record RealTime(int initial, int increment, String show, Speed speed) implements TimeControl {}
    public record Correspondence(int daysPerTurn) implements TimeControl {}
    public record Unlimited() implements TimeControl {}

    public sealed interface ColorInfo permits ColorRequest, ColorOutcome {
        ColorPref request();
    }
    public record ColorRequest(ColorPref request) implements ColorInfo {}
    public record ColorOutcome(ColorPref request, Color outcome) implements ColorInfo {}
}
