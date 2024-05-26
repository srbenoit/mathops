package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The Course screen.
 */
final class ScreenCourse extends AbstractStudentScreen {

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Course". */
    private static final char HISTORY_CHAR = 'h';

    /** The character to select "Schedule". */
    private static final char CURRENT_CHAR = 'c';

    /** The character to select "Discipline". */
    private static final char HOMEWORK_CHAR = 'w';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenCourse}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenCourse(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("COURSE OPTIONS:   History  Current  homework_rpt  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                console.reverse(17, 0, 9);
                console.print("View registration history in PACe courses", 0, 1);
                break;
            case 1:
                console.reverse(26, 0, 9);
                console.print("View current PACe course registrations", 0, 1);
                break;
            case 2:
                console.reverse(35, 0, 14);
                console.print("View homework record for student in selected course/section", 0, 1);
                break;
            case 3:
                console.reverse(49, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 4:
                console.reverse(55, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 5:
                console.reverse(61, 0, 6);
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
                    doHistory();
                } else if (this.selection == 1) {
                    doCurrent();
                } else if (this.selection == 2) {
                    doHomework();
                } else if (this.selection == 3) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 5) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 5;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {

            if (this.selection == 0) {
                doHistory();
                repaint = true;
            } else if (this.selection == 1) {
                doCurrent();
                repaint = true;
            } else if (this.selection == 2) {
                doHomework();
                repaint = true;
            } else if (this.selection == 3) {
                doPick();
                repaint = true;
            } else if (this.selection == 4) {
                doLock();
                repaint = true;
            } else if (this.selection == 5) {
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
        } else if ((int) character == (int) HISTORY_CHAR) {
            doHistory();
            repaint = true;
        } else if ((int) character == (int) CURRENT_CHAR) {
            doCurrent();
            repaint = true;
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            doHomework();
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
     * Handles the selection of the "History" item.
     */
    private void doHistory() {

    }

    /**
     * Handles the selection of the "Current" item.
     */
    private void doCurrent() {

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
