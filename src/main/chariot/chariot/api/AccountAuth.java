package chariot.api;

import java.util.Set;

import chariot.Client.Scope;
import chariot.model.AccountEmail;
import chariot.model.AccountPreferences;
import chariot.model.Ack;
import chariot.model.User;

/**
 * Read and write account information and preferences.
 */
public interface AccountAuth extends Account {

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
    One<Boolean> getKidModeStatus();

    /**
     * Set the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_write}
     * @param value kid mode status
     */
    One<Ack> setKidModeStatus(boolean value);

    /**
     * Revokes the access token sent as Bearer for this request.
     */
    One<Ack> revokeToken();

    /**
     * Get users followed by logged in user.
     */
    Many<User> following();

    /**
     * Read which scopes are available with current token
     */
    Set<Scope> scopes();

}
