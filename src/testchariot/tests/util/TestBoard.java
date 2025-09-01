package tests.util;

import module java.base;
import module chariot;
import chariot.internal.chess.NaiveChess;
import util.Test;

import static util.Assert.*;

public class TestBoard {

    static final String standardFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    public void capture() {
        String moves = "d4 e5 dxe5";

        DefaultBoard board = DefaultBoard.ofStandard().play(moves);

        assertTrue(board.pieces().captured(Side.white).equals(List.of()));
        assertTrue(board.pieces().captured(Side.black).equals(List.of(Piece.pawn)));
        assertTrue(board.pieces().all(Side.black).size() < board.pieces().all(Side.white).size());

        DefaultBoard boardIncremental = DefaultBoard.ofStandard();
        for (String move : moves.split(" "))
            boardIncremental = boardIncremental.play(move);
        assertTrue(boardIncremental.pieces().captured(Side.white).equals(List.of()));
        assertTrue(boardIncremental.pieces().captured(Side.black).equals(List.of(Piece.pawn)));
        assertTrue(boardIncremental.pieces().all(Side.black).size() < boardIncremental.pieces().all(Side.white).size());
    }

    @Test
    public void naive() {
        NaiveChess nc = NaiveChess.of("standard", FEN.standardStr);
        Collection<String> moves = nc.validMoves();
        //moves.stream().sorted().forEach(IO::println);
        assertTrue(20 == moves.size());
    }

    @Test
    public void castling() {
        Board board = Board.fromFEN("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Collection<String> moves = board.validMoves();
        //moves.stream().sorted().forEach(IO::println);
        assertTrue(moves.contains("e1c1"));
        assertTrue(moves.contains("e1g1"));
    }

    @Test
    public void castlingChess960() {
        Board boardKQ = Board.ofChess960("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");
        Board boardHA = Board.ofChess960("r3k2r/8/8/8/8/8/8/R3K2R w HAha - 0 1");
        Collection<String> movesKQ = boardKQ.validMoves();
        Collection<String> movesHA = boardHA.validMoves();
        //movesKQ.stream().sorted().forEach(IO::println);
        //movesHA.stream().sorted().forEach(IO::println);
        assertTrue(movesKQ.contains("e1a1"));
        assertTrue(movesKQ.contains("e1h1"));
        assertTrue(movesHA.contains("e1a1"));
        assertTrue(movesHA.contains("e1h1"));
    }

    @Test
    public void castlingChess960Pos10() {
        Board boardKQ = Board.ofChess960("qnnrbbkr/pppppppp/8/8/8/8/PPPPPPPP/QNNRBBKR w KQkq - 0 1");
        Board boardDH = Board.ofChess960(10);

        assertEquals("qnnrbbkr/pppppppp/8/8/8/8/PPPPPPPP/QNNRBBKR w DHdh - 0 1", boardDH.toFEN());

        String moves = "e3 e6 f3 f6 Bg3 Bg6 Bd3 Bd6 Ne2 Ne7";
        boardKQ = boardKQ.play(moves);
        boardDH = boardDH.play(moves);

        Collection<String> movesKQ = boardKQ.validMoves();
        Collection<String> movesDH = boardDH.validMoves();

        assertTrue(movesKQ.contains("g1d1"));
        assertTrue(movesKQ.contains("g1h1"));
        assertTrue(movesDH.contains("g1d1"));
        assertTrue(movesDH.contains("g1h1"));
    }

    @Test
    public void toUCI() {
        Board board = Board.ofStandard();
        assertEquals("b1c3", board.toUCI("Nc3"));
    }

    @Test
    public void standardFen() {
        Board board = Board.ofStandard();
        assertEquals(standardFen, board.toFEN());
    }

    @Test
    public void customStandardFen() {
        Board board = Board.fromFEN(standardFen);
        assertEquals(standardFen, board.toFEN());
    }

    @Test
    public void threefoldRepetition() {
        Board board = Board.ofStandard();
        board = board.play("Na3 Na6 Nb1 Nb8 Na3 Na6 Nb1");

        assertFalse(board.validMoves().isEmpty());

        // third time position shows up
        board = board.play("Nb8");
    }

    @Test
    public void disambiguationTwoQueensOnSameFile() {
        Board board = Board.fromFEN("rn1k4/pb3r2/q2b4/1p1p4/2P1p3/1K1P1p1p/P3P2P/qNBQ1B1R b -- 0 50");
        String san = board.toSAN("a6a2");

        assertEquals("Q6xa2#", san);
    }

    @Test
    public void disambiguations() {
        Board board = Board.fromFEN("k7/pp6/8/5Q2/4Q2Q/8/5Q1Q/K7 w - - 0 1");
        assertEquals("Qef4",  board.toSAN("e4f4")); // specify file
        assertEquals("Q5f4",  board.toSAN("f5f4")); // specify rank
        assertEquals("Qh2g3", board.toSAN("h2g3")); // specify file and rank
    }

    @Test
    public void castlingUciKingToRookOrFinalSquare() {
        Board boardBeforeCastling = Board.fromFEN("r3kb1r/pbpn1ppp/1p1p4/1P6/P1P2P2/4K2P/8/1q5n b kq - 3 18");

        var kingToRook = "e8a8";
        var castlingKingToRook = boardBeforeCastling.play(kingToRook);

        var kingToTarget = "e8c8";
        var castlingKingToTarget = boardBeforeCastling.play(kingToTarget);

        assertEquals("2kr1b1r/pbpn1ppp/1p1p4/1P6/P1P2P2/4K2P/8/1q5n w - - 4 19", castlingKingToTarget.toFEN());
        assertEquals(castlingKingToRook, castlingKingToTarget);
    }

    @Test
    public void cantCastleIntoCheck() {
        Board boardBeforeIllegalCastling = Board.fromFEN("rn1qk2r/p1p1p2p/1p1p2Rn/5p1P/2b1P3/3P4/PBP2PP1/RN1QKB2 b Qkq - 0 13");
        Board afterIllegalCastlingE8G8 = boardBeforeIllegalCastling.play("e8g8");
        Board afterIllegalCastlingE8H8 = boardBeforeIllegalCastling.play("e8h8");

        assertTrue(boardBeforeIllegalCastling.equals(afterIllegalCastlingE8G8));
        assertTrue(boardBeforeIllegalCastling.equals(afterIllegalCastlingE8H8));
    }


    @Test
    public void revokedCastlingRightsWhite() {
        Board whiteToMove = Board.fromFEN("r3k2r/4p3/8/8/8/8/4P3/R3K2R w KQkq - 0 1");

        assertTrue(whiteToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e1c1"));
        assertTrue(whiteToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e1g1"));

        String blackSimulatedNopMove = "e7e6";

        Board whiteToMoveAfterMovingA1 = whiteToMove.play("a1a2").play(blackSimulatedNopMove);
        assertFalse(whiteToMoveAfterMovingA1.validMoves().stream().map(m -> m.toString()).toList().contains("e1c1"));
        assertTrue(whiteToMoveAfterMovingA1.validMoves().stream().map(m -> m.toString()).toList().contains("e1g1"));

        Board whiteToMoveAfterMovingH1 = whiteToMove.play("h1h2").play(blackSimulatedNopMove);
        assertTrue(whiteToMoveAfterMovingH1.validMoves().stream().map(m -> m.toString()).toList().contains("e1c1"));
        assertFalse(whiteToMoveAfterMovingH1.validMoves().stream().map(m -> m.toString()).toList().contains("e1g1"));

        Board whiteToMoveAfterMovingKing = whiteToMove.play("e1d2").play(blackSimulatedNopMove);
        assertFalse(whiteToMoveAfterMovingKing.validMoves().stream().map(m -> m.toString()).toList().contains("e1c1"));
        assertFalse(whiteToMoveAfterMovingKing.validMoves().stream().map(m -> m.toString()).toList().contains("e1g1"));
    }

    @Test
    public void revokedCastlingRightsBlack() {
        Board blackToMove = Board.fromFEN("r3k2r/4p3/8/8/8/8/4P3/R3K2R b KQkq - 0 1");
        assertTrue(blackToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e8c8"));
        assertTrue(blackToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e8g8"));

        String whiteSimulatedNopMove = "e2e3";

        Board blackToMoveAfterMovingA8 = blackToMove.play("a8a7").play(whiteSimulatedNopMove);
        assertFalse(blackToMoveAfterMovingA8.validMoves().stream().map(m -> m.toString()).toList().contains("e8c8"));
        assertTrue(blackToMoveAfterMovingA8.validMoves().stream().map(m -> m.toString()).toList().contains("e8g8"));

        Board blackToMoveAfterMovingH8 = blackToMove.play("h8h7").play(whiteSimulatedNopMove);
        assertTrue(blackToMoveAfterMovingH8.validMoves().stream().map(m -> m.toString()).toList().contains("e8c8"));
        assertFalse(blackToMoveAfterMovingH8.validMoves().stream().map(m -> m.toString()).toList().contains("e8g8"));

        Board blackToMoveAfterMovingKing = blackToMove.play("e8d7").play(whiteSimulatedNopMove);
        assertFalse(blackToMoveAfterMovingKing.validMoves().stream().map(m -> m.toString()).toList().contains("e8c8"));
        assertFalse(blackToMoveAfterMovingKing.validMoves().stream().map(m -> m.toString()).toList().contains("e8g8"));
    }


    @Test
    public void whiteCastling() {
        Board whiteToMove = Board.fromFEN("r3k2r/8/8/8/8/8/8/R3K2R w KQkq - 0 1");

        String expectedFenAfterQueenSide = "r3k2r/8/8/8/8/8/8/2KR3R b kq - 1 1";
        String expectedFenAfterKingSide  = "r3k2r/8/8/8/8/8/8/R4RK1 b kq - 1 1";

        assertTrue(whiteToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e1c1"));
        assertTrue(whiteToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e1g1"));

        Board queenSideByOOO  = whiteToMove.play("O-O-O");
        Board queenSideBy000  = whiteToMove.play("0-0-0");
        Board queenSideByE1C1 = whiteToMove.play("e1c1");
        //Board queenSideByE1A1 = whiteToMove.play("e1a1");

        assertEquals(expectedFenAfterQueenSide, queenSideByOOO.toFEN());
        assertEquals(expectedFenAfterQueenSide, queenSideBy000.toFEN());
        assertEquals(expectedFenAfterQueenSide, queenSideByE1C1.toFEN());

        Board kingSideByOO  = whiteToMove.play("O-O");
        Board kingSideBy00  = whiteToMove.play("0-0");
        Board kingSideByE1G1 = whiteToMove.play("e1g1");
        //Board kingSideByE1H1 = whiteToMove.play("e1h1");

        assertEquals(expectedFenAfterKingSide, kingSideByOO.toFEN());
        assertEquals(expectedFenAfterKingSide, kingSideBy00.toFEN());
        assertEquals(expectedFenAfterKingSide, kingSideByE1G1.toFEN());
    }

    @Test
    public void blackCastling() {
        Board blackToMove = Board.fromFEN("r3k2r/8/8/8/8/8/8/R3K2R b KQkq - 0 1");

        String expectedFenAfterQueenSide = "2kr3r/8/8/8/8/8/8/R3K2R w KQ - 1 2";
        String expectedFenAfterKingSide  = "r4rk1/8/8/8/8/8/8/R3K2R w KQ - 1 2";

        assertTrue(blackToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e8c8"));
        assertTrue(blackToMove.validMoves().stream().map(m -> m.toString()).toList().contains("e8g8"));

        Board queenSideByOOO  = blackToMove.play("O-O-O");
        Board queenSideBy000  = blackToMove.play("0-0-0");
        Board queenSideByE8C8 = blackToMove.play("e8c8");
        //Board queenSideByE8A8 = blackToMove.play("e8a8");

        assertEquals(expectedFenAfterQueenSide, queenSideByOOO.toFEN());
        assertEquals(expectedFenAfterQueenSide, queenSideBy000.toFEN());
        assertEquals(expectedFenAfterQueenSide, queenSideByE8C8.toFEN());

        Board kingSideByOO  = blackToMove.play("O-O");
        Board kingSideBy00  = blackToMove.play("0-0");
        Board kingSideByE8G8 = blackToMove.play("e8g8");
        //Board kingSideByE1H1 = blackToMove.play("e8h8");

        assertEquals(expectedFenAfterKingSide, kingSideByOO.toFEN());
        assertEquals(expectedFenAfterKingSide, kingSideBy00.toFEN());
        assertEquals(expectedFenAfterKingSide, kingSideByE8G8.toFEN());
    }


    @Test
    public void castling960RookSquareOccupied() {
        Board initial = Board.fromFEN("qnbbrnkr/pppppppp/8/8/8/8/PPPPPPPP/QNBBRNKR w EHeh - 0 1");
        Board positionToTest = initial.play("e4 c6 e5 e6 d4 h6 d5 b5 dxe6 Nh7 exd7 g6 dxe8=Q+ Nf8 Qxd8 Bd7 Qxd7");
        // Kingside castling shouldn't be possible, as there is a Knight on f8 where Rook should end up
        Board illegal = positionToTest.play("g8h8");
        assertTrue(illegal.equals(positionToTest), "Castling shouldn't be possible here");
    }

    @Test
    public void castling960RookSquareNotOccupied() {
        Board initial = Board.fromFEN("qnbbrnkr/pppppppp/8/8/8/8/PPPPPPPP/QNBBRNKR w EHeh - 0 1");
        Board positionToTest = initial.play("e4 c6 e5 e6 d4 h6 d5 b5 dxe6 Nh7 exd7 g6 dxe8=Q+ Nf8 Qxd8 Bd7 Qxd7 Ne6 c3");
        // Kingside castling should be possible, as the Knight has moved from f8
        Board legal = positionToTest.play("g8h8");
        assertTrue(! legal.equals(positionToTest), "Castling should be possible here");
    }

    @Test
    public void castling960With_OO_Notation() {
        Board initial = Board.fromFEN("qnbbrnkr/pppppppp/8/8/8/8/PPPPPPPP/QNBBRNKR w EHeh - 0 1");
        Board positionToTest = initial.play("e4 c6 e5 e6 d4 h6 d5 b5 dxe6 Nh7 exd7 g6 dxe8=Q+ Nf8 Qxd8 Bd7 Qxd7 Ne6 c3");
        // Kingside castling should be possible, as the Knight has moved from f8
        Board legal = positionToTest.play("O-O");
        assertTrue(! legal.equals(positionToTest), "Castling should be possible here");
    }



}
