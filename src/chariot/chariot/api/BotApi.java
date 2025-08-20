package chariot.api;

import chariot.model.*;

public interface BotApi {
    Many<User> botsOnline();
    Many<User> botsOnline(int nb);
}
