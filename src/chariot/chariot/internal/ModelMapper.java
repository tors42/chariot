package chariot.internal;

import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;
import java.util.stream.*;

import chariot.internal.modeladapter.*;
import chariot.internal.yayson.*;
import chariot.internal.yayson.Parser.*;
import chariot.model.*;

@SuppressWarnings("unchecked")
public class ModelMapper {

    private static final YayMapper yayMapper = YayMapper.mapper();
    private static final Map<Class<?>, Function<String, ?>> strToObjMappings = new HashMap<>();
    private static final Map<Class<?>, Function<String, ?>> strToObjArrMappings = new HashMap<>();

    public static <T> Function<String, T> mapper(final Class<T> clazz) {
        return (Function<String, T>) strToObjMappings.computeIfAbsent(clazz, c -> json -> yayMapper.fromString(json, c));
    }

    public static <T> Function<String, T[]> mapperArr(final Class<T> clazz) {
        return (Function<String, T[]>) strToObjArrMappings.computeIfAbsent(clazz, cls -> json -> {
            if (! (Parser.fromString(json) instanceof YayArray(var yarr))) return null;
            var list = yarr.stream()
                .map(e -> yayMapper.fromYayTree(e, cls))
                .toList();
            Object array = Array.newInstance(cls, list.size());
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        });
    }

    static {
        // Add custom mapping of some model classes,
        // where automatic transformation of json model to java model isn't straightforward.

        // Specify Java model <-> JSON model field name mappings,
        // where JSON model contains Java keywords,
        // so another name must be used in the Java model.
        yayMapper.setMappings(PerformanceStatistics.DateResult.class, ModelMapperUtil.intMapping());
        yayMapper.setMappings(UserCount.class,                        ModelMapperUtil.importMapping());
        yayMapper.setMappings(Variant.class,                          ModelMapperUtil.shortMapping());

        yayMapper.setCustomMapper(Challenge.class, ChallengesAdapter::nodeToChallenge);
        yayMapper.setCustomMapper(ChallengeInfo.class, ChallengesAdapter::nodeToChallengeInfo);
        yayMapper.setCustomMapper(ArenaLight.class, TournamentAdapter::nodeToArenaLight);
        yayMapper.setCustomMapper(Arena.TeamStanding.class, node -> TournamentAdapter.nodeToTeamStanding(node, yayMapper));
        yayMapper.setCustomMapper(PuzzleAngle.class, PuzzleAngleAdapter::nodeToPuzzleAngle);
        yayMapper.setCustomMapper(GameMeta.Player.class, GameMetaAdapter::nodeToPlayer);

        strToObjMappings.put(Crosstable.class,         json -> CrosstableAdapter.nodeToCrosstable(Parser.fromString(json), yayMapper));
        strToObjMappings.put(ChallengeInfo.class,      json -> ChallengesAdapter.nodeToChallengeInfo(Parser.fromString(json)));
        strToObjMappings.put(ChallengeOpenEnded.class, json -> ChallengesAdapter.nodeToChallengeOpenEnded(Parser.fromString(json)));
        strToObjMappings.put(Challenge.class,          json -> ChallengesAdapter.nodeToChallenge(Parser.fromString(json)));

        strToObjMappings.put(Arena.class,      json -> TournamentAdapter.nodeToArena(Parser.fromString(json), yayMapper));
        strToObjMappings.put(ArenaLight.class, json -> TournamentAdapter.nodeToArenaLight(Parser.fromString(json)));

        strToObjMappings.put(Swiss.class, json -> TournamentAdapter.nodeToSwiss(Parser.fromString(json), yayMapper));

        strToObjMappings.put(GameMeta.class, json -> GameMetaAdapter.nodeToGameMeta(Parser.fromString(json), yayMapper));

        strToObjMappings.put(ChallengeTokens.class, json -> switch(Parser.fromString(json)) {
            case YayObject yo -> new ChallengeTokens(yo.value().keySet().stream()
                    .collect(Collectors.toUnmodifiableMap(
                                k -> k,
                                k -> yo.getString(k)
                                )));
            case null, default -> null;
        });

        strToObjMappings.put(TokenBulkResult.class, json -> switch(Parser.fromString(json)) {
            case YayObject yo -> new TokenBulkResult(yo.filterCastMap(
                        tokenInfo -> new TokenBulkResult.TokenInfo(
                            tokenInfo.getString("userId"),
                            tokenInfo.getString("scopes"),
                            tokenInfo.getLong("expires") instanceof Long exp ? Util.fromLong(exp) : null),
                        YayObject.class));
            case null, default -> null;
        });

        Function<YayNode, UserData> userDataMapper = node -> UserAdapter.nodeToUserData(node, yayMapper);

        yayMapper.setCustomMapper(UserData.class, userDataMapper);
        strToObjMappings.put(UserData.class, json -> userDataMapper.apply(Parser.fromString(json)));

        strToObjArrMappings.put(UserData.class, json -> switch(Parser.fromString(json)) {
            case YayArray yarr -> yarr.value().stream().map(userDataMapper).toArray(UserData[]::new);
            case null, default -> null;
        });

        Function<YayNode, UserStatus> userStatusMapper = node -> yayMapper.fromYayTree(node, UserData.class).toUserStatus();
        Function<YayNode, LiveStreamer> streamerStatusMapper = node -> yayMapper.fromYayTree(node, UserData.class).toLiveStreamer();

        yayMapper.setCustomMapper(UserStatus.class, userStatusMapper);
        yayMapper.setCustomMapper(LiveStreamer.class, streamerStatusMapper);

        strToObjMappings.put(UserStatus.class, json -> userStatusMapper.apply(Parser.fromString(json)));
        strToObjMappings.put(LiveStreamer.class, json -> streamerStatusMapper.apply(Parser.fromString(json)));

        Function<YayNode, TVFeedEvent> tvFeedEventMapper = node -> TvFeedAdapter.nodeToEvent(node);

        yayMapper.setCustomMapper(TVFeedEvent.class, tvFeedEventMapper);
        strToObjMappings.put(TVFeedEvent.class, json -> tvFeedEventMapper.apply(Parser.fromString(json)));

        Function<YayNode, Player> gamePlayerMapper = node -> GamePlayerAdapter.nodeToPlayer(node, yayMapper);
        yayMapper.setCustomMapper(Player.class, gamePlayerMapper);
        strToObjMappings.put(Player.class, json -> gamePlayerMapper.apply(Parser.fromString(json)));

        Function<YayNode, UserCommon> userCommonMapper = node -> yayMapper.fromYayTree(node, UserData.class).toCommon();

        yayMapper.setCustomMapper(TimelineAdapter.TLUsers.class, node -> TimelineAdapter.nodeToTimelineUsers(node, userCommonMapper));

        yayMapper.setCustomMapper(UserCommon.class, userCommonMapper);

        strToObjMappings.put(UserCommon.class, json -> userCommonMapper.apply(Parser.fromString(json)));

        strToObjArrMappings.put(UserStatus.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userStatusMapper).toArray(UserStatus[]::new);
            }
            return null;
        });
        strToObjArrMappings.put(LiveStreamer.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(streamerStatusMapper).toArray(LiveStreamer[]::new);
            }
            return streamerStatusMapper.apply(node);
        });
        strToObjArrMappings.put(UserCommon.class, json -> {
            var node = Parser.fromString(json);
            if (node instanceof YayArray yarr) {
                return yarr.value().stream().map(userCommonMapper).toArray(UserCommon[]::new);
            }
            return userCommonMapper.apply(node);
        });

        Function<YayNode, GameInfo>  gameInfoMapper = node -> GameInfoAdapter.nodeToInfo(node);
        yayMapper.setCustomMapper(GameInfo.class, gameInfoMapper);
        strToObjMappings.put(GameInfo.class, json -> gameInfoMapper.apply(Parser.fromString(json)));

        Function<YayNode, Event> eventMapper = node -> EventAdapter.nodeToEvent(node);
        yayMapper.setCustomMapper(Event.class, eventMapper);
        strToObjMappings.put(Event.class, json -> eventMapper.apply(Parser.fromString(json)));

        strToObjMappings.put(GameStateEvent.class, json -> GameStateAdapter.nodeToEvent(Parser.fromString(json)));

        strToObjArrMappings.put(RatingHistory.class, json -> RatingHistoryAdapter.nodeToArray(Parser.fromString(json)));

        strToObjArrMappings.put(Activity.class, json -> ActivityAdapter.nodeToArray(Parser.fromString(json), yayMapper));
    }

}
