package chariot.model;

public sealed interface TokenResult {
    record AccessToken(String access_token, String token_type) implements TokenResult {};
    record Error(String error, String error_description) implements TokenResult {};
}
