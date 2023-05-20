package chariot.model;

import java.net.URI;

public record PlayingUser(UserProfile user, URI playingUrl) implements User { }
