package chariot.model;

import java.util.List;

public record ExternalEngineWork(
        String sessionId,
        int threads,
        int hash,
        int multiPv,
        String variant,
        String initialFen,
        List<String> moves,
        int movetime,
        int depth,
        int nodes
        ) {}
