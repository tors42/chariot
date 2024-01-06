package chariot.api;

import java.time.ZonedDateTime;
import java.util.function.Consumer;
import java.util.function.Function;

import chariot.model.*;

/**
 * Read and write account information and preferences.
 */
public interface AccountAuth {

    /**
     * Public informations about the logged in user.<br>
     * <br>
     * Example usage:
     * {@snippet class=AccountAuth region=profile }
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

    /**
     * Entries of the timeline<br>
     * <br>
     * Example usage:
     * {@snippet class=AccountAuth region=timeline }
     */
    Many<TimelineEntry> timeline(Consumer<TimelineParams> params);

    /**
     * See {@link #timeline(Consumer)}
     */
    default Many<TimelineEntry> timeline() { return timeline(__ -> {}); }

    public interface TimelineParams {
        /**
         * @param nb How many entries to fetch. Max 30.
         */
        TimelineParams nb(int nb);
        TimelineParams since(long since);
        default TimelineParams since(ZonedDateTime since) { return since(zdtToMillis(since)); }
        default TimelineParams since(Function<ZonedDateTime, ZonedDateTime> now) { return since(now.apply(ZonedDateTime.now())); }

        private static long zdtToMillis(ZonedDateTime zdt) { return zdt.toInstant().getEpochSecond() * 1000; }
    }


}
