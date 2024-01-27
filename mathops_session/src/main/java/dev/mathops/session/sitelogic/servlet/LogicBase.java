package dev.mathops.session.sitelogic.servlet;

import dev.mathops.commons.log.Log;
import dev.mathops.db.old.cfg.DbProfile;

/**
 * The base class for servlet logic modules, providing storage of the context and database connection, and standardized
 * management of error messages.
 */
class LogicBase {

    /** The context. */
    private final DbProfile dbProfile;

    /** Indication that an operation failed. */
    private boolean error;

    /** A text description of the error that occurred on a failure. */
    private String errorText;

    /**
     * Constructs a new {@code AbstractLogic}.
     *
     * @param theDbProfile the database profile under which the logic will be executed
     */
    LogicBase(final DbProfile theDbProfile) {

        this.dbProfile = theDbProfile;
        this.error = false;
        this.errorText = null;
    }

    /**
     * Gets the database profile.
     *
     * @return the database profile
     */
    public final DbProfile getDbProfile() {

        return this.dbProfile;
    }

    /**
     * Gets the error flag.
     *
     * @return The error flag returned by the login server.
     */
    public final boolean isError() {

        return this.error;
    }

    /**
     * Gets the error text.
     *
     * @return The error text returned by the login server.
     */
    public final String getErrorText() {

        return this.errorText;
    }

    /**
     * Sets the error message text and the flag indicating there is an error.
     *
     * @param errText the error message text
     */
    final void setErrorText(final String errText) {

        this.error = true;
        this.errorText = errText;
        Log.warning(errText);
    }
}
