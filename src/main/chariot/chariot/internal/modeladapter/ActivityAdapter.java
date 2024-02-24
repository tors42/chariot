package chariot.internal.modeladapter;

import java.lang.reflect.Array;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;

public interface ActivityAdapter {

    static Object nodeToArray(YayNode root, YayMapper yayMapper) {
        if (root instanceof YayArray ya) {
            var resultHelper = (Function<YayNode, List<Activity.Result>>)
                (node) -> {
                    if (node instanceof YayObject yo) {
                        return yo.value().entrySet().stream()
                            .map(e -> {
                                try {
                                    var rh = yayMapper.fromYayTree(e.getValue(), Activity.Result.ResultHelper.class);
                                    return new Activity.Result(e.getKey(), rh.win(), rh.loss(), rh.draw(), rh.rp());
                                } catch (Exception ex) {}
                                return null;
                            }).toList();
                    }
                    return List.of();
                };

            var list = ya.value().stream()
                .map(e -> {
                    if (e instanceof YayObject yo) {
                        Activity.Interval interval = null;
                        if (yo.value().get("interval") instanceof YayObject yoi) {
                            var start = Util.fromLong(yoi.getNumber("start").longValue());
                            var end = Util.fromLong(yoi.getNumber("end").longValue());
                            interval = new Activity.Interval(start, end);
                        }
                        var activityTypes = yo.value().keySet();
                        var activities = new HashSet<Activity.Type>();
                        for (String activityType : activityTypes) {
                            if ("interval".equals(activityType)) continue;

                            var node = yo.value().get(activityType);

                            Activity.Type activity = switch(activityType) {
                                case "games" -> new Activity.Type.Games(resultHelper.apply(node));
                                case "puzzles" -> new Activity.Type.Puzzles(resultHelper.apply(node).get(0));
                                case "tournaments" -> yayMapper.fromYayTree(node, Activity.Type.Tournaments.class);
                                case "practice" -> new Activity.Type.Practices(((YayArray)node).value().stream().map(y -> yayMapper.fromYayTree(y, Activity.Practice.class)).toList());
                                case "simuls" -> new Activity.Type.Simuls(((YayArray)node).value().stream().map(y -> yayMapper.fromYayTree(y, Activity.Simul.class)).toList());
                                case "correspondenceMoves" -> yayMapper.fromYayTree(node, Activity.Type.CorrespondenceMoves.class);
                                case "correspondenceEnds" -> {
                                    var ends = yayMapper.fromYayTree(node, Activity.Type.CorrespondenceEnds.class);
                                    yield new Activity.Type.CorrespondenceEnds(resultHelper.apply(node).get(0), ends.games());
                                }
                                case "follows" -> {
                                    var yoFollows = (YayObject) node;
                                    yield new Activity.Type.Follows(
                                            yoFollows.value().get("in") instanceof YayObject yoIn && yoIn.value().get("ids") instanceof YayArray yarr ?
                                            yarr.value().stream().map(yn -> ((YayString) yn).value()).toList() :
                                            List.of(),
                                            yoFollows.value().get("out") instanceof YayObject yoOut && yoOut.value().get("ids") instanceof YayArray yarr ?
                                            yarr.value().stream().map(yn -> ((YayString) yn).value()).toList() :
                                            List.of()
                                            );
                                }
                                case "teams" -> new Activity.Type.Teams(((YayArray) node).value().stream().map(y -> yayMapper.fromYayTree(y, Activity.Team.class)).toList());
                                case "patron" -> yayMapper.fromYayTree(node, Activity.Type.Patron.class);
                                case "posts" -> new Activity.Type.Posts(((YayArray) node).value().stream().map(y -> yayMapper.fromYayTree(y, Activity.Topic.class)).toList());
                                default -> new Activity.Type.Unknown(activityType, yo.value().get(activityType).toString());
                            };

                            activities.add(activity);
                        }
                        return new Activity(interval, activities);
                    }
                    return null;
                }
            ).toList();
            var array = Array.newInstance(Activity.class, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        }
        return null;
    }

}
