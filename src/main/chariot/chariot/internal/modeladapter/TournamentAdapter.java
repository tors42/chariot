package chariot.internal.modeladapter;

import java.net.URI;
import java.time.*;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;
import chariot.model.Clock;
import chariot.model.TourInfo.Speed;
import chariot.model.TourInfo.Status;

public interface TournamentAdapter {

    static ZonedDateTime nodeToZonedDateTime(YayNode node) {
        return switch(node) {
            case YayString(String date)
                -> ZonedDateTime.parse(date).withZoneSameInstant(ZoneId.systemDefault());
            case YayNumber(Number date)
                -> Instant.ofEpochMilli(date.longValue()).atZone(ZoneId.systemDefault());
            default -> null;
        };
    }

    static Clock nodeToClock(YayNode node) {
        return switch(node) {
            case YayObject(var map) -> {
                if (map.get("limit") instanceof YayNumber(var limit)
                    && map.get("increment") instanceof YayNumber(var increment)) {
                    yield Clock.ofSeconds(limit.intValue()).withIncrementSeconds(increment.intValue());
                }
                System.err.println("Failed to parse clock: " + map);
                yield null;
            }
            default -> null;
        };
    }

    record ABox(Arena.Stats stats) {}
    static Opt<Arena.Stats> nodeToArenaStats(YayNode node, YayMapper mapper) {
        return switch(mapper.fromYayTree(node, ABox.class)) {
            case ABox(Arena.Stats stats) -> Opt.of(stats);
            case null, default           -> Opt.empty();
        };
    }
    record SBox(Swiss.Stats stats) {}
    static Opt<Swiss.Stats> nodeToSwissStats(YayNode node, YayMapper mapper) {
        return switch(mapper.fromYayTree(node, SBox.class)) {
            case SBox(Swiss.Stats stats) -> Opt.of(stats);
            case null, default           -> Opt.empty();
        };
    }

    static List<Condition> nodeToConditions(YayObject nodeYo, Speed speed) {
        return nodeYo.value().entrySet().stream()
            .map(entry -> switch(entry.getKey()) {
                case "minRatedGames" -> entry.getValue() instanceof YayObject yo
                                        && yo.getInteger("nb") instanceof Integer nb
                                        ? Opt.of(Condition.minRatedGames(nb, speed)) : Opt.<Condition>of();
                case "minRating"     -> entry.getValue() instanceof YayObject yo
                                        && yo.getInteger("rating") instanceof Integer rating
                                        ? Opt.of(Condition.minRating(rating, speed)) : Opt.<Condition>of();
                case "maxRating"     -> entry.getValue() instanceof YayObject yo
                                        && yo.getInteger("rating") instanceof Integer rating
                                        ? Opt.of(Condition.maxRating(rating, speed)) : Opt.<Condition>of();
                case "onlyTitled"    -> entry.getValue() instanceof YayBool(boolean titled) && titled
                                        ? Opt.of(Condition.titled()) : Opt.<Condition>of();
                case "allowList"     -> entry.getValue() instanceof YayArray yarr
                                        && yarr.filterCastMap(YayString::value, YayString.class) instanceof List<String> users
                                        ? users.isEmpty()
                                            ? Opt.of(Condition.allowListHidden())
                                            : Opt.of(Condition.allowList(users))
                                        : Opt.<Condition>of();
                case "teamMember"     -> entry.getValue() instanceof YayString(String teamId)
                                        ? Opt.of(Condition.member(teamId)) : Opt.<Condition>of();
                case "private"        -> entry.getValue() instanceof YayBool(boolean entryCode) && entryCode
                                        ? Opt.of(Condition.entryCode()) : Opt.<Condition>of();
                case "password"       -> entry.getValue() instanceof YayBool(boolean entryCode) && entryCode
                                        ? Opt.of(Condition.entryCode()) : Opt.<Condition>of();
                default -> Opt.<Condition>of();
            })
            .filter(cond -> cond instanceof Some<Condition>)
            .map(cond -> ((Some<Condition>) cond).value())
            .toList();
    }

    record Cond(String condition, String verdict) {}
    record Verd(List<Cond> list, boolean accepted) {}
    static ConditionInfo<Condition> nodeToConditionInfo(YayNode node, Speed speed) {
        if (! (node instanceof YayObject nodeYo)) return new ConditionInfo<>(List.of(), Map.of());

        List<Condition> conditions = nodeToConditions(nodeYo, speed);

        Map<Condition, String> verdictMap =
            YayMapper.mapper().fromYayTree(nodeYo.value().get("verdicts"), Verd.class) instanceof Verd verdict
            && ! verdict.list().isEmpty()
            ? verdict.list().stream()
                .collect(Collectors.toMap(cond -> parseEntryCondition(cond.condition()), Cond::verdict))
            : Map.of();

        Map<Condition, String> unmetVerdictMap = verdictMap.entrySet().stream()
            .filter(entry -> !"ok".equals(entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        // If an AllowList was parsed from outside the verdict object,
        // and the verdict object contains an AllowListHidden,
        // drop AllowListHidden and keep AllowList
        Set<Condition> allConditions = new HashSet<>(verdictMap.keySet());
        allConditions.addAll(conditions);
        if (allConditions.stream().anyMatch(c -> c.getClass().equals(Condition.AllowList.class))) {
            allConditions.remove(Condition.allowListHidden());
        }

        return new ConditionInfo<>(allConditions.stream()
                .sorted(Comparator.comparing(c -> c.getClass().getName()))
                .toList(), unmetVerdictMap);
    }

    static <T extends Condition> ConditionInfo<T> conditionInfoAs(Class<T> typeClass, ConditionInfo<Condition> conditionInfo) {
        List<T> list = conditionInfo.list().stream()
            .filter(typeClass::isInstance)
            .map(typeClass::cast)
            .toList();
        Map<T, String> unmet = conditionInfo.unmet().entrySet().stream()
            .filter(entry -> typeClass.isInstance(entry.getKey()))
            .map(entry -> Map.entry(typeClass.cast(entry.getKey()), entry.getValue()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return new ConditionInfo<>(list, unmet);
    }

    static Condition parseEntryCondition(String condition) {
        return switch(condition) {
            case "Fixed line-up"            -> Condition.allowListHidden();
            case "Play your games"          -> Condition.notMissedSwiss();
            case "Only titled players"      -> Condition.titled();
            case "Bot players are allowed." -> Condition.bots();
            case String s -> {
                if (s.startsWith("Rated ≤ ")
                        && s.indexOf(" for") != -1
                        && s.substring("Rated ≤ ".length(), s.indexOf(" for")).split(" ") instanceof String[] parts
                        && parts.length == 3) {
                    try {
                        int rating = Integer.parseInt(parts[0]);
                        if (Speed.fromString(parts[2]) instanceof Speed speed) {
                            yield Condition.maxRating(rating, speed);
                        }
                    } catch (NumberFormatException nfe) {}
                } else if (s.startsWith("Rated ≥ ")
                        && s.substring("Rated ≥ ".length()).split(" ") instanceof String[] parts
                        && parts.length == 3) {
                    try {
                        int rating = Integer.parseInt(parts[0]);
                        if (Speed.fromString(parts[2]) instanceof Speed speed) {
                            yield Condition.minRating(rating, speed);
                        }
                    } catch (NumberFormatException nfe) {}
                } else if (s.startsWith("Must be in team ")
                        && s.substring("Must be in team ".length()) instanceof String teamName) {
                    yield Condition.memberByTeamName(teamName);
                } else if (s.endsWith(" old account")
                        && s.split(" old account") instanceof String[] arr
                        && arr.length > 0
                        && arr[0].split(" ") instanceof String[] parts
                        && parts.length == 2) {
                    try {
                        int nb = Integer.parseInt(parts[0]);
                        if (parts[1].contains("day")) {
                            yield Condition.minAccountAge(Period.ofDays(nb));
                        } else if (parts[1].contains("month")) {
                            yield Condition.minAccountAge(Period.ofMonths(nb));
                        } else if (parts[1].contains("year")) {
                            yield Condition.minAccountAge(Period.ofYears(nb));
                        }
                    } catch (NumberFormatException nfe) {}
                } else if (s.endsWith(" rated games")
                        && s.split(" rated games") instanceof String[] arr
                        && arr.length == 1
                        && arr[0].split(" ") instanceof String[] parts
                        && parts.length == 3) {
                    try {
                        int games = Integer.parseInt(parts[1]);
                        if (Speed.fromString(parts[2]) instanceof Speed speed) {
                            yield Condition.minRatedGames(games, speed);
                        }
                    } catch (NumberFormatException nfe) {}
                }

                yield Condition.generic(s);
            }
        };
    }

    static TourInfo nodeToTourInfo(YayNode node) {
        return switch(node) {
            case YayObject yo -> {
                String id              = yo.getString("id");
                String by              = yo.getString("createdBy");
                ZonedDateTime startsAt = nodeToZonedDateTime(yo.value().get("startsAt"));
                String name            = yo.getString("name") instanceof String nameStr
                                             ? nameStr
                                             : yo.getString("fullName");
                Clock clock            = nodeToClock(yo.value().get("clock"));
                Speed speed            = yo.getString("speed") instanceof String str
                                             ? Speed.fromString(str)
                                             : Speed.fromClock(clock);
                Variant variant        = parentNodeToVariant(yo);
                int nbPlayers          = yo.getInteger("nbPlayers") instanceof Integer num
                                             ? num.intValue()
                                             : 0;
                boolean rated          = yo.getBool("rated");
                Opt<String> optDescr      = yo.getString("description") instanceof String description
                                             ? Opt.of(description)
                                             : Opt.of();

                Status status = switch(yo.value().get("status")) {
                    case YayString(String string) -> Status.valueOf(string);
                    case YayNumber(Number number) -> Status.valueOf(number.intValue());
                    case null, default            -> yo.getBool("isFinished")
                                                     ? Status.finished
                                                     : Status.created;
                };
                Opt<TourInfo.Freq> optFreq    = yo.value().get("schedule") instanceof YayObject scheduleYo
                                    && scheduleYo.getString("freq") instanceof String freq
                                    ? Opt.of(TourInfo.Freq.fromString(freq)) : Opt.of();

                yield new TourInfo(id, by, startsAt, name, clock, speed, variant, nbPlayers, rated, status,
                        optDescr, optFreq);
            }
            default -> null;
        };
    }

    /**
     * {
     *   "variant": "standard"
     * }
     *  -> Variant.standard
     *
     * {
     *   "variant": {
     *      "key": "standard",
     *      "short": "Std",
     *      "name": "Standard"
     *   }
     * }
     *  -> Variant.standard
     *
     *
     * {
     *   "variant": "standard"
     *   "fen": "rnb..."
     * } ?
     *  -> Variant.FromPosition("rnb...")
     *
     * {
     *   "variant": "standard"
     *   "postition": {
     *      "name": "Custom Position",
     *      "fen": "rnb..."
     * } ?
     *  -> Variant.FromPosition("rnb...", "Custom Position")
     */

    static Variant parentNodeToVariant(YayObject parentYo) {
        if (parentYo.value().get("position") instanceof YayObject positionYo
            && positionYo.getString("name") instanceof String name
            && positionYo.getString("fen") instanceof String fen) {

            // standard from position
            return new Variant.FromPosition(
                    Opt.of(fen),
                    Opt.of(name));
        }


        //////
        //
        // Workaround if-statements instead of commented out switch expression,
        // due to java.lang.Verify error when compiling ("java build/Build.java")
        if (parentYo.value().get("variant") instanceof YayString(String variant)) {
            return Variant.fromString(variant);
        }
        if (parentYo.value().get("variant") instanceof YayObject variantYo
            && variantYo.getString("key") instanceof String key) {
            return Variant.fromString(key);
        }
        return Variant.Basic.standard;
        // This snippet causes a java.lang.Verify when compiling ("java build/Build.java"), "inconsistent stackframes"
        //return switch(parentYo.value().get("variant")) {
        //    case YayString(String variant) -> Variant.fromString(variant);
        //    case YayObject yo when yo.getString("key") instanceof String key -> Variant.fromString(key);
        //    case null, default -> Variant.Basic.standard;
        //};
    }

    static Swiss nodeToSwiss(YayNode swissNode, YayMapper yayMapper) {
        return switch(swissNode) {
            case YayObject yoSwiss -> {
                TourInfo tourInfo = nodeToTourInfo(swissNode);

                int round = yoSwiss.getInteger("round");
                int nbRounds = yoSwiss.getInteger("nbRounds");
                int nbOngoing = yoSwiss.getInteger("nbOngoing");

                record NextRound(ZonedDateTime at, Duration in) {}
                Opt<NextRound> nextRound = switch(yoSwiss.value().get("nextRound")) {
                    case YayObject yo  -> Opt.of(new NextRound(
                                nodeToZonedDateTime(yo.value().get("at")),
                                Duration.ofSeconds(yo.getInteger("in"))));
                    case null, default -> Opt.of();
                };
                Opt<ZonedDateTime> at = nextRound.map(NextRound::at);
                Opt<Duration> in = nextRound.map(NextRound::in);
                Opt<Swiss.Stats> stats = Opt.of(yayMapper.fromYayTree(yoSwiss.value().get("stats"), Swiss.Stats.class));
                ConditionInfo<SwissCondition> conditions =
                    conditionInfoAs(SwissCondition.class, nodeToConditionInfo(swissNode, tourInfo.speed()));

                yield new Swiss(tourInfo, round, nbRounds, nbOngoing, conditions, at, in, stats);
            }
            default -> null;
        };
    }

    static Arena nodeToArena(YayNode arenaNode, YayMapper yayMapper) {
        return switch(arenaNode) {
            case YayObject yoArena -> {
                TourInfo tourInfo = nodeToTourInfo(arenaNode);

                Duration duration = yoArena.getInteger("minutes") instanceof Integer minutes
                                    ? Duration.ofMinutes(minutes) : Duration.ZERO;
                boolean berserkable = yoArena.getBool("berserkable");
                Opt<String> optSpotlight = yoArena.value().get("spotlight") instanceof YayObject spotlightYo
                                        ? Opt.of(spotlightYo.getString("headline")) : Opt.of();
                boolean pairingsClosed = yoArena.getBool("pairingsClosed");
                boolean isRecentlyFinished = yoArena.getBool("isRecentlyFinished");
                Opt<Arena.Battle> optTeamBattle = nodeToTeamBattle(yoArena.value().get("teamBattle"));
                Opt<Arena.Quote> optQuote    = yoArena.value().get("quote") instanceof YayObject yoQuote
                                      && yoQuote.getString("text") instanceof String text
                                      && yoQuote.getString("author") instanceof String author
                                      ? Opt.of(new Arena.Quote(text, author)) : Opt.of();
                Opt<Arena.GreatPlayer> optGreatPlayer = yoArena.value().get("greatPlayer") instanceof YayObject yoGreat
                                      && yoGreat.getString("name") instanceof String name
                                      && yoGreat.getString("url") instanceof String uri
                                      ? Opt.of(new Arena.GreatPlayer(name, URI.create(uri))) : Opt.of();
                Opt<Arena.Stats> optStats = Opt.of(yayMapper.fromYayTree(yoArena.value().get("stats"), Arena.Stats.class));
                ConditionInfo<ArenaCondition> conditions =
                    conditionInfoAs(ArenaCondition.class, nodeToConditionInfo(arenaNode, tourInfo.speed()));

                List<Arena.Standing> standings = nodeToStandings(yoArena.value().get("standing"), yayMapper);
                List<Arena.Podium> podium = nodeToPodium(yoArena.value().get("podium"), yayMapper);

                List<Arena.TeamStanding> teamStandings = nodeToTeamStandings(yoArena.value().get("teamStanding"), yayMapper);


                List<Arena.TopGame> topGames = nodeToTopGames(yoArena.value().get("duels"), yayMapper);

                if (yoArena.value().get("duelTeams") instanceof YayObject duelTeamsYo
                    && duelTeamsYo.filterCastMap(YayString::value, YayString.class) instanceof Map<String,String> userIdToTeamIdMap) {
                    topGames = topGames.stream()
                        .map(topGame -> new Arena.TopGame(topGame.gameId(),
                                    topGame.whiteName(), topGame.whiteRating(), topGame.whiteRank(),
                                    topGame.blackName(), topGame.blackRating(), topGame.blackRank(),
                                    Opt.of(userIdToTeamIdMap.get(topGame.whiteName().toLowerCase(Locale.ROOT))),
                                    Opt.of(userIdToTeamIdMap.get(topGame.blackName().toLowerCase(Locale.ROOT))))
                            ).toList();
                }
                Opt<Arena.Featured> optFeatured = nodeToFeatured(yoArena.value().get("featured"), yayMapper);

                yield new Arena(tourInfo, duration, berserkable, conditions, standings, podium, teamStandings, topGames,
                        pairingsClosed, isRecentlyFinished, optSpotlight, optTeamBattle, optQuote, optGreatPlayer, optFeatured, optStats);
            }
            default -> null;
        };
    }

    /*
     "teamBattle": {
       "teams": {
         "chessnetwork": [
           "ChessNetwork",
           "nature.horse-face"
         ],
         "im-manitodeplomo-fan-club": [
           "IM Manitodeplomo Fan Club",
           null
         ]
       },
       "nbLeaders": 6
     }
    */
    static Opt<Arena.Battle> nodeToTeamBattle(YayNode yayNode) {
        if (yayNode instanceof YayObject battleYo
            && battleYo.getInteger("nbLeaders") instanceof Integer nbLeaders
            && battleYo.value().get("teams") instanceof YayObject teamsYo
            && teamsYo.filterCastMap(yarr -> yarr.filterCastMap(YayString::value, YayString.class), YayArray.class)
               instanceof Map<String, List<String>> teamsMap
            && ! teamsMap.isEmpty()) {
            List<Arena.TeamInfo> teams = teamsMap.entrySet().stream()
                .filter(entry -> entry.getValue() != null && !entry.getValue().isEmpty())
                .map(entry -> new Arena.TeamInfo(entry.getKey(),
                            entry.getValue().getFirst(),
                            Opt.of(entry.getValue().size() > 1 ? entry.getValue().get(1) : null)))
                .toList();
            return Opt.of(new Arena.Battle(teams, nbLeaders));
        }
        return Opt.of();
    }

    static ArenaLight nodeToArenaLight(YayNode node) {
        return switch(node) {
            case YayObject yoArenaLight -> {
                TourInfo tourInfo = nodeToTourInfo(node);
                Duration duration = yoArenaLight.getInteger("minutes") instanceof Integer minutes
                    ? Duration.ofMinutes(minutes) : Duration.ZERO;
                Opt<ArenaLight.Winner> winner = yoArenaLight.value().get("winner") instanceof YayObject winnerYo
                    && winnerYo.getString("id") instanceof String id
                    && winnerYo.getString("name") instanceof String name
                    ? Opt.of(new ArenaLight.Winner(id, name, Opt.of(winnerYo.getString("title"))))
                    : Opt.of();
                Opt<ArenaLight.BattleLight> optBattle = yoArenaLight.value().get("teamBattle") instanceof YayObject battleYo
                    && battleYo.getInteger("nbLeaders") instanceof Integer nbLeaders
                    && battleYo.value().get("teams") instanceof YayArray teamsYarr
                    && teamsYarr.filterCastMap(YayString::value, YayString.class) instanceof List<String> teams
                    ? Opt.of(new ArenaLight.BattleLight(teams, nbLeaders))
                    : Opt.of();

                ConditionInfo<ArenaCondition> conditions =
                    conditionInfoAs(ArenaCondition.class, nodeToConditionInfo(yoArenaLight, tourInfo.speed()));

                yield new ArenaLight(tourInfo, duration, conditions, optBattle, winner);
            }
            default -> null;
        };
    }

    static List<Arena.Standing> nodeToStandings(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayObject(var standingMap)
            && standingMap.get("players") instanceof YayArray playersArr
            && playersArr.filterCastMap(Function.identity(), YayObject.class) instanceof List<YayObject> playersYo) {

            return playersYo.stream()
                .map(playerYo -> playerYayObjectToStanding(playerYo, yayMapper))
                .filter(Some.class::isInstance)
                .map(s -> (Some<Arena.Standing>) s)
                .map(Some::value)
                .toList();
        }
        return List.of();
    }

    static Opt<Arena.Standing> playerYayObjectToStanding(YayObject playerYo, YayMapper yayMapper) {
        if (yayMapper.fromYayTree(playerYo, LightUser.class) instanceof LightUser tmp
            && playerYo.getInteger("rank") instanceof Integer rank
            && playerYo.getInteger("rating") instanceof Integer rating
            && playerYo.getInteger("score") instanceof Integer score
            && playerYo.value().get("sheet") instanceof YayObject sheetYo
            && sheetYo.getString("scores") instanceof String scores
           ) {
            return Opt.of(new Arena.Standing(
                        new LightUser(tmp.name().toLowerCase(Locale.ROOT), tmp.title(), tmp.name(), tmp.patron(), tmp.flair()),
                        rank, rating, playerYo.getBool("provisional"), score, scores,
                        sheetYo.getBool("fire"), playerYo.getBool("withdraw"), Opt.of(playerYo.getString("team"))));
        }
        return Opt.of();
    }

    static List<Arena.Podium> nodeToPodium(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayArray podiumArr
            && podiumArr.filterCastMap(Function.identity(), YayObject.class) instanceof List<YayObject> podiumYo) {
            return podiumYo.stream()
                .map(p -> podiumYayObjectToPodium(p, yayMapper))
                .filter(Some.class::isInstance)
                .map(s -> (Some<Arena.Podium>) s)
                .map(Some::value)
                .toList();
        }
        return List.of();
    }

    static Opt<Arena.Podium> podiumYayObjectToPodium(YayObject podiumYo, YayMapper yayMapper) {
         if (yayMapper.fromYayTree(podiumYo, LightUser.class) instanceof LightUser tmp
            && podiumYo.getInteger("rank") instanceof Integer rank
            && podiumYo.getInteger("rating") instanceof Integer rating
            && podiumYo.getInteger("score") instanceof Integer score
            && podiumYo.getInteger("performance") instanceof Integer performance
            && podiumYo.value().get("nb") instanceof YayObject nbYo
            && nbYo.getInteger("game") instanceof Integer game
            && nbYo.getInteger("berserk") instanceof Integer berserk
            && nbYo.getInteger("win") instanceof Integer win
           ) {
            return Opt.of(new Arena.Podium(
                        new LightUser(tmp.name().toLowerCase(Locale.ROOT), tmp.title(), tmp.name(), tmp.patron(), tmp.flair()),
                        rank, rating, score, performance, game, berserk, win, Opt.of(podiumYo.getString("team"))));
        }
        return Opt.of();
    }

    static List<Arena.TeamStanding> nodeToTeamStandings(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayArray teamStandingArr
            && teamStandingArr.filterCastMap(Function.identity(), YayObject.class) instanceof List<YayObject> teamStandings) {
            return teamStandings.stream()
                .map(teamStandingYo -> Opt.of(yayMapper.fromYayTree(teamStandingYo, Arena.TeamStanding.class)))
                .filter(Some.class::isInstance)
                .map(s -> (Some<Arena.TeamStanding>) s)
                .map(Some::value)
                .toList();
        }
        return List.of();
    }

    static Arena.TeamStanding nodeToTeamStanding(YayNode teamStandingNode, YayMapper yayMapper) {
        if (teamStandingNode instanceof YayObject teamStandingYo
            && teamStandingYo.getInteger("rank") instanceof Integer rank
            && teamStandingYo.getString("id") instanceof String teamId
            && teamStandingYo.getInteger("score") instanceof Integer score
            && teamStandingYo.value().get("players") instanceof YayArray playersArr
            && playersArr.filterCastMap(Function.identity(), YayObject.class) instanceof List<YayObject> players
           ) {
            record UserScore(LightUser user, int score) {}
            Map<LightUser, Integer> playerMap = players.stream()
                .map(playerYo -> switch(playerYo) {
                    case YayObject yo
                        when yo.value().get("user") instanceof YayObject userYo
                             && yayMapper.fromYayTree(userYo, LightUser.class) instanceof LightUser user
                             && yo.getInteger("score") instanceof Integer memberScore
                             -> Opt.<UserScore>of(new UserScore(user, memberScore));
                    default  -> Opt.<UserScore>of();
                })
                .filter(Some.class::isInstance)
                .map(s -> (Some<UserScore>) s)
                .map(Some::value)
                .collect(Collectors.toMap(UserScore::user, UserScore::score, (user, duplicate) -> user, LinkedHashMap::new));
            return new Arena.TeamStanding(rank, teamId, score, playerMap);
        }
        return null;
    }

    static Opt<Arena.Featured> nodeToFeatured(YayNode node, YayMapper yayMapper) {
        if (node instanceof YayObject featuredYo
            && featuredYo.getString("id") instanceof String gameId
            && featuredYo.getString("fen") instanceof String fen
            && featuredYo.getString("orientation") instanceof String orientationStr
            && featuredYo.getString("color") instanceof String colorStr
            && featuredYo.getString("lastMove") instanceof String lastMove
            && featuredYo.value().get("white") instanceof YayObject whiteYo
            && yayMapper.fromYayTree(whiteYo, LightUser.class) instanceof LightUser white
            && whiteYo.getInteger("rank") instanceof Integer whiteRank
            && whiteYo.getInteger("rating") instanceof Integer whiteRating
            && featuredYo.value().get("black") instanceof YayObject blackYo
            && yayMapper.fromYayTree(blackYo, LightUser.class) instanceof LightUser black
            && blackYo.getInteger("rank") instanceof Integer blackRank
            && blackYo.getInteger("rating") instanceof Integer blackRating
            && featuredYo.value().get("c") instanceof YayObject clocksYo
            && clocksYo.getInteger("white") instanceof Integer whiteSeconds
            && clocksYo.getInteger("black") instanceof Integer blackSeconds
            ) {
            return Opt.of(new Arena.Featured(
                    gameId,
                    fen,
                    Enums.Color.valueOf(orientationStr),
                    Enums.Color.valueOf(colorStr),
                    lastMove,
                    white,
                    whiteRating,
                    whiteRank,
                    whiteYo.getBool("berserk"),
                    black,
                    blackRating,
                    blackRank,
                    blackYo.getBool("berserk"),
                    Duration.ofSeconds(whiteSeconds),
                    Duration.ofSeconds(blackSeconds),
                    Opt.of(featuredYo.getString("winner")).map(Enums.Color::valueOf)
                    ));
        }
        return Opt.of();
    }

    static List<Arena.TopGame> nodeToTopGames(YayNode node, YayMapper yayMapper) {
        return node instanceof YayArray duelsYarr
            && duelsYarr.filterCastMap(TournamentAdapter::nodeToTopGame, YayObject.class) instanceof List<Opt<Arena.TopGame>> optTopGames
            ? optTopGames.stream().filter(Opt::isPresent).map(Opt::get).toList()
            : List.of();
    }

    static Opt<Arena.TopGame> nodeToTopGame(YayNode node) {
        return node instanceof YayObject duelYo
            && duelYo.getString("id") instanceof String gameId
            && duelYo.value().get("p") instanceof YayArray pairingArr
            && pairingArr.filterCastMap(Function.identity(), YayObject.class) instanceof List<YayObject> playersYarr
            && playersYarr.size() == 2
            && playersYarr.getFirst() instanceof YayObject whiteYo
            && whiteYo.getString("n") instanceof String whiteName
            && whiteYo.getInteger("r") instanceof Integer whiteRating
            && whiteYo.getInteger("k") instanceof Integer whiteRank
            && playersYarr.getLast() instanceof YayObject blackYo
            && blackYo.getString("n") instanceof String blackName
            && blackYo.getInteger("r") instanceof Integer blackRating
            && blackYo.getInteger("k") instanceof Integer blackRank
            ? Opt.of(new Arena.TopGame(gameId, whiteName, whiteRating, whiteRank, blackName, blackRating, blackRank, Opt.of(), Opt.of()))
            : Opt.of();
    }
}
