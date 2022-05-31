package chariot.api;

import java.util.function.Consumer;

import chariot.model.Pgn;
import chariot.model.Result;

public interface Studies {

    Result<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params);
    default Result<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId) {
        return exportChapterByStudyAndChapterId(studyId, chapterId, __ -> {});
    }

    Result<Pgn> exportChaptersByStudyId(String studyId, Consumer<Params> params);
    default Result<Pgn> exportChaptersByStudyId(String studyId) {
        return exportChaptersByStudyId(studyId, __ -> {});
    }

    Result<Pgn> exportStudiesByUserId(String userId, Consumer<Params> params);
    default Result<Pgn> exportStudiesByUserId(String userId) {
        return exportStudiesByUserId(userId, __ -> {});
    }

    interface Params {
        Params clocks(boolean clocks);
        Params comments(boolean comments);
        Params variations(boolean variations);
    }

}
