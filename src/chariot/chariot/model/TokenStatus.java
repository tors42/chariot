package chariot.model;

import module java.base;

public record TokenStatus(List<Valid> valid, List<String> invalid) {
    public record Valid(String token, String userId, List<String> scopes, Duration expiresIn) {}
}
