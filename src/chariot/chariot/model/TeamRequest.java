package chariot.model;

import java.time.ZonedDateTime;

import chariot.internal.Util;

public record TeamRequest(Request request, UserData user)  {

    public record Request(Long date, String message, String teamId, String userId) {
        public ZonedDateTime dateTime() {
            return Util.fromLong(date);
        }
    }

}
