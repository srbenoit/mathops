package dev.mathops.db.oldadmin;

import dev.mathops.db.old.Cache;

import java.awt.event.KeyEvent;

/**
 * The Resource screen.
 */
final class ScreenResource extends AbstractScreen {

    /** The character to select "Loan". */
    private static final char LOAN_CHAR = 'l';

    /** The character to select "Return". */
    private static final char RETURN_CHAR = 'r';

    /** The character to select "Outstanding". */
    private static final char OUTSTANDING_CHAR = 'o';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenResource}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenResource(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("RESOURCE OPTIONS:   Loan  Return  Outstanding  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                console.reverse(19, 0, 6);
                console.print("Record PACe materials being loaned to a student", 0, 1);
                break;
            case 1:
                console.reverse(25, 0, 8);
                console.print("Record the return of borrowed PACe materials", 0, 1);
                break;
            case 2:
                console.reverse(33, 0, 13);
                console.print("View unreturned PACe materials for a student", 0, 1);
                break;
            case 3:
                console.reverse(46, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 4:
                console.reverse(52, 0, 6);
                console.print("Return to MAIN ADMIN menu", 0, 1);
                break;
        }

        if (isLocked()) {
            drawLocked();
        }

        drawErrors();

        console.commit();
    }

    /**
     * Processes a key pressed.
     *
     * @param key       the key code
     * @param modifiers key modifiers
     * @return true if the screen should be repainted after this event
     */
    public boolean processKeyPressed(final int key, final int modifiers) {

        boolean repaint = false;

        if (isLocked()) {
            processKeyPressInLocked(key);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 4) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 4;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {

            if (this.selection == 0) {
                doLoan();
                repaint = true;
            } else if (this.selection == 1) {
                doReturn();
                repaint = true;
            } else if (this.selection == 2) {
                doOutstanding();
                repaint = true;
            } else if (this.selection == 3) {
                doLock();
                repaint = true;
            } else if (this.selection == 4) {
                doQuit();
                repaint = true;
            }
        }

        return repaint;
    }

    /**
     * Processes a key typed.
     *
     * @param character the character
     * @return true if the screen should be repainted after this event
     */
    public boolean processKeyTyped(final char character) {

        boolean repaint = false;

        if (isLocked()) {
            processKeyTypedInLocked(character);
            repaint = true;
        } else if ((int) character == (int) LOAN_CHAR) {
            doLoan();
            repaint = true;
        } else if ((int) character == (int) RETURN_CHAR) {
            doReturn();
            repaint = true;
        } else if ((int) character == (int) OUTSTANDING_CHAR) {
            doOutstanding();
            repaint = true;
        } else if ((int) character == (int) LOCK_CHAR) {
            doLock();
            repaint = true;
        } else if ((int) character == (int) QUIT_CHAR) {
            doQuit();
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "Loan" item.
     */
    private void doLoan() {

    }

    /**
     * Handles the selection of the "Return" item.
     */
    private void doReturn() {

    }

    /**
     * Handles the selection of the "Outstanding" item.
     */
    private void doOutstanding() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
