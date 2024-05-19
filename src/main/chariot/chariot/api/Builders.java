package chariot.api;

import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import chariot.internal.Config;

public interface Builders {

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

        /**
         * Duration to wait after sending a request, until next request can be sent.
         * Default: 1 second
         */
        ConfigBuilder spacing(Duration spacing);


        /**
         * Customization of the User-Agent header set in HTTP requests.<br/>
         *
         * The default value for the User-Agent header is formatted as:<br/>
         * {@code Java/<java-version> <chariot-module> [main-app] [identifier]},<br/>
         * where {@code [main-app]} defaults to the main module name and version, if exists (via System property {@systemProperty jdk.module.main}).<br/>
         * <br/>
         * Example of User-Agent values:<br/>
         * {@code Java/21 chariot@0.0.75 my.app@1.2}<br/>
         * {@code Java/17.0.8.1 chariot@0.0.75}<br/>
         * {@code Java/17.0.8.1 chariot@0.0.75 My Identifier}<br/>
         * {@code My Custom User-Agent}<br/>
         * <br/>
         * This method can be used to include a custom identifier for Lichess to identify the application.<br/>
         * It is also possible to replace the entire contents of the User-Agent header by supplying {@code true} for the {@code replaceAll} parameter.
         *
         * @param identifier The identifier to add to the User-Agent string
         * @param replaceAll Replace the entire User-Agent string if the value is true, otherwise the identifier is added to the default User-Agent string
         */
        ConfigBuilder userAgent(String identifier, boolean replaceAll);

        /**
         * See {@link #userAgent(String, boolean)}
         */
        default ConfigBuilder userAgent(String identifier) { return userAgent(identifier, false); }
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
