///usr/bin/env java -p out/modules/ --add-modules chariot "$0" "$@" ; exit $?

import module chariot;

ClientAuth client = Client.basic().withToken(System.getenv("SNIPPET_TOKEN"));

void main() {
    while (true) {
        System.out.println("""

            Input number to run example.
            1) masters
            2) lichess
            3) player

            Anything else to exit.
            """);

        switch(choose()) {
            case 1 -> masters();
            case 2 -> lichess();
            case 3 -> player();
            default -> System.exit(0);
        }
    }
}

int choose() {
    var console = System.console();
    if (console == null) return -1;

    try {
        return Integer.parseInt(console.readLine());
    } catch(Exception e) {
        return -1;
    }
}

void masters() {
    // @start region="masters"
    ExploreResult.OpeningDB result = client.openingExplorer().masters(params -> params
            .play("e2e4,d7d6,d2d4,e7e5")
            ).maybe().orElseThrow();
    String opening = result.opening().map(o -> "ECO %s - %s".formatted(o.eco(), o.name())).orElse("No matching opening");
    // ECO B07 - King's Pawn Game: Maróczy Defense
    System.out.println("Opening: %s".formatted(opening)); // @replace regex='.*' replacement=''
    long numGames = result.white() + result.draws() + result.black();
    // 534
    System.out.println("Number or games: %s".formatted(numGames)); // @replace regex='.*' replacement=''
    String topThreeMoves = String.join("\n", result.moves().stream()
            .limit(3)
            .map(move -> "%s: White win count %d - Draw count %d - Black win count %d".formatted(move.san(), move.white(), move.draws(), move.black()))
            .toList());
    // Nf3: White win count 192 - Draw count 108 - Black win count: 63
    // dxe5: White win count 42 - Draw count 56 - Black win count: 28
    // Ne2: White win count 5 - Draw count 5 - Black win count: 3
    System.out.println("Top 3 moves: %s".formatted(topThreeMoves)); // @replace regex='.*' replacement=''
    String topGameId = result.topGames().getFirst().id();
    // p1lHx7rU
    System.out.println("Top game id: %s".formatted(topGameId)); // @replace regex='.*' replacement=''
    // @end region="masters"
}

void lichess() {
    // @start region="lichess"
    ExploreResult.OpeningDB result = client.openingExplorer().lichess(params -> params
            .play("e2e4,d7d6,d2d4,e7e5")
            ).maybe().orElseThrow();

    String opening = result.opening().map(o -> "ECO %s - %s".formatted(o.eco(), o.name())).orElse("No matching opening");
    // ECO B07 - King's Pawn Game: Maróczy Defense
    System.out.println("Opening: %s".formatted(opening)); // @replace regex='.*' replacement=''
    long numGames = result.white() + result.draws() + result.black();
    // 10848227
    System.out.println("Number or games: %d".formatted(numGames)); // @replace regex='.*' replacement=''
    String topThreeMoves = String.join("\n",
            result.moves().stream()
            .limit(3)
            .map(move -> "%s: White win count %d - Draw count %d - Black win count %d".formatted(move.san(), move.white(), move.draws(), move.black()))
            .toList());
    // dxe5: White win count 2675681 - Draw count 292613 - Black win count 2175418
    // d5: White win count 1289065 - Draw count 90856 - Black win count 1169944
    // Nf3: White win count 785463 - Draw count 57343 - Black win count 620240
    System.out.println("Top 3 moves: %s".formatted(topThreeMoves)); // @replace regex='.*' replacement=''
    String topGameId = result.topGames().getFirst().id();
    // g8xbSjJp
    System.out.println("Top game id: %s".formatted(topGameId)); // @replace regex='.*' replacement=''
    // @end region="lichess"
}

void player() {
    // @start region="player"
    ExploreResult.OpeningPlayer result = client.openingExplorer().player("lance5500", params -> params
            .play("e2e4,d7d6,d2d4,e7e5")
            ).maybe().orElseThrow();

    String opening = result.opening().map(o -> "ECO %s - %s".formatted(o.eco(), o.name())).orElse("No matching opening");
    // ECO B07 - King's Pawn Game: Maróczy Defense
    System.out.println("Opening: %s".formatted(opening)); // @replace regex='.*' replacement=''
    long numGames = result.white() + result.draws() + result.black();
    // 93
    System.out.println("Number or games: %d".formatted(numGames)); // @replace regex='.*' replacement=''
    String topThreeMoves = String.join("\n",
            result.moves().stream()
            .limit(3)
            .map(move -> "%s: White win count %d - Draw count %d - Black win count %d".formatted(move.san(), move.white(), move.draws(), move.black()))
            .toList());
    // Ne2: White win count 54 - Draw count 3 - Black win count 3
    // Nf3: White win count 16 - Draw count 1 - Black win count 2
    // dxe5: White win count 5 - Draw count 3 - Black win count 2
    System.out.println("Top 3 moves: %s".formatted(topThreeMoves)); // @replace regex='.*' replacement=''
    String recentGameId = result.recentGames().getFirst().id();
    // XS9UwMx2
    System.out.println("Recent game id: %s".formatted(recentGameId)); // @replace regex='.*' replacement=''
    // @end region="player"
}
