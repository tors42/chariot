package chariot.model;

public interface Enums {

    enum DaysPerTurn {
        _1(1), _3(3), _5(5), _7(7), _10(10), _14(14);
        public final int days;
        DaysPerTurn(int days) { this.days = days; }

        // No need for user to import the enum class,
        // if they can Lambda invoke a method instead...
        //
        //      interface Foo {
        //          void bar(FooEnum e);
        //          void baz(Function<FooEnum.Provider, FooEnum> e);
        //      }
        //
        // Instead of:
        //
        // 1     import FooEnum;
        // 2
        // 3     Foo foo = ...;
        // 4     foo.bar(FooEnum.CAR);
        //
        // One can do:
        //
        // 1     Foo foo = ...;
        // 2     foo.baz(e -> e.car());
        //
        public interface Provider {
            default DaysPerTurn one()      { return _1; }
            default DaysPerTurn three()    { return _3; }
            default DaysPerTurn five()     { return _5; }
            default DaysPerTurn seven()    { return _7; }
            default DaysPerTurn ten()      { return _10; }
            default DaysPerTurn fourteen() { return _14; }
        }
        public static Provider provider() {return new Provider(){};}
    }

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

    enum Room {
        player, spectator;
        public interface Provider {
            default Room player()    { return player; }
            default Room spectator() { return spectator; }
        }
        public static Provider provider() {return new Provider(){};}
    }

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

    public enum Speed { bullet, blitz, rapid, classical, correspondence;
        public interface Provider {
            default Speed bullet()    { return bullet; }
            default Speed blitz() { return blitz; }
            default Speed rapid() { return rapid; }
            default Speed classical() { return classical; }
            default Speed correspondence() { return correspondence; }
        }
        public static Provider provider() {return new Provider(){};}
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
     * Starting time on the clock, in minutes.
     */
     enum ClockInitial { _0(0f), _0_25(0.25f), _0_5(0.5f), _0_75(0.75f), _1(1), _1_5(1.5f), _2(2), _3(3), _4(4), _5(5), _6(6), _7(7), _10(10), _15(15), _20(20), _25(25), _30(30), _40(40), _50(50), _60(60);
        public final float minutes;
        ClockInitial(float minutes) { this.minutes = minutes; }
        public interface Provider {
            default ClockInitial _0m() { return _0; }
            default ClockInitial _0_25m() { return _0_25; }
            default ClockInitial _0_5m() { return _0_5; }
            default ClockInitial _0_75m() { return _0_75; }
            default ClockInitial _1m() { return _1; }
            default ClockInitial _1_5m() { return _1_5; }
            default ClockInitial _2m() { return _2; }
            default ClockInitial _3m() { return _3; }
            default ClockInitial _4m() { return _4; }
            default ClockInitial _5m() { return _5; }
            default ClockInitial _6m() { return _6; }
            default ClockInitial _7m() { return _7; }
            default ClockInitial _10m() { return _10; }
            default ClockInitial _15m() { return _15; }
            default ClockInitial _20m() { return _20; }
            default ClockInitial _25m() { return _25; }
            default ClockInitial _30m() { return _30; }
            default ClockInitial _40m() { return _40; }
            default ClockInitial _50m() { return _50; }
            default ClockInitial _60m() { return _60; }
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
