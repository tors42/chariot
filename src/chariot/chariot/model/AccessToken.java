package chariot.model;

public record AccessToken(String access_token, long expires_in, String token_type) {}
