package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.sql.SQLException;

/**
 * The main screen.
 */
final class ScreenMain implements IScreen {

    /** The character to select "Pick". */
    private static final char PICK_CHAR = 'p';

    /** The character to select "Course". */
    private static final char COURSE_CHAR = 'c';

    /** The character to select "Schedule". */
    private static final char SCHEDULE_CHAR = 's';

    /** The character to select "Discipline". */
    private static final char DISCIPLINE_CHAR = 'd';

    /** The character to select "Holds". */
    private static final char HOLDS_CHAR = 'h';

    /** The character to select "Exams". */
    private static final char EXAMS_CHAR = 'e';

    /** The character to select "MPE". */
    private static final char MPE_CHAR = 'm';

    /** The character to select "Resource". */
    private static final char RESOURCE_CHAR = 'r';

    /** The character to select "Lock". */
    private static final char LOCK_CHAR = 'k';

    /** The character to select "Quit". */
    private static final char QUIT_CHAR = 'k';

    /** The cache. */
    private final Cache cache;

    /** The main window. */
    private final MainWindow mainWindow;

    /** The console. */
    private final Console console;

    /** The lock-screen password. */
    private final String lockPassword;

    /** The current student ID. */
    private Field studentIdField;

    /** The lock password being typed. */
    private Field lockPasswordField;

    /** The current selection (0 through 9). */
    private int selection;

    /** Flag indicating "Pick student" is being shown. */
    private boolean showingPick;

    /** Flag indicating "Press RETURN to select, or F5 to cancel" is being shown. */
    private boolean showingAccept;

    /** The current student record. */
    private RawStudent student = null;

    /** An error message. */
    private String errorMessage;

    /** Flag indicating lock screen is being shown. */
    private boolean showingLock;

    /**
     * Constructs a new {@code ScreenMain}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenMain(final Cache theCache, final MainWindow theMainWindow) {

        this.cache = theCache;
        this.mainWindow = theMainWindow;
        this.console = this.mainWindow.getConsole();

        this.lockPassword = this.mainWindow.getUserData().getClearPassword("LOCK");

        this.lockPasswordField = new Field(this.console, 21, 11, 8, true, null);
        this.studentIdField = new Field(this.console, 28, 11, 9, false, "0123456789");

        this.selection = 0;
        this.errorMessage = CoreConstants.EMPTY;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        this.console.clear();
        this.console.print("MAIN ADMIN:   Pick  Course  Schedule  Discipline  Holds  Exams  MPE  Resource  locK  " +
                "QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                this.console.reverse(13, 0, 6);
                this.console.print("Select a student", 0, 1);
                break;
            case 1:
                this.console.reverse(19, 0, 8);
                this.console.print("View registration history in PACe courses", 0, 1);
                break;
            case 2:
                this.console.reverse(27, 0, 10);
                this.console.print("View student testing and deadline history", 0, 1);
                break;
            case 3:
                this.console.reverse(37, 0, 12);
                this.console.print("View/add/update disciplinary incidents", 0, 1);
                break;
            case 4:
                this.console.reverse(49, 0, 7);
                this.console.print("View/Add/Delete administrative holds and participation info", 0, 1);
                break;
            case 5:
                this.console.reverse(56, 0, 7);
                this.console.print("Add/Modify/Delete exams, check answers, issue calculators & make-up exams", 0, 1);
                break;
            case 6:
                this.console.reverse(63, 0, 5);
                this.console.print("Verify MPE, ELM Exam & Intensive Review results, plus transfer and AP credit", 0,
                        1);
                break;
            case 7:
                this.console.reverse(68, 0, 10);
                this.console.print("Record loan of Resource items", 0, 1);
                break;
            case 8:
                this.console.reverse(78, 0, 6);
                this.console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 9:
                this.console.reverse(84, 0, 6);
                this.console.print("Exit this program and return to login", 0, 1);
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

            if (this.student != null) {
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
        } else if (this.student != null) {
            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                final String shortened = name.substring(0, 34);
                this.console.print(shortened, 0, 4);
            } else {
                this.console.print(name, 0, 4);
            }

            final String idMsg = SimpleBuilder.concat("Student ID: ", this.student.stuId);
            this.console.print(idMsg, 40, 4);
        }

        if (!this.errorMessage.isBlank()) {
            this.console.print(this.errorMessage, 1, 21);
            final int len = this.errorMessage.length();
            this.console.reverse(0, 21, len + 2);
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
                    this.errorMessage = CoreConstants.EMPTY;
                    this.console.setCursor(-1, -1);
                } else {
                    this.errorMessage = "Invalid password";
                }
            } else {
                this.lockPasswordField.processKey(key);
            }
            repaint = true;
        } else if (this.showingAccept) {
            if (key == KeyEvent.VK_ENTER) {
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.console.setCursor(-1, -1);
                repaint = true;
            } else if (key == KeyEvent.VK_F5) {
                this.student = null;
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage = CoreConstants.EMPTY;
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
                    this.errorMessage = CoreConstants.EMPTY;
                    repaint = true;
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.studentIdField.clear();
                    this.errorMessage = "ERROR:  Student not found.";
                    repaint = true;
                }
            } else if (key == KeyEvent.VK_C && (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                this.student = null;
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.console.setCursor(-1, -1);
                repaint = true;
            } else {
                this.studentIdField.processKey(key);
                repaint = true;
            }

        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            ++this.selection;
            if (this.selection > 9) {
                this.selection = 0;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            --this.selection;
            if (this.selection < 0) {
                this.selection = 9;
            }
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {
            if (this.selection == 0) {
                this.student = null;
                this.showingPick = true;
                this.showingAccept = false;
                this.errorMessage = CoreConstants.EMPTY;
                this.studentIdField.clear();
                this.studentIdField.activate();
                repaint = true;
            } else if (this.selection == 1) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Course page
            } else if (this.selection == 2) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Schedule page
            } else if (this.selection == 3) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Discipline page
            } else if (this.selection == 4) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Holds page
            } else if (this.selection == 5) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Exams page
            } else if (this.selection == 6) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do MPE page
            } else if (this.selection == 7) {
                // TODO: If no student picked, do "Pick" operation first
                // TODO: otherwise, do Resource page
            } else if (this.selection == 8) {
                if (this.lockPassword != null) {
                    this.showingLock = true;
                    this.lockPasswordField.clear();
                    this.lockPasswordField.activate();
                    repaint = true;
                }
            } else if (this.selection == 9) {
                this.mainWindow.quit();
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
            this.errorMessage = CoreConstants.EMPTY;
            this.studentIdField.clear();
            this.studentIdField.activate();
            repaint = true;
        } else if ((int) character == (int) COURSE_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Course page
        } else if ((int) character == (int) SCHEDULE_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Schedule page
        } else if ((int) character == (int) DISCIPLINE_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Discipline page
        } else if ((int) character == (int) HOLDS_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Holds page
        } else if ((int) character == (int) EXAMS_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Exams page
        } else if ((int) character == (int) MPE_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do MPE page
        } else if ((int) character == (int) RESOURCE_CHAR) {
            // TODO: If no student picked, do "Pick" operation first
            // TODO: otherwise, do Resource page
        } else if ((int) character == (int) LOCK_CHAR && this.lockPassword != null) {
            this.showingLock = true;
            this.lockPasswordField.clear();
            this.lockPasswordField.activate();
            repaint = true;
        } else if ((int) character == (int) QUIT_CHAR) {
            this.mainWindow.quit();
        }

        return repaint;
    }
}
