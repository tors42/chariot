///
/// Provides access to the [Lichess API](https://lichess.org/api).
///
module chariot {

    exports chariot;
    exports chariot.api;
    exports chariot.model;
    exports chariot.chess;
    exports chariot.util;

    uses chariot.chess.BoardProvider;

    provides chariot.chess.BoardProvider
        with chariot.internal.chess.InternalBoardProvider;

    requires transitive java.logging;
    requires transitive java.prefs;
    requires java.net.http;

    requires jdk.httpserver; //OAuth PKCE

}
