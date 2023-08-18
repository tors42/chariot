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
import chariot.model.Enums.*;
import chariot.model.GameStateEvent.Side;
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
        mapper.setMappings(UserCount.class,                ModelMapperUtil.importMapping());
        mapper.setMappings(Variant.class,                  ModelMapperUtil.shortMapping());

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

        Function<YayNode, GameType> gameTypeMapper = node -> {

            if (! (node instanceof YayObject yo)) return null;

            boolean rated = yo.getBool("rated");
            Speed speed = Speed.valueOf(yo.getString("speed"));

            TimeControl timeControl = null;

            if (yo.value().get("timeControl") instanceof YayObject timeYo) {
                timeControl = switch(timeYo.getString("type")) {
                    case "unlimited"      -> new Unlimited();
                    case "correspondence" -> new Correspondence(timeYo.getInteger("daysPerTurn"));
                    case "clock"          -> new RealTime(
                                                    Duration.ofSeconds(timeYo.getInteger("limit")),
                                                    Duration.ofSeconds(timeYo.getInteger("increment")),
                                                    timeYo.getString("show"),
                                                    speed);
                    default -> null;
                };
            } else {
                if (yo.value().get("clock") instanceof YayObject millisClockYo) {
                    var initial = Duration.ofMillis(millisClockYo.getLong("initial"));
                    var increment = Duration.ofMillis(millisClockYo.getLong("increment"));
                    timeControl = new RealTime(
                                         initial,
                                         increment,
                                         "%d+%d".formatted(initial.toMinutes(), increment.toSeconds()),
                                         speed);
                } else if (yo.value().get("daysPerTurn") instanceof YayNumber yayNum) {
                    timeControl = new Correspondence(yayNum.value().intValue());
                } else {
                    timeControl = new Unlimited(); // guess.
                }
            }

            VariantType variantType = null;
            if (yo.value().get("variant") instanceof YayObject varYo) {
                String key = varYo.getString("key");
                variantType = switch(key) {
                    case "chess960"     -> new VariantType.Chess960(Opt.of(yo.getString("initialFen")));
                    case "fromPosition" -> new VariantType.FromPosition(Opt.of(yo.getString("initialFen")));
                    default             -> VariantType.Variant.valueOf(key);
                };
            }

            var gameType = new GameType(rated, variantType, timeControl);
            return gameType;
        };

        Function<YayNode, ChallengeInfo> challengeInfoMapper = node -> {

            if (! (node instanceof YayObject challengeYo)) return null;

            String id = challengeYo.getString("id");
            URI url = URI.create(challengeYo.getString("url"));

            ChallengeInfo.Players players = new ChallengeInfo.OpenEnded();

            if (challengeYo.value().get("challenger") instanceof YayObject challengerYo ) {
                String challengerId = challengerYo.getString("id");
                String challengerName = challengerYo.getString("name");
                String challengerTitle = challengerYo.getString("title");
                var challengerInfo = challengerTitle == null
                    ? new IdName(challengerName, challengerName)
                    : new IdNameTitle(challengerId, challengerName, challengerTitle);
                var challenger = new ChallengeInfo.Player(
                        challengerInfo,
                        challengerYo.getNumber("rating").intValue(),
                        challengerYo.getBool("provisional"),
                        challengerYo.getBool("online"));

                players = new ChallengeInfo.From(challenger);

                if (challengeYo.value().get("destUser") instanceof YayObject challengedYo) {
                    String challengedId = challengedYo.getString("id");
                    String challengedName = challengedYo.getString("name");
                    String challengedTitle = challengedYo.getString("title");
                    var challengedInfo = challengedTitle == null
                        ? new IdName(challengedName, challengedName)
                        : new IdNameTitle(challengedId, challengedName, challengedTitle);
                    var challenged = new ChallengeInfo.Player(
                            challengedInfo,
                            challengedYo.getNumber("rating").intValue(),
                            challengedYo.getBool("provisional"),
                            challengedYo.getBool("online"));

                    players = new ChallengeInfo.FromTo(challenger, challenged);
                }
            }

            GameType gameType = gameTypeMapper.apply(challengeYo);

            ChallengeInfo.ColorInfo colorInfo = new ChallengeInfo.ColorRequest(ColorPref.valueOf(challengeYo.getString("color")));

            String colorOutcome = challengeYo.getString("finalColor");
            if (colorOutcome != null) {
                colorInfo = new ChallengeInfo.ColorOutcome(colorInfo.request(), Color.valueOf(colorOutcome));
            }

            var challengeInfo = new ChallengeInfo(id, url, players, gameType, colorInfo);
            return challengeInfo;
        };

        mappings.put(ChallengeInfo.class, json -> challengeInfoMapper.apply(Parser.fromString(json)));

        mappings.put(ChallengeOpenEnded.class, json -> {
            var node = Parser.fromString(json);
            ChallengeOpenEnded openEnded = null;
            if (! (node instanceof YayObject yo)) return openEnded;
            if (! (yo.value().get("challenge") instanceof YayObject challengeYo)) return openEnded;

            var challengeInfo = challengeInfoMapper.apply(challengeYo);
            String urlWhite = yo.getString("urlWhite");
            String urlBlack = yo.getString("urlBlack");

            ChallengeOpenEnded.Players players = new ChallengeOpenEnded.Any();
            if (challengeYo.value().get("open") instanceof YayObject openYo && openYo.value().get("userIds") instanceof YayArray yarr) {
                List<String> twoUserIds = yarr.value().stream()
                    .filter(YayString.class::isInstance)
                    .map(YayString.class::cast)
                    .map(YayString::value)
                    .toList();
                if (twoUserIds.size() == 2) {
                    players = new ChallengeOpenEnded.Reserved(twoUserIds.get(0), twoUserIds.get(1));
                }
            }
            List<String> rules = List.of();
            if (challengeYo.value().get("rules") instanceof YayArray rulesYarr) {
                rules = rulesYarr.value().stream()
                    .filter(YayString.class::isInstance)
                    .map(YayString.class::cast)
                    .map(YayString::value)
                    .toList();
            }
            openEnded = new ChallengeOpenEnded(challengeInfo, urlWhite, urlBlack, players, rules);
            return openEnded;
        });

        Function<YayNode, Challenge> challengeMapper = node -> {
            Challenge challenge = null;
            if (! (node instanceof YayObject yo)) return challenge;

            ///
            // keepAliveStream responds with challenge info json on first line,
            // and then waits for the opponent to accept or decline the challenge,
            // before writing a {"done":"accepted"} or {"done":"declined"} line.
            // In case the json here is the "done"-object,
            // it means the keepAliveStream has finished so we can return
            // a result to the user.
            // If the challenge wasn't "accepted", we generate
            // a DeclinedChallenge here, which will be assembled with a previous first line
            // json challenge info - and if it was "accepted", we send a null sentinel value
            // to indicate that the previous first line json challenge info can be used as
            // response to the user.
            String done = yo.getString("done");
            if (done != null) {
                return done.equals("accepted")
                    ? null // sentinel for "accepted"
                    : new Challenge.DeclinedChallenge("generic", done, null);
            }
            //
            ///

            // PendingChallenges for instance,
            // uses top level challenge,
            // i.e { "id":"...", "speed", ... },
            // as opposed to a wrapped challenge,
            // i.e "{ "challenge": { "id":"...", "speed", ...}, ... }"
            YayObject challengeYo = yo;

            if (yo.value().get("challenge") instanceof YayObject wrappedYo) {
                challengeYo = wrappedYo;
            }

            ChallengeInfo challengeInfo = challengeInfoMapper.apply(challengeYo);
            challenge = challengeInfo;

            if (challengeYo.value().get("rules") instanceof YayArray rulesYarr) {
                var rules = rulesYarr.value().stream()
                    .filter(YayString.class::isInstance)
                    .map(YayString.class::cast)
                    .map(YayString::value)
                    .toList();
                challenge = new Challenge.ChallengeWithRules(rules, challenge);
            }

            String rematchOf = challengeYo.getString("rematchOf");
            if (rematchOf != null) {
                challenge = new Challenge.RematchChallenge(rematchOf, challenge);
            }

            String declineKey = challengeYo.getString("declineReasonKey");
            String declineReason = challengeYo.getString("declineReason");
            if (declineKey != null) {
                challenge = new Challenge.DeclinedChallenge(declineKey, declineReason, challenge);
            }

            return challenge;
        };

        mappings.put(Challenge.class, json -> challengeMapper.apply(Parser.fromString(json)));

        mapper.setCustomMapper(Challenge.class, challengeMapper);

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
                .filter(entry -> ! (entry.getValue() instanceof YayNull)) // Skip null-entries from Lichess
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
                    case "playingId"    -> playingGameId.of(yo.getString(entry.getKey()));
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
                         yield streamerInfo.of(new UserData.StreamerInfo(map));
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
        mappings.put(UserData.class, json -> userDataMapper.apply(Parser.fromString(json)));

        mappingsArr.put(UserData.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userDataMapper).toArray(UserData[]::new);

            }
            return null;
        });

        Function<YayNode, UserStatus> userStatusMapper = node -> mapper.fromYayTree(node, UserData.class).toUserStatus();
        Function<YayNode, LiveStreamer> streamerStatusMapper = node -> mapper.fromYayTree(node, UserData.class).toLiveStreamer();

        mapper.setCustomMapper(UserStatus.class, userStatusMapper);
        mapper.setCustomMapper(LiveStreamer.class, streamerStatusMapper);

        mappings.put(UserStatus.class, json -> userStatusMapper.apply(Parser.fromString(json)));
        mappings.put(LiveStreamer.class, json -> streamerStatusMapper.apply(Parser.fromString(json)));

        Function<YayNode, TVFeedEvent.Featured> tvFeedEventFeaturedMapper = node -> {
            if (! (node instanceof YayObject yo)) return null;
            String id = yo.getString("id");
            Color orientation = Color.valueOf(yo.getString("orientation"));
            List<TVFeedEvent.PlayerInfo> players = yo.value().get("players") instanceof YayArray yarr
                ? yarr.value().stream()
                    .filter(YayObject.class::isInstance)
                    .map(YayObject.class::cast)
                    .map(playerYo -> {
                        if (! (playerYo.value().get("user") instanceof YayObject userYo)) return null;
                        UserInfo userInfo = UserInfo.of(
                                userYo.getString("id"),
                                userYo.getString("name"),
                                userYo.getString("title"));
                        Color color = Color.valueOf(playerYo.getString("color"));
                        var rating = playerYo.getInteger("rating");
                        var seconds = Duration.ofSeconds(playerYo.getInteger("seconds"));
                        var pi = new TVFeedEvent.PlayerInfo(userInfo, color, rating, seconds);
                        return pi;
                    }).toList()
                : List.of();
            String fen = yo.getString("fen");
            return new TVFeedEvent.Featured(id, orientation, players, fen);
        };
        Function<YayNode, TVFeedEvent.Fen> tvFeedEventFenMapper = node -> {
            if (! (node instanceof YayObject yo)) return null;
            var fen = yo.getString("fen");
            var lastMove = yo.getString("lm");
            var whiteTime = Duration.ofSeconds(yo.getLong("wc"));
            var blackTime = Duration.ofSeconds(yo.getLong("bc"));
            return new TVFeedEvent.Fen(fen, lastMove, whiteTime, blackTime);
        };
        Function<YayNode, TVFeedEvent> tvFeedEventMapper = node -> {
            TVFeedEvent event = null;
            if (! (node instanceof YayObject nodeYo)) return event;
            var data = nodeYo.value().get("d");
            event = switch(nodeYo.getString("t")) {
                case "featured" -> tvFeedEventFeaturedMapper.apply(data);
                case "fen" -> tvFeedEventFenMapper.apply(data);
                default -> null;
            };
            return event;
        };
        mapper.setCustomMapper(TVFeedEvent.class, tvFeedEventMapper);
        mappings.put(TVFeedEvent.class, json -> tvFeedEventMapper.apply(Parser.fromString(json)));

        /////
        Function<YayNode, Player> gamePlayerMapper = node -> {
            if (node instanceof YayObject yo) {
                Player player = null;
                if (yo.value().containsKey("aiLevel")) {
                    player = new AI(yo.getInteger("aiLevel"));
                } else if (! yo.value().containsKey("user")) {
                    player = new Anonymous();
                } else if (yo.value().get("user") instanceof YayObject userObject) {
                    var userData = mapper.fromYayTree(userObject, UserData.class);
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
                    player = new Player.Analyzed(player, analysis);
                }
                return player;
            }
            return null;
        };
        mapper.setCustomMapper(Player.class, gamePlayerMapper);

        mappings.put(Player.class, json -> gamePlayerMapper.apply(Parser.fromString(json)));

        Function<YayNode, UserCommon> userCommonMapper = node -> mapper.fromYayTree(node, UserData.class).toCommon();

        mapper.setCustomMapper(UserCommon.class, userCommonMapper);

        mappings.put(UserCommon.class, json -> userCommonMapper.apply(Parser.fromString(json)));

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

        Function<YayNode, GameInfo>  gameInfoMapper = node -> {
            if (! (node instanceof YayObject gameYo)) return null;
            var gameId = gameYo.getString("gameId");
            var fullId = gameYo.getString("fullId");
            var fen = gameYo.getString("fen");
            var color = Color.valueOf(gameYo.getString("color"));
            String lm = gameYo.getString("lastMove");
            Opt<String> lastMove = (lm != null && ! lm.isBlank()) ? Opt.of(lm) : Opt.empty();
            var status = Status.valueOf(((YayObject) gameYo.value().get("status")).getInteger("id"));

            VariantType variantType = null;
            if (gameYo.value().get("variant") instanceof YayObject varYo) {
                String key = varYo.getString("key");
                variantType = switch(key) {
                    case "chess960"     -> new VariantType.Chess960(Opt.of(gameYo.getString("initialFen")));
                    case "fromPosition" -> new VariantType.FromPosition(Opt.of(gameYo.getString("initialFen")));
                    default             -> VariantType.Variant.valueOf(key);
                };
            }

            var speed = Speed.valueOf(gameYo.getString("speed"));
            var timeInfo = new GameInfo.TimeInfo(speed, Opt.of(gameYo.getInteger("secondsLeft")).map(Duration::ofSeconds));

            var rated = gameYo.getBool("rated");

            var hasMoved = gameYo.getBool("hasMoved");
            var isMyTurn = gameYo.getBool("isMyTurn");

            GameInfo.Opponent opponent = null;
            if (gameYo.value().get("opponent") instanceof YayObject oppYo) {
                Integer aiLevel = oppYo.getInteger("ai");
                String id = oppYo.getString("id");
                String username = oppYo.getString("username");
                if (aiLevel != null) {
                    opponent = username == null
                        ? new AI(aiLevel, "Level %d".formatted(aiLevel))
                        : new AI(aiLevel, username);
                } else if(id == null) {
                    opponent = new Anonymous();
                } else {
                    int rating = oppYo.getInteger("rating");
                    opponent = new GameInfo.Account(id, username, rating, Opt.of(oppYo.getInteger("ratingDiff")));
                }
            }
            var source = gameYo.getString("source");

            TournamentId tournamentInfo = null;
            var swissId = gameYo.getString("swissId");
            var arenaId = gameYo.getString("tournamentId");
            if (swissId != null) {
                tournamentInfo = TournamentId.swiss(swissId);
            } else if(arenaId != null) {
                tournamentInfo = TournamentId.arena(arenaId);
            }

            var gameInfo = new GameInfo(fullId, gameId, fen,
                    color, status, variantType, timeInfo,
                    rated, hasMoved, isMyTurn, opponent,
                    source, lastMove,
                    Opt.of(gameYo.getInteger("ratingDiff")), Opt.of(tournamentInfo));

            return gameInfo;
        };

        mapper.setCustomMapper(GameInfo.class, gameInfoMapper);

        mappings.put(GameInfo.class, json -> gameInfoMapper.apply(Parser.fromString(json)));

        Function<YayNode, Event> eventMapper = node -> {

            if (! (node instanceof YayObject yo)) return null;

            String eventType = yo.getString("type");
            Event event = switch(eventType) {

                case "gameStart",
                     "gameFinish" -> {

                    Event.GameEvent gameEvent = null;

                    if (! (yo.value().get("game") instanceof YayObject gameYo)) yield gameEvent;

                    var gameInfo = gameInfoMapper.apply(gameYo);


                    Event.Compat compat = null;
                    if (gameYo.value().get("compat") instanceof YayObject compatYo) {
                        compat = new Event.Compat(compatYo.getBool("bot"), compatYo.getBool("board"));
                    }

                    gameEvent = switch(eventType) {
                        case "gameStart"  -> new Event.GameStartEvent(gameInfo, compat);
                        case "gameFinish" -> {
                            Enums.Outcome outcome = gameInfo.status().status() > 25
                                ? Enums.Outcome.draw
                                : Enums.Outcome.none;
                            String winner = gameYo.getString("winner");
                            if (winner != null) {
                                outcome = Color.valueOf(winner) == gameInfo.color()
                                    ? Enums.Outcome.win
                                    : Enums.Outcome.loss;
                            }

                            yield new Event.GameStopEvent(gameInfo, outcome, compat);
                        }
                        default -> null;
                    };

                    yield gameEvent;
                }

                case "challenge",
                     "challengeCanceled",
                     "challengeDeclined" -> {

                    Event.ChallengeEvent challengeEvent = null;

                    if (! (yo.value().get("challenge") instanceof YayObject challengeYo)) yield challengeEvent;

                    ChallengeInfo challengeInfo = challengeInfoMapper.apply(challengeYo);

                    Event.Compat compat = null;

                    if (yo.value().get("compat") instanceof YayObject compatYo) {
                        compat = new Event.Compat(compatYo.getBool("bot"), compatYo.getBool("board"));
                    }

                    challengeEvent = switch(eventType) {
                        case "challenge"         -> new Event.ChallengeCreatedEvent(challengeInfo,
                                                        Opt.of(challengeYo.getString("rematchOf")), compat);
                        case "challengeCanceled" -> new Event.ChallengeCanceledEvent(challengeInfo);
                        case "challengeDeclined" -> new Event.ChallengeDeclinedEvent(challengeInfo,
                                                        new Event.DeclineReason(
                                                            challengeYo.getString("declineReasonKey"),
                                                            challengeYo.getString("declineReason")));
                        default -> null;
                    };

                    yield challengeEvent;
                }

                default -> null;
            };

            return event;
        };
        mapper.setCustomMapper(ChallengeInfo.class, challengeInfoMapper);

        mapper.setCustomMapper(Event.class, eventMapper);

        mappings.put(Event.class, json -> eventMapper.apply(Parser.fromString(json)));

        Function<YayNode, GameStateEvent.State> gameStateEventStateMapper = node -> {
            if (! (node instanceof YayObject stateYo)) return null;
            return new GameStateEvent.State(
                    stateYo.getString("moves"),
                    Duration.ofMillis(stateYo.getLong("wtime")),
                    Duration.ofMillis(stateYo.getLong("btime")),
                    Duration.ofMillis(stateYo.getLong("winc")),
                    Duration.ofMillis(stateYo.getLong("binc")),
                    Status.valueOf(stateYo.getString("status")),
                    Opt.of(stateYo.getString("winner")).map(Color::valueOf),
                    stateYo.getBool("wdraw")
                        ? Opt.of(Color.white)
                        : stateYo.getBool("bdraw")
                          ? Opt.of(Color.black)
                          : Opt.empty(),
                    stateYo.getBool("wtakeback")
                        ? Opt.of(Color.white)
                        : stateYo.getBool("btakeback")
                          ? Opt.of(Color.black)
                          : Opt.empty(),
                    Opt.of(stateYo.getString("rematch"))
                    );
        };

        Function<YayNode, Side> sideMapper = node -> {
            if (! (node instanceof YayObject yo)) return new Anonymous();
            Integer aiLevel = yo.getInteger("aiLevel");
            if (aiLevel != null) {
                return new AI(aiLevel);
            }
            String id = yo.getString("id");
            if (id == null) return new Anonymous();
            String name = yo.getString("name");
            String title = yo.getString("title");
            var userInfo = UserInfo.of(id, name, title);
            int rating = yo.getInteger("rating");
            boolean provisional = yo.getBool("provisional");
            return new GameStateEvent.Account(userInfo, rating, provisional);
        };

        Function<YayNode, GameStateEvent> gameStateEventMapper = node -> {
            if (! (node instanceof YayObject yo)) return null;
            String eventType = yo.getString("type");
            GameStateEvent event = switch(eventType) {
                case "gameFull" -> {
                    String id = yo.getString("id");
                    GameType gameType = gameTypeMapper.apply(yo);
                    ZonedDateTime createdAt = Util.fromLong(yo.getLong("createdAt"));
                    GameStateEvent.Side white = sideMapper.apply(yo.value().get("white"));
                    GameStateEvent.Side black = sideMapper.apply(yo.value().get("black"));
                    Opt<TournamentId> tournamentId = Opt.of(yo.getString("tournamentId")).map(TournamentId.ArenaId::new);
                    var state = gameStateEventStateMapper.apply(yo.value().get("state"));

                    yield new GameStateEvent.Full(id, gameType, createdAt, white, black, tournamentId, state);
                }
                case "gameState" -> gameStateEventStateMapper.apply(yo);
                case "chatLine" -> new GameStateEvent.Chat(yo.getString("username"), yo.getString("text"), yo.getString("room"));
                case "opponentGone" -> {
                    boolean gone = yo.getBool("gone");
                    GameStateEvent.Claim claim = new GameStateEvent.No();
                    Integer claimWinInSeconds = yo.getInteger("claimWinInSeconds");
                    if (claimWinInSeconds != null) {
                        claim = claimWinInSeconds <= 0
                            ? new GameStateEvent.Yes()
                            : new GameStateEvent.Soon(Duration.ofSeconds(claimWinInSeconds));
                    }
                    yield new GameStateEvent.OpponentGone(gone, claim);
                }
                default -> null;
            };
            return event;
        };

        mappings.put(GameStateEvent.class, json -> gameStateEventMapper.apply(Parser.fromString(json)));

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
