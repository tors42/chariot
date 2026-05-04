package chariot.internal.modeladapter;

import module chariot;

import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;

public interface PlayersAdapter {

    static Players nodeToPlayers(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayObject yo) {
            if (yo.value().get("white") instanceof YayObject whiteYo
                && yo.value().get("black") instanceof YayObject blackYo) {
                Players.Pair pair = new Players.Pair(nodeToPlayer(whiteYo, yayMapper), nodeToPlayer(blackYo, yayMapper));
                if (nodeToAnalysis(whiteYo) instanceof Analysis wa
                    && nodeToAnalysis(blackYo) instanceof Analysis ba) {
                    return new Players.Analyzed(pair.white(), pair.black(), wa, ba);
                }
                return pair;
            }
        }
        return null;
    }

    static Player nodeToPlayer(YayNode node, YayMapper yayMapper) {
        if (! (node instanceof YayObject yo)) return null;
        Player player = null;
        if (yo.value().get("name") instanceof YayObject nameYo) {
            IO.println("nameYo");
            nameYo.value().keySet().forEach(IO::println);
        }

        if (yo.value().containsKey("aiLevel")) {
            player = new AI(yo.getInteger("aiLevel"));
        } else if (! yo.value().containsKey("user")) {
            if (yo.getString("name") instanceof String name) {
                player = new Player.Name(name);
            } else {
                player = new Anonymous();
            }
        } else if (yo.value().get("user") instanceof YayObject userObject) {
            var userData = yayMapper.fromYayTree(userObject, UserData.class);
            UserCommon common = userData.toCommon();
            int rating = yo.getInteger("rating");
            boolean provisional = yo.getBool("provisional");
            var ratingDiff = Opt.of(yo.getInteger("ratingDiff"));
            Opt<Boolean> berserk = yo.value().containsKey("berserk") ? Opt.of(yo.getBool("berserk")) : Opt.empty();
            var team = Opt.of(yo.getString("team"));
            Opt<Player.ArenaInfo> arenaInfo = berserk instanceof Some || team instanceof Some
                ? Opt.of(new Player.ArenaInfo(berserk, team))
                : Opt.empty();
            player = new Player.Account(common, rating, provisional, ratingDiff, arenaInfo);
        }
        return player;
   }

   static Analysis nodeToAnalysis(YayObject yo) {
       if (yo.value().get("analysis") instanceof YayObject analysisObject) {
           return new Analysis(
                   analysisObject.getInteger("inaccuracy"),
                   analysisObject.getInteger("mistake"),
                   analysisObject.getInteger("blunder"),
                   analysisObject.getInteger("acpl"),
                   Opt.of(analysisObject.getInteger("accuracy")));
       }
       return null;
   }
}
