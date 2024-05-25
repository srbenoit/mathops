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
 * The Exams screen.
 */
final class ScreenExams implements IScreen {

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
     * Constructs a new {@code ScreenExams}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenExams(final Cache theCache, final MainWindow theMainWindow) {

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
        this.console.print("EXAMS:   Add  Delete  Check_ans  make-Up  Exams  Homework  Pick  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                this.console.reverse(8, 0, 5);
                this.console.print("Issue an ONLINE exam", 0, 1);
                break;
            case 1:
                this.console.reverse(13, 0, 8);
                this.console.print("Delete an ONLINE exam", 0, 1);
                break;
            case 2:
                this.console.reverse(21, 0, 11);
                this.console.print("Check answers recorded for a specific UNIT exam attempt", 0, 1);
                break;
            case 3:
                this.console.reverse(32, 0, 9);
                this.console.print("Issue a calculator OR a make-up exam", 0, 1);
                break;
            case 4:
                this.console.reverse(41, 0, 7);
                this.console.print("View complete REVIEW & UNIT exam record currently on file", 0, 1);
                break;
            case 5:
                this.console.reverse(48, 0, 10);
                this.console.print("View complete online homework record currently on file", 0, 1);
                break;
            case 6:
                this.console.reverse(58, 0, 6);
                this.console.print("Select a different student", 0, 1);
                break;
            case 7:
                this.console.reverse(64, 0, 6);
                this.console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 8:
                this.console.reverse(70, 0, 6);
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
            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                final String shortened = name.substring(0, 34);
                this.console.print(shortened, 0, 4);
            } else {
                this.console.print(name, 0, 4);
            }

            final String idMsg = SimpleBuilder.concat("Student ID: ", this.student.stuId);
            this.console.print(idMsg, 41, 4);
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
                    ;
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
                this.student = null;
                this.showingPick = true;
                this.errorMessage1 = CoreConstants.EMPTY;
                this.errorMessage2 = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.studentIdField.activate();
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
