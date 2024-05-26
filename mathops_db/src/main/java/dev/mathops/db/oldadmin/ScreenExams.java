package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The Exams screen.
 */
final class ScreenExams extends AbstractStudentScreen {

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Add". */
    private static final char ADD_CHAR = 'a';

    /** The character to select "Delete". */
    private static final char DELETE_CHAR = 'd';

    /** The character to select "Check Ans". */
    private static final char CHECK_ANS_CHAR = 'd';

    /** The character to select "Make-Upo. */
    private static final char MAKE_UP_CHAR = 'u';

    /** The character to select "Exams". */
    private static final char EXAMS_CHAR = 'e';

    /** The character to select "Homework". */
    private static final char HOMEWORK_CHAR = 'h';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenExams}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenExams(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("EXAMS:   Add  Delete  Check_ans  make-Up  Exams  Homework  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
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
                    doAdd();
                } else if (this.selection == 1) {
                    doDelete();
                } else if (this.selection == 2) {
                    doCheckAns();
                } else if (this.selection == 3) {
                    doMakeUp();
                } else if (this.selection == 4) {
                    doExams();
                } else if (this.selection == 5) {
                    doHomework();
                } else if (this.selection == 6) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 8) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 8;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {

            if (this.selection == 0) {
                doAdd();
                repaint = true;
            } else if (this.selection == 1) {
                doDelete();
                repaint = true;
            } else if (this.selection == 2) {
                doCheckAns();
                repaint = true;
            } else if (this.selection == 3) {
                doMakeUp();
                repaint = true;
            } else if (this.selection == 4) {
                doExams();
                repaint = true;
            } else if (this.selection == 5) {
                doHomework();
                repaint = true;
            } else if (this.selection == 6) {
                doPick();
                repaint = true;
            } else if (this.selection == 7) {
                doLock();
                repaint = true;
            } else if (this.selection == 8) {
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
        } else if ((int) character == (int) ADD_CHAR) {
            doAdd();
            repaint = true;
        } else if ((int) character == (int) DELETE_CHAR) {
            doDelete();
            repaint = true;
        } else if ((int) character == (int) CHECK_ANS_CHAR) {
            doCheckAns();
            repaint = true;
        } else if ((int) character == (int) MAKE_UP_CHAR) {
            doMakeUp();
            repaint = true;
        } else if ((int) character == (int) EXAMS_CHAR) {
            doExams();
            repaint = true;
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            doHomework();
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
