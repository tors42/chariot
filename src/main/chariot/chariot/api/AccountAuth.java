package chariot.api;

import chariot.model.*;

/**
 * Read and write account information and preferences.
 */
public interface AccountAuth {

    /**
     * Public informations about the logged in user.
     */
    One<UserAuth> profile();

    /**
     * Read the email address of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#email_read}
     */
    One<String> emailAddress();

    /**
     * Read the preferences of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    One<AccountPreferences> preferences();

    /**
     * Read the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    One<Boolean> kidMode();

    /**
     * Set the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_write}
     * @param kid true to enable kid mode, false to disable kid mode
     */
    One<Void> kidMode(boolean kid);

    /**
     * Get users followed by logged in user.
     */
    Many<UserAuth> following();

}
