package chariot.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.*;
import chariot.model.Enums.GameVariant;

public interface StudiesApiAuth extends StudiesApi {

    /**
     * Import PGN into a study
     *
     * {@snippet :
     *      var client = Client.auth("token");
     *
     *      List<ChapterMeta> createdChapters = client.studies().importPgn("studyId", params -> params
     *              .name("Imported Chapter")
     *              .pgn("""
     *                   [Event "My Event"]
     *                   [White "My Opponent"]
     *                   [Black "Me"]
     *                   [Result "0-1"]
     *
     *                   1. d4 d5 0-1""")
     *              .orientationBlack())
     *          .stream().toList();
     *
     *      for (var chapter : createdChapters) {
     *          System.out.println(STR."""
     *          Name: \{chapter.name()}
     *          Id:   \{chapter.id()}"""
     *          );
     *      }
     *
     * }
     *
     * @param studyId ID of the study
     * @param params {@code params -> params.name("chapter name").pgn("...")}
     */
    Many<ChapterMeta> importPgn(String studyId, Consumer<ImportParams> params);

    /**
     * Delete a chapter of a study
     *
     * @param studyId Study id
     * @param chapterId Chapter id
     */
    One<Void> deleteStudyChapter(String studyId, String chapterId);

    interface ImportParams {

        /**
         * @param name Required. Name of the new chapter. If multiple chapters are created, the following names will be infered from the PGN tags.
         */
        ImportParams name(String name);

        /**
         * @param pgn Required. PGN to import. Can contain multiple games separated by 2 or more newlines.
         */
        ImportParams pgn(String pgn);

        /**
         * @param pgns PGNs to import.
         */
        default ImportParams pgn(List<String> pgns) { return pgn(String.join("\n\n", pgns)); }

        /**
         * Make the default orientation of the chapters white
         */
        ImportParams orientationWhite();
        /**
         * Make the default orientation of the chapters black
         */
        ImportParams orientationBlack();

        ImportParams variant(GameVariant variant);
        default ImportParams variant(Function<GameVariant.Provider, GameVariant> variant) { return variant(variant.apply(GameVariant.provider())); }

    }

}


