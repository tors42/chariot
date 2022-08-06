package chariot.api;

import chariot.model.User;

public interface Bot {
    Many<User> botsOnline();
    Many<User> botsOnline(int nb);
}
