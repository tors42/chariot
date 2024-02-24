package chariot.internal.modeladapter;

import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;

public interface GamePlayerAdapter {
    static Player nodeToPlayer(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayObject yo) {
            Player player = null;
            if (yo.value().containsKey("aiLevel")) {
                player = new AI(yo.getInteger("aiLevel"));
            } else if (! yo.value().containsKey("user")) {
                player = new Anonymous();
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

            if (yo.value().get("analysis") instanceof YayObject analysisObject) {
                var accuracy = Opt.of(analysisObject.getInteger("accuracy"));
                var analysis = new Analysis(
                        analysisObject.getInteger("inaccuracy"),
                        analysisObject.getInteger("mistake"),
                        analysisObject.getInteger("blunder"),
                        analysisObject.getInteger("acpl"),
                        accuracy);
                player = new Player.Analysed(player, analysis);
            }
            return player;
        }
        return null;
   }
}
