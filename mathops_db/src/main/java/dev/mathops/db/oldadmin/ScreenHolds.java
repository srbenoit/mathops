package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Objects;

/**
 * The Holds screen.
 */
final class ScreenHolds implements IScreen {

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

    /** The cache. */
    private final Cache cache;

    /** The main window. */
    private final MainWindow mainWindow;

    /** The console. */
    private final Console console;

    /** The lock-screen password. */
    private final String lockPassword;

    /** The current student ID. */
    private final Field studentIdField;

    /** The lock password being typed. */
    private final Field lockPasswordField;

    /** The current selection (0 through 9). */
    private int selection;

    /** Flag indicating "Pick student" is being shown. */
    private boolean showingPick = false;

    /** Flag indicating "Press RETURN to select, or F5 to cancel" is being shown. */
    private boolean showingAccept = false;

    /** The current student record. */
    private RawStudent student = null;

    /** An error message. */
    private String errorMessage1;

    /** An error message line 2. */
    private String errorMessage2;

    /** Flag indicating lock screen is being shown. */
    private boolean showingLock = false;

    /**
     * Constructs a new {@code ScreenHolds}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenHolds(final Cache theCache, final MainWindow theMainWindow) {

        this.cache = theCache;
        this.mainWindow = theMainWindow;
        this.console = this.mainWindow.getConsole();

        this.lockPassword = this.mainWindow.getUserData().getClearPassword("LOCK");

        this.lockPasswordField = new Field(this.console, 21, 11, 8, true, null);
        this.studentIdField = new Field(this.console, 28, 11, 9, false, "0123456789");

        this.selection = 0;
        this.errorMessage1 = CoreConstants.EMPTY;
        this.errorMessage2 = CoreConstants.EMPTY;
    }

    /**
     * Sets the student.
     *
     * @param theStudent the student
     */
    public void setStudent(final RawStudent theStudent) {

        this.student = theStudent;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        this.console.clear();
        this.console.print("HOLDS:   Delete  Add  Registration  Order  Screen  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                this.console.reverse(8, 0, 8);
                this.console.print("Select and delete administrative holds", 0, 1);
                break;
            case 1:
                this.console.reverse(16, 0, 5);
                this.console.print("Add administrative holds", 0, 1);
                break;
            case 2:
                this.console.reverse(21, 0, 14);
                this.console.print("View student's registration", 0, 1);
                break;
            case 3:
                this.console.reverse(35, 0, 7);
                this.console.print("Change the order courses should be completed for calculus", 0, 1);
                break;
            case 4:
                this.console.reverse(42, 0, 8);
                this.console.print("View additional administrative holds", 0, 1);
                break;
            case 5:
                this.console.reverse(50, 0, 6);
                this.console.print("Select a different student", 0, 1);
                break;
            case 6:
                this.console.reverse(56, 0, 6);
                this.console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 7:
                this.console.reverse(62, 0, 6);
                this.console.print("Return to MAIN ADMIN menu", 0, 1);
                break;
        }

        if (this.showingLock) {
            this.console.print("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557", 18, 8);
            this.console.print("\u2551                                     \u2551", 18, 9);
            this.console.print("\u2551  Enter your ADMIN screen password:  \u2551", 18, 10);
            this.console.print("\u2551                                     \u2551", 18, 11);
            this.console.print("\u2551                                     \u2551", 18, 12);
            this.console.print("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D", 18, 13);

            this.lockPasswordField.draw();
        } else if (this.showingPick) {
            this.console.print("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557", 10, 7);
            this.console.print("\u2551                                                    \u2551", 10, 8);
            this.console.print("\u2551          -----Student Identification-----          \u2551", 10, 9);
            this.console.print("\u2551                                                    \u2551", 10, 10);
            this.console.print("\u2551    Student ID:                                     \u2551", 10, 11);
            this.console.print("\u2551                                                    \u2551", 10, 12);
            this.console.print("\u2551    Name:                                           \u2551", 10, 13);
            this.console.print("\u2551                                                    \u2551", 10, 14);
            this.console.print("\u2551                                                    \u2551", 10, 15);
            this.console.print("\u2551                                                    \u2551", 10, 16);
            this.console.print("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D", 10, 17);

            this.studentIdField.draw();

            if (Objects.nonNull(this.student)) {
                final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
                if (name.length() > 34) {
                    final String shortened = name.substring(0, 34);
                    this.console.print(shortened, 28, 13);
                } else {
                    this.console.print(name, 28, 13);
                }
            }

            if (this.showingAccept) {
                this.console.print("Press RETURN to select or F5 to cancel...", 15, 16);
            }
        } else if (Objects.nonNull(this.student)) {

            this.console.print("NAME:", 4, 3);
            this.console.print("ID:", 6, 4);
            this.console.print("Format:", 2, 5);

            this.console.print("Max # of Courses Allowed:", 40, 3);
            this.console.print("Order Enforced for Calculus:", 37, 4);
            this.console.print("Passed User's Exam:", 46, 5);

            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                final String shortened = name.substring(0, 34);
                this.console.print(shortened, 10, 3);
            } else {
                this.console.print(name, 10, 3);
            }

            this.console.print(this.student.stuId, 10, 4);

            // TODO: Print the format at (10, 5);
            // TODO: Print the max courses allowed at (66, 3);
            // TODO: Print the order enforced flag at (66, 4);
            // TODO: Print the passed user's exam at (66, 5);

            this.console.print("Administrative Holds", 24, 7);

            // TODO: Following applies if there are no holds - different display if there are...
            this.console.print("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + " \u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557", 4,
                    8);
            this.console.print("\u2551                                                         \u2551", 4, 9);
            this.console.print("\u2551                                                         \u2551", 4, 10);
            this.console.print("\u2551                                                         \u2551", 4, 11);
            this.console.print("\u2551                                                         \u2551", 4, 12);
            this.console.print("\u2551             No Administrative Holds on record           \u2551", 4, 13);
            this.console.print("\u2551                                                         \u2551", 4, 14);
            this.console.print("\u2551                                                         \u2551", 4, 15);
            this.console.print("\u2551                                                         \u2551", 4, 16);
            this.console.print("\u2551                                                         \u2551", 4, 17);
            this.console.print("\u2551                                                         \u2551", 4, 18);
            this.console.print("\u2551                                                         \u2551", 4, 19);
            this.console.print("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D", 4,
                    20);
        }

        if (!this.errorMessage1.isBlank()) {
            this.console.print(this.errorMessage1, 1, 21);
            final int len = this.errorMessage1.length();
            this.console.reverse(0, 21, len + 2);
        }
        if (!this.errorMessage2.isBlank()) {
            this.console.print(this.errorMessage2, 1, 22);
            final int len = this.errorMessage2.length();
            this.console.reverse(0, 22, len + 2);
        }

        this.console.commit();
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

        if (this.showingLock) {
            if (key == KeyEvent.VK_ENTER) {
                final String entered = this.lockPasswordField.getValue();
                if (entered.equals(this.lockPassword)) {
                    this.showingLock = false;
                    this.errorMessage1 = CoreConstants.EMPTY;
                    this.console.setCursor(-1, -1);
                } else {
                    this.errorMessage1 = "Invalid password";
                }
                this.errorMessage2 = CoreConstants.EMPTY;
            } else {
                this.errorMessage2 = CoreConstants.EMPTY;
                this.lockPasswordField.processKey(key);
            }
            repaint = true;
        } else if (this.showingAccept) {
            if (key == KeyEvent.VK_ENTER) {
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage1 = CoreConstants.EMPTY;
                this.errorMessage2 = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.console.setCursor(-1, -1);

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

                repaint = true;
            } else if (key == KeyEvent.VK_F5) {
                this.student = null;
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage1 = CoreConstants.EMPTY;
                this.errorMessage2 = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.console.setCursor(-1, -1);
                repaint = true;
            }
        } else if (this.showingPick) {
            if (key == KeyEvent.VK_ENTER) {
                final String entered = this.studentIdField.getValue();
                try {
                    this.student = RawStudentLogic.query(this.cache, entered, false);
                    this.showingAccept = true;
                    this.errorMessage1 = CoreConstants.EMPTY;
                    this.errorMessage2 = CoreConstants.EMPTY;
                    repaint = true;
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.studentIdField.clear();
                    this.errorMessage1 = "ERROR:  Student not found.";
                    this.errorMessage2 = CoreConstants.EMPTY;
                    repaint = true;
                }
            } else if (key == KeyEvent.VK_C && (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                this.student = null;
                this.showingPick = false;
                this.errorMessage1 = CoreConstants.EMPTY;
                this.errorMessage2 = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.console.setCursor(-1, -1);
                repaint = true;
            } else {
                this.studentIdField.processKey(key);
                repaint = true;
            }

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
                this.student = null;
                this.showingPick = true;
                this.errorMessage1 = CoreConstants.EMPTY;
                this.errorMessage2 = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.studentIdField.activate();
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

        if (this.showingLock) {
            this.lockPasswordField.processChar(character);
            repaint = true;
        } else if (this.showingPick && !this.showingAccept) {
            this.studentIdField.processChar(character);
            repaint = true;
        } else if ((int) character == (int) PICK_CHAR) {
            this.student = null;
            this.showingPick = true;
            this.showingAccept = false;
            this.errorMessage1 = CoreConstants.EMPTY;
            this.errorMessage2 = CoreConstants.EMPTY;
            this.studentIdField.clear();
            this.studentIdField.activate();
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
     * Handles the selection of the "Pick" item.
     */
    private void doPick() {

    }

    /**
     * Handles the selection of the "Lock" item.
     */
    private void doLock() {

        if (this.lockPassword != null) {
            this.showingLock = true;
            this.lockPasswordField.clear();
            this.lockPasswordField.activate();
        }
    }

    /**
     * Handles the selection of the "Quit" item.
     */
    private void doQuit() {

    }
}
