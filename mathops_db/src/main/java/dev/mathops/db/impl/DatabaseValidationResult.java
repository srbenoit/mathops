package dev.mathops.db.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The results of database validation.
 */
public final class DatabaseValidationResult {

    /** The validation status. */
    private final EValidationStatus status;

    /** A list of errors. */
    private final List<String> errors;

    /** A list of actions taken. */
    private final List<String> actionsTaken;

    /**
     * Constructs a new {@code DatabaseValidationResult}.
     *
     * @param theStatus       the status
     * @param theErrors       any errors encountered
     * @param theActionsTaken a list of actions taken during the validation process
     */
    public DatabaseValidationResult(final EValidationStatus theStatus, final Collection<String> theErrors,
                                    final Collection<String> theActionsTaken) {

        if (theStatus == null) {
            throw new IllegalArgumentException("Status may not be null");
        }

        this.status = theStatus;
        this.errors = theErrors == null ? new ArrayList<>(0) : new ArrayList<>(theErrors);
        this.actionsTaken = theActionsTaken == null ? new ArrayList<>(0) : new ArrayList<>(theActionsTaken);
    }

    /**
     * Gets the validation status.
     *
     * @return the status
     */
    public EValidationStatus getStatus() {

        return this.status;
    }

    /**
     * Gets a copy of the list of errors encountered (should be empty if status is "VALID" and non-empty if status
     * is "INNVALID").
     *
     * @return a copy of the list of errors
     */
    public List<String> getErrors() {

        return new ArrayList<>(this.errors);
    }

    /**
     * Gets a copy of the list of actions that were taken during the process of validating the database.  For example,
     * creating missing roles, the database itself, or missing tables, or altering tables to add missing fields.
     *
     * @return a copy of the list of actions taken
     */
    public List<String> getActionsTaken() {

        return new ArrayList<>(this.actionsTaken);
    }
}
