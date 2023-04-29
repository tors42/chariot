package chariot.api;

import java.net.URI;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.prefs.Preferences;

import chariot.Client.Scope;
import chariot.internal.Config;

public interface Builders {

    interface OAuthHelper {
        /**
         * @param preferences A local Preferences node to hold the client authorization token,
         * to be interactively populated at first run and then automatically
         * be used for consecutive runs.
         */
        OAuthHelper prefs(Preferences preferences);

        /**
         * @param scopes the scopes, if any, needed for the authorized client
         */
        OAuthHelper scopes(Scope... scopes);

        /**
         * {@snippet :
         *      Preferences userPrefsNode = Preferences.userRoot().node(preferences); //@highlight regex="preferences"
         *      return prefs(userPrefsNode);
         * }
         * @param preferences the name of a preferences node
         */
        default OAuthHelper prefs(String preferences) {
            return prefs(Preferences.userRoot().node(preferences));
        }
    }

    interface Clock<T> {
        /**
         * @param initial Initial time on clock, in seconds
         * @param increment Increment added to clock after each move, in seconds
         */
        T clock(int initial, int increment);

        default T clockBullet1m0s()      { return clock((int) TimeUnit.MINUTES.toSeconds(1),   0); }
        default T clockBullet2m1s()      { return clock((int) TimeUnit.MINUTES.toSeconds(2),   1); }
        default T clockBlitz3m1s()       { return clock((int) TimeUnit.MINUTES.toSeconds(3),   1); }
        default T clockBlitz3m2s()       { return clock((int) TimeUnit.MINUTES.toSeconds(3),   2); }
        default T clockBlitz5m0s()       { return clock((int) TimeUnit.MINUTES.toSeconds(5),   0); }
        default T clockBlitz5m3s()       { return clock((int) TimeUnit.MINUTES.toSeconds(5),   3); }
        default T clockRapid10m0s()      { return clock((int) TimeUnit.MINUTES.toSeconds(10),  0); }
        default T clockRapid10m5s()      { return clock((int) TimeUnit.MINUTES.toSeconds(10),  5); }
        default T clockRapid15m10s()     { return clock((int) TimeUnit.MINUTES.toSeconds(15), 10); }
        default T clockClassical30m0s()  { return clock((int) TimeUnit.MINUTES.toSeconds(30),  0); }
        default T clockClassical30m20s() { return clock((int) TimeUnit.MINUTES.toSeconds(30), 20); }
    }

    interface ClockCorrespondence<T> {
        /**
         * @param daysPerTurn Number of days for each move. [ 1, 3, 5, 7, 10, 14 ]
         */
        T daysPerTurn(int daysPerTurn);
    }

    interface ClockMinute<T> {
        /**
         * @param initial Initial time on clock, in minutes [ 0.0, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0, 3.0, 4.0, 5.0, 6.0, 7.0, 10.0, 15.0, 20.0, 25.0, 30.0, 40.0, 50.0, 60.0 ]
         * @param increment Increment added to clock after each move, in seconds<br>
         * [ 0, 1, 2, 3, 4, 5, 6, 7, 10, 15, 20, 25, 30, 40, 50, 60 ]
        */
        T clock(float initial, int increment);

        default T clockBullet1m0s()      { return clock(1.0f,   0); }
        default T clockBullet2m1s()      { return clock(2.0f,   1); }
        default T clockBlitz3m1s()       { return clock(3.0f,   1); }
        default T clockBlitz3m2s()       { return clock(3.0f,   2); }
        default T clockBlitz5m0s()       { return clock(5.0f,   0); }
        default T clockBlitz5m3s()       { return clock(5.0f,   3); }
        default T clockRapid10m0s()      { return clock(10.0f,  0); }
        default T clockRapid10m5s()      { return clock(10.0f,  5); }
        default T clockRapid15m10s()     { return clock(15.0f, 10); }
        default T clockClassical30m0s()  { return clock(30.0f,  0); }
        default T clockClassical30m20s() { return clock(30.0f, 20); }
    }

    interface ConfigBuilder {
        /**
         * Lichess<br/>
         * API: https://lichess.org<br/>
         * Explorer: https://explorer.lichess.ovh<br/>
         * Tablebase: https://tablebase.lichess.ovh<br/>
         * Engine: https://engine.lichess.ovh<br/>
         */
        default ConfigBuilder production() {
            var prod = Config.Servers.of();
            return servers(s -> s
                    .api(prod.api())
                    .explorer(prod.explorer())
                    .tablebase(prod.tablebase())
                    .engine(prod.engine())
                    );
        }

        /**
         * Development<br/>
         * API: http://localhost:9663
         */
        default ConfigBuilder local() { return api("http://localhost:9663"); }

        /**
         * Custom API URL
         */
        default ConfigBuilder api(String uri) { return api(URI.create(uri)); }
        /**
         * Custom API URL
         */
        ConfigBuilder api(URI uri);

        /**
         * Custom api, explorer, tablebase and engine URLs
         */
        ConfigBuilder servers(Consumer<ServerBuilder> params);

        /**
         * Log levels
         */
        ConfigBuilder logging(Consumer<LoggingBuilder> params);

        /**
         * Number of times to retry sending a request if server indicates throttling (status code 429).<br/>
         * The waiting time until performing a retry is 60 seconds.<br/>
         * Default: 1 retry
         */
        ConfigBuilder retries(int retries);
    }

    interface ServerBuilder {
        /**
         * Address of the api service
         */
        ServerBuilder api(URI uri);
        /**
         * Address of the api service
         */
        default ServerBuilder api(String uri) { return api(URI.create(uri)); }


        /**
         * Address of the explorer service
         */
        ServerBuilder explorer(URI uri);
        /**
         * Address of the explorer service
         */
        default ServerBuilder explorer(String uri) { return explorer(URI.create(uri)); }

        /**
         * Address of the tablebase service
         */
        ServerBuilder tablebase(URI uri);
        /**
         * Address of the tablebase service
         */
        default ServerBuilder tablebase(String uri) { return tablebase(URI.create(uri)); }

        /**
         * Address of the engine service
         */
        ServerBuilder engine(URI uri);
        /**
         * Address of the engine service
         */
        default ServerBuilder engine(String uri) { return engine(URI.create(uri)); }
    }

    interface LoggingBuilder {
        /**
         * Log level of logger "chariot.request", default warning
         */
        LogLevel request();
        /**
         * Log level of logger "chariot.response-body-raw", default warning
         */
        LogLevel response();
        /**
         * Log level of logger "chariot.request", default off
         */
        LogLevel auth();
    }

    interface LogLevel {
        LoggingBuilder all();
        LoggingBuilder finest();
        LoggingBuilder finer();
        LoggingBuilder fine();
        LoggingBuilder config();
        LoggingBuilder info();
        LoggingBuilder warning();
        LoggingBuilder severe();
        LoggingBuilder off();

        LoggingBuilder parse(String level);
    }

}
