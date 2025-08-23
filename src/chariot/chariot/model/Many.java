package chariot.model;

import java.util.function.Function;
import java.util.stream.Stream;

/// A container for responses with multiple entries.  
///
/// The response can be either {@link Entries} for successful requests or {@link Fail} for failed requests.  
///
/// Here is an example where two request are made and where one result has values and the other doesn't,
/// {@snippet :
///      Client client = Client.basic();
///
///      Many<PGN> good = client.studies().exportChaptersByStudyId("YtBYXc3m"); // "Beautiful Checkmates" by NoseKnowsAll
///      Many<PGN> bad  = client.studies().exportChaptersByStudyId("non-existing-study-id");
///
///      IO.println(good);  // Entries[stream=java.util.stream.ReferencePipeline$Head@3d121db3]
///      IO.println(bad);   // Fail[status=404, message=404 - Resource not found]
///      }
/// Typically one would pattern match on the result in order to process it,
/// {@snippet :
///      String process(Many<PGN> result) {
///         return switch(result) {
///             case Entries(Stream<PGN> stream)  -> "Found " + stream.count() + " PGNs!";
///             case Fail(int status, String msg) -> "Failed to find PGNs! Status " + status;
///         };
///      }
///
///      String goodMessage = process(good);
///      String badMessage = process(bad);
///
///      IO.println(goodMessage); // Found 64 PGNs!
///      IO.println(badMessage);  // Failed to find PGNs! Status 404
///      }
///
/// Another way to access the values is to use the method {@link #stream()},
/// {@snippet :
///      List<PGN> goodList = good.stream().toList();
///      List<PGN> badList  = bad.stream().toList();
///
///      IO.println(goodList.size()); // 64
///      IO.println(badList.size());  // 0
///      }
/// But a problem with this is that we don't know if the `badList` was empty because the Study
/// was empty or because we failed to find the Study.
public sealed interface Many<T> permits
    Entries,
    Fail {

    static <T> Many<T> entries(Stream<T> stream) {
        return new Entries<>(stream);
    }

    static <T> Many<T> fail(int status, String message) {
        return new Fail<>(status, message);
    }

    default Stream<T> stream() {
        return switch(this) {
            case Entries(Stream<T> stream) -> stream;
            case Fail(_,_)                 -> Stream.of();
        };
    }

    default <R> Many<R> map(Function<T, R> mapper) {
        return switch(this) {
            case Entries(Stream<T> stream)    -> Many.entries(stream.map(mapper));
            case Fail(int status, String msg) -> Many.fail(status, msg);
        };
    }
}
