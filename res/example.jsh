import chariot.Client;

var client = Client.basic();
IO.println(client.teams().byTeamId("lichess-swiss").maybe()
    .map(team -> "Team %s has %d members!".formatted(team.name(), team.nbMembers()))
    .orElse("Couldn't find team!"));
