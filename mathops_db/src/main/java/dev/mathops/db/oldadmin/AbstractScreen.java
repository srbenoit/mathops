package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.old.Cache;

import java.awt.event.KeyEvent;

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

    /** Flag indicating lock screen is being shown. */
    private boolean showingLock = false;

    /** The lock-screen password. */
    private final String lockPassword;

    /** The lock password being typed. */
    private final Field lockPasswordField;

    /** An error message. */
    private String errorMessage1;

    /** An error message line 2. */
    private String errorMessage2;

    /** The current selection index. */
    private int selection;

    /** The number of possible selections. */
    private int numSelections;

    /**
     * Constructs a new {@code AbstractScreen}.
     *
     * @param theCache         the cache
     * @param theMainWindow    the main window
     * @param theNumSelections the number of selections
     */
    AbstractScreen(final Cache theCache, final MainWindow theMainWindow, final int theNumSelections) {

        this.cache = theCache;
        this.mainWindow = theMainWindow;
        this.console = this.mainWindow.getConsole();

        this.lockPassword = theMainWindow.getUserData().getClearPassword("LOCK");
        this.lockPasswordField = new Field(this.console, 21, 11, 8, true, null);

        this.errorMessage1 = CoreConstants.EMPTY;
        this.errorMessage2 = CoreConstants.EMPTY;

        this.selection = 0;
        this.numSelections = theNumSelections;
    }

    /**
     * Gets the cache.
     *
     * @return the cache
     */
    final Cache getCache() {

        return this.cache;
    }

    /**
     * Gets the main window.
     *
     * @return the main window
     */
    final MainWindow getMainWindow() {

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
     * Tests whether the "locked" box is showing.
     *
     * @return true if locked
     */
    protected boolean isLocked() {

        return this.showingLock;
    }

    /**
     * Draws the "locked" box with password entry.
     */
    void drawLocked() {

        drawBox(18, 8, 39, 6);
        this.console.print("Enter your ADMIN screen password:", 21, 10);
        this.lockPasswordField.draw();
    }

    /**
     * Sets the selected menu item index.
     *
     * @param newSelection the new selection index
     */
    void setSelection(final int newSelection) {

        this.selection = 0;
    }

    /**
     * Gets the current selection.
     *
     * @return the selection
     */
    int getSelection() {

        return this.selection;
    }

    /**
     * Increments the selection index, wrapping to zero beyond the last selection.
     */
    void incrementSelection() {

        ++this.selection;
        if (this.selection >= this.numSelections) {
            this.selection = 0;
        }
    }

    /**
     * Decrements the selection index, wrapping to the last item to the left of zero.
     */
    void decrementSelection() {

        --this.selection;
        if (this.selection < 0) {
            this.selection = this.numSelections - 1;
        }
    }

    /**
     * Processes a key press in the locked state.
     *
     * @param key the key
     */
    void processKeyPressInLocked(final int key) {

        if (key == KeyEvent.VK_ENTER) {
            final String entered = this.lockPasswordField.getValue();
            if (entered.equals(this.lockPassword)) {
                this.showingLock = false;
                clearErrors();
                this.console.setCursor(-1, -1);
            } else {
                setError("Invalid password");
            }
        } else {
            clearErrors();
            this.lockPasswordField.processKey(key);
        }
    }

    /**
     * Processes a key typed in the locked state.
     *
     * @param character the character
     */
    void processKeyTypedInLocked(final char character) {

        this.lockPasswordField.processChar(character);
    }

    /**
     * Handles the selection of the "Lock" item.
     */
    void doLock() {

        if (this.lockPassword != null) {
            this.showingLock = true;
            this.lockPasswordField.clear();
            this.lockPasswordField.activate();
        }
    }

    /**
     * Draws a box.
     *
     * @param x       the x position of the top left corner
     * @param y       the y position of the top left corner
     * @param numCols the number of columns (including border)
     * @param numRows the number of rows (including border)
     */
    final void drawBox(final int x, final int y, final int numCols, final int numRows) {

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
    final void drawErrors() {

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
    final void clearErrors() {

        this.errorMessage1 = CoreConstants.EMPTY;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Sets a single error message.
     */
    final void setError(final String error1) {

        this.errorMessage1 = error1;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Sets a double error message.
     */
    final void setError(final String error1, final String error2) {

        this.errorMessage1 = error1;
        this.errorMessage2 = error2;
    }
}
