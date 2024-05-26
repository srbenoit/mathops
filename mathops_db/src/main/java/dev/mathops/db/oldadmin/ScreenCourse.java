package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Objects;

/**
 * The Course screen.
 */
final class ScreenCourse extends AbstractScreen {

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

    /** Flag indicating lock screen is being shown. */
    private boolean showingLock = false;

    /**
     * Constructs a new {@code ScreenCourse}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenCourse(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.lockPassword = theMainWindow.getUserData().getClearPassword("LOCK");

        final Console console = getConsole();
        this.lockPasswordField = new Field(console, 21, 11, 8, true, null);
        this.studentIdField = new Field(console, 28, 11, 9, false, "0123456789");

        this.selection = 0;
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

        if (this.showingLock) {
            drawBox(18, 8, 39, 6);
            console.print("Enter your ADMIN screen password:", 21, 10);
            this.lockPasswordField.draw();
        } else if (this.showingPick) {
            drawBox(10, 7, 54, 11);
            console.print("-----Student Identification-----", 21, 9);
            console.print("Student ID:", 15, 11);
            console.print("Name:", 15, 13);
            this.studentIdField.draw();

            if (Objects.nonNull(this.student)) {
                final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
                if (name.length() > 34) {
                    final String shortened = name.substring(0, 34);
                    console.print(shortened, 28, 13);
                } else {
                    console.print(name, 28, 13);
                }
            }

            if (this.showingAccept) {
                console.print("Press RETURN to select or F5 to cancel...", 15, 16);
            }
        } else if (Objects.nonNull(this.student)) {
            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                final String shortened = name.substring(0, 34);
                console.print(shortened, 0, 4);
            } else {
                console.print(name, 0, 4);
            }

            final String idMsg = SimpleBuilder.concat("Student ID: ", this.student.stuId);
            console.print(idMsg, 41, 4);
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
        final Console console = getConsole();

        if (this.showingLock) {
            if (key == KeyEvent.VK_ENTER) {
                final String entered = this.lockPasswordField.getValue();
                if (entered.equals(this.lockPassword)) {
                    this.showingLock = false;
                    clearErrors();
                    console.setCursor(-1, -1);
                } else {
                    setError("Invalid password");
                }
            } else {
                clearErrors();
                this.lockPasswordField.processKey(key);
            }
            repaint = true;
        } else if (this.showingAccept) {
            if (key == KeyEvent.VK_ENTER) {
                this.showingPick = false;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                console.setCursor(-1, -1);

                if (this.selection == 0) {
                    doHistory();
                } else if (this.selection == 1) {
                    doCurrent();
                } else if (this.selection == 2) {
                    doHomework();
                } else if (this.selection == 3) {
                    doPick();
                }

                repaint = true;
            } else if (key == KeyEvent.VK_F5) {
                this.student = null;
                this.showingPick = false;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                console.setCursor(-1, -1);
                repaint = true;
            }
        } else if (this.showingPick) {
            if (key == KeyEvent.VK_ENTER) {
                final String entered = this.studentIdField.getValue();
                try {
                    this.student = RawStudentLogic.query(getCache(), entered, false);
                    this.showingAccept = true;
                    clearErrors();
                    repaint = true;
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.studentIdField.clear();
                    setError("ERROR:  Student not found.");
                    repaint = true;
                }
            } else if (key == KeyEvent.VK_C && (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
                this.student = null;
                this.showingPick = false;
                clearErrors();
                this.studentIdField.clear();
                console.setCursor(-1, -1);
                repaint = true;
            } else {
                this.studentIdField.processKey(key);
                repaint = true;
            }

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
                this.student = null;
                this.showingPick = true;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
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
            clearErrors();
            this.studentIdField.clear();
            this.studentIdField.activate();
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
