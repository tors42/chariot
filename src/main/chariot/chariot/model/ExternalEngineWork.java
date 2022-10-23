package chariot.model;

import java.util.List;

public record ExternalEngineWork(
        String sessionId,
        int threads,
        int hash,
        boolean infinite,
        int multiPv,
        String variant,
        String initialFen,
        List<String> moves
        ) {}
