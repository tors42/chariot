package chariot.api;

import module java.base;
import module chariot;

import chariot.model.Study.UserSelection;

public interface StudiesApiAuth extends StudiesApi {

    /// Create a new Study.
    /// {@snippet :
    ///      var client = Client.auth("token-with-scope-study:write");
    ///
    ///      One<String> newStudy = client.studies().create("MyStudy", params -> params
    ///              .visibilityPublic()
    ///              .computer(p -> p.owner())
    ///              .chat(p -> p.nobody())
    ///              .sticky());
    ///
    ///      switch (newStudy) {
    ///          case Some(String id) -> IO.println("Study ID: " + id);
    ///          case Fail<?> f -> IO.println(f);
    ///      }
    /// }
    ///
    /// @return the ID of the created study
    /// @param name The name of the study. Minimum 2 characters.
    /// @param params Default values:
    /// - visibility: unlisted
    /// - chat: member
    /// - computer: everyone
    /// - explorer: everyone
    /// - cloneable: everyone
    /// - shareable: everyone
    One<String> create(String name, Consumer<CreateParams> params);

    default One<String> create(String name) { return create(name, _ -> {}); }

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
    ///          IO.println("""
    ///          Name: %s
    ///          Id:   %s
    ///          """.formatted(chapter.name(), chapter.id()));
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
    Ack deleteStudyChapter(String studyId, String chapterId);

    /// Update PGN tags in a chapter of a study  
    ///  
    /// Any chapter tags not included in the `tags` map will remain unchanged.  
    /// New tags from the `tags` map will be added to the chapter.  
    /// Matching tags from the `tags` map will be updated with the provided value.  
    /// If the value is an empty string, the tag will be removed from the chapter.
    ///
    /// @param studyId Study id
    /// @param chapterId Chapter id
    /// @param tags a map of tags to update
    Ack updateStudyChapterTags(String studyId, String chapterId, Map<String, String> tags);

    interface CreateParams {
        CreateParams visibilityPublic();
        CreateParams visibilityUnlisted();
        CreateParams visibilityPrivate();
        CreateParams chat(UserSelection selection);
        CreateParams computer(UserSelection selection);
        CreateParams explorer(UserSelection selection);
        CreateParams cloneable(UserSelection selection);
        CreateParams shareable(UserSelection selection);
        CreateParams sticky(boolean sticky);

        default CreateParams sticky() { return sticky(true); }
        default CreateParams computer(Function<UserSelection.Provider, UserSelection> mapper) { return computer(mapper.apply(UserSelection.provider())); }
        default CreateParams explorer(Function<UserSelection.Provider, UserSelection> mapper) { return explorer(mapper.apply(UserSelection.provider())); }
        default CreateParams cloneable(Function<UserSelection.Provider, UserSelection> mapper) { return cloneable(mapper.apply(UserSelection.provider())); }
        default CreateParams shareable(Function<UserSelection.Provider, UserSelection> mapper) { return shareable(mapper.apply(UserSelection.provider())); }
        default CreateParams chat(Function<UserSelection.Provider, UserSelection> mapper) { return chat(mapper.apply(UserSelection.provider())); }
    }

    interface ImportParams {

        /// @param pgn Required. PGN to import. Can contain multiple games separated by 2 or more newlines.
        ImportParams pgn(String pgn);

        /// @param name Required. Name of the new chapter. If multiple chapters are created, the following names will be infered from the PGN tags.
        ImportParams name(String name);

        /// @param pgns PGNs to import.
        default ImportParams pgn(List<String> pgns) { return pgn(String.join("\n\n", pgns)); }

        ImportParams orientationWhite();
        ImportParams orientationBlack();
        ImportParams variant(Enums.GameVariant variant);
        default ImportParams variant(Function<Enums.GameVariant.Provider, Enums.GameVariant> variant) {
            return variant(variant.apply(Enums.GameVariant.provider()));
        }

        /// Analysis mode: Practice with computer
        ImportParams modePractice();
        /// Analysis mode: Hide next moves
        ImportParams modeHideNextMoves();
        /// Analysis mode: Interactive lesson
        ImportParams modeInteractive();
    }
}
