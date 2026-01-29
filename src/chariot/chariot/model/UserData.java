package chariot.model;

import java.net.URI;
import java.time.*;
import java.util.*;

import static chariot.model.UserData.UserPropertyEnum.*;
import static chariot.model.UserData.ProfilePropertyEnum.*;
import static chariot.model.UserData.StreamerInfoPropertyEnum.*;

public record UserData(Map<UserPropertyEnum, ?> properties) {

    public UserCommon toCommon() {
        if (_disabled().orElse(false)) {
            return new Disabled(_id(), _name());
        }
        LightUser lightUser = new LightUser(_id(), Opt.of(_title().orElse(null)), _name(), _patronColor(), Opt.of(_flair().orElse(null)));
        UserCommon common = lightUser;
        return common;
    }

    public User toUser() {
        return toUser(false, false);
    }

    public User toUser(boolean includeTrophies, boolean includeCanChallenge) {
        return toUserProfileData(includeTrophies, includeCanChallenge);
    }

    public UserAuth toUserAuth() {
        return toUserAuth(false, false);
    }

    public UserAuth toUserAuth(boolean includeTrophies, boolean includeCanChallenge) {
        return toUserProfileData(includeTrophies, includeCanChallenge);
    }

    public UserProfileData toUserProfileData(boolean includeTrophies, boolean includeCanChallenge) {
        UserCommon common = toCommon();
        ProvidedProfile profile = _profile().orElse(Provided.emptyProfile);
        UserStats stats = new UserStats(_ratings().orElse(Map.of()), _count().orElse(null));
        UserTimes times = new UserTimes(_createdAt().orElse(null), _seenAt().orElse(null), _playTime().map(PlayTime::total).orElse(null), _playTime().map(PlayTime::tv).orElse(null));
        UserFlags flags = new UserFlags(
                _tosViolation().orElse(false),
                _disabled().orElse(false),
                _verified().orElse(false),
                _streaming().orElse(false));
        URI url = _url().orElse(null);
        Opt<List<Trophy>> trophies = includeTrophies ? Opt.of(_trophies().orElse(List.of())) : Opt.empty();
        Opt<UserAuthFlags> authFlags = Opt.empty();
        if (_followable() instanceof Some<Boolean>) {
            authFlags = Opt.of(new UserAuthFlags(
                _followable().orElse(false),
                _following().orElse(false),
                _blocking().orElse(false)));
        }
        Opt<Boolean> canChallenge = includeCanChallenge ? Opt.of(_canChallenge().orElse(false)) : Opt.empty();
        Opt<String> twitch = _channelInfo() instanceof Some<ChannelInfo> some ? some.value().twitch() : Opt.empty();
        Opt<String> youtube = _channelInfo() instanceof Some<ChannelInfo> some ? some.value().youtube() : Opt.empty();
        var userProfileData = new UserProfileData(
                common,
                profile,
                stats,
                times,
                flags,
                Opt.of(_playing().orElse(null)),
                authFlags,
                trophies,
                canChallenge,
                twitch,
                youtube,
                url);
        return userProfileData;
    }

    public UserStatus toUserStatus() {
        UserCommon common = toCommon();
        UserStatusData status = new UserStatusData(
                common,
                _online().orElse(false),
                _isPlaying().orElse(false),
                _playingGameId() instanceof Some<String> some
                    ? Opt.of(some.value())
                    : Opt.of( _playingGameMeta().map(meta -> meta.id()).orElse(null)),
                _playingGameMeta(),
                Opt.of(_signal().orElse(null))
                );

        return status;
    }

    public LiveStreamer toLiveStreamer() {
        UserCommon common = toCommon();
        return new LiveStreamer(common, _liveStreamInfo().orElse(null), _liveStreamerInfo().orElse(null));
    }

    public TeamMember toTeamMember() {
        UserCommon user = toCommon();
        TeamMember member = new TeamMember(user, "", _joinedTeamAt().orElse(null));
        return member;
    }

    public TeamMemberFull toTeamMemberFull() {
        User user = toUser();
        TeamMemberFull member = new TeamMemberFull(user, "", _joinedTeamAt().orElse(null));
        return member;
    }


    public UserData _userData() { return this; }

    public enum UserPropertyEnum {
        id, name, username, title, patronColor, flair,
        online, streaming, playing, playingUrl, playingGameId,
        tosViolation, disabled, closed, verified, trophies,
        playTime, createdAt, seenAt, url, counts, ratings,
        profile, followable, following, blocking,
        joinedTeamAt, streamInfo, streamerInfo, channelInfo,
        signal, playingGameMeta, canChallenge,

        unmapped
        ;
        public <T> Property<UserPropertyEnum, T> of(T value) { return Property.of(this, value); }
    }

    public enum ProfilePropertyEnum {
        flag, location, bio, realName, links,
        ratingFide, ratingUscf, ratingEcf, ratingRcf, ratingCfc, ratingDsb,
        unmapped,
        ;
        public <T> Property<ProfilePropertyEnum, T> of(T value) { return Property.of(this, value); }
    }

    public enum StreamerInfoPropertyEnum {
        name, headline, description, twitch, youtube, image,
        unmapped,
        ;
        public <T> Property<StreamerInfoPropertyEnum, T> of(T value) { return Property.of(this, value); }
    }

    public record Property<K extends Enum<K>, V>(K key, V value) {
        static <T extends Enum<T>, U> Property<T, U> of(T key, U value) { return new Property<>(key, value); }
    }

    Opt<String> property(UserPropertyEnum key) { return property(key, String.class); }
    Opt<Boolean> propertyB(UserPropertyEnum key) { return property(key, Boolean.class); }

    <T> Opt<T> property(UserPropertyEnum key, Class<T> cls) {
        var property = properties().get(key);
        if (property == null) return Opt.empty();
        return cls.isInstance(property) ? Opt.of(cls.cast(property)) : Opt.empty();
    }

    public UserData {
        properties = properties == null
            ? Collections.unmodifiableMap(new EnumMap<>(UserPropertyEnum.class))
            : properties;
    }

    public String     _id()       { return property(id).orElse(null); }
    public String     _username() { return property(username).orElseGet(() -> _name()); }
    public String     _name()     { return property(UserPropertyEnum.name).orElseGet(() -> property(username).orElse(null)); }
    Opt<String>  _title() { return property(title); }
    Opt<Integer> _patronColor() { return property(patronColor, Integer.class); }
    Opt<String>  _flair() { return property(flair); }
    Opt<Boolean> _online() { return propertyB(online); }
    Opt<Boolean> _streaming() { return propertyB(streaming); }
    Opt<Boolean> _isPlaying() { return propertyB(playing) instanceof Some<Boolean> some && some.value()
                                ? some
                                : Opt.of(_playingGameMeta() instanceof Some<?>); }
    Opt<URI>     _playing() { return property(playingUrl, URI.class); }
    Opt<String>  _playingGameId() { return property(playingGameId); }
    Opt<UserStatus.GameMeta>  _playingGameMeta() { return property(playingGameMeta, UserStatus.GameMeta.class); }
    Opt<Integer>  _signal() { return property(signal, Integer.class); }
    Opt<Boolean> _canChallenge() { return propertyB(canChallenge); }
    Opt<Boolean> _tosViolation() { return propertyB(tosViolation); }
    Opt<Boolean> _disabled() { return propertyB(disabled); }
    Opt<Boolean> _verified() { return propertyB(verified); }
    Opt<List<Trophy>> _trophies() { return property(trophies, Trophies.class).map(Trophies::trophies); }
    Opt<PlayTime> _playTime() { return property(playTime, PlayTime.class); }
    Opt<ZonedDateTime> _createdAt() { return property(createdAt, ZonedDateTime.class); }
    Opt<ZonedDateTime> _seenAt() { return property(seenAt, ZonedDateTime.class); }
    Opt<URI>     _url() { return property(url, URI.class); }
    Opt<UserCount> _count() { return property(counts, UserCount.class); }
    Opt<Map<StatsPerfType, StatsPerf>> _ratings() { return property(ratings, Ratings.class).map(Ratings::ratings); }
    Opt<Provided> _profile() { return property(profile, Provided.class); }
    Opt<Boolean> _followable() { return propertyB(followable); }
    Opt<Boolean> _following() { return propertyB(following); }
    Opt<Boolean> _blocking() { return propertyB(blocking); }
    Opt<ZonedDateTime> _joinedTeamAt() { return property(joinedTeamAt, ZonedDateTime.class); }
    Opt<StreamInfo> _liveStreamInfo() { return property(streamInfo, StreamInfo.class); }
    Opt<StreamerInfo> _liveStreamerInfo() { return property(streamerInfo, StreamerInfo.class); }
    Opt<ChannelInfo> _channelInfo() { return property(channelInfo, ChannelInfo.class); }

    public record ChannelInfo(Map<String, String> channels) {
        Opt<String> twitch() { return Opt.of(channels.get("twitch")); }
        Opt<String> youtube() { return Opt.of(channels.get("youtube")); }
    }

    public UserData withProperties(Map<UserPropertyEnum, ?> map) {
        return new UserData(Collections.unmodifiableMap(new EnumMap<>(map)));
    }

    public UserData withAddedProperty(Property<UserPropertyEnum, ?> property) {
        var mutable = new EnumMap<UserPropertyEnum, Object>(properties());
        mutable.put(property.key(), property.value());
        return new UserData(Collections.unmodifiableMap(mutable));
    }

    public record Trophies(List<Trophy> trophies) {}
    public record Ratings(Map<StatsPerfType, StatsPerf> ratings) {}

    public record Provided(Map<ProfilePropertyEnum, ?> properties) implements ProvidedProfile {

        public static final Provided emptyProfile = new Provided(Map.of());

        public Opt<String> flag() { return property(flag); }
        public Opt<String> location() { return property(location); }
        public Opt<String> bio() { return property(bio); }
        public Opt<String> realName() { return property(realName); }
        @Deprecated public Opt<String> firstName() { return property(realName); }
        @Deprecated public Opt<String> lastName() { return property(realName); }
        public Opt<String> links() { return property(links); }
        public Opt<Integer> ratingFide() { return propertyI(ratingFide); }
        public Opt<Integer> ratingUscf() { return propertyI(ratingUscf); }
        public Opt<Integer> ratingEcf() { return propertyI(ratingEcf); }
        public Opt<Integer> ratingRcf() { return propertyI(ratingRcf); }
        public Opt<Integer> ratingCfc() { return propertyI(ratingCfc); }
        public Opt<Integer> ratingDsb() { return propertyI(ratingDsb); }

        Opt<String> property(ProfilePropertyEnum key) { return property(key, String.class); }
        Opt<Integer> propertyI(ProfilePropertyEnum key) { return property(key, Integer.class); }

        <T> Opt<T> property(ProfilePropertyEnum key, Class<T> cls) {
            var property = properties().get(key);
            if (property == null) return Opt.empty();
            return cls.isInstance(property) ? Opt.of(cls.cast(property)) : Opt.empty();
        }
    }


    public record PlayTime(Duration total, Duration tv) {}
    public record StreamInfo(String service, String status, String lang) implements chariot.model.StreamInfo {}
    public record StreamerInfo(Map<StreamerInfoPropertyEnum, ?> properties) implements chariot.model.StreamerInfo {
        public String name()              { return property(StreamerInfoPropertyEnum.name).orElse(null); }
        public String headline()          { return property(headline).orElse(null); }
        public String description()       { return property(description).orElse(null); }

        public Opt<String> twitch()  { return property(twitch); }
        public Opt<String> youtube() { return property(youtube); }
        public Opt<String> image()   { return property(image); }

        Opt<String> property(StreamerInfoPropertyEnum key) { return property(key, String.class); }

        <T> Opt<T> property(StreamerInfoPropertyEnum key, Class<T> cls) {
            var property = properties().get(key);
            if (property == null) return Opt.empty();
            return cls.isInstance(property) ? Opt.of(cls.cast(property)) : Opt.empty();
        }

        public StreamerInfo withAddedProperty(Property<StreamerInfoPropertyEnum, ?> property)   {
            var mutable = new EnumMap<StreamerInfoPropertyEnum, Object>(properties());
            mutable.put(property.key(), property.value());
            return new StreamerInfo(Collections.unmodifiableMap(mutable));
        }
    }
}
