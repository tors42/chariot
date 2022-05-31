package chariot.internal.impl;

import java.util.Map;

import chariot.internal.Base;
import chariot.internal.Endpoint;
import chariot.internal.InternalClient;
import chariot.internal.Util;
import chariot.model.Pgn;
import chariot.model.Result;

public class StudiesImpl extends Base implements Internal.Studies {

    public StudiesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Result<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId, Map<String, Object> map) {

        var request = Endpoint.exportChapter.newRequest()
            .path(studyId, chapterId)
            .query(map)
            .build();

        var result = fetchMany(request);
        return Util.toPgnResult(result);
    }

    @Override
    public Result<Pgn> exportChaptersByStudyId(String studyId, Map<String, Object> map) {
        var request = Endpoint.exportChapters.newRequest()
            .path(studyId)
            .query(map)
            .build();
        var result = fetchMany(request);
        return Util.toPgnResult(result);
     }

    @Override
    public Result<Pgn> exportStudiesByUserId(String userId, Map<String, Object> map) {
        var request = Endpoint.exportStudies.newRequest()
            .path(userId)
            .query(map)
            .build();
        var result = fetchMany(request);
        return Util.toPgnResult(result);
    }

}
