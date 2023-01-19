package chariot.api;

import java.util.function.Consumer;

import chariot.model.*;

public interface Studies {

    Many<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params);
    default Many<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId) {
        return exportChapterByStudyAndChapterId(studyId, chapterId, __ -> {});
    }

    Many<Pgn> exportChaptersByStudyId(String studyId, Consumer<Params> params);
    default Many<Pgn> exportChaptersByStudyId(String studyId) {
        return exportChaptersByStudyId(studyId, __ -> {});
    }

    Many<Pgn> exportStudiesByUserId(String userId, Consumer<Params> params);
    default Many<Pgn> exportStudiesByUserId(String userId) {
        return exportStudiesByUserId(userId, __ -> {});
    }

    interface Params {
        Params clocks(boolean clocks);
        default Params clocks() { return clocks(true); }
        Params comments(boolean comments);
        default Params comments() { return comments(true); }
        Params variations(boolean variations);
        default Params variations() { return variations(true); }
    }

}
