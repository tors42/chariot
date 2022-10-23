package chariot.model;

public record ExternalEngineRequest(
        String id,
        ExternalEngineWork work,
        ExternalEngineInfo engine
        ) {}
