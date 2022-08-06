package chariot.internal;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import chariot.internal.yayson.YayMapper;
import chariot.internal.yayson.Parser;
import chariot.internal.yayson.Parser.YayNode;
import chariot.internal.yayson.Parser.YayNode.*;
import chariot.internal.yayson.Parser.YayNode.YayValue.*;
import chariot.model.*;

@SuppressWarnings("unchecked")
public class ModelMapper {

    private static final YayMapper mapper = YayMapper.mapper();
    private static final Map<Class<?>, Function<String, ?>> mappings = new HashMap<>();
    private static final Map<Class<?>, Function<String, ?>> mappingsArr = new HashMap<>();

    public static <T> Function<String, T> mapper(final Class<T> clazz) {
        return (Function<String, T>) mappings.computeIfAbsent(clazz, c -> json -> mapper.fromString(json, c));
    }

    public static <T> Function<String, T[]> mapperArr(final Class<T> clazz) {
        return (Function<String, T[]>) mappingsArr.computeIfAbsent(clazz, c -> json -> {
            var root = Parser.fromString(json);
            if (root instanceof YayArray ya) {
                var list = ya.value().stream()
                    .map(e -> mapper.fromYayTree(e, c))
                    .toList();
                Object array = Array.newInstance(c, list.size());
                for (int i = 0; i < list.size(); i++) {
                    Array.set(array, i, list.get(i));
                }
                return array;
            }
            return null;
        });
    }

    static {
        // Add custom mapping of some model classes,
        // where automatic transformation of json model to java model isn't straightforward.

        // Specify Java model <-> JSON model field name mappings,
        // where JSON model contains Java keywords,
        // so another name must be used in the Java model.
        mapper.setMappings(PerfStat.Stat.DateResult.class,    ModelMapperUtil.intMapping());
        mapper.setMappings(Game.class,                        ModelMapperUtil.createdAtAndLastMoveAtMapping());
        mapper.setMappings(StreamMove.Info.class,             ModelMapperUtil.createdAtMapping());
        mapper.setMappings(StreamGameEvent.Full.class,        ModelMapperUtil.createdAtMapping());
        mapper.setMappings(TVChannels.class,                  ModelMapperUtil.tvChannelsMapping());
        mapper.setMappings(User.Count.class,                  ModelMapperUtil.importMapping());
        mapper.setMappings(Variant.class,                     ModelMapperUtil.shortMapping());
        mapper.setMappings(Broadcast.Round.class,             ModelMapperUtil.startsAtMapping());
        mapper.setMappings(ChallengeAI.class,                 ModelMapperUtil.createdAtAndLastMoveAtMapping());

        // "Exotic" JSON model...
        mappings.put(Crosstable.class,
                (json) -> {
                    var helper = (Function<YayNode, Crosstable.Results>)
                        (node) -> {
                            if (node instanceof YayObject yo && yo.value().get("users") instanceof YayObject you) {
                                var set = you.value().entrySet().stream()
                                    .map(e -> new Crosstable.Results.Result(e.getKey(), ((YayNumber)e.getValue()).value().doubleValue()))
                                    .collect(Collectors.toSet());

                                // Results model
                                return new Crosstable.Results(set, yo.getInteger("nbGames"));
                            }
                            return null;
                        };

                    var root = Parser.fromString(json);

                    if (root instanceof YayObject yo) {
                        var total = helper.apply(root);
                        var matchup = Optional.ofNullable(helper.apply(yo.value().get("matchup")));

                        // Crosstabel model
                        var crosstable = new Crosstable(total, matchup);
                        return Crosstable.class.cast(crosstable);
                    }
                    return null;
                });



        // Some guidance for the correct challenge type to be modelled...
        mappings.put(ChallengeResult.class,
                (json) -> {
                    var node = Parser.fromString(json);
                    if (node instanceof YayObject yo) {
                        var outer = yo.value();
                        if (outer.get("urlWhite") != null) {
                            return mapper.fromYayTree(node, ChallengeOpenEnded.class);
                        } else {
                            if (outer.get("challenge") != null) {
                                return mapper.fromYayTree(node, ChallengeResult.ChallengeInfo.class);
                            } else {
                                if (outer.get("done") != null) {
                                    // keepAliveStream responds with the challenge info,
                                    // and when the opponent decides - a "done" message with the choice
                                    // is sent "accepted"/"declined"...
                                    return mapper.fromYayTree(node, ChallengeResult.OpponentDecision.class);
                                } else {
                                    return mapper.fromYayTree(node, ChallengeAI.class);
                                }
                            }
                        }
                    }
                    return null;
                });

        // Some guidance for the correcte tournament type to be modelled...
        mappings.put(Tournament.class,
                (json) -> {
                    var node = Parser.fromString(json);
                    if (node instanceof YayObject yo) {
                        var map = yo.value();
                        if (map.get("schedule") != null) {
                            return mapper.fromYayTree(node, Tournament.Scheduled.class);
                        } else {
                            if (map.get("teamBattle") != null) {
                                return mapper.fromYayTree(node, Tournament.TeamBattle.class);
                            } else {
                                return mapper.fromYayTree(node, Tournament.LocalArena.class);
                            }
                        }
                    }
                    return null;
                });

        mappings.put(ChallengeTokens.class,
                (json) -> {
                    var node = Parser.fromString(json);

                    if (node instanceof YayObject yo) {
                        var map = yo.value().keySet().stream()
                            .collect(Collectors.toMap(
                                        k -> k,
                                        k -> yo.getString(k)
                                        )
                                    );
                        return new ChallengeTokens(map);
                    }

                    return null;
                });

        mappings.put(TokenBulkResult.class,
                (json) -> {
                    var node = Parser.fromString(json);

                    if (node instanceof YayObject yo) {
                        var map = new HashMap<String, TokenBulkResult.TokenInfo>();
                        yo.value().keySet().stream()
                            .forEach(token -> {
                                var val = yo.value().get(token) instanceof YayObject tokenInfo ?
                                    new TokenBulkResult.TokenInfo(
                                            tokenInfo.getString("userId"),
                                            tokenInfo.getString("scopes"),
                                            Util.fromLong(tokenInfo.getLong("expires"))
                                            ) : null;
                                map.put(token, val);
                            });
                        return new TokenBulkResult(map);
                    }

                    return null;
                });


        mappingsArr.put(RatingHistory.class,
                (json) -> {
                    var helper = (Function<YayNode, List<RatingHistory.DateResult>>)
                        (node) -> {
                            if (node instanceof YayArray yarr) {
                                return yarr.value().stream().map( e -> {
                                    if (e instanceof YayArray ya) {
                                        var list = ya.value().stream().map(yn -> (YayNumber)yn).toList();
                                        var y = list.get(0);
                                        var m = list.get(1);
                                        var d = list.get(2);
                                        var p = list.get(3);

                                        var date = LocalDate.of(y.value().intValue(), m.value().intValue()+1, d.value().intValue());
                                        var points = p.value().intValue();

                                        // DateResult model
                                        return new RatingHistory.DateResult(date, points);
                                    }
                                    return null;
                                }).toList();
                            }
                            return List.of();
                        };

                    var root = Parser.fromString(json);

                    if (root instanceof YayArray ya) {
                        var list = ya.value().stream()
                            .map(e -> {
                                if (e instanceof YayObject yo) {

                                    // RatingHistory model
                                    return new RatingHistory(yo.getString("name"), helper.apply(yo.value().get("points")));
                                }
                                return null;
                            })
                        .toList();

                        var array = Array.newInstance(RatingHistory.class, list.size());
                        for (int i = 0; i < list.size(); i++) {
                            Array.set(array, i, list.get(i));
                        }
                        return array;
                    }
                    return null;
                });

        mappingsArr.put(Activity.class,
                (json) -> {
                    var root = Parser.fromString(json);
                    if (root instanceof YayArray ya) {
                        var resultHelper = (Function<YayNode, List<Activity.Result>>)
                            (node) -> {
                                if (node instanceof YayObject yo) {
                                    return yo.value().entrySet().stream()
                                        .map(e -> {
                                            try {
                                                var rh = mapper.fromYayTree(e.getValue(), Activity.Result.ResultHelper.class);
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
                                            case "tournaments" -> mapper.fromYayTree(node, Activity.Type.Tournaments.class);
                                            case "practice" -> new Activity.Type.Practices(((YayArray)node).value().stream().map(y -> mapper.fromYayTree(y, Activity.Practice.class)).toList());
                                            case "simuls" -> new Activity.Type.Simuls(((YayArray)node).value().stream().map(y -> mapper.fromYayTree(y, Activity.Simul.class)).toList());
                                            case "correspondenceMoves" -> mapper.fromYayTree(node, Activity.Type.CorrespondenceMoves.class);
                                            case "correspondenceEnds" -> {
                                                var ends = mapper.fromYayTree(node, Activity.Type.CorrespondenceEnds.class);
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
                                            case "teams" -> new Activity.Type.Teams(((YayArray) node).value().stream().map(y -> mapper.fromYayTree(y, Activity.Team.class)).toList());
                                            case "posts" -> new Activity.Type.Posts(((YayArray) node).value().stream().map(y -> mapper.fromYayTree(y, Activity.Topic.class)).toList());

                                            default -> new Activity.Type.Unknown(activityType, yo.value().get(activityType).toString());
                                        };

                                        // :
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
                });



    }
}
