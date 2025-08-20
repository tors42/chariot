package chariot.model;
import java.time.ZonedDateTime;

import chariot.model.Enums.*;

public record ChallengeAI(
        String id,
        Speed speed,
        String fen,
        Color player,
        ZonedDateTime createdAt
        ){}
