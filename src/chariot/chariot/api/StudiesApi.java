package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Consumer;

import chariot.model.*;

public interface StudiesApi {

    Many<PGN> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params);
    default Many<PGN> exportChapterByStudyAndChapterId(String studyId, String chapterId) {
        return exportChapterByStudyAndChapterId(studyId, chapterId, _ -> {});
    }

    Many<PGN> exportChaptersByStudyId(String studyId, Consumer<Params> params);
    default Many<PGN> exportChaptersByStudyId(String studyId) {
        return exportChaptersByStudyId(studyId, _ -> {});
    }

    Many<PGN> exportStudiesByUserId(String userId, Consumer<Params> params);
    default Many<PGN> exportStudiesByUserId(String userId) {
        return exportStudiesByUserId(userId, _ -> {});
    }

    /**
     * Lookup the Last-Modified timestamp of the study
     * @param studyId 8 character study id
     * @return A ZonedDateTime of when the study was last modified
     */
    One<ZonedDateTime> lastModifiedByStudyId(String studyId);

    /**
     * Get metadata (name and dates) of all studies of a user.<br>
     * If authenticated, then all public, unlisted, and private studies are included.<br>
     * If not, only public, listed studies are included.<br>
     * @param user user id or username
     */
    Many<StudyMeta> listStudiesByUser(String user);

    One<PageStudy> byPage(int page);
    Many<Study> listStudies();

    interface Params {
        /**
         * Include clock comments in the PGN moves, when available.
         * Example: {@code 2. exd5 { [%clk 1:01:27] } e5 { [%clk 1:01:28] }}
         * Default: true
         */
        Params clocks(boolean clocks);
        default Params clocks() { return clocks(true); }
        /**
         * Include analysis and annotator comments in the PGN moves, when available.
         * Example: {@code 12. Bxf6 { [%eval 0.23] } a3 { White is in a pickle. }}
         * Default: true
         */
        Params comments(boolean comments);
        default Params comments() { return comments(true); }

        /**
         * Include non-mainline moves, when available.
         * Example: {@code 4. d4 Bb4+ (4... Nc6 5. Nf3 Bb4+ 6. Bd2 (6. Nbd2 O-O 7. O-O) 6... Bd6) 5. Nd2}
         * Default: true
         */
        Params variations(boolean variations);
        default Params variations() { return variations(true); }

        /**
         * Add a `Source` PGN tag with the study chapter URL.
         * Example: {@code [Source "https://lichess.org/study/4NBHImfM/1Tk4IyTz"]}
         * Default: false
         */
        Params source(boolean source);
        default Params source() { return source(true); }

        /**
         * Add a `Orientation` PGN tag with the chapter predefined orientation.
         * Example: {@code [Orientation "white"]}
         * Default: false
         */
        Params orientation(boolean orientation);
        default Params orientation() { return orientation(true); }
    }

}
