package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;

import chariot.api.Studies;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class StudiesHandler implements Studies {

    private final RequestHandler requestHandler;
    private final InternalClient client;

    public StudiesHandler(InternalClient client, RequestHandler requestHandler) {
        this.client = client;
        this.requestHandler = requestHandler;
    }

    @Override
    public Many<Pgn> exportChapterByStudyAndChapterId(String studyId, String chapterId, Consumer<Params> params) {
        return Endpoint.exportChapter.newRequest(request -> request
                .path(studyId, chapterId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public Many<Pgn> exportChaptersByStudyId(String studyId, Consumer<Params> params) {
        return Endpoint.exportChapters.newRequest(request -> request
                .path(studyId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
     }

    @Override
    public Many<Pgn> exportStudiesByUserId(String userId, Consumer<Params> params) {
        return Endpoint.exportStudies.newRequest(request -> request
                .path(userId)
                .query(MapBuilder.of(Params.class).toMap(params)))
            .process(requestHandler);
    }

    @Override
    public One<ZonedDateTime> lastModifiedByStudyId(String studyId) {
        var headers = client.fetchHeaders(Endpoint.lastModifiedStudy.endpoint().formatted(studyId));
        if (headers == null) return One.fail(404, Err.from("Maybe 404"));
        return headers.allValues("Last-Modified").stream()
            .map(rfc_1123 -> ZonedDateTime.parse(rfc_1123, DateTimeFormatter.RFC_1123_DATE_TIME))
            .map(One::entry)
            .findFirst()
            .orElse(One.fail(404, Err.from("Maybe 404")));
    }

    @Override
    public Many<StudyMeta> listStudiesByUser(String user) {
        return Endpoint.listStudiesByUser.newRequest(request -> request
                .path(user))
            .process(requestHandler);
     }

}
