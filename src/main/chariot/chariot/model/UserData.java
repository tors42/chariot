package chariot.model;

import java.net.URI;
import java.time.*;
import java.util.*;

import static chariot.model.UserData.UserPropertyEnum.*;
import static chariot.model.UserData.ProfilePropertyEnum.*;
import static chariot.model.UserData.StreamerInfoPropertyEnum.*;

public record UserData(Map<UserPropertyEnum, ?> properties) implements UserAuth {

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
    public String     _username() { return property(username).orElseGet(() -> name()); }
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
    Optional<Boolean> _closed() { return propertyB(closed); }
    Optional<Boolean> _verified() { return propertyB(verified); }
    Optional<List<Trophy>> _trophies() { return property(trophies, Trophies.class).map(Trophies::trophies); }
    Optional<PlayTime> _playTime() { return property(playTime, PlayTime.class); }
    Optional<ZonedDateTime> _createdAt() { return property(createdAt, ZonedDateTime.class); }
    Optional<ZonedDateTime> _seenAt() { return property(seenAt, ZonedDateTime.class); }
    Optional<URI>     _url() { return property(url, URI.class); }
    Optional<Count> _count() { return property(counts, Count.class); }
    Optional<Map<StatsPerfType, StatsPerf>> _ratings() { return property(ratings, Ratings.class).map(Ratings::ratings); }
    Optional<Profile> _profile() { return property(profile, Profile.class); }
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

    public record Profile(Map<ProfilePropertyEnum, ?> properties) {

        public static final Profile emptyProfile = new Profile(Map.of());

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

    public record Count(
            int all, int rated, int ai,
            int draw, int drawH,
            int loss, int lossH,
            int win, int winH,
            int bookmark, int playing,
            int imported, // "import"
            int me) {}

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
