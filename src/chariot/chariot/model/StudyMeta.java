package chariot.model;

import java.time.ZonedDateTime;

public record StudyMeta(String id, String name, ZonedDateTime createdAt, ZonedDateTime updatedAt) {}
