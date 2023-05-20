package chariot.model;

public sealed interface UserAuth extends User permits UserProfileAuth {
    private UserAuthFlags auth() {
        return ((UserProfileAuth)this).auth();
    }
    default boolean followable() { return auth().followable(); }
    default boolean following()  { return auth().following();  }
    default boolean followsYou() { return auth().followsYou(); }
    default boolean blocked()    { return auth().blocking();   }
}
