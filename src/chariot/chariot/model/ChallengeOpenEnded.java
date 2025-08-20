package chariot.model;

import java.util.List;

public record ChallengeOpenEnded(
        ChallengeInfo challenge,
        String urlWhite,
        String urlBlack,
        Players players,
        List<String> rules) {

    public sealed interface Players permits Any, Reserved {}
    public record Any() implements Players {}
    public record Reserved(String userIdWhite, String userIdBlack) implements Players {}
}
