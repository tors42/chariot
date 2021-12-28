package chariot.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static java.util.Objects.requireNonNull;

import chariot.Client.Scope;

public record TokenBulkResult(Map<String, TokenInfo> map) implements Model {
    public record TokenInfo(String userId, List<Scope> scopes) {

        public TokenInfo {
            requireNonNull(userId);
            requireNonNull(scopes);
            scopes = List.copyOf(scopes);
        }

        public TokenInfo(String userId, String scopesString) {
            this(userId, Arrays.stream(scopesString.split(",")).map(Scope::fromString).filter(Optional::isPresent).map(Optional::get).toList());
        }
    }
}
