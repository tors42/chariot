package chariot.model;

import chariot.model.Enums.VariantName;

public record Variant (VariantName key, String name, String shortname) {}
