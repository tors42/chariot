package chariot.internal.impl;

import java.util.Map;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.model.Result;

public class StudiesImpl extends Base implements Internal.Studies {

    public StudiesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<String> exportChapterByStudyAndChapterId(String studyId, String chapterId, Map<String, Object> map) {

        var request = Endpoint.exportChapter.newRequest()
            .path(studyId, chapterId)
            .query(map)
            .build();

        return fetchMany(request);
    }

    @Override
    public Result<String> exportChaptersByStudyId(String studyId, Map<String, Object> map) {
        var request = Endpoint.exportChapters.newRequest()
            .path(studyId)
            .query(map)
            .build();

        return fetchMany(request);
     }

    @Override
    public Result<String> exportStudiesByUserId(String userId, Map<String, Object> map) {
        var request = Endpoint.exportStudies.newRequest()
            .path(userId)
            .query(map)
            .build();

        return fetchMany(request);
    }

}
