package dev.mathops.db.oldadmin;

import dev.mathops.db.old.Cache;

import java.awt.event.KeyEvent;

/**
 * The Exams screen.
 */
final class ScreenExams extends AbstractStudentScreen {

    /** The character to select "Add". */
    private static final char ADD_CHAR = 'a';

    /** The character to select "Delete". */
    private static final char DELETE_CHAR = 'd';

    /** The character to select "Check Ans". */
    private static final char CHECK_ANS_CHAR = 'c';

    /** The character to select "Make-Upo. */
    private static final char MAKE_UP_CHAR = 'u';

    /** The character to select "Exams". */
    private static final char EXAMS_CHAR = 'e';

    /** The character to select "Homework". */
    private static final char HOMEWORK_CHAR = 'h';

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /**
     * Constructs a new {@code ScreenExams}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenExams(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 9);
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("EXAMS:   Add  Delete  Check_ans  make-Up  Exams  Homework  Pick  locK  QUIT", 0, 0);

        switch (getSelection()) {
            case 0:
                console.reverse(8, 0, 5);
                console.print("Issue an ONLINE exam", 0, 1);
                break;
            case 1:
                console.reverse(13, 0, 8);
                console.print("Delete an ONLINE exam", 0, 1);
                break;
            case 2:
                console.reverse(21, 0, 11);
                console.print("Check answers recorded for a specific UNIT exam attempt", 0, 1);
                break;
            case 3:
                console.reverse(32, 0, 9);
                console.print("Issue a calculator OR a make-up exam", 0, 1);
                break;
            case 4:
                console.reverse(41, 0, 7);
                console.print("View complete REVIEW & UNIT exam record currently on file", 0, 1);
                break;
            case 5:
                console.reverse(48, 0, 10);
                console.print("View complete online homework record currently on file", 0, 1);
                break;
            case 6:
                console.reverse(58, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 7:
                console.reverse(64, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 8:
                console.reverse(70, 0, 6);
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
                    doAdd();
                } else if (sel == 1) {
                    doDelete();
                } else if (sel == 2) {
                    doCheckAns();
                } else if (sel == 3) {
                    doMakeUp();
                } else if (sel == 4) {
                    doExams();
                } else if (sel == 5) {
                    doHomework();
                } else if (sel == 6) {
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
                doAdd();
            } else if (sel == 1) {
                doDelete();
            } else if (sel == 2) {
                doCheckAns();
            } else if (sel == 3) {
                doMakeUp();
            } else if (sel == 4) {
                doExams();
            } else if (sel == 5) {
                doHomework();
            } else if (sel == 6) {
                doPick();
            } else if (sel == 7) {
                doLock();
            } else if (sel == 8) {
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
        } else if ((int) character == (int) ADD_CHAR) {
            setSelection(0);
            doAdd();
        } else if ((int) character == (int) DELETE_CHAR) {
            setSelection(1);
            doDelete();
        } else if ((int) character == (int) CHECK_ANS_CHAR) {
            setSelection(2);
            doCheckAns();
        } else if ((int) character == (int) MAKE_UP_CHAR) {
            setSelection(3);
            doMakeUp();
        } else if ((int) character == (int) EXAMS_CHAR) {
            setSelection(4);
            doExams();
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            setSelection(5);
            doHomework();
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(6);
            doPick();
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(7);
            doLock();
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(8);
            doQuit();
        } else {
            repaint = false;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "Add" item.
     */
    private void doAdd() {

    }

    /**
     * Handles the selection of the "Delete" item.
     */
    private void doDelete() {

    }

    /**
     * Handles the selection of the "Check Ans" item.
     */
    private void doCheckAns() {

    }

    /**
     * Handles the selection of the "Make-Up" item.
     */
    private void doMakeUp() {

    }

    /**
     * Handles the selection of the "Exams" item.
     */
    private void doExams() {

    }

    /**
     * Handles the selection of the "Homework" item.
     */
    private void doHomework() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
