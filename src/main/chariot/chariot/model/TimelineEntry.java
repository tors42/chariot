package chariot.model;

import java.time.ZonedDateTime;

public sealed interface TimelineEntry {

    record Follow(LightUser user, LightUser otherUser, ZonedDateTime date)                                    implements TimelineEntry {}
    record TeamJoin(LightUser user, String teamId, ZonedDateTime date)                                        implements TimelineEntry {}
    record TeamCreate(LightUser user, String teamId, ZonedDateTime date)                                      implements TimelineEntry {}
    record ForumPost(LightUser user, String topicId, String topicName, String postId, ZonedDateTime date)     implements TimelineEntry {}
    record UblogPost(LightUser user, String id, String slug, String title, ZonedDateTime date)                implements TimelineEntry {}
    record TourJoin(LightUser user, String tourId, String tourName, ZonedDateTime date)                       implements TimelineEntry {}
    record GameEnd(String fullId, String perf, Opt<LightUser> opponent, Opt<Boolean> win, ZonedDateTime date) implements TimelineEntry {}
    record SimulCreate(LightUser userId, String simulId, String simulName, ZonedDateTime date)                implements TimelineEntry {}
    record SimulJoin(LightUser user, String simulId, String simulName, ZonedDateTime date)                    implements TimelineEntry {}
    record StudyLike(LightUser user, String studyId, String studyName, ZonedDateTime date)                    implements TimelineEntry {}
    record PlanStart(LightUser user, ZonedDateTime date)                                                      implements TimelineEntry {}
    record PlanRenew(LightUser user, int months, ZonedDateTime date)                                          implements TimelineEntry {}
    record BlogPost(String id, String slug, String title, ZonedDateTime date)                                 implements TimelineEntry {}
    record UblogPostLike(LightUser user, String id, String title, ZonedDateTime date)                         implements TimelineEntry {}
    record StreamStart(LightUser user, String name, ZonedDateTime date)                                       implements TimelineEntry {}

}
