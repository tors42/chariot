package chariot.model;

import java.util.function.Function;
import java.util.stream.Stream;

/**
 * A container for responses with multiple entries.<br>
 * The response can be either {@link Entries} for successful requests or {@link Fail} for failed requests.<br>
 *
 * {@snippet :
 *      Client client = Client.basic();
 *
 *      Many<Pgn> good = client.studies().exportChaptersByStudyId("YtBYXc3m"); // "Beautiful Checkmates" by NoseKnowsAll
 *      Many<Pgn> bad  = client.studies().exportChaptersByStudyId("non-existing-study-id");
 *
 *      System.out.println(good);  // Entries[stream=java.util.stream.ReferencePipeline$Head@3d121db3]
 *      System.out.println(bad);   // Fail[status=404, info=Info[message=404 - Resource not found]]
 *      }
 * A "lazy" way to access the values is to use the method {@code Many}.{@link Many#stream()} which is shared by all {@code Many}
 * {@snippet :
 *      List<Pgn> goodList = good.stream().toList();
 *      List<Pgn> badList  = bad.stream().toList();
 *
 *      System.out.println(goodList.size()); // 64
 *      System.out.println(badList.size());  // 0
 *      }
 * The problem is that we don't know if the "badList" was empty because the Study was empty or
 * because we failed to find the Study. <br>
 * <br>
 * Another way is to check the type
 * {@snippet :
 *      if (good instanceof Entries<Pgn> entries) {
 *          entries.stream().findFirst().ifPresent(pgn ->
 *              System.out.println(pgn.tagMap().get("Event"))); // Beautiful Checkmates: Study by Ercole del Rio
 *      }
 *
 *      if (bad instanceof Entries<Pgn> entries) {
 *          // not reached
 *      }
 *
 *      // If we are interested in any failures, we can check for the Fail type
 *      if (bad instanceof Fail<Pgn> fail) {
 *          System.out.println(fail.status()); // 404
 *      }
 *      }
 * When Pattern Matching in {@code switch} arrives, we can leave out the {@code if}-statements
 * {@snippet :
 *      String message = switch(bad) {
 *           case Entries<Pgn> entries -> "There are " + entries.stream().count() + " PGNs";
 *           case Fail<Pgn> fail -> "Couldn't find the Study! (" + fail.status() + ")";
 *      };
 *
 *      System.out.println(message); // Couldn't find the Study! (404)
 *      }
 */
 public sealed interface Many<T> permits
    Entries,
    Fail {

    static <T> Many<T> entries(Stream<T> stream) {
        return new Entries<>(stream);
    }

    static <T> Many<T> fail(int status, Err err) {
        return new Fail<>(status, err);
    }

    default Stream<T> stream() {
        return this instanceof Entries<T> many ?
            many.stream() :
            Stream.of();
    }

    default <R> Many<R> mapMany(Function<T, R> mapper) {
        if (this instanceof Fail<T> f) {
            return Many.fail(f.status(), f.info());
        } else {
            return Many.entries(stream().map(mapper));
        }
    }

}
