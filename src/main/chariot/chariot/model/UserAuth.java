package chariot.model;

public sealed interface UserAuth extends User permits UserProfileData {
    private UserAuthFlags auth() {
        return ((UserProfileData)this).authFlags().orElse(null);
    }
    default boolean followable() { return auth().followable(); }
    default boolean following()  { return auth().following();  }
    default boolean followsYou() { return auth().followsYou(); }
    default boolean blocked()    { return auth().blocking();   }
}
