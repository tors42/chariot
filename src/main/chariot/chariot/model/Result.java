package chariot.model;

import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * A "container" for responses, which can be of the types {@link One}, {@link Many}, {@link Zero} or {@link Fail}.
 *
 * <p>Examples of how to access a response value:
 * {@snippet :
 *      Client client = Client.basic();
 *      Result<User> result = client.users().byId("lichess");
 *
 *      // 1: Not accessing the value directly, but printing the "raw" container (with value if inside)
 *      System.out.println(result.toString());
 * }
 *
 * {@snippet :
 *      // 2: Yolo get! Trying to access the value without knowing if it is present... (use with caution, might throw NoSuchElementException)!
 *      User user = result.get();
 * }
 *
 * {@snippet :
 *      // 3: Use if-statement to check if value is present
 *      if (result.isPresent()) {
 *          User user = result.get();
 *          System.out.println("Patron: " + user.patron());
 *      }
 * }
 *
 * {@snippet :
 *      // 4: Use a lambda if value is present
 *      result.ifPresent(user -> System.out.println("Patron: " + user.patron()));
 * }
 *
 * {@snippet :
 *      // 5: Use a lambda if value is present, or else another lambda
 *      result.ifPresentOrElse(user -> System.out.println("Patron: " + user.patron()), () -> System.out.println("Couldn't find user lichess!"));
 * }
 *
 * {@snippet :
 *      // 6: Stream the value
 *      result.stream().forEach(user -> System.out.println(user.id() + " Patron: " + user.patron()));
 * }
 *
 * {@snippet :
 *      // 7: instanceof pattern match
 *      if (result instanceof Result.One<User> one) {
 *          User user = one.entry();
 *          System.out.println("Patron: " + user.patron());
 *      } else if (result instanceof Result.Many<User> many) {
 *          Stream<User> users = many.entries();
 *          users.forEach(user -> System.out.println(user.id() + " Patron: " + user.patron()));
 *      }
 * }
 *
 * {@snippet :
 *      // 8: JEP 420: Pattern Matching for switch (Second Preview) / --enable-preview
 *      String message = switch(result) {
 *          case Result.One<User>  one  -> "Patron: " + one.entry().patron();
 *          case Result.Many<User> many -> many.entries().map(user -> user.id() + " Patron: " + user.patron()).collect(Collectors.joining("\n"));
 *          case Result.Zero<User> zero -> "No user found";
 *          case Result.Fail<User> fail -> fail.message();
 *      };
 *      System.out.println(message);
 * }
 *
 * {@snippet :
 *      // 9: Deconstruction patterns, "Future work" in JEP 420
 *      String message = switch(result) {
 *          case Result.One<User>(var user)     -> "Patron: " + user.patron();
 *          case Result.Many<User>(var users)   -> users.map(user -> user.id() + " Patron: " + user.patron()).collect(Collectors.joining("\n"));
 *          case Result.Zero<User>()        -> "No user found";
 *          case Result.Fail<User>(var message) -> message;
 *      };
 *      System.out.println(message);
 * }
 */
public sealed interface Result<T> {
    /**
     * One entry
     * @param entry Single entry
     */
    record One<T>(T entry) implements Result<T> {}
    /**
     * A Stream of entries
     * @param entries Stream of entries
     */
    record Many<T>(Stream<T> entries) implements Result<T> {}
    /**
     * No entry
     */
    record Zero<T>() implements Result<T> {}
    /**
     * Failure
     * @param message A message about the failure
     */
    record Fail<T>(String message) implements Result<T> {}

    public static <T> Result<T> one(T t) {
        return new One<>(t);
    }

    public static <T> Result<T> many(Stream<T> t) {
        return new Many<>(t);
    }

    public static <T> Zero<T> zero() {
        return new Zero<>();
    }

    public static <T> Fail<T> fail(String message) {
        return new Fail<>(message);
    }

    /**
     * If a value is present, returns `true`, otherwise `false`.<br/>
     * See {@link java.util.Optional#isPresent}
     */
     default boolean isPresent() {
        return this instanceof One || this instanceof Many;
    }

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.<br/>
     * If the result contains many entries, the action is performed on each entry
     * See {@link java.util.Optional#ifPresent(Consumer)}
     */
    default void ifPresent(Consumer<T> consumer) {
        ifPresentOrElse(consumer, () -> {});
    }

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given empty-based action.<br>
     * If the result contains many entries, the action is performed on each entry
     * See {@link java.util.Optional#ifPresentOrElse(Consumer, Runnable)}
     */
     default void ifPresentOrElse(Consumer<T> consumer, Runnable action) {
        if (this instanceof One<T> o) {
            consumer.accept(o.entry());
        } else if (this instanceof Many<T> m) {
            m.entries.forEach(consumer);
        } else {
            action.run();
        }
    }

    /**
     * If a single value is present, returns a stream with that value. Or if there are multiple entries, returns those entries.
     * See {@link java.util.Optional#ifPresentOrElse(Consumer, Runnable)}
     */
     default Stream<T> stream() {
        if (this instanceof One<T> o) {
            return Stream.of(o.entry());
        } else if (this instanceof Many<T> m) {
            return m.entries();
        } else {
            return Stream.of();
        }
    }

    /**
     * Returns the entry. Note, one should check if entry is present first.<br/>
     * If there are multipe entries, the first entry is returned.<br/>
     * If there is no entry available, throws NoSuchElementException.
     */
    default T get() {
        if (this instanceof One<T> o) {
            return o.entry();
        } else if (this instanceof Many<T> m) {
            return m.entries().findFirst().orElseThrow();
        } else {
            throw new NoSuchElementException();
        }
    }

    default T getOrElse(T t) {
        if (this instanceof One<T> o) {
            return o.entry();
        } else if (this instanceof Many<T> m) {
            return m.entries().findFirst().orElse(t);
        } else {
            return t;
        }
    }

    /**
     * Returns a error message, in case the result is Fail.
     */
    default String error() {
        if (this instanceof Fail<T> f) {
            return f.message();
        } else {
            return "<no error>";
        }
    }
}
