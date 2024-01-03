package build.snippets;

import chariot.Client;
import chariot.model.*;

class OpeningExplorer {

    static Client client = Client.basic();

    public static void main(String[] args) {
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

    static int choose() {
        var console = System.console();
        if (console == null) return -1;

        try {
            return Integer.parseInt(console.readLine());
        } catch(Exception e) {
            return -1;
        }
    }

    static void masters() {
        // @start region="masters"
        ExploreResult.OpeningDB result = client.openingExplorer().masters(params -> params
                .play("e2e4,d7d6,d2d4,e7e5")
                ).maybe().orElseThrow();
        String opening = result.opening().map(o -> STR."ECO \{o.eco()} - \{o.name()}").orElse("No matching opening");
        // ECO B07 - King's Pawn Game: Maróczy Defense
        System.out.println(STR."Opening: \{opening}"); // @replace regex='.*' replacement=''
        long numGames = result.white() + result.draws() + result.black();
        // 534
        System.out.println(STR."Number or games: \{numGames}"); // @replace regex='.*' replacement=''
        String topThreeMoves = String.join("\n", result.moves().stream()
                .limit(3)
                .map(move -> STR."\{move.san()}: White win count \{move.white()} - Draw count \{move.draws()} - Black win count \{move.black()}")
                .toList());
        // Nf3: White win count 192 - Draw count 108 - Black win count: 63
        // dxe5: White win count 42 - Draw count 56 - Black win count: 28
        // Ne2: White win count 5 - Draw count 5 - Black win count: 3
        System.out.println(STR."Top 3 moves: \{topThreeMoves}"); // @replace regex='.*' replacement=''
        String topGameId = result.topGames().getFirst().id();
        // p1lHx7rU
        System.out.println(STR."Top game id: \{topGameId}"); // @replace regex='.*' replacement=''
        // @end region="masters"
    }

    static void lichess() {
        // @start region="lichess"
        ExploreResult.OpeningDB result = client.openingExplorer().lichess(params -> params
                .play("e2e4,d7d6,d2d4,e7e5")
                ).maybe().orElseThrow();

        String opening = result.opening().map(o -> STR."ECO \{o.eco()} - \{o.name()}").orElse("No matching opening");
        // ECO B07 - King's Pawn Game: Maróczy Defense
        System.out.println(STR."Opening: \{opening}"); // @replace regex='.*' replacement=''
        long numGames = result.white() + result.draws() + result.black();
        // 10848227
        System.out.println(STR."Number or games: \{numGames}"); // @replace regex='.*' replacement=''
        String topThreeMoves = String.join("\n",
                result.moves().stream()
                .limit(3)
                .map(move -> STR."\{move.san()}: White win count \{move.white()} - Draw count \{move.draws()} - Black win count \{move.black()}")
                .toList());
        // dxe5: White win count 2675681 - Draw count 292613 - Black win count 2175418
        // d5: White win count 1289065 - Draw count 90856 - Black win count 1169944
        // Nf3: White win count 785463 - Draw count 57343 - Black win count 620240
        System.out.println(STR."Top 3 moves: \{topThreeMoves}"); // @replace regex='.*' replacement=''
        String topGameId = result.topGames().getFirst().id();
        // g8xbSjJp
        System.out.println(STR."Top game id: \{topGameId}"); // @replace regex='.*' replacement=''
        // @end region="lichess"
    }

    static void player() {
        // @start region="player"
        ExploreResult.OpeningPlayer result = client.openingExplorer().player("lance5500", params -> params
                .play("e2e4,d7d6,d2d4,e7e5")
                ).maybe().orElseThrow();

        String opening = result.opening().map(o -> STR."ECO \{o.eco()} - \{o.name()}").orElse("No matching opening");
        // ECO B07 - King's Pawn Game: Maróczy Defense
        System.out.println(STR."Opening: \{opening}"); // @replace regex='.*' replacement=''
        long numGames = result.white() + result.draws() + result.black();
        // 93
        System.out.println(STR."Number or games: \{numGames}"); // @replace regex='.*' replacement=''
        String topThreeMoves = String.join("\n",
                result.moves().stream()
                .limit(3)
                .map(move -> STR."\{move.san()}: White win count \{move.white()} - Draw count \{move.draws()} - Black win count \{move.black()}")
                .toList());
        // Ne2: White win count 54 - Draw count 3 - Black win count 3
        // Nf3: White win count 16 - Draw count 1 - Black win count 2
        // dxe5: White win count 5 - Draw count 3 - Black win count 2
        System.out.println(STR."Top 3 moves: \{topThreeMoves}"); // @replace regex='.*' replacement=''
        String recentGameId = result.recentGames().getFirst().id();
        // XS9UwMx2
        System.out.println(STR."Recent game id: \{recentGameId}"); // @replace regex='.*' replacement=''
        // @end region="player"
    }

}
