package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Function;

import chariot.model.Ack;
import chariot.model.Broadcast;
import chariot.model.Result;
import chariot.model.Broadcast.Round;

public interface BroadcastsAuth extends Broadcasts {

    Result<Broadcast> create(Function<BroadcastBBuilder, BroadcastBuilder> params);
    Result<Ack>       update(String tourId, Function<BroadcastBBuilder, BroadcastBuilder> params);

    Result<Round>     createRound(String tourId, Function<RoundBBuilder, RoundBuilder> params);
    Result<Ack>       updateRound(String roundId, Function<RoundBBuilder, RoundBuilder> params);

    Result<Broadcast> broadcastById(String tourId);
    Result<Round>     roundById(String roundId);
    Result<Ack>       pushPgnByRoundId(String roundId, String pgn);



    interface BroadcastBBuilder {
        /**
         * @param name Name of the broadcast tournament.<br/>
         *             Length must be between 3 and 80 characters.<br/>
         *        Example: Sinquefield Cup
         * @param description Short description of the broadcast tournament.<br/>
         *                    Length must be between 3 and 400 characters.<br/>
         *        Example: An 11 round classical tournament featuring the 9 highest rated players in the world. Including
         *                 Carlsen, Caruana, Ding, Aronian, Nakamura and more.
         */
        public BroadcastBuilder info(String name, String description);
    }

    interface BroadcastBuilder {
        /**
         * @param markup Optional long description of the broadcast. Markdown is supported.<br/>
         *        Length must be less than 20,000 characters.
         */
        BroadcastBuilder markup(String markup);

        /**
         * @param official For Lichess internal usage only.<br/>
         *        You are not allowed to use this flag.<br/>
         *        If you do it, we will have to call the police.
         */
        BroadcastBuilder official(boolean official);
    }

    interface RoundBBuilder {
        /**
         * @param name Name of the broadcast round. Length must be between 3 and 80 characters.<br/>
         *        Example: Round 1
         */
        public RoundBuilder info(String name);
    }

    interface RoundBuilder {
        /**
         * @param syncUrl URL that Lichess will poll to get updates about the games.<br/>
         *                It must be publicly accessible from the Internet.<br/>
         *                If the syncUrl is missing, then the broadcast needs to be fed by pushing PGN to it.<br/>
         *        Example: https://myserver.org/myevent/round-10/games.pgn
         */
        RoundBuilder syncUrl(String syncUrl);

        /**
         * @param startsAt Broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        RoundBuilder startsAt(ZonedDateTime startsAt);

        /**
         * @param startsAt Timestamp in milliseconds of broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        RoundBuilder startsAt(long startsAt);
    }

}
