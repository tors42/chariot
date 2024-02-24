package chariot.internal.modeladapter;

import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import chariot.internal.yayson.Parser.*;
import chariot.model.*;

public interface TimelineAdapter {

    static TLUsers nodeToTimelineUsers(YayNode node, Function<YayNode, UserCommon> userCommonMapper) {
        return switch (node) {
            case YayObject users -> new TLUsers(users.value().values().stream()
                    .map(userCommonMapper)
                    .map(LightUser.class::cast)
                    .collect(Collectors.toMap(LightUser::id, Function.identity())));
            default -> new TLUsers(Map.of());
        };
    }

    record Timeline(List<TLEntry> entries, TLUsers users) {
        public Stream<TimelineEntry> toTimelineEntries() {
            return entries.stream().map(entry -> entry.toTimelineEntry(users.users())).filter(Objects::nonNull);
        }
    }

    record TLEntry(String type, Map<String, Object> data, ZonedDateTime date) {
        TimelineEntry toTimelineEntry(Map<String, LightUser> users) {
            return switch(type) {
                case "follow" -> new TimelineEntry.Follow(users.get(dataStr("u1")), users.get(dataStr("u2")), date);
                case "team-join" -> new TimelineEntry.TeamJoin(users.get(dataStr("userId")), dataStr("teamId"), date);
                case "team-create" -> new TimelineEntry.TeamCreate(users.get(data.get("userId")), dataStr("teamId"), date);
                case "forum-post" -> new TimelineEntry.ForumPost(users.get(data.get("userId")), dataStr("topicId"), dataStr("topicName"), dataStr("postId"), date);
                case "ublog-post" -> new TimelineEntry.UblogPost(users.get(data.get("userId")), dataStr("id"), dataStr("slug"), dataStr("title"), date);
                case "tour-join" -> new TimelineEntry.TourJoin(users.get(data.get("userId")), dataStr("tourId"), dataStr("tourName"), date);
                case "game-end" -> new TimelineEntry.GameEnd(dataStr("fullId"), dataStr("perf"), Opt.of(users.get(data.get("opponent"))), Opt.of((Boolean)data.get("win")), date);
                case "simul-create" -> new TimelineEntry.SimulCreate(users.get(data.get("userId")), dataStr("simulId"), dataStr("simulName"), date);
                case "simul-join" -> new TimelineEntry.SimulJoin(users.get(data.get("userId")), dataStr("simulId"), dataStr("simulName"), date);
                case "study-like" -> new TimelineEntry.StudyLike(users.get(data.get("userId")), dataStr("studyId"), dataStr("studyName"), date);
                case "plan-start" -> new TimelineEntry.PlanStart(users.get(data.get("userId")), date);
                case "plan-renew" -> new TimelineEntry.PlanRenew(users.get(data.get("userId")), (Integer) data.get("months"), date);
                case "blog-post" -> new TimelineEntry.BlogPost(dataStr("id"), dataStr("slug"), dataStr("title"), date);
                case "ublog-post-like" -> new TimelineEntry.UblogPostLike(users.get(data.get("userId")), dataStr("id"), dataStr("title"), date);
                case "stream-start" -> new TimelineEntry.StreamStart(users.get(data.get("userId")), dataStr("title"), date);
                default -> null;
            };
        }
        private String dataStr(String key) { return (String) data.get(key); }
    }
    record TLUsers(Map<String, LightUser> users) {}
}
