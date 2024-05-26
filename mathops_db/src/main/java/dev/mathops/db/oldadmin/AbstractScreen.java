package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.Cache;

/**
 * A base class for screens.
 */
abstract class AbstractScreen implements IScreen {

    /** The cache. */
    private final Cache cache;

    /** The main window. */
    private final MainWindow mainWindow;

    /** The console. */
    private final Console console;

    /** An error message. */
    private String errorMessage1;

    /** An error message line 2. */
    private String errorMessage2;

    /**
     * Constructs a new {@code AbstractScreen}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    AbstractScreen(final Cache theCache, final MainWindow theMainWindow) {

        this.cache = theCache;
        this.mainWindow = theMainWindow;
        this.console = this.mainWindow.getConsole();

        this.errorMessage1 = CoreConstants.EMPTY;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    protected final Cache getCache() {

        return this.cache;
    }

    /**
     * Gets the main window.
     *
     * @return the main window
     */
    protected final MainWindow getMainWindow() {

        return this.mainWindow;
    }

    /**
     * Gets the console.
     *
     * @return the console
     */
    protected final Console getConsole() {

        return this.console;
    }

    /**
     * Draws a box.
     * @param x the x position of the top left corner
     * @param y the y position of the top left corner
     * @param numCols the number of columns (including border)
     * @param numRows the number of rows (including border)
     */
    protected final void drawBox(final int x, final int y, final int numCols, final int numRows) {

        final StringBuilder builder = new StringBuilder(numCols);
        final int numHorizontal = numCols - 2;
        final int numVertical = numRows - 2;

        builder.append('\u2554');
        for (int i = 0; i < numHorizontal; ++i) {
            builder.append('\u2550');
        }
        builder.append('\u2557');
        final String top = builder.toString();
        this.console.print(top, x, y);

        builder.setLength(0);
        builder.append('\u2551');
        for (int i = 0; i < numHorizontal; ++i) {
            builder.append(' ');
        }
        builder.append('\u2551');
        final String middle = builder.toString();
        for (int j = 0; j < numVertical; ++j) {
            this.console.print(middle, x, y + j + 1);
        }

        builder.setLength(0);
        builder.append('\u255A');
        for (int i = 0; i < numHorizontal; ++i) {
            builder.append('\u2550');
        }
        builder.append('\u255D');
        final String bottom = builder.toString();
        this.console.print(bottom, x, y + numRows - 1);
    }

    /**
     * Draws the error messages, if thee is an error indicated.
     */
    protected final void drawErrors() {

        if (!this.errorMessage1.isBlank()) {
            this.console.print(this.errorMessage1, 1, 21);
            final int len = this.errorMessage1.length();
            this.console.reverse(0, 21, len + 2);
        }

        if (!this.errorMessage2.isBlank()) {
            this.console.print(this.errorMessage2, 1, 22);
            final int len = this.errorMessage2.length();
            this.console.reverse(0, 22, len + 2);
        }
    }

    /**
     * Clears all error messages.
     */
    protected final void clearErrors() {

        this.errorMessage1 = CoreConstants.EMPTY;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Sets a single error message.
     */
    protected final void setError(final String error1) {

        this.errorMessage1 = error1;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Sets a double error message.
     */
    protected final void setError(final String error1, final String error2) {

        this.errorMessage1 = error1;
        this.errorMessage2 = error2;
    }
}
