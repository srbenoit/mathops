package dev.mathops.db.oldadmin;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The Discipline screen.
 */
final class ScreenDiscipline extends AbstractStudentScreen {

    /** The character to select "Next". */
    private static final char NEXT_CHAR = 'n';

    /** The character to select "preVious". */
    private static final char PREVIOUS_CHAR = 'v';

    /** The character to select "Add". */
    private static final char ADD_CHAR = 'a';

    /** The character to select "Update". */
    private static final char UPDATE_CHAR = 'u';

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /**
     * Constructs a new {@code ScreenDiscipline}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenDiscipline(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 7);
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();
        console.clear();
        console.print("DISCIPLINE:   Next  preVious  Add  Update  Pick  locK  QUIT", 0, 0);

        switch (getSelection()) {
            case 0:
                console.reverse(13, 0, 6);
                console.print("View the next incident on record", 0, 1);
                break;
            case 1:
                console.reverse(19, 0, 10);
                console.print("View the previous incident on record", 0, 1);
                break;
            case 2:
                console.reverse(29, 0, 5);
                console.print("Document a new cheating incident", 0, 1);
                break;
            case 3:
                console.reverse(34, 0, 8);
                console.print("Update an existing incident on record", 0, 1);
                break;
            case 4:
                console.reverse(42, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 5:
                console.reverse(48, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 6:
                console.reverse(54, 0, 6);
                console.print("Return to MAIN ADMIN menu", 0, 1);
                break;
        }

        if (isLocked()) {
            drawLocked();
        } else if (isPicking()) {
            drawPickBox();
        } else {
            drawStudentNameId();
            final RawStudent stu = getStudent();

            if (Objects.nonNull(stu)) {
                console.print("*************************************************************************", 0, 6);

                console.print("   date of incident:                  interviewer:", 0, 8);
                console.print("             course:                      proctor:", 0, 9);
                console.print("               unit:", 0, 10);

                console.print("      incident code:", 0, 12);
                console.print(" description of", 0, 13);
                console.print("  cheating incident:", 0, 14);

                console.print("        action code:", 0, 16);
                console.print("additional comments:", 0, 17);

                console.print("*************************************************************************", 0, 20);

                // TODO: Populate the fields of the current incident
            }
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
        } else if (isAcceptingPick()) {
            if (processKeyPressInAcceptingPick(key)) {
                final int sel = getSelection();

                if (sel == 0) {
                    doNext();
                } else if (sel == 1) {
                    doPrevious();
                } else if (sel == 2) {
                    doAdd();
                } else if (sel == 3) {
                    doUpdate();
                } else if (sel == 4) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            incrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            incrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {
            final int sel = getSelection();

            if (sel == 0) {
                doNext();
                repaint = true;
            } else if (sel == 1) {
                doPrevious();
                repaint = true;
            } else if (sel == 2) {
                doAdd();
                repaint = true;
            } else if (sel == 3) {
                doUpdate();
                repaint = true;
            } else if (sel == 4) {
                doPick();
                repaint = true;
            } else if (sel == 5) {
                doLock();
                repaint = true;
            } else if (sel == 6) {
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
        } else if (isPicking()) {
            processKeyTypedInPick(character);
            repaint = true;
        } else if ((int) character == (int) NEXT_CHAR) {
            setSelection(0);
            doNext();
            repaint = true;
        } else if ((int) character == (int) PREVIOUS_CHAR) {
            setSelection(1);
            doPrevious();
            repaint = true;
        } else if ((int) character == (int) ADD_CHAR) {
            setSelection(2);
            doAdd();
            repaint = true;
        } else if ((int) character == (int) UPDATE_CHAR) {
            setSelection(3);
            doUpdate();
            repaint = true;
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(4);
            doPick();
            repaint = true;
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(5);
            doLock();
            repaint = true;
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(6);
            doQuit();
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "Next" item.
     */
    private void doNext() {

    }

    /**
     * Handles the selection of the "preVious" item.
     */
    private void doPrevious() {

    }

    /**
     * Handles the selection of the "Add" item.
     */
    private void doAdd() {

    }

    /**
     * Handles the selection of the "Update" item.
     */
    private void doUpdate() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
