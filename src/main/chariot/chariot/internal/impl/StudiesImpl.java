package chariot.internal.impl;

import java.util.function.Consumer;

import chariot.api.Studies;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class StudiesImpl extends Base implements Studies {

    public StudiesImpl(InternalClient client) {
        super(client);
    }

    @Override
    public Many<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params) {
        return Endpoint.exportChapter.newRequest(request -> request
                .path(studyId, chapterId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(this);
    }

    @Override
    public Many<Pgn> exportChaptersByStudyId(String studyId, Consumer<Params> params) {
        return Endpoint.exportChapters.newRequest(request -> request
                .path(studyId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(this);
     }

    @Override
    public Many<Pgn> exportStudiesByUserId(String userId, Consumer<Params> params) {
        return Endpoint.exportStudies.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(this);
    }

}
