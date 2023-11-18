package dev.mathops.core.log;

/**
 * An interface for processes that can accumulate error and warning messages.
 */
interface IErrorWarningLog {

    /**
     * Adds an error to the list of errors encountered during the process.
     *
     * @param message the error message
     */
    void addError(String message);

    /**
     * Adds a warning to the list of warnings encountered during the process.
     *
     * @param message the warning message
     */
    void addWarning(String message);

    /**
     * Tests whether any errors have been added to the error log.
     *
     * @return {@code true} if at least one error has been added
     */
    boolean isError();

    /**
     * Tests whether any warnings have been added to the warning log.
     *
     * @return {@code true} if at least one warning has been added
     */
    boolean isWarning();

    /**
     * Gets the full list of errors that have been added to the error log.
     *
     * @return the array of errors; an empty array if none have been added
     */
    String[] getErrors();

    /**
     * Gets the full list of errors that have been added to the error log in an HTML format.
     *
     * @return the list of errors; an empty string if none have been added
     */
    String getErrorsHtml();

    /**
     * Gets the full list of warnings that have been added to the warning log.
     *
     * @return the array of warnings; an empty array if none have been added
     */
    String[] getWarnings();

    /**
     * Logs a warning to the system log and to the warnings list of an {@code ILogicProcess}.
     *
     * @param message the message to log
     * @return false
     */
    boolean indicateWarning(String message);

    /**
     * Logs an error to the system log and to the errors list of an {@code ILogicProcess}.
     *
     * @param message the message to log
     * @return false
     */
    boolean indicateError(String message);

    /**
     * Logs the failure to obtain an interface to the system log and to the errors list of an {@code ILogicProcess}.
     *
     * @param name the name of the interface being queried
     * @return false
     */
    boolean indicateNoInterface(String name);

    /**
     * Logs a failure with an exception to the system log and to the errors list of an {@code ILogicProcess}.
     *
     * @param message the message to log
     * @param ex      the exception that was thrown
     * @return false
     */
    boolean indicateException(String message, Throwable ex);
}
