package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

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

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenMPE}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenMPE(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("PLACEMENT OPTIONS:   Credit  cHallenge  Transfer  Bypass  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
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
            final RawStudent stu = getStudent();

            if (Objects.nonNull(stu)) {
                final String name = SimpleBuilder.concat(stu.lastName, ", ", stu.firstName);
                if (name.length() > 34) {
                    final String shortened = name.substring(0, 34);
                    console.print(shortened, 0, 4);
                } else {
                    console.print(name, 0, 4);
                }

                final String idMsg = SimpleBuilder.concat("Student ID: ", stu.stuId);
                console.print(idMsg, 41, 4);
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
                if (this.selection == 0) {
                    doCredit();
                } else if (this.selection == 1) {
                    doChallenge();
                } else if (this.selection == 2) {
                    doTransfer();
                } else if (this.selection == 3) {
                    doBypass();
                } else if (this.selection == 4) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 6) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 6;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {

            if (this.selection == 0) {
                doCredit();
                repaint = true;
            } else if (this.selection == 1) {
                doChallenge();
                repaint = true;
            } else if (this.selection == 2) {
                doTransfer();
                repaint = true;
            } else if (this.selection == 3) {
                doBypass();
                repaint = true;
            } else if (this.selection == 4) {
                doPick();
                repaint = true;
            } else if (this.selection == 5) {
                doLock();
                repaint = true;
            } else if (this.selection == 6) {
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
        } else if ((int) character == (int) PICK_CHAR) {
            doPick();
            repaint = true;
        } else if ((int) character == (int) CREDIT_CHAR) {
            doCredit();
            repaint = true;
        } else if ((int) character == (int) CHALLENGE_CHAR) {
            doChallenge();
            repaint = true;
        } else if ((int) character == (int) TRANSFER_CHAR) {
            doTransfer();
            repaint = true;
        } else if ((int) character == (int) BYPASS_CHAR) {
            doBypass();
            repaint = true;
        } else if ((int) character == (int) PICK_CHAR) {
            doPick();
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
