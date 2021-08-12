package chariot.api;

import chariot.model.Ack;
import chariot.model.Result;

public interface UsersAuth extends Users {

    Result<Ack> sendMessageToUser(String userId, String text);

}
