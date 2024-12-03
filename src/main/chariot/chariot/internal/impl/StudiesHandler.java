package chariot.internal.impl;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import chariot.api.StudiesApiAuth;
import chariot.internal.*;
import chariot.internal.Util.MapBuilder;
import chariot.model.*;

public class StudiesHandler implements StudiesApiAuth {

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
        return switch(client.fetchHeaders(Endpoint.lastModifiedStudy.endpoint().formatted(studyId))) {
            case Entry(var headers) -> headers.allValues("Last-Modified").stream()
                .map(rfc_1123 -> ZonedDateTime.parse(rfc_1123, DateTimeFormatter.RFC_1123_DATE_TIME))
                .map(One::entry)
                .findFirst()
                .orElse(One.none());
            case Fail(int status, Err err) -> One.fail(status, err);
            case None() -> One.none();
        };
    }

    @Override
    public Many<StudyMeta> listStudiesByUser(String user) {
        return Endpoint.listStudiesByUser.newRequest(request -> request
                .path(user))
            .process(requestHandler);
     }

    @Override
    public Many<ChapterMeta> importPgn(String studyId, Consumer<ImportParams> params) {

        return Endpoint.importStudyChapters.newRequest(request -> request
                .path(studyId)
                .body(MapBuilder.of(ImportParams.class)
                    .addCustomHandler("orientationWhite", (args, map) -> map.put("orientation", "white"))
                    .addCustomHandler("orientationBlack", (args, map) -> map.put("orientation", "black"))
                    .addCustomHandler("modePractice",       (__, map) -> map.put("mode", "practice"))
                    .addCustomHandler("modeHideNextMoves",  (__, map) -> map.put("mode", "conceal"))
                    .addCustomHandler("modeInteractive",    (__, map) -> map.put("mode", "gamebook"))
                    .toMap(params))
                )
            .process(requestHandler);

    }

    @Override
    public One<Void> deleteStudyChapter(String studyId, String chapterId) {
        return Endpoint.deleteStudyChapter.newRequest(request -> request
                .path(studyId, chapterId)
                )
            .process(requestHandler);
    }


    @Override
    public One<PageStudy> byPage(int page) {
        return Endpoint.studyPage.newRequest(request -> request
                .query(Map.of("page", page)))
            .process(requestHandler);
     }

    @Override
    public Many<Study> listStudies() {
        var firstPage = byPage(1);
        if (firstPage instanceof Entry<PageStudy> one) {
            var spliterator = Util.PageSpliterator.of(one.entry(),
                    pageNum -> byPage(pageNum) instanceof Entry<PageStudy> pt ?
                    pt.entry() : new PageStudy(0,0,List.of(),0,0,0,0));
            return Many.entries(StreamSupport.stream(spliterator, false));
        } else {
            return Many.entries(Stream.of());
        }
    }

}
