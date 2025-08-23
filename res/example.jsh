import chariot.Client;

var client = Client.basic();
System.out.println(client.teams().byTeamId("lichess-swiss")
    .map(team -> "Team %s has %d members!".formatted(team.name(), team.nbMembers()))
    .orElse("Couldn't find team!"));
