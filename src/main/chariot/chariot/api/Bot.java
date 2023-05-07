package chariot.api;

import chariot.model.*;

public interface Bot<T extends User> {
    Many<T> botsOnline();
    Many<T> botsOnline(int nb);
}
