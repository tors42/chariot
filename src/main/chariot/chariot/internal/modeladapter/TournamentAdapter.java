package chariot.internal.modeladapter;

import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;

public interface TournamentAdapter {
    static Tournament nodeToTournament(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayObject yo) {
            var map = yo.value();
            if (map.get("schedule") != null) {
                return yayMapper.fromYayTree(node, Tournament.Scheduled.class);
            } else {
                if (map.get("teamBattle") != null) {
                    return yayMapper.fromYayTree(node, Tournament.TeamBattle.class);
                } else {
                    return yayMapper.fromYayTree(node, Tournament.LocalArena.class);
                }
            }
        }
        return null;
    }
}
