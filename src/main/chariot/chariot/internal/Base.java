package chariot.internal;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

import chariot.model.Result;

public class Base {

    protected final InternalClient client;

    protected Base(InternalClient client) {
        this.client = client;
    }
    public <T> Result<T> fetchMany(Request<T> request) {
        return fetch(request, (s) -> Result.many(s.data()));
    }

    public <T> Result<T> fetchOne(Request<T> request) {
        return fetch(request, (s) -> s.data().findFirst().map(e -> Result.one(e)).orElse(Result.zero()));
    }

    public <T> Result<T> fetchArr(Request<T[]> request) {
        var res = fetchMany(request);
        if (res instanceof Result.Many<T[]> many) {
            return Result.many(many.entries().flatMap(Arrays::stream));
        }
        return Result.fail(res.error());
    }

    // todo,
    // should probably "move out" the retry logic to this place instead - i.e,
    //
    // var tries = config.tries();
    // if (failure.statusCode() == 429) {
    //    ...
    // }
    //
    // Also figure out a way to identify/specify if/that the resource
    // is 429:ed because of hammering or if it is because the x:th resource in the y time
    // window has been reached (for instance, one can create 12:ish tournaments per 24:ish hours)
    //
    // If because of hammering, it will be enough to wait 60 seconds.
    // But if the 12th tournament has already been created,
    // 60 seconds won't matter - might need to wait 24 hours...
    // So automatic retry might not be wanted in that case...
    private <T> Result<T> fetch(Request<T> request, Function<InternalResult.Success<T>, Result<T>> successMapper) {
        var result = client.fetch(request);

        if (result instanceof InternalResult.Success<T> success) {
            return successMapper.apply(success);
        } else if (result instanceof InternalResult.Failure<T> failure) {
            var message = Optional.ofNullable(failure.err()).map(err -> err.text()).orElse("<unknown>");
            return Result.fail(failure.statusCode() + " - " + message);
        } else if (result instanceof InternalResult.Error<T> error) {
            return Result.fail(error.error());
        }

        return Result.fail("<unknown>");
    }
}
