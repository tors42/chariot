package chariot.internal;

import java.lang.reflect.Array;
import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import chariot.internal.yayson.*;
import chariot.internal.yayson.Parser.*;
import chariot.model.*;
import chariot.model.UserData.*;
import static chariot.model.UserData.UserPropertyEnum.name;
import static chariot.model.UserData.UserPropertyEnum.*;
import static chariot.model.UserData.ProfilePropertyEnum.*;
import static chariot.model.UserData.StreamerInfoPropertyEnum.*;

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
        mapper.setMappings(PerformanceStatistics.DateResult.class, ModelMapperUtil.intMapping());
        mapper.setMappings(Game.class,                     ModelMapperUtil.createdAtAndLastMoveAtMapping());
        mapper.setMappings(MoveInfo.GameSummary.class,     ModelMapperUtil.createdAtMapping());
        mapper.setMappings(GameStateEvent.Full.class,      ModelMapperUtil.createdAtMapping());
        mapper.setMappings(UserCount.class,                ModelMapperUtil.importMapping());
        mapper.setMappings(TVChannels.class,               ModelMapperUtil.tvChannelsMapping());
        mapper.setMappings(Variant.class,                  ModelMapperUtil.shortMapping());
        mapper.setMappings(Broadcast.Round.class,          ModelMapperUtil.startsAtMapping());
        mapper.setMappings(ChallengeAI.class,              ModelMapperUtil.createdAtAndLastMoveAtMapping());

        // "Exotic" JSON model...
        mappings.put(Crosstable.class, json -> {
            var helper = (Function<YayNode, Crosstable.Results>) node -> {
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
        mappings.put(ChallengeResult.class, json -> {
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
        mappings.put(Tournament.class, json -> {
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

        mappings.put(ChallengeTokens.class, json -> {
            var node = Parser.fromString(json);

            if (node instanceof YayObject yo) {
                var map = yo.value().keySet().stream()
                    .collect(Collectors.toUnmodifiableMap(
                                k -> k,
                                k -> yo.getString(k)
                                )
                            );
                return new ChallengeTokens(map);
            }

            return null;
        });

        mappings.put(TokenBulkResult.class, json -> {
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

        Function<YayNode, ?> userDataMapper = yayNode -> {

            var data = new UserData(null);

            if (! (yayNode instanceof YayObject yo)) return data;

            var mappedUserProperties = yo.value().entrySet().stream()
                .map(entry -> switch(entry.getKey()) {
                    case "id"           -> id.of(yo.getString(entry.getKey()));
                    case "username"     -> username.of(yo.getString(entry.getKey()));
                    case "name"         -> name.of(yo.getString(entry.getKey()));
                    case "title"        -> title.of(yo.getString(entry.getKey()));
                    case "patron"       -> patron.of(yo.getBool(entry.getKey()));
                    case "online"       -> online.of(yo.getBool(entry.getKey()));
                    case "streaming"    -> streaming.of(yo.getBool(entry.getKey()));
                    case "playing"      -> entry.getValue() instanceof YayBool bool
                            ? playing.of(bool.value())
                            : playingUrl.of(URI.create(yo.getString(entry.getKey())));
                    case "playingID"    -> playingGameId.of(yo.getString(entry.getKey()));
                    case "tosViolation" -> tosViolation.of(yo.getBool(entry.getKey()));
                    case "disabled"     -> disabled.of(yo.getBool(entry.getKey()));
                    case "closed"       -> closed.of(yo.getBool(entry.getKey()));
                    case "verified"     -> verified.of(yo.getBool(entry.getKey()));
                    case "createdAt"    -> createdAt.of(Util.fromLong(yo.getLong(entry.getKey())));
                    case "seenAt"       -> seenAt.of(Util.fromLong(yo.getLong(entry.getKey())));
                    case "joinedTeamAt" -> joinedTeamAt.of(Util.fromLong(yo.getLong(entry.getKey())));
                    case "url"          -> url.of(URI.create(yo.getString(entry.getKey())));
                    case "followable"   -> followable.of(yo.getBool(entry.getKey()));
                    case "following"    -> following.of(yo.getBool(entry.getKey()));
                    case "blocking"     -> blocking.of(yo.getBool(entry.getKey()));
                    case "followsYou"   -> followsYou.of(yo.getBool(entry.getKey()));
                    case "trophies"     -> trophies.of(new Trophies(entry.getValue() instanceof YayArray yarr
                            ? yarr.value().stream().map(trophyNode -> mapper.fromYayTree(trophyNode, Trophy.class)).toList()
                            : List.of()));
                    case "playTime"     -> playTime.of(new PlayTime(
                                            Duration.ofSeconds(entry.getValue() instanceof YayObject playYo
                                                ? playYo.getLong("total") : 0),
                                            Duration.ofSeconds(entry.getValue() instanceof YayObject playYo
                                                ? playYo.getLong("tv") : 0)));
                    case "count"        -> entry.getValue() instanceof YayObject countYo
                            ? counts.of(mapper.fromYayTree(countYo, UserCount.class))
                            : UserPropertyEnum.unmapped.of(entry);
                    case "perfs" -> entry.getValue() instanceof YayObject perfsYo
                            ? ratings.of(new Ratings(perfsYo.value().entrySet().stream().collect(Collectors.toUnmodifiableMap(
                                                     yoMapEntry -> StatsPerfType.valueOf(yoMapEntry.getKey()),
                                                     yoMapEntry -> mapper.fromYayTree(yoMapEntry.getValue(), StatsPerf.class)))))
                            : UserPropertyEnum.unmapped.of(entry);
                    case "profile" -> {
                        if (! (entry.getValue() instanceof YayObject profileYo)) yield UserPropertyEnum.unmapped.of(entry);
                        var map = profileYo.value().entrySet().stream()
                            .map(profileEntry -> switch(profileEntry.getKey()) {
                                case "country"    -> country.of(profileYo.getString(profileEntry.getKey()));
                                case "location"   -> location.of(profileYo.getString(profileEntry.getKey()));
                                case "bio"        -> bio.of(profileYo.getString(profileEntry.getKey()));
                                case "firstName"  -> firstName.of(profileYo.getString(profileEntry.getKey()));
                                case "lastName"   -> lastName.of(profileYo.getString(profileEntry.getKey()));
                                case "links"      -> links.of(profileYo.getString(profileEntry.getKey()));
                                case "fideRating" -> ratingFide.of(profileYo.getInteger(profileEntry.getKey()));
                                case "uscfRating" -> ratingUscf.of(profileYo.getInteger(profileEntry.getKey()));
                                case "ecfRating"  -> ratingEcf.of(profileYo.getInteger(profileEntry.getKey()));
                                case "rcfRating"  -> ratingRcf.of(profileYo.getInteger(profileEntry.getKey()));
                                case "cfcRating"  -> ratingCfc.of(profileYo.getInteger(profileEntry.getKey()));
                                case "dsbRating"  -> ratingDsb.of(profileYo.getInteger(profileEntry.getKey()));
                                default           -> ProfilePropertyEnum.unmapped.of(profileEntry);
                            })
                            .collect(Collectors.toUnmodifiableMap(Property::key, Property::value));
                        yield profile.of(new Provided(map.isEmpty() ? Map.of() : new EnumMap<>(map)));
                    }
                    case "stream" -> entry.getValue() instanceof YayObject streamYo
                        ? streamInfo.of(mapper.fromYayTree(streamYo, UserData.StreamInfo.class))
                        : UserPropertyEnum.unmapped.of(entry);
                    case "streamer" -> {
                        if (! (entry.getValue() instanceof YayObject streamerYo)) yield UserPropertyEnum.unmapped.of(entry);
                        var map = streamerYo.value().entrySet().stream()
                            .map(streamerEntry -> switch(streamerEntry.getKey()) {
                                case "name"        -> StreamerInfoPropertyEnum.name.of(streamerYo.getString(streamerEntry.getKey()));
                                case "headline"    -> headline.of(streamerYo.getString(streamerEntry.getKey()));
                                case "description" -> description.of(streamerYo.getString(streamerEntry.getKey()));
                                case "twitch"      -> twitch.of(streamerYo.getString(streamerEntry.getKey()));
                                case "youTube"     -> youtube.of(streamerYo.getString(streamerEntry.getKey()));
                                case "image"       -> image.of(streamerYo.getString(streamerEntry.getKey()));
                                default            -> StreamerInfoPropertyEnum.unmapped.of(streamerEntry);
                            })
                            .collect(Collectors.toUnmodifiableMap(Property::key, Property::value));
                         yield streamerInfo.of(new StreamerInfo(map));
                    }
                    default -> UserPropertyEnum.unmapped.of(entry);
                })
            .toList();

            var allUnmapped = mappedUserProperties.stream()
                .filter(p -> p.key() == UserPropertyEnum.unmapped)
                .toList();

            if (! allUnmapped.isEmpty()) {
                // Merge all unmapped value into single unmapped List(values)
                mappedUserProperties = Stream.concat(mappedUserProperties.stream()
                        .filter(p -> ! p.key().equals(UserPropertyEnum.unmapped)),
                        Stream.of(UserPropertyEnum.unmapped.of(allUnmapped.stream()
                                .map(Property::value).toList()))
                        )
                    .toList();
            }

            var map = mappedUserProperties.stream().collect(Collectors.toUnmodifiableMap(Property::key, Property::value));
            data = data.withProperties(new EnumMap<>(map));
            return data;
        };

        mapper.setCustomMapper(UserData.class, userDataMapper);
        mappings.put(UserData.class, json -> {
            var node = Parser.fromString(json);
            var userData = userDataMapper.apply(node);
            return userData;
        });
        mappingsArr.put(UserData.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userDataMapper).toArray(UserData[]::new);

            }
            return null;
        });

        Function<YayNode, UserStatus> userStatusMapper = node -> {
            var userData = mapper.fromYayTree(node, UserData.class);
            return userData.toUserStatus();
        };
        mapper.setCustomMapper(UserStatus.class, userStatusMapper);

        mappings.put(UserStatus.class, json -> {
            var node = Parser.fromString(json);
            return userStatusMapper.apply(node);
        });

        Function<YayNode, LiveStreamer> streamerStatusMapper = node -> {
            var userData = mapper.fromYayTree(node, UserData.class);
            return userData.toLiveStreamer();
        };
        mapper.setCustomMapper(LiveStreamer.class, streamerStatusMapper);

        mappings.put(LiveStreamer.class, json -> {
            var node = Parser.fromString(json);
            return streamerStatusMapper.apply(node);
        });

        Function<YayNode, TVFeedEvent.PlayerInfo> playerInfoMapper = node -> {
            if (node instanceof YayObject yo) {
                var userData = mapper.fromYayTree(yo.value().get("user"), UserData.class);
                UserCommon common = userData.toCommon();
                chariot.model.Enums.Color color = chariot.model.Enums.Color.valueOf(yo.getString("color"));
                var rating = yo.getNumber("rating").intValue();
                var seconds = yo.getNumber("seconds").intValue();
                var pi = new TVFeedEvent.PlayerInfo(common, color, rating, seconds);
                return pi;
            }
            return null;
        };
        mapper.setCustomMapper(TVFeedEvent.PlayerInfo.class, playerInfoMapper);

        mappings.put(TVFeedEvent.PlayerInfo.class, json -> {
            var node = Parser.fromString(json);
            return playerInfoMapper.apply(node);
        });

        /////
        Function<YayNode, Player> gamePlayerMapper = node -> {
            if (node instanceof YayObject yo) {
                Player player = null;
                if (yo.value().containsKey("aiLevel")) {
                    player = new Player.AI(yo.getNumber("aiLevel").intValue());
                } else if (! yo.value().containsKey("user")) {
                    player = new Player.Anonymous();
                } else if (yo.value().get("user") instanceof YayObject userObject) {
                    var userData = mapper.fromYayTree(userObject, UserData.class);
                    UserCommon common = userData.toCommon();

                    // rating
                    // provisional
                    // Opt berserk
                    // Opt team
                    int rating = yo.getNumber("rating").intValue();
                    boolean provisional = yo.getBool("provisional");

                    player = new Player.Account(common, rating, provisional);

                    Optional<Boolean> berserk = yo.value().containsKey("berserk")
                        ? Optional.of(yo.getBool("berserk"))
                        : Optional.empty();
                    Optional<String> team = yo.value().containsKey("team")
                        ? Optional.of(yo.getString("team"))
                        : Optional.empty();

                    if (berserk.isPresent() || team.isPresent()) {
                        player = new Player.AccountArena(player, berserk, team);
                    }
                }

                if (yo.value().containsKey("ratingDiff")) {
                    player = new Player.AccountDiff(player, yo.getNumber("ratingDiff").intValue());
                }

                if (yo.value().get("analysis") instanceof YayObject analysisObject) {
                    var basic = new Player.Base(
                                analysisObject.getNumber("inaccuracy").intValue(),
                                analysisObject.getNumber("mistake").intValue(),
                                analysisObject.getNumber("blunder").intValue(),
                                analysisObject.getNumber("acpl").intValue());

                    Player.Analysis analysis = basic;
                    if (analysisObject.value().containsKey("accuracy")) {
                        analysis = new Player.Accuracy(basic, analysisObject.getNumber("accuracy").intValue());
                    }

                    player = new Player.Analyzed(player, analysis);
                }
                return player;
            }
            return null;
        };
        mapper.setCustomMapper(Player.class, gamePlayerMapper);

        mappings.put(Player.class, json -> {
            var node = Parser.fromString(json);
            return gamePlayerMapper.apply(node);
        });


        Function<YayNode, UserCommon> userCommonMapper = node -> {
            var userData = mapper.fromYayTree(node, UserData.class);
            var common = userData.toCommon();
            return common;
        };
        mapper.setCustomMapper(UserCommon.class, userCommonMapper);

        mappings.put(UserCommon.class, json -> {
            var node = Parser.fromString(json);
            return userCommonMapper.apply(node);
        });

        mappingsArr.put(UserStatus.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userStatusMapper).toArray(UserStatus[]::new);
            }
            return null;
        });
        mappingsArr.put(LiveStreamer.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(streamerStatusMapper).toArray(LiveStreamer[]::new);
            }
            return streamerStatusMapper.apply(node);
        });
        mappingsArr.put(UserCommon.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userCommonMapper).toArray(UserCommon[]::new);
            }
            return userCommonMapper.apply(node);
        });


        mappingsArr.put(RatingHistory.class, json -> {
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

        mappingsArr.put(Activity.class, json -> {
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
                                    case "patron" -> mapper.fromYayTree(node, Activity.Type.Patron.class);
                                    case "posts" -> new Activity.Type.Posts(((YayArray) node).value().stream().map(y -> mapper.fromYayTree(y, Activity.Topic.class)).toList());
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
        });

    }
}
