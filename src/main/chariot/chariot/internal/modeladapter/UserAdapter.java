package chariot.internal.modeladapter;

import chariot.internal.Util;
import chariot.internal.yayson.Parser.*;
import chariot.internal.yayson.YayMapper;
import chariot.model.*;
import chariot.model.UserData.*;

import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.stream.*;

import static chariot.model.UserData.UserPropertyEnum.name;
import static chariot.model.UserData.UserPropertyEnum.*;
import static chariot.model.UserData.ProfilePropertyEnum.*;
import static chariot.model.UserData.StreamerInfoPropertyEnum.*;

public interface UserAdapter {

    static UserData nodeToUserData(YayNode node, YayMapper yayMapper) {
        var data = new UserData(null);

        if (! (node instanceof YayObject yo)) return data;

        var mappedUserProperties = yo.value().entrySet().stream()
            .filter(entry -> ! (entry.getValue() instanceof YayNull)) // Skip null-entries from Lichess
            .map(entry -> switch(entry.getKey()) {
                case "id"           -> id.of(yo.getString(entry.getKey()));
                case "username"     -> username.of(yo.getString(entry.getKey()));
                case "name"         -> name.of(yo.getString(entry.getKey()));
                case "title"        -> title.of(yo.getString(entry.getKey()));
                case "patron"       -> patron.of(yo.getBool(entry.getKey()));
                case "flair"        -> flair.of(yo.getString(entry.getKey()));
                case "online"       -> online.of(yo.getBool(entry.getKey()));
                case "streaming"    -> streaming.of(yo.getBool(entry.getKey()));
                case "playing"      -> {
                                          if (entry.getValue() instanceof YayBool bool) {
                                              yield playing.of(bool.value());
                                          } else if (entry.getValue() instanceof YayObject yoGameMetas
                                                  && yoGameMetas.value().get("id") instanceof YayString yayId
                                                  && yoGameMetas.value().get("clock") instanceof YayString yayClock) {
                                              yield playingGameMeta.of(new UserStatus.GameMeta(yayId.value(), yayClock.value(),
                                                          yoGameMetas.value().get("variant") instanceof YayString yayVariant
                                                          ? Enums.GameVariant.valueOf(yayVariant.value())
                                                          : Enums.GameVariant.standard));
                                          } else {
                                              yield playingUrl.of(URI.create(yo.getString(entry.getKey())));
                                          }
                                       }
                case "playingId"    -> playingGameId.of(yo.getString(entry.getKey()));
                case "signal"       -> signal.of(yo.getInteger(entry.getKey()));
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
                                           ? yarr.value().stream().map(trophyNode -> yayMapper.fromYayTree(trophyNode, Trophy.class)).toList()
                                           : List.of()));
                case "challenge"    -> canChallenge.of(yo.getBool(entry.getKey()));
                case "playTime"     -> playTime.of(new PlayTime(
                                           Duration.ofSeconds(entry.getValue() instanceof YayObject playYo
                                               ? playYo.getLong("total") : 0),
                                           Duration.ofSeconds(entry.getValue() instanceof YayObject playYo
                                               ? playYo.getLong("tv") : 0)));
                case "count"        -> entry.getValue() instanceof YayObject countYo
                                           ? counts.of(yayMapper.fromYayTree(countYo, UserCount.class))
                                           : UserPropertyEnum.unmapped.of(entry);
                case "perfs" -> entry.getValue() instanceof YayObject perfsYo
                                           ? ratings.of(new Ratings(perfsYo.value().entrySet().stream().filter(e -> !e.getKey().equals("standard"))
                                                       .collect(Collectors.toUnmodifiableMap(
                                                               yoMapEntry -> StatsPerfType.valueOf(yoMapEntry.getKey()),
                                                               yoMapEntry -> yayMapper.fromYayTree(yoMapEntry.getValue(), StatsPerf.class)))))
                                           : UserPropertyEnum.unmapped.of(entry);
                case "profile" -> {
                                      if (! (entry.getValue() instanceof YayObject profileYo)) yield UserPropertyEnum.unmapped.of(entry);
                                      var map = profileYo.value().entrySet().stream()
                                          .map(profileEntry -> switch(profileEntry.getKey()) {
                                              case "flag"       -> flag.of(profileYo.getString(profileEntry.getKey()));
                                              case "location"   -> location.of(profileYo.getString(profileEntry.getKey()));
                                              case "bio"        -> bio.of(profileYo.getString(profileEntry.getKey()));
                                              case "realName"   -> realName.of(profileYo.getString(profileEntry.getKey()));
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
                                    ? streamInfo.of(yayMapper.fromYayTree(streamYo, UserData.StreamInfo.class))
                                    : UserPropertyEnum.unmapped.of(entry);
                case "streamer" -> {
                                       if (! (entry.getValue() instanceof YayObject streamerYo)) yield UserPropertyEnum.unmapped.of(entry);

                                       if (! streamerYo.value().containsKey("name")) {
                                           var map = new HashMap<String,String>();
                                           if (streamerYo.value().get("twitch") instanceof YayObject twitchYo)
                                               map.put("twitch", twitchYo.getString("channel"));
                                           if (streamerYo.value().get("youTube") instanceof YayObject youtubeYo)
                                               map.put("youTube", youtubeYo.getString("channel"));
                                           yield channelInfo.of(new UserData.ChannelInfo(Collections.unmodifiableMap(map)));
                                       }

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
    }
}
