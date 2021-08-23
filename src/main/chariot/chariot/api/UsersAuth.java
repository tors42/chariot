package chariot.api;

import chariot.model.Ack;
import chariot.model.Result;

public interface UsersAuth extends Users {

    Result<Ack> sendMessageToUser(String userId, String text);

    Result<Ack> followUser(String userId);
    Result<Ack> unfollowUser(String userId);

}
