package chariot.api;

import java.util.function.Function;

import chariot.model.Enums.Room;
import chariot.model.*;

/**
 * Play on Lichess as a bot.
 * <p>Allows engine play.<br/>
 * Only works with Bot accounts.
 * <p><b>Features</b>
 * <ul>
 * <li>Stream incoming chess moves
 * <li>Play chess moves
 * <li>Read and write in the player and spectator chats
 * <li>Receive, create and accept (or decline) challenges
 * <li>Abort and resign games
 * <li>Engine assistance is allowed
 * </ul>
 * <p><b>Restrictions</b>
 * <ul>
 * <li>Bots can only play challenge games: pools and tournaments are off-limits
 * <li>Bots cannot play UltraBullet (¼+0) because it requires making too many requests. But 0+1 and ½+0 are allowed.
 * </ul>
 * Example:
 * {@snippet :
 *      ClientAuth client = Client.auth("my-bot-token");
 *
 *      // Connect the bot to Lichess, becoming challengeable
 *      Stream<StreamEvent> events = client.bot().streamEvents().stream();
 *
 *      // Handle events
 *      events.forEach( event -> {
 *          switch(event.type()) {
 *              case challenge:
 *                  // A challenge! Let's accept!
 *                  client.bot().acceptChallenge(event.id());
 *                  break;
 *              case gameStart:
 *                  // A game has started! Likely because we've accepted a challenge.
 *
 *                  // Connect to the game, receiving game events. We'll always get a Full game state first.
 *                  Stream<StreamGameEvent> gameEvents = client.bot().streamGameEvents(event.id()).stream();
 *
 *                  client.bot().chat(event.id(), "I wish you a pleasant game!");
 *                  gameEvents.forEach( gameEvent -> {
 *                      switch (gameEvent.type()) {
 *                          case gameFull:
 *                              if (weShouldStart) {
 *                                  client.bot().move(event.id(), "e2e4")
 *                              }
 *                              break;
 *                          case chatLine:
 *                              // Chat? We don't have cycles to spare for chatting...
 *                              // *stays silent*
 *                              break;
 *                          case gameState:
 *                              // With record pattern matching we wouldn't have needed this ugly cast...
 *                              Game.Status status = ((StreamGameEvent.State) gameEvent).status();
 *                              if (status == Game.Status.mate) {
 *                                  client.bot().chat(event.id(), "Nice we could play until the end! Laters!");
 *                              } else {
 *                                  client.bot().move(event.id(), ...);
 *                              }
 *                              break;
 *                      }
 *                  });
 *                  break;
 *                  ...
 *              }
 *          });
 * }
 */
public interface BotAuth extends ChallengesAuthCommon, Bot {

    /**
     * Upgrade a lichess player account into a Bot account. Only Bot accounts can use the Bot API.<br>
     * The account cannot have played any game before becoming a Bot account. The upgrade is irreversible. The account will only be able to play as a Bot.<br>
     */
    One<Ack> upgradeToBotAccount();

    /**
     * Stream the state of a game being played with the Bot API
     * <p>Use this endpoint to get updates about the game in real-time, with a single request.
     * <p>Event types
     * <ul>
     * <li> gameFull Full game data.
     * <li> gameState Current state of the game. Sent when a move is played, a draw is offered, or when the game ends.
     * <li> chatLine Chat message sent by a user in the {@code room} player or spectator.
     * </ul>
     * <p> The first event is always of type gameFull
     *  @param gameId Example: 5IrD6Gzz
     */
    Many<StreamGameEvent> streamGameState(String gameId);

    /**
     *  Make a move in a game being played with the Bot API.<br/>
     *
     *  @param gameId Example: 5IrD6Gzz
     *  @param move The move to play, in UCI format. Example: e2e4
     *  @param drawOffer Whether to offer (or agree to) a draw
     */
    One<Ack> move(String gameId, String move, boolean drawOffer);
    /**
     *  Make a move in a game being played with the Bot API.<br/>
     *  The move can also contain a draw offer/agreement. {@link #move(String, String, boolean) move(..., boolean)}
     *
     *  @param gameId Example: 5IrD6Gzz
     *  @param move The move to play, in UCI format. Example: e2e4
     */
     One<Ack> move(String gameId, String move);

    /**
     * Post a message to the player or spectator chat, in a game being played with the Bot API.
     * @param gameId  Example: 5IrD6Gzz
     * @param text The message to send
     * @param room Target either player or spectators
     */
    One<Ack> chat(String gameId, String text, Room room);

    /**
     * Abort a game being played with the Bot API.
     * @param gameId  Example: 5IrD6Gzz
     */
    One<Ack> abort(String gameId);

    /**
     * Resign a game being played with the Bot API.
     * @param gameId  Example: 5IrD6Gzz
     */
    One<Ack> resign(String gameId);


    /**
     * See {@link #chat(String, String, Room) chat(..., room)}
     */
    default One<Ack> chat(String gameId, String text) {
        return chat(gameId, text, Room.player);
    }

    /**
     * See {@link #chat(String, String, Room) chat(..., room)}
     */
    default One<Ack> chat(String gameId, String text, Function<Room.Provider, Room> room) {
        return chat(gameId, text, room.apply(Room.provider()));
    }

    /**
     * Get the messages posted in the game chat.
     * @param gameId  Example: 5IrD6Gzz
     */
    Many<ChatMessage> fetchChat(String gameId);

}
