package chariot.api;

import chariot.model.*;

/**
 * Read and write account information and preferences.
 */
public interface AccountAuth {

    /**
     * Public informations about the logged in user.
     */
    One<User> profile();

    /**
     * Read the email address of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#email_read}
     */
    One<AccountEmail> emailAddress();

    /**
     * Read the preferences of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    One<AccountPreferences> preferences();

    /**
     * Read the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    One<AccountKid> getAccountKidMode();

    /**
     * Set the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_write}
     * @param value kid mode status
     */
    One<Void> setAccountKidMode(boolean value);

    /**
     * Get users followed by logged in user.
     */
    Many<User> following();

}
