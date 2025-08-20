package it.tournamentapi;

import chariot.model.*;
import chariot.model.Variant.*;
import util.*;
import static util.Assert.*;

import java.time.Duration;

public class SwissCustomPosition {

    static final String FRENCH_DEFENSE       = "rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2";
    static final String FRENCH_DEFENSE_SHORT = "rnbqkbnr/pppp1ppp/4p3/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq -";
    static final String NO_PAWNS             = "rnbqkbnr/8/8/8/8/8/8/RNBQKBNR w KQkq - 0 1";
    static final String NO_PAWNS_SHORT       = "rnbqkbnr/8/8/8/8/8/8/RNBQKBNR w KQkq -";
    static final String CHESS960_585         = "qrnbbkrn/pppppppp/8/8/8/8/PPPPPPPP/QRNBBKRN w KQkq - 0 1";
    static final String CHESS960_INVALID     = "kqnbrbnr/pppppppp/8/8/8/8/PPPPPPPP/KQNBRBNR w KQkq - 0 1";

    static One<Swiss> createSwiss(String name, Variant variant) {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            return One.fail(-1, "Couldn't find team leader for creating a swiss");
        }

        Clock clock = new Clock(Duration.ofMinutes(3), Duration.ofSeconds(2));
        boolean rated = false;
        One<Swiss> createRes = client.tournaments().createSwiss(teamId, params -> params
                .clock(clock)
                .name(name)
                .rated(rated)
                .variant(variant));
        unboxEquals(createRes, clock,        swiss -> swiss.tourInfo().clock());
        unboxEquals(createRes, rated,        swiss -> swiss.tourInfo().rated());
        return createRes;
    }

    @IntegrationTest
    public void standardVariantWithPositionHmmmUsingNamedPositionRecognizesPosition() {
        Variant variantStandardFrenchDefense = Variant.provider().standard(FRENCH_DEFENSE);
        One<Swiss> createRes = createSwiss("standard-french", variantStandardFrenchDefense);
        Variant namedVariant = new FromPosition(Opt.of(FRENCH_DEFENSE_SHORT), Opt.of("French Defense"));
        unboxEquals(createRes, namedVariant, swiss -> swiss.tourInfo().variant());
    }

    @IntegrationTest
    public void standardVariantWithPositionHmmmUsingNamedPositionShortRecognizesPosition() {
        Variant variantStandardFrenchDefenseShort = Variant.provider().standard(FRENCH_DEFENSE_SHORT);
        One<Swiss> createRes = createSwiss("standard-french-short", variantStandardFrenchDefenseShort);
        Variant namedVariant = new FromPosition(Opt.of(FRENCH_DEFENSE_SHORT), Opt.of("French Defense"));
        unboxEquals(createRes, namedVariant, swiss -> swiss.tourInfo().variant());
    }


    @IntegrationTest
    public void standardVariantWithPositionHmmmUsingWeirdPositionRecognizesPosition() {
        Variant variantStandardNoPawns = Variant.provider().standard(NO_PAWNS);
        One<Swiss> createRes = createSwiss("standard-nopawns", variantStandardNoPawns);
        Variant customVariant = new FromPosition(Opt.of(NO_PAWNS_SHORT), Opt.of("Custom position"));
        unboxEquals(createRes, customVariant, swiss -> swiss.tourInfo().variant());
    }

    @IntegrationTest
    public void fromPositionVariantWithPostionIgnoresPosition() {
        FromPosition variantFromPositionNoPawns = new FromPosition(Opt.of(NO_PAWNS), Opt.of("Custom position"));
        One<Swiss> createRes = createSwiss("fromposition-nopawns", variantFromPositionNoPawns);
        Variant variantFromPositionNoPosition = new FromPosition(Opt.of());
        unboxEquals(createRes, variantFromPositionNoPosition, swiss -> swiss.tourInfo().variant());
    }

    @IntegrationTest
    public void chess960VariantWithValidPositionIgnoresPosition() {
        Chess960 chess960_585 = new Chess960(Opt.of(CHESS960_585));
        One<Swiss> createRes = createSwiss("chess960pos585", chess960_585);
        Variant variantChess960NoPosition = new Chess960(Opt.of());
        unboxEquals(createRes, variantChess960NoPosition, swiss -> swiss.tourInfo().variant());
    }

    @IntegrationTest
    public void chess960VariantWithInvalidPositionIgnoresPosition() {
        Chess960 chess960Invalid = new Chess960(Opt.of(CHESS960_INVALID));
        One<Swiss> createRes = createSwiss("chess960Invalid", chess960Invalid);
        Variant variantChess960NoPosition = new Chess960(Opt.of());
        unboxEquals(createRes, variantChess960NoPosition, swiss -> swiss.tourInfo().variant());
    }

    // Comment out annotation to keep Swisses (i.e for debugging purposes)
    @IntegrationTest
    public void deleteAllSwisses() {
        if (! (IT.findTeamLeader() instanceof Some(IT.TeamLeader(var client, var userId, var teamId)))) {
            fail("Couldn't find team leader for creating a swiss");
            return;
        }
        client.teams().swissByTeamId(teamId).stream().map(s -> s.id()).forEach(id -> client.tournaments().terminateSwiss(id));
    }
}
