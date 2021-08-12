package chariot.api;

import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

import chariot.Client.Scope;
import chariot.internal.Config;

public interface Builders {

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
