package chariot.model;

public interface Enums {

    enum Level {
        _1(1), _2(2), _3(3), _4(4), _5(5), _6(6), _7(7), _8(8);
        public final int level;
        Level(int level) { this.level = level; }

        public interface Provider {
            default Level one()   { return _1; }
            default Level two()   { return _2; }
            default Level three() { return _3; }
            default Level four()  { return _4; }
            default Level five()  { return _5; }
            default Level six()   { return _6; }
            default Level seven() { return _7; }
            default Level eight() { return _8; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    enum ColorPref { random, white, black;
        public interface Provider {
            default ColorPref random() { return random; }
            default ColorPref white()  { return white; }
            default ColorPref black()  { return black; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    enum Color { white, black;
        public interface Provider {
            default Color white()  { return white; }
            default Color black()  { return black; }
        }
        public Color other() { return this == white ? black : white; }
        public static Provider provider() {return new Provider(){};}
    }

    enum Offer {
        yes,no;
        public interface Provider {
            default Offer yes() { return yes; }
            default Offer no()  { return no; }
        }
        public static Provider provider() {return new Provider(){};}
    }


    enum Direction { in, out }

    enum TournamentState {
        created(10),started(20),finished(30);
        TournamentState(int status) {
            this.status = status;
        }
        final int status;
        public int status() {
            return status;
        }
        public static TournamentState valueOf(int status) {
            return switch(status) {
                case 10 -> created;
                case 20 -> started;
                case 30 -> finished;
                default -> throw new IllegalArgumentException("Unknown tournament status: " + status);
            };
        }
        //:
        public interface Provider {
            default TournamentState created() { return created; }
            default TournamentState started()  { return started; }
            default TournamentState finished()  { return finished; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    public enum Speed { ultraBullet, bullet, blitz, rapid, classical, correspondence;
        public interface Provider {
            default Speed ultraBullet()     { return ultraBullet; }
            default Speed bullet()    { return bullet; }
            default Speed blitz() { return blitz; }
            default Speed rapid() { return rapid; }
            default Speed classical() { return classical; }
            default Speed correspondence() { return correspondence; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    /**
     * Specifies a rating group, which includes ratings up to next rating
     * group.<br/>
     * _1600 indicates ratings between 1600-1800 and
     * _2500 indicates ratings from 2500 and up.
     */
    enum RatingGroup {
        _0, _1000, _1200, _1400, _1600, _1800, _2000, _2200, _2500;

        public String asString() {
            return name().substring(1);
        }

        public interface Provider {
            /**
             * 600-1000
             */
            default RatingGroup _0() {
                return _0;
            }

            /**
             * 1000-1200
             */
            default RatingGroup _1000() {
                return _1000;
            }

            /**
             * 1200-1400
             */
            default RatingGroup _1200() {
                return _1200;
            }

            /**
             * 1400-1600
             */
            default RatingGroup _1400() {
                return _1400;
            }

            /**
             * 1600-1800
             */
            default RatingGroup _1600() {
                return _1600;
            }

            /**
             * 1800-2000
             */
            default RatingGroup _1800() {
                return _1800;
            }

            /**
             * 2000-2200
             */
            default RatingGroup _2000() {
                return _2000;
            }

            /**
             * 2200-2500
             */
            default RatingGroup _2200() {
                    return _2200;
            }

            /**
             * 2500-over 9000!
             */
            default RatingGroup _2500() {
                return _2500;
            }
        }

        public static Provider provider() {
            return new Provider() {
            };
        }
    }

    public enum VariantName {
        standard,
        chess960,
        crazyhouse,
        antichess,
        atomic,
        horde,
        kingOfTheHill,
        racingKings,
        threeCheck;
        public interface Provider {
            default VariantName standard(){ return standard; }
            default VariantName chess960() { return chess960; }
            default VariantName crazyhouse() { return crazyhouse; }
            default VariantName antichess() { return antichess; }
            default VariantName atomic() { return atomic; }
            default VariantName horde() { return horde; }
            default VariantName kingOfTheHill() { return kingOfTheHill; }
            default VariantName racingKings() { return racingKings; }
            default VariantName threeCheck() { return threeCheck; }
        }
        public static Provider provider() {return new Provider(){};}
     }

    public enum GameVariant {
        standard,
        chess960,
        crazyhouse,
        antichess,
        atomic,
        horde,
        kingOfTheHill,
        racingKings,
        threeCheck,
        fromPosition;
        public interface Provider {
            default GameVariant standard(){ return standard; }
            default GameVariant chess960() { return chess960; }
            default GameVariant crazyhouse() { return crazyhouse; }
            default GameVariant antichess() { return antichess; }
            default GameVariant atomic() { return atomic; }
            default GameVariant horde() { return horde; }
            default GameVariant kingOfTheHill() { return kingOfTheHill; }
            default GameVariant racingKings() { return racingKings; }
            default GameVariant threeCheck() { return threeCheck; }
            default GameVariant fromPosition() { return fromPosition; }
        }
        public static Provider provider() {return new Provider(){};}
     }

    /**
     * Specifies who will be able to chat
     */
     enum ChatFor { none(0), onlyTeamLeaders(10), onlyTeamMembers(20), allLichessPlayers(30);
        public final int id;
        private ChatFor(int id) { this.id = id; }
        public interface Provider {
            default ChatFor none() { return none; }
            default ChatFor onlyTeamLeaders() { return onlyTeamLeaders; }
            default ChatFor onlyTeamMembers() { return onlyTeamMembers; }
            default ChatFor allLichessPlayers() { return allLichessPlayers; }
        }
        public static Provider provider() {return new Provider(){};}
    };

    /**
     * The reasons for declining a challenge
     */
     enum DeclineReason {
        generic,
        later,
        tooFast,
        tooSlow,
        timeControl,
        rated,
        casual,
        standard,
        variant,
        noBot,
        onlyBot;

        public interface Provider {
            default DeclineReason generic()     { return generic; }
            default DeclineReason later()       { return later; }
            default DeclineReason tooFast()     { return tooFast; }
            default DeclineReason tooSlow()     { return tooSlow; }
            default DeclineReason timeControl() { return timeControl; }
            default DeclineReason rated()       { return rated; }
            default DeclineReason casual()      { return casual; }
            default DeclineReason standard()    { return standard; }
            default DeclineReason variant()     { return variant; }
            default DeclineReason noBot()       { return noBot; }
            default DeclineReason onlyBot()     { return onlyBot; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    /**
     * The TV Channels
     */
     enum Channel {
        bot, blitz, racingKings, ultraBullet, bullet, classical, threeCheck, antichess, computer, horde, rapid, atomic, crazyhouse, chess960, kingOfTheHill, topRated;
        public interface Provider {
            default Channel bot()     { return bot; }
            default Channel blitz()       { return blitz; }
            default Channel racingKings() { return racingKings; }
            default Channel ultraBullet()     { return ultraBullet; }
            default Channel bullet()     { return bullet; }
            default Channel classical()       { return classical; }
            default Channel threeCheck()      { return threeCheck; }
            default Channel antichess()    { return antichess; }
            default Channel computer()     { return computer; }
            default Channel horde()       { return horde; }
            default Channel rapid()     { return rapid; }
            default Channel atomic()     { return atomic; }
            default Channel crazyhouse()     { return crazyhouse; }
            default Channel chess960()     { return chess960; }
            default Channel kingOfTheHill()     { return kingOfTheHill; }
            default Channel topRated()     { return topRated; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    /**
     * The possible performance types
     */
     enum PerfType {
        antichess, atomic, blitz, bullet, chess960, classical, correspondence, crazyhouse, horde, kingOfTheHill, racingKings, rapid, threeCheck, ultraBullet;

        public interface Provider {
            default PerfType atomic()         { return atomic; }
            default PerfType antichess()      { return antichess; }
            default PerfType blitz()          { return blitz; }
            default PerfType bullet()         { return bullet; }
            default PerfType chess960()       { return chess960; }
            default PerfType classical()      { return classical; }
            default PerfType correspondence() { return correspondence; }
            default PerfType crazyhouse()     { return crazyhouse; }
            default PerfType horde()          { return horde; }
            default PerfType kingOfTheHill()  { return kingOfTheHill; }
            default PerfType racingKings()    { return racingKings; }
            default PerfType rapid()          { return rapid; }
            default PerfType threeCheck()     { return threeCheck; }
            default PerfType ultraBullet()    { return ultraBullet; }
        }
        public static Provider provider() {return new Provider(){};}
    }

    /**
     * The possible performance types, excluding correspondence
     */
    enum PerfTypeNoCorr {
        antichess, atomic, blitz, bullet, chess960, classical, crazyhouse, horde, kingOfTheHill, racingKings, rapid, threeCheck, ultraBullet;
        public interface Provider {
            default PerfTypeNoCorr atomic()         { return atomic; }
            default PerfTypeNoCorr antichess()      { return antichess; }
            default PerfTypeNoCorr blitz()          { return blitz; }
            default PerfTypeNoCorr bullet()         { return bullet; }
            default PerfTypeNoCorr chess960()       { return chess960; }
            default PerfTypeNoCorr classical()      { return classical; }
            default PerfTypeNoCorr crazyhouse()     { return crazyhouse; }
            default PerfTypeNoCorr horde()          { return horde; }
            default PerfTypeNoCorr kingOfTheHill()  { return kingOfTheHill; }
            default PerfTypeNoCorr racingKings()    { return racingKings; }
            default PerfTypeNoCorr rapid()          { return rapid; }
            default PerfTypeNoCorr threeCheck()     { return threeCheck; }
            default PerfTypeNoCorr ultraBullet()    { return ultraBullet; }
        }
        public static Provider provider() {return new Provider(){};}
     }

     enum PerfTypeWithFromPos {
        antichess, atomic, blitz, bullet, chess960, classical, correspondence, crazyhouse, horde, kingOfTheHill, racingKings, rapid, threeCheck, ultraBullet, fromPosition;

        public interface Provider {
            default PerfTypeWithFromPos atomic()         { return atomic; }
            default PerfTypeWithFromPos antichess()      { return antichess; }
            default PerfTypeWithFromPos blitz()          { return blitz; }
            default PerfTypeWithFromPos bullet()         { return bullet; }
            default PerfTypeWithFromPos chess960()       { return chess960; }
            default PerfTypeWithFromPos classical()      { return classical; }
            default PerfTypeWithFromPos correspondence() { return correspondence; }
            default PerfTypeWithFromPos crazyhouse()     { return crazyhouse; }
            default PerfTypeWithFromPos horde()          { return horde; }
            default PerfTypeWithFromPos kingOfTheHill()  { return kingOfTheHill; }
            default PerfTypeWithFromPos racingKings()    { return racingKings; }
            default PerfTypeWithFromPos rapid()          { return rapid; }
            default PerfTypeWithFromPos threeCheck()     { return threeCheck; }
            default PerfTypeWithFromPos ultraBullet()    { return ultraBullet; }
        }
        public static Provider provider() {return new Provider(){};}
    }

}
