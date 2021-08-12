package chariot.api;

import chariot.model.Broadcast;
import chariot.model.Result;

public interface Broadcasts {

    Result<Broadcast> official(int nb);
    Result<Broadcast> official();

}
