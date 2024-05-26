package dev.mathops.db.oldadmin;

import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The Schedule screen.
 */
final class ScreenSchedule extends AbstractStudentScreen {

    /** The character to select "Registration". */
    private static final char REGISTRATION_CHAR = 'r';

    /** The character to select "Deadlines". */
    private static final char DEADLINES_CHAR = 'd';

    /** The character to select "Weekly". */
    private static final char WEEKLY_CHAR = 'w';

    /** The character to select "Appeal". */
    private static final char APPEAL_CHAR = 'a';

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenSchedule}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenSchedule(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("SCHEDULE:   Registration  Deadlines  Weekly  Appeal  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                console.reverse(11, 0, 14);
                console.print("View student's registration", 0, 1);
                break;
            case 1:
                console.reverse(25, 0, 11);
                console.print("View deadline schedule and progress", 0, 1);
                break;
            case 2:
                console.reverse(36, 0, 8);
                console.print("View student's work completed grouped by week", 0, 1);
                break;
            case 3:
                console.reverse(44, 0, 8);
                console.print("View/add/update appeals of required deadlines", 0, 1);
                break;
            case 4:
                console.reverse(52, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 5:
                console.reverse(58, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 6:
                console.reverse(64, 0, 6);
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
                console.print("NAME:", 4, 3);
                console.print("ID:", 6, 4);

                console.print("Format:", 49, 3);
                console.print("Passed User's Exam:", 37, 4);
                console.print("Earned TC Bonus:", 40, 5);

                final String name = getClippedStudentName();
                console.print(name, 10, 3);
                console.print(stu.stuId, 10, 4);

                // TODO: Print the "Format", "Passed User's Exam", and "Earned TC Bonus" fields
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
                    doRegistration();
                } else if (this.selection == 1) {
                    doDeadlines();
                } else if (this.selection == 2) {
                    doWeekly();
                } else if (this.selection == 3) {
                    doAppeal();
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
                doRegistration();
                repaint = true;
            } else if (this.selection == 1) {
                doDeadlines();
                repaint = true;
            } else if (this.selection == 2) {
                doWeekly();
                repaint = true;
            } else if (this.selection == 3) {
                doAppeal();
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
        } else if ((int) character == (int) REGISTRATION_CHAR) {
            doRegistration();
            repaint = true;
        } else if ((int) character == (int) DEADLINES_CHAR) {
            doDeadlines();
            repaint = true;
        } else if ((int) character == (int) WEEKLY_CHAR) {
            doWeekly();
            repaint = true;
        } else if ((int) character == (int) APPEAL_CHAR) {
            doAppeal();
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
     * Handles the selection of the "Registration" item.
     */
    private void doRegistration() {

    }

    /**
     * Handles the selection of the "Deadlines" item.
     */
    private void doDeadlines() {

    }

    /**
     * Handles the selection of the "Weekly" item.
     */
    private void doWeekly() {

    }

    /**
     * Handles the selection of the "Appeal" item.
     */
    private void doAppeal() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
