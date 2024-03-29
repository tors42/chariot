package chariot.model;

import java.net.URI;
import java.util.Optional;

import chariot.model.Enums.Color;
import chariot.model.Enums.ColorPref;

public record ChallengeInfo(String id, URI url, Players players, GameType gameType, ColorInfo colorInfo) implements Challenge {

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

    public record OpenEnded()                                  implements Players {}
    public record From(Player challenger)                      implements Players {}
    public record FromTo(Player challenger, Player challenged) implements Players {}

    public record Player(UserInfo user, int rating, boolean provisional, boolean online) {}

    public sealed interface ColorInfo permits ColorRequest, ColorOutcome {
        ColorPref request();
    }
    public record ColorRequest(ColorPref request)                implements ColorInfo {}
    public record ColorOutcome(ColorPref request, Color outcome) implements ColorInfo {}
}
