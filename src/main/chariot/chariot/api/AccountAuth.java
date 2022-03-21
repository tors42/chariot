package chariot.api;

import java.util.Set;
import java.util.function.Supplier;

import chariot.Client.Scope;
import chariot.model.AccountEmail;
import chariot.model.AccountPreferences;
import chariot.model.Ack;
import chariot.model.Result;
import chariot.model.User;

/**
 * Read and write account information and preferences.
 */
public interface AccountAuth extends Account {

    /**
     * Public informations about the logged in user.
     */
    Result<User> profile();

    /**
     * Read the email address of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#email_read}
     */
    Result<AccountEmail> emailAddress();

    /**
     * Read the preferences of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    Result<AccountPreferences> preferences();

    /**
     * Read the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_read}
     */
    Result<Boolean> getKidModeStatus();

    /**
     * Set the kid mode status of the logged in user.<br/>
     * Auth {@link chariot.Client.Scope#preference_write}
     * @param value kid mode status
     */
    Result<Ack> setKidModeStatus(boolean value);

    /**
     * Revokes the access token sent as Bearer for this request.
     */
    Result<Ack> revokeToken();

    /**
     * Get users followed by logged in user.
     */
    Result<User> following();

    /**
     * Read which scopes are available with current token
     */
    Set<Scope> scopes();

}
