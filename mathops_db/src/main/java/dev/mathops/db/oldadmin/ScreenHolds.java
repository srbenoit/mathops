package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The Holds screen.
 */
final class ScreenHolds extends AbstractStudentScreen {

    /** The character to select "Delete". */
    private static final char DELETE_CHAR = 'd';

    /** The character to select "Add". */
    private static final char ADD_CHAR = 'a';

    /** The character to select "Registration". */
    private static final char REGISTRATION_CHAR = 'r';

    /** The character to select "Order". */
    private static final char ORDER_CHAR = 'o';

    /** The character to select "Screen". */
    private static final char SCREEN_CHAR = 's';

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'q';

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenHolds}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenHolds(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("HOLDS:   Delete  Add  Registration  Order  Screen  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                console.reverse(8, 0, 8);
                console.print("Select and delete administrative holds", 0, 1);
                break;
            case 1:
                console.reverse(16, 0, 5);
                console.print("Add administrative holds", 0, 1);
                break;
            case 2:
                console.reverse(21, 0, 14);
                console.print("View student's registration", 0, 1);
                break;
            case 3:
                console.reverse(35, 0, 7);
                console.print("Change the order courses should be completed for calculus", 0, 1);
                break;
            case 4:
                console.reverse(42, 0, 8);
                console.print("View additional administrative holds", 0, 1);
                break;
            case 5:
                console.reverse(50, 0, 6);
                console.print("Select a different student", 0, 1);
                break;
            case 6:
                console.reverse(56, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 7:
                console.reverse(62, 0, 6);
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
                console.print("Format:", 2, 5);

                console.print("Max # of Courses Allowed:", 40, 3);
                console.print("Order Enforced for Calculus:", 37, 4);
                console.print("Passed User's Exam:", 46, 5);

                final String name = SimpleBuilder.concat(stu.lastName, ", ", stu.firstName);
                if (name.length() > 34) {
                    final String shortened = name.substring(0, 34);
                    console.print(shortened, 10, 3);
                } else {
                    console.print(name, 10, 3);
                }

                console.print(stu.stuId, 10, 4);

                // TODO: Print the format at (10, 5);
                // TODO: Print the max courses allowed at (66, 3);
                // TODO: Print the order enforced flag at (66, 4);
                // TODO: Print the passed user's exam at (66, 5);

                console.print("Administrative Holds", 24, 7);

                // TODO: Following applies if there are no holds - different display if there are...
                drawBox(4, 8, 59, 13);
                console.print("No Administrative Holds on record", 18, 13);
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
                    doDelete();
                } else if (this.selection == 1) {
                    doAdd();
                } else if (this.selection == 2) {
                    doRegistration();
                } else if (this.selection == 3) {
                    doOrder();
                } else if (this.selection == 4) {
                    doScreen();
                } else if (this.selection == 5) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 7) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 7;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {

            if (this.selection == 0) {
                doDelete();
                repaint = true;
            } else if (this.selection == 1) {
                doAdd();
                repaint = true;
            } else if (this.selection == 2) {
                doRegistration();
                repaint = true;
            } else if (this.selection == 3) {
                doOrder();
                repaint = true;
            } else if (this.selection == 4) {
                doScreen();
                repaint = true;
            } else if (this.selection == 5) {
                doPick();
                repaint = true;
            } else if (this.selection == 6) {
                doLock();
                repaint = true;
            } else if (this.selection == 7) {
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
        } else if ((int) character == (int) DELETE_CHAR) {
            doDelete();
            repaint = true;
        } else if ((int) character == (int) ADD_CHAR) {
            doAdd();
            repaint = true;
        } else if ((int) character == (int) REGISTRATION_CHAR) {
            doRegistration();
            repaint = true;
        } else if ((int) character == (int) ORDER_CHAR) {
            doOrder();
            repaint = true;
        } else if ((int) character == (int) SCREEN_CHAR) {
            doScreen();
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
     * Handles the selection of the "Delete" item.
     */
    private void doDelete() {

    }

    /**
     * Handles the selection of the "Add" item.
     */
    private void doAdd() {

    }

    /**
     * Handles the selection of the "Registration" item.
     */
    private void doRegistration() {

    }

    /**
     * Handles the selection of the "Order" item.
     */
    private void doOrder() {

    }

    /**
     * Handles the selection of the "Screen" item.
     */
    private void doScreen() {

    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

        getMainWindow().goToMain();
    }
}
