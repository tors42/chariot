package chariot.model;

public sealed interface PuzzleAngle {

    enum Theme implements PuzzleAngle {
        mix,
        advancedPawn,
        advantage,
        anastasiaMate,
        arabianMate,
        attackingF2F7,
        attraction,
        backRankMate,
        bishopEndgame,
        bodenMate,
        capturingDefender,
        castling,
        clearance,
        crushing,
        defensiveMove,
        deflection,
        discoveredAttack,
        doubleBishopMate,
        doubleCheck,
        dovetailMate,
        equality,
        endgame,
        enPassant,
        exposedKing,
        fork,
        hangingPiece,
        hookMate,
        interference,
        intermezzo,
        kingsideAttack,
        knightEndgame,
        long_,
        master,
        masterVsMaster,
        mate,
        mateIn1,
        mateIn2,
        mateIn3,
        mateIn4,
        mateIn5,
        smotheredMate,
        middlegame,
        oneMove,
        opening,
        pawnEndgame,
        pin,
        promotion,
        queenEndgame,
        queenRookEndgame,
        queensideAttack,
        quietMove,
        rookEndgame,
        sacrifice,
        short_,
        skewer,
        superGM,
        trappedPiece,
        underPromotion,
        veryLong,
        xRayAttack,
        zugzwang,
        checkFirst,
    }

    record Custom(String name) implements PuzzleAngle {}

    interface Provider {

        default PuzzleAngle opening(String name) { return new Custom(name); }

        default PuzzleAngle mix()               { return Theme.mix; }
        default PuzzleAngle advancedPawn()      { return Theme.advancedPawn; }
        default PuzzleAngle advantage()         { return Theme.advantage; }
        default PuzzleAngle anastasiaMate()     { return Theme.anastasiaMate; }
        default PuzzleAngle arabianMate()       { return Theme.arabianMate; }
        default PuzzleAngle attackingF2F7()     { return Theme.attackingF2F7; }
        default PuzzleAngle attraction()        { return Theme.attraction; }
        default PuzzleAngle backRankMate()      { return Theme.backRankMate; }
        default PuzzleAngle bishopEndgame()     { return Theme.bishopEndgame; }
        default PuzzleAngle bodenMate()         { return Theme.bodenMate; }
        default PuzzleAngle capturingDefender() { return Theme.capturingDefender; }
        default PuzzleAngle castling()          { return Theme.castling; }
        default PuzzleAngle clearance()         { return Theme.clearance; }
        default PuzzleAngle crushing()          { return Theme.crushing; }
        default PuzzleAngle defensiveMove()     { return Theme.defensiveMove; }
        default PuzzleAngle deflection()        { return Theme.deflection; }
        default PuzzleAngle discoveredAttack()  { return Theme.discoveredAttack; }
        default PuzzleAngle doubleBishopMate()  { return Theme.doubleBishopMate; }
        default PuzzleAngle doubleCheck()       { return Theme.doubleCheck; }
        default PuzzleAngle dovetailMate()      { return Theme.dovetailMate; }
        default PuzzleAngle equality()          { return Theme.equality; }
        default PuzzleAngle endgame()           { return Theme.endgame; }
        default PuzzleAngle enPassant()         { return Theme.enPassant; }
        default PuzzleAngle exposedKing()       { return Theme.exposedKing; }
        default PuzzleAngle fork()              { return Theme.fork; }
        default PuzzleAngle hangingPiece()      { return Theme.hangingPiece; }
        default PuzzleAngle hookMate()          { return Theme.hookMate; }
        default PuzzleAngle interference()      { return Theme.interference; }
        default PuzzleAngle intermezzo()        { return Theme.intermezzo; }
        default PuzzleAngle kingsideAttack()    { return Theme.kingsideAttack; }
        default PuzzleAngle knightEndgame()     { return Theme.knightEndgame; }
        default PuzzleAngle long_()             { return Theme.long_; }
        default PuzzleAngle master()            { return Theme.master; }
        default PuzzleAngle masterVsMaster()    { return Theme.masterVsMaster; }
        default PuzzleAngle mate()              { return Theme.mate; }
        default PuzzleAngle mateIn1()           { return Theme.mateIn1; }
        default PuzzleAngle mateIn2()           { return Theme.mateIn2; }
        default PuzzleAngle mateIn3()           { return Theme.mateIn3; }
        default PuzzleAngle mateIn4()           { return Theme.mateIn4; }
        default PuzzleAngle mateIn5()           { return Theme.mateIn5; }
        default PuzzleAngle smotheredMate()     { return Theme.smotheredMate; }
        default PuzzleAngle middlegame()        { return Theme.middlegame; }
        default PuzzleAngle oneMove()           { return Theme.oneMove; }
        default PuzzleAngle opening()           { return Theme.opening; }
        default PuzzleAngle pawnEndgame()       { return Theme.pawnEndgame; }
        default PuzzleAngle pin()               { return Theme.pin; }
        default PuzzleAngle promotion()         { return Theme.promotion; }
        default PuzzleAngle queenEndgame()      { return Theme.queenEndgame; }
        default PuzzleAngle queenRookEndgame()  { return Theme.queenRookEndgame; }
        default PuzzleAngle queensideAttack()   { return Theme.queensideAttack; }
        default PuzzleAngle quietMove()         { return Theme.quietMove; }
        default PuzzleAngle rookEndgame()       { return Theme.rookEndgame; }
        default PuzzleAngle sacrifice()         { return Theme.sacrifice; }
        default PuzzleAngle short_()            { return Theme.short_; }
        default PuzzleAngle skewer()            { return Theme.skewer; }
        default PuzzleAngle superGM()           { return Theme.superGM; }
        default PuzzleAngle trappedPiece()      { return Theme.trappedPiece; }
        default PuzzleAngle underPromotion()    { return Theme.underPromotion; }
        default PuzzleAngle veryLong()          { return Theme.veryLong; }
        default PuzzleAngle xRayAttack()        { return Theme.xRayAttack; }
        default PuzzleAngle zugzwang()          { return Theme.zugzwang; }
        default PuzzleAngle checkFirst()        { return Theme.checkFirst; }
     }

    static Provider provider() {return new Provider(){};}
}
