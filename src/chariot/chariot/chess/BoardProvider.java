package chariot.chess;

import module java.base;

import chariot.internal.chess.InternalBoardProvider;

public interface BoardProvider {

    Set<String> supportedVariants();

    default boolean supports(String variant) {
        return supportedVariants().contains(variant);
    }

    Board init(String variant);
    Board fromFEN(String variant, String fen);

    default String positionsByMirroredPieces(String pieces) {
        return "%s/%s/%s/%s/%s".formatted(
                pieces.toLowerCase(Locale.ROOT),
                "p".repeat(8),
                "8/8/8/8",
                "P".repeat(8),
                pieces.toUpperCase(Locale.ROOT));
    }

    static Map<String, BoardProvider> providers() {
        return ServiceLoader.load(BoardProvider.class).stream()
            .map(ServiceLoader.Provider::get)
            // Sort so that InternalBoardProvider ends up last,
            // so that a user provided provider will be used instead
            .sorted(Comparator.comparing(provider -> provider instanceof InternalBoardProvider))
            .<Map.Entry<String, BoardProvider>>mapMulti((provider, mapper) ->
                    provider.supportedVariants().stream().forEach(str ->
                        mapper.accept(Map.entry(str, provider))))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (firstMapping, _) -> firstMapping));
    }
}
