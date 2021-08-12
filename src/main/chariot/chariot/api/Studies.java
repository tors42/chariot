package chariot.api;

import java.util.function.Consumer;

import chariot.model.Result;

public interface Studies {

    Result<String> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params);
    default Result<String> exportChapterByStudyAndChapterId(String studyId, String chapterId) {
        return exportChapterByStudyAndChapterId(studyId, chapterId, __ -> {});
    }

    Result<String> exportChaptersByStudyId(String studyId, Consumer<Params> params);
    default Result<String> exportChaptersByStudyId(String studyId) {
        return exportChaptersByStudyId(studyId, __ -> {});
    }

    Result<String> exportStudiesByUserId(String userId, Consumer<Params> params);
    default Result<String> exportStudiesByUserId(String userId) {
        return exportStudiesByUserId(userId, __ -> {});
    }

    interface Params {
        Params clocks(boolean clocks);
        Params comments(boolean comments);
        Params variations(boolean variations);
    }

}
