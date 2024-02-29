package tests.util;

import chariot.util.Board;
import chariot.util.Board.GameState;
import util.Test;

import static util.Assert.*;

public class TestBoard {

    static final String standardFen = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    @Test
    public void standardFen() {
        Board board = Board.fromStandardPosition();
        assertEquals(standardFen, board.toFEN());
    }

    @Test
    public void customStandardFen() {
        Board board = Board.fromFEN(standardFen);
        assertEquals(standardFen, board.toFEN());
    }

    @Test
    public void threefoldRepetition() {
        Board board = Board.fromStandardPosition();
        board = board.play("Na3 Na6 Nb1 Nb8 Na3 Na6 Nb1");

        assertEquals(GameState.ongoing, board.gameState());

        // third time position shows up
        board = board.play("Nb8");

        assertEquals(GameState.draw_by_threefold_repetition, board.gameState());
    }

    @Test
    public void disambiguationTwoQueensOnSameFile() {
        Board board = Board.fromFEN("rn1k4/pb3r2/q2b4/1p1p4/2P1p3/1K1P1p1p/P3P2P/qNBQ1B1R b -- 0 50");
        String san = board.toSAN("a6a2");

        assertEquals("Q6xa2#", san);
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


}
