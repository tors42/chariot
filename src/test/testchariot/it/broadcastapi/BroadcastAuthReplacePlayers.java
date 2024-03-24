package it.broadcastapi;

import chariot.ClientAuth;
import chariot.api.BroadcastsAuth.BroadcastBuilder;
import chariot.model.*;
import chariot.model.Pgn.Tag;
import util.IntegrationTest;
import util.IT;

import static util.Assert.*;

import java.net.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.stream.*;

public class BroadcastAuthReplacePlayers {

    static ClientAuth client = IT.diego();

    sealed interface Replacement { String name(); }

    record FideId(String name, int id) implements Replacement {}
    record Manual(String name, Player replacement) implements Replacement {}

    @IntegrationTest
    public void replacePlayers() {
        var player1 = new Player("Player One",   Opt.of(1200), Opt.empty());
        var player2 = new Player("Player Two",   Opt.empty(),  Opt.empty());
        var player3 = new Player("Player Three", Opt.of(1800), Opt.of("WFM"));
        var player4 = new Player("Player Four",  Opt.of(2300), Opt.of("WGM"));

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

        List<List<Pgn>> roundPgns = roundsAsPgn(rounds);

        // push pgn seems to not be a use-case for player replacement...
        //createRoundsAndPushPgn(noReplacementsBroadcast.id(), roundPgns);

        record PollsAndPgnString(Semaphore poll, String pgn) {}

        Map<String, PollsAndPgnString> roundToPgn = namedPgnRounds(roundPgns).stream()
            .flatMap(entry -> Stream.of(
                        new RoundNameAndPgn("normal/" + entry.name(), entry.pgn()),
                        new RoundNameAndPgn("replace/" + entry.name(), entry.pgn())))
            .collect(Collectors.toMap(
                        RoundNameAndPgn::name,
                        nameAndPgn -> new PollsAndPgnString(new Semaphore(0), nameAndPgn.pgn()))
                    );

        var httpContext = BroadcastAuth.createPgnContext("/it/", exchange -> {
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

        var replacement1 = new Manual(player1.name(), new Player("Substitute One", Opt.empty(), Opt.empty()));
        var replacement2 = new Manual(player2.name(), new Player("Substitute Two", Opt.of(2000), Opt.of("WIM")));
        var replacement3 = new Manual(player3.name(), new Player(player3.name(), Opt.of(1840), Opt.empty()));
        var replacement4 = new FideId(player4.name(), 309095);

        List<Replacement> noReplacements = List.of();
        List<Replacement> replacements   = List.of(replacement1, replacement2, replacement3, replacement4);

        // 3 active rounds in the same broadcast seems to not be a use-case for polling sync urls
        //createRoundsForSync(noReplacementsBroadcast.id(), roundPgns, syncUrlBase);

        // Create 1 broadcast per round instead...
        if (! (createBroadcast(noReplacements) instanceof Entry(var broadcastNormal1)
            && createBroadcast(noReplacements) instanceof Entry(var broadcastNormal2)
            && createBroadcast(noReplacements) instanceof Entry(var broadcastNormal3)
            && createBroadcast(replacements)   instanceof Entry(var broadcastReplacement1)
            && createBroadcast(replacements)   instanceof Entry(var broadcastReplacement2)
            && createBroadcast(replacements)   instanceof Entry(var broadcastReplacement3))) {
            fail("Couldn't create broadcasts");
            return;
        }

        var normalBroadcasts = List.of(broadcastNormal1, broadcastNormal2, broadcastNormal3);
        var replacementBroadcasts = List.of(broadcastReplacement1, broadcastReplacement2, broadcastReplacement3);

        for (int i = 0; i < normalBroadcasts.size(); i++) {
            createRoundsForSync(normalBroadcasts.get(i).id(), List.of(roundPgns.get(i)), syncUrlBaseNormal);
        }
        for (int i = 0; i < replacementBroadcasts.size(); i++) {
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

        // No replacements
        for (int i = 0; i < normalBroadcasts.size(); i++) {
            exportedPgnMatchesExpectedPgn(
                    client.broadcasts().exportPgn(normalBroadcasts.get(i).id()).stream().toList(),
                    roundPgns.get(i),
                    noReplacements);
        }

        // Replacements
        for (int i = 0; i < replacementBroadcasts.size(); i++) {
            exportedPgnMatchesExpectedPgn(
                    client.broadcasts().exportPgn(replacementBroadcasts.get(i).id()).stream().toList(),
                    roundPgns.get(i),
                    replacements);
        }
    }

    static Set<String> tagsToValidate = Set.of(
            "Round", "Board", "Result",
            "White", "WhiteElo", "WhiteTitle", "WhiteFideId",
            "Black", "BlackElo", "BlackTitle", "BlackFideId"
            );

    void exportedPgnMatchesExpectedPgn(List<Pgn> exportedPgns, List<Pgn> sourcePgns, List<Replacement> replacements) {
        assertEquals(sourcePgns.size(), exportedPgns.size(), "Wrong number of PGNs");
        for (int i = 0;  i < sourcePgns.size(); i++) {
            var sourcePgn = sourcePgns.get(i);
            var exportedPgn = exportedPgns.get(i);

            var replacedSourcePgn = applyReplacementsToSourcePgn(sourcePgn, replacements);
            var filteredExportedPgn = filterExportedPgn(exportedPgn);

            assertEquals(replacedSourcePgn.toString(), filteredExportedPgn.toString());
        }
    }

    Pgn filterExportedPgn(Pgn pgn) {
        return Pgn.of(pgn.tags().stream()
                .filter(tag -> tagsToValidate.contains(tag.name()))
                .sorted(Comparator.comparing(Tag::name))
                .toList(),
                pgn.moves());
    }

    Pgn applyReplacementsToSourcePgn(Pgn pgn, List<Replacement> replacements) {
        var newMap = new HashMap<String,String>(pgn.tagMap());
        replacements.stream().filter(r -> r.name().equals(newMap.get("White")) || r.name().equals(newMap.get("Black")))
            .forEach(replacement -> {
                String color = newMap.get("White").equals(replacement.name()) ? "White" : "Black";
                switch(replacement) {
                    case FideId(var name, int fideId) -> newMap.put(color + "FideId", String.valueOf(fideId));
                    case Manual(var name, Player(var newName, var rating, var title)) -> {
                        newMap.put(color, newName);
                        if (rating instanceof Some(var value)) newMap.put(color + "Elo", String.valueOf(value));
                        if (title instanceof Some(var value)) newMap.put(color + "Title", value);
                    }
                }
            });

        return Pgn.of(newMap.entrySet().stream()
                .map(entry -> Tag.of(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparing(Tag::name))
                .toList(),
                pgn.moves());
    }

    void createRoundsForSync(String broadcastId, List<List<Pgn>> roundPgns, URI syncUrlBase) {
        namedPgnRounds(roundPgns).stream()
            .map(nameAndPgn -> client.broadcasts().createRound(broadcastId, p -> p
                        .name(nameAndPgn.name())
                        .syncUrl(syncUrlBase.resolve(URLEncoder.encode(nameAndPgn.name(), Charset.defaultCharset())).toString())
                        .startsAt(now -> now.minusHours(3))))
            .findFirst().filter(one -> one instanceof Entry).orElseThrow();
    }

    record RoundNameAndPgn(String name, String pgn) {}
    record Player(String name, Opt<Integer> rating, Opt<String> title) {}
    record Matchup(Player white, Player black, String moves) {
        Pgn toPgnAtRoundAndBoard(int round, int board) {
            Stream<Tag> present = Stream.of(
                    Tag.of("Round", String.valueOf(round)),
                    Tag.of("Board", String.valueOf(board)),
                    Tag.of("Result", Arrays.asList(moves().split(" ")).getLast()),
                    Tag.of("White", white().name()),
                    Tag.of("Black", black().name()));
            Stream<Opt<Tag>> optional = Stream.of(
                    white.rating() instanceof Some(var rating) ? Opt.of(Tag.of("WhiteElo", String.valueOf(rating))) : Opt.empty(),
                    white.title() instanceof Some(var title)   ? Opt.of(Tag.of("WhiteTitle", title)) : Opt.empty(),
                    black.rating() instanceof Some(var rating) ? Opt.of(Tag.of("BlackElo", String.valueOf(rating))) : Opt.empty(),
                    black.title() instanceof Some(var title)   ? Opt.of(Tag.of("BlackTitle", title)) : Opt.empty());
            List<Tag> tags = Stream.concat(present, optional
                    .filter(ot -> ot instanceof Some<Tag>)
                    .map(ot -> (Some<Tag>) ot)
                    .map(Some::value))
                .sorted(Comparator.comparing(Tag::name))
                .toList();
            return Pgn.of(tags, moves());
        }
    }

    List<List<Pgn>> roundsAsPgn(List<List<Matchup>> rounds) {
        return IntStream.range(0, rounds.size())
            .mapToObj(roundNum -> IntStream.range(0, rounds.get(roundNum).size())
                    .mapToObj(boardNum -> rounds.get(roundNum).get(boardNum)
                        .toPgnAtRoundAndBoard(roundNum+1, boardNum+1))
                    .toList()
        ) .toList();
    }

    One<Broadcast> createBroadcast(List<Replacement> replacements) {
        String suffix = replacements.isEmpty()
            ? "without replacements"
            : "with replacements";

        Consumer<BroadcastBuilder> params = bb -> bb
            .name("Broadcast " + suffix)
            .shortDescription("A broadcast " + suffix)
            .longDescription("Testing out replacements and this is " + suffix);

        if (! replacements.isEmpty()) {
            params = params.andThen(bb -> bb
                    .players(replacements.stream()
                        .map(r -> switch(r) {
                            case FideId(var name, int fideId) -> name + " = " + fideId;
                            case Manual(var name, var replacement) ->
                                String.join(" / ",
                                        name,
                                        replacement.rating() instanceof Some(var rating) ? String.valueOf(rating) : "",
                                        replacement.title() instanceof Some(var title) ? title : "",
                                        !name.equals(replacement.name()) ? replacement.name() : "");
                                })
                        .collect(Collectors.joining("\n"))));
        }

        return client.broadcasts().create(params);
    }

    List<RoundNameAndPgn> namedPgnRounds(List<List<Pgn>> rounds) {
        return rounds.stream().map(roundPgns -> new RoundNameAndPgn(
                    "Round " + roundPgns.getFirst().tagMap().get("Round"),
                    roundPgns.stream().map(Pgn::toString).collect(Collectors.joining("\n\n"))
                    )).toList();

    }

    void createRoundsAndPushPgn(String broadcastId, List<List<Pgn>> rounds) {
        record IdAndPgn(String id, String pgn) {}
        namedPgnRounds(rounds).stream()
            .map(nameAndPgn -> new IdAndPgn(
                        client.broadcasts().createRound(broadcastId, p -> p.name(nameAndPgn.name()))
                        .map(MyRound::id).orElse(""),
                        nameAndPgn.pgn()))
            .filter(idAndPgn -> !idAndPgn.id().isEmpty())
            .forEach(idAndPgn -> client.broadcasts().pushPgnByRoundId(idAndPgn.id(), idAndPgn.pgn()));
    }
}
