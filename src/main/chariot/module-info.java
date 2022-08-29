/**
 * Provides access to the <a href="https://lichess.org/api">Lichess API</a>.
 */
module chariot {

    exports chariot;
    exports chariot.api;
    exports chariot.model;
    exports chariot.util;

    requires transitive java.logging;
    requires transitive java.prefs;
    requires java.net.http;

    requires jdk.crypto.ec;  //https
    requires jdk.httpserver; //OAuth PKCE

}
