package chariot.model;

public sealed interface Player permits Anonymous, AI, Player.Account, Player.Analysed {

    String name();

    static Player account(UserCommon user, int rating, boolean provisional) {
        return new Account(user, rating, provisional, Opt.empty(), Opt.empty());
    }

    static Player account(UserCommon user, int rating, boolean provisional, int ratingDiff) {
        return new Account(user, rating, provisional, Opt.some(ratingDiff), Opt.empty());
    }

    record Account(UserCommon user, int rating, boolean provisional, Opt<Integer> ratingDiff, Opt<ArenaInfo> arenaInfo) implements Player {
        @Override public String name() { return user.name(); }
    }

    // All types (Anon, AI and Accounts) can be analysed)
    record Analysed(Player player, Analysis analysis) implements Player {
        @Override public String name() { return player.name(); }
    }

    record ArenaInfo(Opt<Boolean> berserk, Opt<String> team) {}
}
