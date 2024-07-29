package dev.mathops.db.oldadmin;

import dev.mathops.db.Cache;

import java.awt.event.KeyEvent;

/**
 * The MPE screen.
 */
final class ScreenMPE  extends AbstractStudentScreen {

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Add". */
    private static final char CREDIT_CHAR = 'C';

    /** The character to select "Delete". */
    private static final char CHALLENGE_CHAR = 'H';

    /** The character to select "Check Ans". */
    private static final char TRANSFER_CHAR = 'T';

    /** The character to select "Make-Upo. */
    private static final char BYPASS_CHAR = 'B';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /**
     * Constructs a new {@code ScreenMPE}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenMPE(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 7);
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("PLACEMENT OPTIONS:   Credit  cHallenge  Transfer  Bypass  Pick  locK  QUIT", 0, 0);

        switch (getSelection()) {
            case 0:
                console.reverse(20, 0, 8);
                console.print("View results on record from all sources", 0, 1);
                break;
            case 1:
                console.reverse(28, 0, 11);
                console.print("View challenge exams taken by student", 0, 1);
                break;
            case 2:
                console.reverse(39, 0, 10);
                console.print("Add/View/Modify transfer evaluation information", 0, 1);
                break;
            case 3:
                console.reverse(49, 0, 8);
                console.print("Temporarily bypass the course prerequisite", 0, 1);
                break;
            case 4:
                console.reverse(57, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 5:
                console.reverse(63, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 6:
                console.reverse(69, 0, 6);
                console.print("Return to MAIN ADMIN menu", 0, 1);
                break;
        }

        if (isLocked()) {
            drawLocked();
        } else if (isPicking()) {
            drawPickBox();
        } else {
            drawStudentNameId();
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
        } else if (isAcceptingPick()) {
            if (processKeyPressInAcceptingPick(key)) {
                final int sel = getSelection();

                if (sel == 0) {
                    doCredit();
                } else if (sel == 1) {
                    doChallenge();
                } else if (sel == 2) {
                    doTransfer();
                } else if (sel == 3) {
                    doBypass();
                } else if (sel == 4) {
                    doPick();
                }
            }
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            incrementSelection();
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            decrementSelection();
        } else if (key == KeyEvent.VK_ENTER) {
            final int sel = getSelection();

            if (sel == 0) {
                doCredit();
            } else if (sel == 1) {
                doChallenge();
            } else if (sel == 2) {
                doTransfer();
            } else if (sel == 3) {
                doBypass();
            } else if (sel == 4) {
                doPick();
            } else if (sel == 5) {
                doLock();
            } else if (sel == 6) {
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
        } else if (isPicking()) {
            processKeyTypedInPick(character);
        } else if ((int) character == (int) CREDIT_CHAR) {
            setSelection(0);
            doCredit();
        } else if ((int) character == (int) CHALLENGE_CHAR) {
            setSelection(1);
            doChallenge();
        } else if ((int) character == (int) TRANSFER_CHAR) {
            setSelection(2);
            doTransfer();
        } else if ((int) character == (int) BYPASS_CHAR) {
            setSelection(3);
            doBypass();
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(4);
            doPick();
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(5);
            doLock();
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(6);
            doQuit();
        } else {
            repaint = false;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "Credit" item.
     */
    private void doCredit() {

    }

    /**
     * Handles the selection of the "Challenge" item.
     */
    private void doChallenge() {

    }

    /**
     * Handles the selection of the "Transfer" item.
     */
    private void doTransfer() {

    }

    /**
     * Handles the selection of the "Bypass" item.
     */
    private void doBypass() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
