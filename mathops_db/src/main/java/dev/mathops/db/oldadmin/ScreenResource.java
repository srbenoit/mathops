package dev.mathops.db.oldadmin;

import dev.mathops.db.logic.Cache;

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

    /**
     * Constructs a new {@code ScreenResource}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenResource(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 5);
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("RESOURCE OPTIONS:   Loan  Return  Outstanding  locK  QUIT", 0, 0);

        switch (getSelection()) {
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

        boolean repaint = true;

        if (isLocked()) {
            processKeyPressInLocked(key);
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            incrementSelection();
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            decrementSelection();
        } else if (key == KeyEvent.VK_ENTER) {
            final int sel = getSelection();

            if (sel == 0) {
                doLoan();
            } else if (sel == 1) {
                doReturn();
            } else if (sel == 2) {
                doOutstanding();
            } else if (sel == 3) {
                doLock();
            } else if (sel == 4) {
                doQuit();
            }
        } else {
            repaint = false;
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

        boolean repaint = true;

        if (isLocked()) {
            processKeyTypedInLocked(character);
        } else if ((int) character == (int) LOAN_CHAR) {
            setSelection(0);
            doLoan();
        } else if ((int) character == (int) RETURN_CHAR) {
            setSelection(1);
            doReturn();
        } else if ((int) character == (int) OUTSTANDING_CHAR) {
            setSelection(2);
            doOutstanding();
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(3);
            doLock();
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(4);
            doQuit();
        } else {
            repaint = false;
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
