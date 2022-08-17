package build;

import chariot.Client;
import java.time.*;

class Example {

    public static void main(String[] args) {

        var client = Client.auth("my-token");

        var tomorrow = ZonedDateTime.now().plusDays(1).with(
            LocalTime.parse("17:00"));

        String teamId = "my-team-id";

        var result = client.tournaments().createSwiss(teamId, params -> params
            .clockBlitz5m3s()
            .name("My 5+3 Swiss")
            .rated(false)
            .description("Created via API")
            .startsAt(tomorrow));

        System.out.println(result);
    }
}
