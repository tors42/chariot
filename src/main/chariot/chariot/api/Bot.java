package chariot.api;

import chariot.model.Result;
import chariot.model.User;

public interface Bot {

    Result<User> botsOnline();
    Result<User> botsOnline(int nb);

}
