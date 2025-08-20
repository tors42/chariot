package chariot.model;

import java.time.Duration;
import java.time.ZonedDateTime;

public record TourInfo(
        String id,
        String createdBy,

        ZonedDateTime startsAt, // 2024-09-18T21:00:00Z
                                // 1720322955000

        String name,            // "fullName"
        Clock clock,
        Speed speed,
        Variant variant,     // "variant" : "standard"
                             // "variant" : {
                             //     "key": "standard",
                             //     "short": "Std",
                             //     "name": "Standard"
                             // }
                             // "position": {
                             //     "name": "Custom position",
                             //     "fen": "rbnqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RBNQKBNR w KQkq -"
                             // }
                             // "position": {
                             //     "name": "French Defense",
                             //     "fen": "rbnqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RBNQKBNR w KQkq -",
                             //     "eco": "C00",
                             //     "url": "http://"
                             // }
        int nbPlayers,
        boolean rated,
        Status status,
        Opt<String> description,
        Opt<Freq> freq
        ) {

    public enum Freq {
        hourly, daily, eastern, weekly, weekend, monthly, shield, marathon, yearly, unique, unknown;
        public static Freq fromString(String freq) {try{ return Freq.valueOf(freq); }catch(Exception e){} return unknown;}
    }

    public enum Status {
        created(10),started(20),finished(30),unknown(42);
        Status(int status) {
            this.status = status;
        }
        final int status;
        public int status() {
            return status;
        }
        public static Status valueOf(int status) {
            return switch(status) {
                case 10 -> created;
                case 20 -> started;
                case 30 -> finished;
                default -> unknown;
            };
        }
        public interface Provider {
            default Status created() { return created; }
            default Status started()  { return started; }
            default Status finished()  { return finished; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    public enum Speed {
        ultraBullet("Ultrabullet"),
        hyperBullet("Hyperbullet"),
        bullet("Bullet"),
        hippoBullet("HippoBullet"),
        superBlitz("SuperBlitz"),
        blitz("Blitz"),
        rapid("Rapid"),
        classical("Classical"),
        unknown("Unknown");

        Speed(String name) {
            this.name = name;
        }
        public final String name;

        public static Speed fromString(String speed) {
            return switch(speed.toLowerCase(java.util.Locale.ROOT)) {
                case "ultrabullet" -> ultraBullet;
                case "hyperbullet" -> hyperBullet;
                case "bullet"      -> bullet;
                case "hippobullet" -> hippoBullet;
                case "superblitz"  -> superBlitz;
                case "blitz"       -> blitz;
                case "rapid"       -> rapid;
                case "classical"   -> classical;
                default -> unknown;
            };
        }

        public static Speed fromClock(Clock clock) {
            return switch(clock) {
                case Clock(var initial, var increment) -> switch(initial.plusSeconds(40*increment.toSeconds())) {
                    case Duration estimated when estimated.toSeconds() <= 29   -> ultraBullet;
                    case Duration estimated when estimated.toSeconds() <= 179  -> bullet;
                    case Duration estimated when estimated.toSeconds() <= 479  -> blitz;
                    case Duration estimated when estimated.toSeconds() <= 1499 -> rapid;
                    case Duration estimated when estimated.toSeconds() >= 1500 -> classical;
                    default -> unknown;
                };
            };
        }
    }
}
