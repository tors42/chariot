package chariot.model;

import java.util.Optional;
import java.util.function.*;

/**
 * A container for responses with a single entry.<br>
 * The response can be either {@link Entry} for responses with a value or {@link None} or {@link Fail} for responses without a value.<br>
 *
 * {@snippet :
 *      Client client = Client.basic();
 *
 *      One<User> good = client.users().byId("lichess");
 *      One<User> bad  = client.users().byId("non-existing-user-id");
 *
 *      System.out.println(good);  // Entry[entry=User[id=lichess, username= ...
 *      System.out.println(bad);   // Fail[status=404, info=Empty[]]
 *      }
 * One way to access the value is via an {@link Optional} which can be gotten via {@link #maybe()}
 * {@snippet :
 *      Optional<User> goodUser = good.maybe();
 *      Optioanl<User> badUser  = bad.maybe();
 *
 *      goodUser.ifPresent(user -> System.out.println(user.username())); // Lichess
 *      badUser.ifPresent(user  -> System.out.println(user.username())); //
 *      }
 * Many of the {@code Optional} methods are also available directly via {@link One} for convenience
 * {@snippet :
 *      String goodName = good.map(user -> user.username()).orElse("No user!");
 *      String badName  =  bad.map(user -> user.username()).orElse("No user!");
 *
 *      System.out.println(goodName); // Lichess
 *      System.out.println(badName);  // No user!
 *      }
 * Another way is to check the type
 * {@snippet :
 *      if (good instanceof Entry<User> u) {
 *          User user = u.entry();
 *          System.out.println(user.url()); // https://lichess.org/@/Lichess
 *      }
 *
 *      if (bad instanceof Entry<User> u) {
 *          // not reached
 *      }
 *
 *      // If we are interested in any failures, we can check for the Fail type
 *      if (bad instanceof Fail<User> fail) {
 *          System.out.println(fail.status()); // 404
 *      }
 *      }
 * Note, "inverted" match in {@code if}-statements can also be used to get an entry into scope
 * {@snippet :
 *      if (! (good instanceof Entry<User> u)) {
 *          return;
 *      }
 *
 *      User user = u.entry();  // The u-variable is reachable here!
 *
 *      var teams = client.teams().byUserId(user.id());
 *      ...
 *      }
 * When Pattern Matching in {@code switch} arrives, we can leave out the {@code if}-statements
 * {@snippet :
 *      String message = switch(bad) {
 *           case Entry<User> user -> "Found user!";
 *           case NoEntry<User> nouser -> "Couldn't find user!"; // NoEntry "catches" both None and Fail
 *      };
 *
 *      System.out.println(message); // Couldn't find user!
 *      }
 */

public sealed interface One<T> permits
    Entry,
    NoEntry {

    static <T> One<T> entry(T entry) {
        return new Entry<>(entry);
    }

    static <T> One<T> none() {
        return new None<>();
    }

    static <T> One<T> fail(int status, Err err) {
        return new Fail<>(status, err);
    }


    default Optional<T> maybe() { return this instanceof Entry<T> t ? Optional.of(t.entry()) : Optional.empty(); }

    default <R> Optional<R> map(Function<? super T, ? extends R> mapper) { return maybe().map(mapper); }
    default T orElse(T other) { return maybe().orElse(other); }
    default T orElseGet(Supplier<? extends T> supplier) { return maybe().orElseGet(supplier); }
    default void ifPresent(Consumer<? super T> consumer) { maybe().ifPresent(consumer); }
    default void ifPresentOrElse(Consumer<? super T> consumer, Runnable action) { maybe().ifPresentOrElse(consumer, action); }
    default boolean isPresent() { return maybe().isPresent(); }
    default T get() { return maybe().get(); }
    default <R> One<R> mapOne(Function<T, R> mapper) {
        if (this instanceof Entry<T> one) {
            return One.entry(mapper.apply(one.entry()));
        } else if (this instanceof Fail<T> f) {
            return One.fail(f.status(), f.info());
        } else {
            return One.none();
        }
    }
}
