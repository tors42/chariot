package chariot.api;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import chariot.Client.Scope;
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



    interface TokenBuilder extends Builder, AuthBuilder {

        /**
         * {@inheritDoc}
         */
        default TokenBuilder auth(String token) {
            return auth(() -> token.toCharArray());
        }

        /**
         * {@inheritDoc}
         */
        TokenBuilder auth(Supplier<char[]> token);

        /**
         * {@inheritDoc}
         */
        TokenBuilder auth(Set<Supplier<char[]>> tokens);

        /**
         * {@inheritDoc}
         */
        TokenBuilder auth(Map<Scope, Supplier<char[]>> tokens);

        /**
         * {@inheritDoc}
         */
        TokenBuilder api(String url);

        /**
         * {@inheritDoc}
         */
        TokenBuilder servers(Consumer<ExtServBuilder> params);

        /**
         * {@inheritDoc}
         */
        TokenBuilder levels(Consumer<LogSetter> params);

        /**
         * {@inheritDoc}
         */
        default TokenBuilder production() { Builder.super.production(); return this; }

        /**
         * {@inheritDoc}
         */
        default TokenBuilder local() { Builder.super.local(); return this;}
    }

    interface AuthBuilder {
        /**
         *  Enables use of authenticated endpoints,
         *  by using the supplied token for authentication.
         */
        default AuthBuilder auth(String token) {
            return auth(() -> token.toCharArray());
        }

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * token for authentication.
         */
        AuthBuilder auth(Supplier<char[]> token);

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * tokens for authentication.
         * The server will be queried for each token to see which scopes they
         * are valid for, and when making a request to an endpoint which needs
         * a specific scope, a token providing that scope will be used.
         */
        AuthBuilder auth(Set<Supplier<char[]>> tokens);

        /**
         * Enables use of authenticated endpoints, by using the supplied
         * tokens for authentication.
         * When making a request to an endpoint which needs a specific scope,
         * the token providing that scope will be used.
         */
        AuthBuilder auth(Map<Scope, Supplier<char[]>> tokens);
    }

    interface Builder {
        /**
         * Lichess<br/>
         * API: https://lichess.org<br/>
         * Explorer: https://explorer.lichess.ova<br/>
         * Tablebase: https://tablebase.lichess.ova<br/>
         */
        default Builder production() { return api(Config.lichess).servers(s -> s.explorer(Config.explorer).tablebase(Config.tablebase)); }

        /**
         * Development<br/>
         * API: http://localhost:9663
         */
        default Builder local() { return api(Config.local); }

        /**
         * Custom API URL
         */
        Builder api(String url);

        /**
         * Custom explorer and tablebase URLs
         */
        Builder servers(Consumer<ExtServBuilder> params);

        /**
         * Log levels
         */
        Builder levels(Consumer<LogSetter> params);
    }

    interface ExtServBuilder {
        /**
         * Address of the explorer service
         */
        ExtServBuilder explorer(String url);

        /**
         * Address of the tablebase service
         */
        ExtServBuilder tablebase(String url);
    }

    interface LogSetter {
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
        LogSetter all();
        LogSetter finest();
        LogSetter finer();
        LogSetter fine();
        LogSetter config();
        LogSetter info();
        LogSetter warning();
        LogSetter severe();
        LogSetter off();
    }

}
