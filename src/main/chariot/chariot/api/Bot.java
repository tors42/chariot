package chariot.api;

import chariot.model.*;

public interface Bot {
    Many<User> botsOnline();
    Many<User> botsOnline(int nb);
}
