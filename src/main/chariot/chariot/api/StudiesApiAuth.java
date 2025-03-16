package chariot.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.*;
import chariot.model.Enums.GameVariant;

public interface StudiesApiAuth extends StudiesApi {

    /// Import PGN into a study
    ///
    /// {@snippet :
    ///      var client = Client.auth("token");
    ///
    ///      List<ChapterMeta> createdChapters = client.studies().importPgn("studyId", params -> params
    ///              .pgn("""
    ///                   [Event "My Event"]
    ///                   [White "My Opponent"]
    ///                   [Black "Me"]
    ///                   [Result "0-1"]
    ///
    ///                   1. d4 d5 0-1""")
    ///              .orientationBlack())
    ///          .stream().toList();
    ///
    ///      for (var chapter : createdChapters) {
    ///          System.out.println(STR."""
    ///          Name: \{chapter.name()}
    ///          Id:   \{chapter.id()}"""
    ///          );
    ///      }
    ///
    /// }
    /// @param studyId ID of the study
    /// @param params `importPgn(studyId, params -> params.name("chapter name").pgn("..."))`
    Many<ChapterMeta> importPgn(String studyId, Consumer<ImportParams> params);

    /// Delete a chapter of a study
    ///
    /// @param studyId Study id
    /// @param chapterId Chapter id
    One<Void> deleteStudyChapter(String studyId, String chapterId);

    interface ImportParams {

        /// @param pgn Required. PGN to import. Can contain multiple games separated by 2 or more newlines.
        ImportParams pgn(String pgn);

        /// @param name Required. Name of the new chapter. If multiple chapters are created, the following names will be infered from the PGN tags.
        ImportParams name(String name);

        /// @param pgns PGNs to import.
        default ImportParams pgn(List<String> pgns) { return pgn(String.join("\n\n", pgns)); }

        ImportParams orientationWhite();
        ImportParams orientationBlack();
        ImportParams variant(GameVariant variant);
        default ImportParams variant(Function<GameVariant.Provider, GameVariant> variant) { return variant(variant.apply(GameVariant.provider())); }

        /// Analysis mode: Practice with computer
        ImportParams modePractice();
        /// Analysis mode: Hide next moves
        ImportParams modeHideNextMoves();
        /// Analysis mode: Interactive lesson
        ImportParams modeInteractive();
    }
}
