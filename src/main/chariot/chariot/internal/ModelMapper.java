package chariot.internal;

import java.lang.reflect.Array;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.ArrayList;
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
    private static final Map<Class<? extends Model>, Function<String, ?>> mappings;
    private static final Map<Class<? extends Model>, Function<String, ?>> mappingsArr;

    public static <T extends Model> Function<String, T> mapper(final Class<T> clazz) {
        return (Function<String, T>) mappings.get(clazz);
    }

    public static <T extends Model> Function<String, T[]> mapperArr(final Class<T> clazz) {
        return (Function<String, T[]>) mappingsArr.get(clazz);
    }


    static Function<Class<? extends Model>, Function<String, ?>> arr =
            (cls) -> (json) -> {
                var root = Parser.fromString(json);
                if (root instanceof YayArray ya) {
                    var list = ya.value().stream()
                        .map(e -> mapper.fromYayTree(e, cls))
                        .toList();
                    var array = Array.newInstance(cls, list.size());
                    for (int i = 0; i < list.size(); i++) {
                        Array.set(array, i, list.get(i));
                    }
                    return array;
                }
                return Model.unmapped(json);
            };



    static {

        // Populate the Model mappings with the generic json-to-java mapper
        mappings = Arrays.stream(Model.class.getPermittedSubclasses())
            .map(c -> (Class<? extends Model>) c)
            .collect(
                    Collectors.toMap(
                        c -> c,
                        c -> (json) -> mapper.fromString(json, c)
                        )
                    );

        mappingsArr = Arrays.stream(Model.class.getPermittedSubclasses())
            .map(c -> (Class<? extends Model>) c)
            .collect(
                    Collectors.toMap(
                        c -> c,
                        c -> arr.apply(c)
                        )
                    );


        // And that should be it,
        // the Mapper is finished! Neat!











        // ...
        // Uhm, what are you doing?
        // ...
        // Why are you continuing reading the source code?
        // It's finished.
        //










        // Ok ok ok,
        // you got me!
        // Some of the JSON models will be customly fitted into the Java model...

        // Some especially troublesome models needs handling...
        // (I'm looking at you Crosstable, RatingHistory and Activity - yeah...)
        //
        // Specify Java model <-> JSON model field name mappings,
        // where JSON model contains Java keywords,
        // so another name must be used in the Java model.
        //mapper.setMappings(Tournament.class,                  ModelMapperUtil.privateMapping());
        mapper.setMappings(PerfStat.Stat.DateResult.class,    ModelMapperUtil.intMapping());
        mapper.setMappings(Game.class,                        ModelMapperUtil.createdAtAndLastMoveAtMapping());
        mapper.setMappings(StreamMove.Info.class,             ModelMapperUtil.createdAtMapping());
        mapper.setMappings(StreamGameEvent.Full.class,        ModelMapperUtil.createdAtMapping());
        mapper.setMappings(TVChannels.class,                  ModelMapperUtil.tvChannelsMapping());
        mapper.setMappings(User.Count.class,           ModelMapperUtil.importMapping());
        mapper.setMappings(Variant.class,                     ModelMapperUtil.shortMapping());
        mapper.setMappings(Broadcast.Round.class,             ModelMapperUtil.startsAtMapping());
        mapper.setMappings(ChallengeResult.ChallengeAI.class, ModelMapperUtil.createdAtAndLastMoveAtMapping());


        // Helper for Sheet model
        Function<YayNode, Arena.Sheet> sheetMapper = (node) -> {
            if (node instanceof YayObject yo) {
                var scores = new ArrayList<Arena.Sheet.Score>();
                if (yo.value().get("scores") instanceof YayArray ya) {
                    ya.value().stream().forEach( s -> {
                        if (s instanceof YayArray innerArr) {
                            scores.add(new Arena.Sheet.Score.P(
                                        ((YayNumber)innerArr.value().get(0)).value().intValue(),
                                        ((YayNumber)innerArr.value().get(1)).value().intValue()
                                        ));
                        } else if (s instanceof YayNumber n) {
                            scores.add(new Arena.Sheet.Score.S(n.value().intValue()));
                        }
                    });
                }
                var total = yo.getNumber("total").intValue();
                var fire = yo.getBool("fire");

                // Sheet model
                return new Arena.Sheet(scores, total, fire);
            }
            return null;
        };

        mapper.setCustomMapper(Arena.Sheet.class, sheetMapper);

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
                    return Model.unmapped(json);
                });


        mappings.put(Err.class,
                (json) -> {
                    var node = Parser.fromString(json);
                    var err = mapper.fromYayTree(node, Err.class);
                    if (err instanceof Err.Error e && e.error() == null) {
                        return Err.error(json);
                    } else if (err instanceof Err.Failure f && (f.message() == null || f.message().isEmpty())) {
                        return Err.error(json);
                    }
                    return err;
                });


        // Some guidance for the correct challenge type to be modelled...
        mappings.put(ChallengeResult.class,
                (json) -> {
                    var node = Parser.fromString(json);
                    if (node instanceof YayObject yo) {
                        var outer = yo.value();
                        if (outer.get("urlWhite") != null) {
                            return mapper.fromYayTree(node, ChallengeResult.ChallengeOpenEnded.class);
                        } else {
                            if (outer.get("challenge") != null) {
                                return mapper.fromYayTree(node, ChallengeResult.ChallengeInfo.class);
                            } else {
                                return mapper.fromYayTree(node, ChallengeResult.ChallengeAI.class);
                            }
                        }
                    }
                    return Model.unmapped(json);
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
                    return Model.unmapped(json);
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

                    return Model.unmapped(json);
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
                                return Model.unmapped(json);
                            })
                        .toList();

                        var array = Array.newInstance(RatingHistory.class, list.size());
                        for (int i = 0; i < list.size(); i++) {
                            Array.set(array, i, list.get(i));
                        }
                        return array;
                    }
                    return Model.unmapped(json);
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
                                            } catch (Exception ex) {
                                            }
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

                                    var games = new Activity.Games(resultHelper.apply(yo.value().get("games")));

                                    Activity.Puzzles puzzles = null;
                                    var puzzlesNode = yo.value().get("puzzles");
                                    if (puzzlesNode != null) {
                                        puzzles = new Activity.Puzzles(resultHelper.apply(puzzlesNode).get(0));
                                    }

                                    var tournaments = mapper.fromYayTree(yo.value().get("tournaments"), Activity.Tournaments.class);

                                    var practiceArr = yo.value().get("practice");
                                    List<Activity.Practice> practice = null;
                                    if (practiceArr != null) {
                                        if (practiceArr instanceof YayArray pya) {
                                            practice = pya.value().stream()
                                                .map(y -> mapper.fromYayTree(y, Activity.Practice.class))
                                                .toList();
                                        }
                                    }

                                    var correspondenceMoves = mapper.fromYayTree(yo.value().get("correspondenceMoves"), Activity.CorrespondenceMoves.class);

                                    // Map firstly to get the List<Game> mapping performed,
                                    // and then manually get and fix the Result member of the record
                                    var correspondenceEnds = mapper.fromYayTree(yo.value().get("correspondenceEnds"), Activity.CorrespondenceEnds.class);
                                    if (yo.value().get("correspondenceEnds") instanceof YayObject correspondenceEndsNode) {
                                        correspondenceEnds = new Activity.CorrespondenceEnds(
                                                resultHelper.apply(correspondenceEndsNode).get(0),
                                                correspondenceEnds.games()
                                                );
                                    }

                                    var in = new ArrayList<String>();
                                    var out = new ArrayList<String>();
                                    if (yo.value().get("follows") instanceof YayObject yoFollows) {
                                        if (yoFollows.value().get("in") instanceof YayObject yoIn) {
                                            if (yoIn.value().get("ids") instanceof YayArray yarr) {
                                                in.addAll(yarr.value().stream().map(yn -> ((YayString) yn).value()).toList());
                                            }
                                        }
                                        if (yoFollows.value().get("out") instanceof YayObject yoOut) {
                                            if (yoOut.value().get("ids") instanceof YayArray yarr) {
                                                out.addAll(yarr.value().stream().map(yn -> ((YayString) yn).value()).toList());
                                            }
                                        }
                                    }
                                    var follows = new Activity.Follows(in, out);

                                    var teamsArr = yo.value().get("teams");
                                    List<Activity.Teams> teams = null;
                                    if (teamsArr != null) {
                                        if (teamsArr instanceof YayArray tya) {
                                            teams = tya.value().stream()
                                                .map(y -> mapper.fromYayTree(y, Activity.Teams.class))
                                                .toList();
                                        }
                                    }

                                    var postsArr = yo.value().get("posts");
                                    List<Activity.Posts> posts = null;
                                    if (postsArr != null) {
                                        if (postsArr instanceof YayArray pya) {
                                            posts = pya.value().stream()
                                                .map(y -> mapper.fromYayTree(y, Activity.Posts.class))
                                                .toList();
                                        }
                                    }

                                    // Activity model ... sigh
                                    var activity = new Activity(
                                            interval,
                                            games,
                                            puzzles,
                                            tournaments,
                                            practice,
                                            correspondenceMoves,
                                            correspondenceEnds,
                                            follows,
                                            teams,
                                            posts
                                            );

                                    return activity;
                                }
                                //return null;
                                return Model.unmapped(json);
                            }
                        ).toList();
                        var array = Array.newInstance(Activity.class, list.size());
                        for (int i = 0; i < list.size(); i++) {
                            Array.set(array, i, list.get(i));
                        }
                        return array;
                    }
                    return Model.unmapped(json);
                });

    }
}
