package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import chariot.model.Ack;
import chariot.model.Broadcast;
import chariot.model.Broadcast.Round;

public interface BroadcastsAuth extends Broadcasts {

    One<Broadcast> create(Consumer<BroadcastBuilder> params);
    One<Ack>       update(String tourId, Consumer<BroadcastBuilder> params);

    One<Round>     createRound(String tourId, Consumer<RoundBuilder> params);
    One<Round>     updateRound(String roundId, Consumer<RoundBuilder> params);

    One<Broadcast> broadcastById(String tourId);
    One<Round>     roundById(String roundId);
    One<Ack>       pushPgnByRoundId(String roundId, String pgn);

    interface BroadcastBuilder {

        /**
         * @param name Name of the broadcast tournament.<br/>
         *             Length must be between 3 and 80 characters.<br/>
         *        Example: Sinquefield Cup
         */
        BroadcastBuilder name(String name);

        /**
         * @param description Short description of the broadcast tournament.<br/>
         *                    Length must be between 3 and 400 characters.<br/>
         *        Example: An 11 round classical tournament featuring the 9 highest rated players in the world. Including
         *                 Carlsen, Caruana, Ding, Aronian, Nakamura and more.
         */
        BroadcastBuilder shortDescription(String description);

        /**
         * @param markup Optional long description of the broadcast. Markdown is supported.<br/>
         *        Length must be less than 20,000 characters.
         */
        BroadcastBuilder longDescription(String markup);

        /**
         * @param tier For Lichess internal usage only. [3 4 5]
         */
        BroadcastBuilder tier(int tier);
    }

    interface RoundBuilder {

        /**
         * @param name Name of the broadcast round. Length must be between 3 and 80 characters.<br/>
         *        Example: Round 1
         */
        public RoundBuilder name(String name);

        /**
         * @param syncUrl URL that Lichess will poll to get updates about the games.<br/>
         *                It must be publicly accessible from the Internet.<br/>
         *                If the syncUrl is missing, then the broadcast needs to be fed by pushing PGN to it.<br/>
         *        Example: https://myserver.org/myevent/round-10/games.pgn
         */
        RoundBuilder syncUrl(String syncUrl);

        /**
         * @param startsAt Timestamp in milliseconds of broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        RoundBuilder startsAt(long startsAt);

        /**
         * @param startsAt Broadcast round start.<br/>
         *                 Leave empty to manually start the broadcast round.
         */
        default RoundBuilder startsAt(ZonedDateTime startsAt) { return startsAt(startsAt.toInstant().toEpochMilli()); }

    }
}
