package it.broadcastapi;

import chariot.ClientAuth;
import chariot.api.BroadcastsApiAuth.BroadcastBuilder;
import chariot.model.*;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.net.*;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.*;

import com.sun.net.httpserver.*;

public class PushAndSyncPgn {

    static ClientAuth client = IT.diego();

    @IntegrationTest
    public void pushPgn() {
        List<List<PGN>> roundPgns = generateRounds();
        List<String> roundPgnStrings = roundPgns.stream()
            .map(round -> round.stream()
                    .map(PGN::toString)
                    .collect(Collectors.joining("\n\n")))
            .toList();

        var broadcast = createBroadcast(List.of());
        var rounds    = createRoundsForPush(broadcast.id(), roundPgns);
        for (int i = 0; i < 3; i++) {
            client.broadcasts().pushPgnByRoundId(rounds.get(i).id(), roundPgnStrings.get(i));
        }
        exportedPgnMatchesExpectedPgn(
                client.broadcasts().exportPgn(broadcast.id()).stream().toList(),
                roundPgns.stream().flatMap(List::stream).toList(),
                List.of());
    }

    record Replacement(String name, Player replacement) {}

    List<Replacement> generateReplacements() { return List.of(
            new Replacement("Player One",   new Player("Substitute One", Opt.empty(), Opt.empty(),  Opt.empty())),
            new Replacement("Player Two",   new Player("Substitute Two", Opt.empty(), Opt.of(2000), Opt.of("WIM"))),
            new Replacement("Player Three", new Player("Player Three",   Opt.empty(), Opt.of(1840), Opt.empty())),
            new Replacement("Player Four",  new Player("Player Four", Opt.of(309095), Opt.empty(), Opt.empty())));
    }

    @IntegrationTest
    public void pushPgnReplacements() {
        List<List<PGN>> roundPgns = generateRounds();
        List<String> roundPgnStrings = roundPgns.stream()
            .map(round -> round.stream()
                    .map(PGN::toString)
                    .collect(Collectors.joining("\n\n")))
            .toList();

        List<Replacement> replacements = generateReplacements();

        var broadcastReplacement = createBroadcast(replacements);
        var replaceRounds        = createRoundsForPush(broadcastReplacement.id(), roundPgns);
        for (int i = 0; i < 3; i++) {
            client.broadcasts().pushPgnByRoundId(replaceRounds.get(i).id(), roundPgnStrings.get(i));
        }
        exportedPgnMatchesExpectedPgn(
                client.broadcasts().exportPgn(broadcastReplacement.id()).stream().toList(),
                roundPgns.stream().flatMap(List::stream).toList(),
                replacements);
    }

    // @IntegrationTest(expectedSeconds = 80)
    public void syncUrlWithAndWithoutReplacements() {
        List<List<PGN>> roundPgns = generateRounds();
        List<Replacement> replacements = generateReplacements();

        record PollsAndPgnString(Semaphore poll, String pgn) {}

        Map<String, PollsAndPgnString> roundToPgn = namedPgnRounds(roundPgns).stream()
            .flatMap(entry -> Stream.of(
                        new RoundNameAndPgn("normal/" + entry.name(), entry.pgn()),
                        new RoundNameAndPgn("replace/" + entry.name(), entry.pgn())))
            .collect(Collectors.toMap(
                        RoundNameAndPgn::name,
                        nameAndPgn -> new PollsAndPgnString(new Semaphore(0), nameAndPgn.pgn())));

        var httpContext = createPgnContext("/it/", exchange -> {
            //System.out.println("Request Path: " + exchange.getRequestURI().getPath());
            String decodedPath = URLDecoder.decode(exchange.getRequestURI().getPath(), Charset.defaultCharset());
            String round = decodedPath.substring("/it/".length());
            //System.out.println("Poll Round: " + round);
            var pollsAndPgn = roundToPgn.get(round);
            //System.out.println("Responding:\n" + pollsAndPgn.pgn());
            byte[] bytes = pollsAndPgn.pgn().getBytes();
            exchange.getResponseHeaders().put("content-type", List.of("application/x-chess-pgn"));
            exchange.sendResponseHeaders(200, bytes.length);
            exchange.getResponseBody().write(bytes);
            exchange.close();
            pollsAndPgn.poll().release();
        });

        var serverAddress = httpContext.getServer().getAddress();
        URI syncUrlBaseNormal  = URI.create("http://" + serverAddress.getHostName() + "/it/normal/");
        URI syncUrlBaseReplace = URI.create("http://" + serverAddress.getHostName() + "/it/replace/");

        var normalBroadcasts = List.of(
                createBroadcast(List.of()),
                createBroadcast(List.of()),
                createBroadcast(List.of()));

        var replacementBroadcasts = List.of(
                createBroadcast(replacements),
                createBroadcast(replacements),
                createBroadcast(replacements));

        for (int i = 0; i < 3; i++) {
            createRoundsForSync(normalBroadcasts.get(i).id(),      List.of(roundPgns.get(i)), syncUrlBaseNormal);
            createRoundsForSync(replacementBroadcasts.get(i).id(), List.of(roundPgns.get(i)), syncUrlBaseReplace);
        }

        int neededNumberOfPolls = 3;
        try {
            for (int i = 0; i < neededNumberOfPolls; i++) {
                System.out.println("Waiting for lila to poll " + (i+1) + "/" + neededNumberOfPolls);
                for (var poll : roundToPgn.values().stream().map(PollsAndPgnString::poll).toList()) {
                    if (! poll.tryAcquire(1, 2, TimeUnit.MINUTES)) {
                        fail("Missing poll");
                        return;
                    }
                }
            }
        } catch(InterruptedException ie) {}
        httpContext.getServer().stop(0);

        for (int i = 0; i < 3; i++) {
            // No replacements
            exportedPgnMatchesExpectedPgn(
                    client.broadcasts().exportPgn(normalBroadcasts.get(i).id()).stream().toList(),
                    roundPgns.get(i),
                    List.of());

            // Replacements
            exportedPgnMatchesExpectedPgn(
                    client.broadcasts().exportPgn(replacementBroadcasts.get(i).id()).stream().toList(),
                    roundPgns.get(i),
                    replacements);
        }
    }

    List<List<PGN>> generateRounds() {
        var player1 = new Player("Player One",   Opt.empty(), Opt.of(1200), Opt.empty());
        var player2 = new Player("Player Two",   Opt.empty(), Opt.empty(),  Opt.empty());
        var player3 = new Player("Player Three", Opt.empty(), Opt.of(1800), Opt.of("WFM"));
        var player4 = new Player("Player Four",  Opt.empty(), Opt.of(2300), Opt.of("WGM"));

        var matchup1 = new Matchup(player1, player2, "1. d4 d5 2. e4 e5 1-0");
        var matchup2 = new Matchup(player3, player4, "1. a3 e5 2. h3 d5 1/2-1/2");
        var matchup3 = new Matchup(player3, player1, "1. c4 e5 0-1");
        var matchup4 = new Matchup(player2, player4, "1. b3 d5 2. Nc3 d4 3. Ne4 1/2-1/2");
        var matchup5 = new Matchup(player4, player1, "1. f4 1-0");
        var matchup6 = new Matchup(player2, player3, "1. d4 e6 2. e4 d5 1-0");

        var round1 = List.of(matchup1, matchup2);
        var round2 = List.of(matchup3, matchup4);
        var round3 = List.of(matchup5, matchup6);

        var rounds = List.of(round1, round2, round3);

        return roundsAsPgn(rounds);
    }

    void createRoundsForSync(String broadcastId, List<List<PGN>> roundPgns, URI syncUrlBase) {
        var superadmin = IT.superadmin(); // to be able to set "period"
        namedPgnRounds(roundPgns).stream()
            .map(nameAndPgn -> superadmin.broadcasts().createRound(broadcastId, p -> p
                        .name(nameAndPgn.name())
                        .syncUrl(syncUrlBase.resolve(URLEncoder.encode(nameAndPgn.name(), Charset.defaultCharset())).toString())
                        .period(Duration.ofSeconds(2)) // needs Study/Broadcast Admin
                        .startsAt(now -> now.minusHours(3))))
            .findFirst().filter(one -> one instanceof Entry).orElseThrow();
    }

    List<MyRound> createRoundsForPush(String broadcastId, List<List<PGN>> roundPgns) {
        return namedPgnRounds(roundPgns).stream()
            .map(nameAndPgn -> client.broadcasts().createRound(broadcastId, p -> p.name(nameAndPgn.name())))
            .filter(one -> one instanceof Entry)
            .map(One::get)
            .toList();
    }

    void exportedPgnMatchesExpectedPgn(List<PGN> exportedPgns, List<PGN> sourcePgns, List<Replacement> replacements) {
        assertEquals(sourcePgns.size(), exportedPgns.size(), "Wrong number of PGNs");
        for (int i = 0;  i < sourcePgns.size(); i++) {
            var sourcePgn = sourcePgns.get(i);
            var exportedPgn = exportedPgns.get(i);

            var replacedSourcePgn = applyReplacementsToSourcePgn(sourcePgn, replacements);
            var filteredExportedPgn = filterExportedSortedPgn(exportedPgn);

            assertEquals(replacedSourcePgn.toString(), filteredExportedPgn.toString());
        }
    }

    static Set<String> tagsToValidate = Set.of(
            "Round", "Board", "Result",
            "White", "WhiteElo", "WhiteTitle", "WhiteFideId",
            "Black", "BlackElo", "BlackTitle", "BlackFideId"
            );

    PGN filterExportedSortedPgn(PGN pgn) {
        return pgn.filterTags((tag, _) -> tagsToValidate.contains(tag))
            .withTags(s -> s.sorted(Map.Entry.comparingByKey()));
    }

    PGN applyReplacementsToSourcePgn(PGN pgn, List<Replacement> replacements) {
        var newMap = new HashMap<String,String>(pgn.tags());
        replacements.stream().filter(r -> r.name().equals(newMap.get("White")) || r.name().equals(newMap.get("Black")))
            .forEach(replacement -> {
                String color = newMap.get("White").equals(replacement.name()) ? "White" : "Black";
                if (replacement.replacement() instanceof Player(var newName, var fideOpt, var ratingOpt, var titleOpt)) {
                    newMap.put(color, newName);
                    if (fideOpt instanceof Some(var value)) newMap.put(color + "FideId", String.valueOf(value));
                    if (ratingOpt instanceof Some(var value)) newMap.put(color + "Elo", String.valueOf(value));
                    if (titleOpt instanceof Some(var value)) newMap.put(color + "Title", value);
                }
            });

        return pgn.withTags(newMap)
            .withTags(s -> s.sorted(Map.Entry.comparingByKey()));
    }

    record RoundNameAndPgn(String name, String pgn) {}
    record Player(String name, Opt<Integer> fideId, Opt<Integer> rating, Opt<String> title) {}
    record Matchup(Player white, Player black, String moves) {
        PGN toPgnAtRoundAndBoard(int round, int board) {
            Map<String,String> present = Map.ofEntries(
                    Map.entry("Round", String.valueOf(round)),
                    Map.entry("Board", String.valueOf(board)),
                    Map.entry("Result", Arrays.asList(moves().split(" ")).getLast()),
                    Map.entry("White", white().name()),
                    Map.entry("Black", black().name()));
            Map<String, String> optional = Stream.<List<Map.Entry<String,String>>>of(
                    white.rating() instanceof Some(var rating) ? List.of(Map.entry("WhiteElo", String.valueOf(rating))) : List.of(),
                    white.title() instanceof Some(var title)   ? List.of(Map.entry("WhiteTitle", title)) : List.of(),
                    black.rating() instanceof Some(var rating) ? List.of(Map.entry("BlackElo", String.valueOf(rating))) : List.of(),
                    black.title() instanceof Some(var title)   ? List.of(Map.entry("BlackTitle", title)) : List.of())
                .flatMap(List::stream)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
            return PGN.read(moves())
                .withTags(present)
                .addTags(optional)
                .withTags(s -> s.sorted(Map.Entry.comparingByKey()));
        }
    }

    List<List<PGN>> roundsAsPgn(List<List<Matchup>> rounds) {
        return IntStream.range(0, rounds.size())
            .mapToObj(roundNum -> IntStream.range(0, rounds.get(roundNum).size())
                    .mapToObj(boardNum -> rounds.get(roundNum).get(boardNum)
                        .toPgnAtRoundAndBoard(roundNum+1, boardNum+1))
                    .toList()
        ) .toList();
    }

    Broadcast createBroadcast(List<Replacement> replacements) {
        String suffix = replacements.isEmpty()
            ? "without replacements"
            : "with replacements";

        Consumer<BroadcastBuilder> params = bb -> bb
            .name("Broadcast " + suffix)
            .description("Testing out replacements and this is " + suffix);

        if (! replacements.isEmpty()) {
            params = params.andThen(bb -> bb
                    .players(replacements.stream()
                        .map(r -> String.join(" / ",
                                r.name(),
                                r.replacement().fideId().map(String::valueOf).orElse(""),
                                r.replacement().title().orElse(""),
                                r.replacement().rating().map(String::valueOf).orElse(""),
                                !r.name().equals(r.replacement().name()) ? r.replacement.name() : "")
                            )
                        .collect(Collectors.joining("\n"))));
        }

        return client.broadcasts().create(params).get();
    }

    List<RoundNameAndPgn> namedPgnRounds(List<List<PGN>> rounds) {
        return rounds.stream()
            .map(roundPgns -> new RoundNameAndPgn(
                        "Round " + roundPgns.getFirst().tags().get("Round"),
                        roundPgns.stream().map(PGN::toString).collect(Collectors.joining("\n\n"))
                    )).toList();

    }

    static HttpContext createPgnContext(String path, HttpHandler handler) {
        try {
            var server = HttpServer.create(new InetSocketAddress(InetAddress.getLocalHost(), 80), 0);
            var ctx = server.createContext(path, handler);
            server.start();
            return ctx;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
