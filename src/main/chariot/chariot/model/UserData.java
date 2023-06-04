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
        LightUser lightUser = new LightUser(_id(), Opt.of(_title().orElse(null)), _name(), _patron().orElse(false));
        UserCommon common = lightUser;
        return common;
    }

    public User toUser() {
        UserCommon common = toCommon();
        Provided profile = _profile().orElse(Provided.emptyProfile);
        UserStats stats = new UserStats(_ratings().orElse(Map.of()), _count().orElse(null));
        UserTimes times = new UserTimes(_createdAt().orElse(null), _seenAt().orElse(null), _playTime().map(PlayTime::total).orElse(null), _playTime().map(PlayTime::tv).orElse(null));
        UserFlags flags = new UserFlags(
                _tosViolation().orElse(false),
                _disabled().orElse(false),
                _verified().orElse(false),
                _streaming().orElse(false));
        URI url = _url().orElse(null);
        UserProfile profileUser = new UserProfile(common, profile, stats, times, flags, url);
        User user = _playing().map(playingUrl -> (User) new PlayingUser(profileUser, playingUrl)).orElse(profileUser);
        return user;
    }

    public UserAuth toUserAuth() {
        User user = toUser();
        UserAuthFlags auth = new UserAuthFlags(
                _followable().orElse(false),
                _following().orElse(false),
                _followsYou().orElse(false),
                _blocking().orElse(false));
        return new UserProfileAuth(user, auth);
    }

    public UserStatus toUserStatus() {
        UserCommon common = toCommon();
        UStatus status = new UStatus(common, _online().orElse(false), _isPlaying().orElse(false));
        UserStatus userStatus = _playingGameId().map(gameId -> (UserStatus) new PlayingStatus(status, gameId)).orElse(status);
        return userStatus;
    }

    public LiveStreamer toLiveStreamer() {
        UserCommon common = toCommon();
        return new LiveStreamer(common);
    }

    public TeamMember toTeamMember() {
        User user = toUser();
        TeamMember member = new TeamMember(user, "", _joinedTeamAt().orElse(null));
        return member;
    }


    public UserData _userData() { return this; }

    public enum UserPropertyEnum {
        id, name, username, title, patron,
        online, streaming, playing, playingUrl, playingGameId,
        tosViolation, disabled, closed, verified, trophies,
        playTime, createdAt, seenAt, url, counts, ratings,
        profile, followable, following, followsYou, blocking,
        joinedTeamAt, streamInfo, streamerInfo,

        unmapped
        ;
        public <T> Property<UserPropertyEnum, T> of(T value) { return Property.of(this, value); }
    }

    public enum ProfilePropertyEnum {
        country, location, bio, firstName, lastName, links,
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

    Optional<String> property(UserPropertyEnum key) { return property(key, String.class); }
    Optional<Boolean> propertyB(UserPropertyEnum key) { return property(key, Boolean.class); }

    <T> Optional<T> property(UserPropertyEnum key, Class<T> cls) {
        var property = properties().get(key);
        if (property == null) return Optional.empty();
        return cls.isInstance(property) ? Optional.of(cls.cast(property)) : Optional.empty();
    }

    public UserData {
        properties = properties == null
            ? Collections.unmodifiableMap(new EnumMap<>(UserPropertyEnum.class))
            : properties;
    }

    public String     _id()       { return property(id).orElse(null); }
    public String     _username() { return property(username).orElseGet(() -> _name()); }
    public String     _name()     { return property(UserPropertyEnum.name).orElseGet(() -> property(username).orElse(null)); }
    Optional<String>  _title() { return property(title); }
    Optional<Boolean> _patron() { return propertyB(patron); }
    Optional<Boolean> _online() { return propertyB(online); }
    Optional<Boolean> _streaming() { return propertyB(streaming); }
    Optional<Boolean> _isPlaying() { return propertyB(playing); }
    Optional<URI>     _playing() { return property(playingUrl, URI.class); }
    Optional<String>  _playingGameId() { return property(playingGameId); }
    Optional<Boolean> _tosViolation() { return propertyB(tosViolation); }
    Optional<Boolean> _disabled() { return propertyB(disabled); }
    Optional<Boolean> _verified() { return propertyB(verified); }
    Optional<List<Trophy>> _trophies() { return property(trophies, Trophies.class).map(Trophies::trophies); }
    Optional<PlayTime> _playTime() { return property(playTime, PlayTime.class); }
    Optional<ZonedDateTime> _createdAt() { return property(createdAt, ZonedDateTime.class); }
    Optional<ZonedDateTime> _seenAt() { return property(seenAt, ZonedDateTime.class); }
    Optional<URI>     _url() { return property(url, URI.class); }
    Optional<UserCount> _count() { return property(counts, UserCount.class); }
    Optional<Map<StatsPerfType, StatsPerf>> _ratings() { return property(ratings, Ratings.class).map(Ratings::ratings); }
    Optional<Provided> _profile() { return property(profile, Provided.class); }
    Optional<Boolean> _followable() { return propertyB(followable); }
    Optional<Boolean> _following() { return propertyB(following); }
    Optional<Boolean> _followsYou() { return propertyB(followsYou); }
    Optional<Boolean> _blocking() { return propertyB(blocking); }
    Optional<ZonedDateTime> _joinedTeamAt() { return property(joinedTeamAt, ZonedDateTime.class); }
    Optional<StreamInfo> _liveStreamInfo() { return property(streamInfo, StreamInfo.class); }
    Optional<StreamerInfo> _liveStreamerInfo() { return property(streamerInfo, StreamerInfo.class); }

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

    public record Provided(Map<ProfilePropertyEnum, ?> properties) {

        public static final Provided emptyProfile = new Provided(Map.of());

        public Optional<String> country() { return property(country); }
        public Optional<String> location() { return property(location); }
        public Optional<String> bio() { return property(bio); }
        public Optional<String> firstName() { return property(firstName); }
        public Optional<String> lastName() { return property(lastName); }
        public Optional<String> links() { return property(links); }
        public Optional<Integer> ratingFide() { return propertyI(ratingFide); }
        public Optional<Integer> ratingUscf() { return propertyI(ratingUscf); }
        public Optional<Integer> ratingEcf() { return propertyI(ratingEcf); }
        public Optional<Integer> ratingRcf() { return propertyI(ratingRcf); }
        public Optional<Integer> ratingCfc() { return propertyI(ratingCfc); }
        public Optional<Integer> ratingDsb() { return propertyI(ratingDsb); }

        Optional<String> property(ProfilePropertyEnum key) { return property(key, String.class); }
        Optional<Integer> propertyI(ProfilePropertyEnum key) { return property(key, Integer.class); }

        <T> Optional<T> property(ProfilePropertyEnum key, Class<T> cls) {
            var property = properties().get(key);
            if (property == null) return Optional.empty();
            return cls.isInstance(property) ? Optional.of(cls.cast(property)) : Optional.empty();
        }
    }


    public record PlayTime(Duration total, Duration tv) {}
    public record StreamInfo(String service, String status, String lang) {}
    public record StreamerInfo(Map<StreamerInfoPropertyEnum, ?> properties) {
        public String name()              { return property(StreamerInfoPropertyEnum.name).orElse(null); }
        public String headline()          { return property(headline).orElse(null); }
        public String description()       { return property(description).orElse(null); }

        public Optional<String> twitch()  { return property(twitch); }
        public Optional<String> youtube() { return property(youtube); }
        public Optional<String> image()   { return property(image); }

        Optional<String> property(StreamerInfoPropertyEnum key) { return property(key, String.class); }

        <T> Optional<T> property(StreamerInfoPropertyEnum key, Class<T> cls) {
            var property = properties().get(key);
            if (property == null) return Optional.empty();
            return cls.isInstance(property) ? Optional.of(cls.cast(property)) : Optional.empty();
        }

        public StreamerInfo withAddedProperty(Property<StreamerInfoPropertyEnum, ?> property)   {
            var mutable = new EnumMap<StreamerInfoPropertyEnum, Object>(properties());
            mutable.put(property.key(), property.value());
            return new StreamerInfo(Collections.unmodifiableMap(mutable));
        }
    }
}
