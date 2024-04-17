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

    /** The cache. */
    private final Cache cache;

    /** The main window. */
    private final MainWindow mainWindow;

    /** The current selection (0 through 9). */
    private int selection;

    /** Flag indicating "Pick student" is being shown. */
    private boolean showingPick;

    /** Flag indicating "Press RETURN to select, or F5 to cancel" is being shown. */
    private boolean showingAccept;

    /** The current student ID. */
    private String studentId;

    /** The current student record. */
    private RawStudent student = null;

    /** An error message. */
    private String errorMessage;

    /** Flag indicating lock screen is being shown. */
    private boolean showingLock;

    /** The lock password being typed. */
    private String lockPassword;

    /**
     * Constructs a new {@code ScreenMain}.
     *
     * @param theCache the cache
     * @param theMainWindow the main window
     */
    ScreenMain(final Cache theCache, final MainWindow theMainWindow) {

        this.cache = theCache;
        this.mainWindow = theMainWindow;

        this.selection = 0;
        this.studentId = CoreConstants.EMPTY;
        this.errorMessage = CoreConstants.EMPTY;
    }

    /**
     * Draws the screen to a console.
     *
     * @param console the console
     */
    public void draw(final Console console) {

        console.clear();
        console.print("MAIN ADMIN:   Pick  Course  Schedule  Discipline  Holds  Exams  MPE  Resource  locK  QUIT", 0, 0);

        switch (this.selection) {
            case 0:
                console.reverse(13, 0, 6);
                console.print("Select a student", 0, 1);
                break;
            case 1:
                console.reverse(19, 0, 8);
                console.print("View registration history in PACe courses", 0, 1);
                break;
            case 2:
                console.reverse(27, 0, 10);
                console.print("View student testing and deadline history", 0, 1);
                break;
            case 3:
                console.reverse(37, 0, 12);
                console.print("View/add/update disciplinary incidents", 0, 1);
                break;
            case 4:
                console.reverse(49, 0, 7);
                console.print("View/Add/Delete administrative holds and participation info", 0, 1);
                break;
            case 5:
                console.reverse(56, 0, 7);
                console.print("Add/Modify/Delete exams, check answers, issue calculators & make-up exams", 0, 1);
                break;
            case 6:
                console.reverse(63, 0, 5);
                console.print("Verify MPE, ELM Exam & Intensive Review results, plus transfer and AP credit", 0, 1);
                break;
            case 7:
                console.reverse(68, 0, 10);
                console.print("Record loan of Resource items", 0, 1);
                break;
            case 8:
                console.reverse(78, 0, 6);
                console.print("Lock the terminal to restrict unauthorized use", 0, 1);
                break;
            case 9:
                console.reverse(84, 0, 6);
                console.print("Exit this program and return to login", 0, 1);
                break;
        }

        if (this.showingPick) {
            console.print("\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557", 10, 7);
            console.print("\u2551                                                    \u2551", 10, 8);
            console.print("\u2551          -----Student Identification-----          \u2551", 10, 9);
            console.print("\u2551                                                    \u2551", 10, 10);
            console.print("\u2551    Student ID:                                     \u2551", 10, 11);
            console.print("\u2551                                                    \u2551", 10, 12);
            console.print("\u2551    Name:                                           \u2551", 10, 13);
            console.print("\u2551                                                    \u2551", 10, 14);
            console.print("\u2551                                                    \u2551", 10, 15);
            console.print("\u2551                                                    \u2551", 10, 16);
            console.print("\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550"
                    + "\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D", 10, 17);

            console.print(this.studentId, 28, 11);
            if (this.student != null) {
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
        } else if (this.student != null) {
            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                final String shortened = name.substring(0, 34);
                console.print(shortened, 0, 4);
            } else {
                console.print(name, 0, 4);
            }

            final String idMsg = SimpleBuilder.concat("Student ID: ", this.student.stuId);
            console.print(idMsg, 40, 4);
        }

        if (!this.errorMessage.isBlank()) {
            console.print(this.errorMessage, 1, 21);
            final int len = this.errorMessage.length();
            console.reverse(0, 21, len + 2);
        }

        console.commit();
    }

    /**
     * Processes a key pressed.
     *
     * @param key the key code
     * @return true if the screen should be repainted after this event
     */
    public boolean processKeyPressed(final int key) {

        boolean repaint = false;

        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
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
            if (this.showingAccept) {
                this.showingPick = false;
                this.showingAccept = false;
                this.errorMessage = CoreConstants.EMPTY;
                repaint = true;
            } else if (this.showingPick) {
                try {
                    this.student = RawStudentLogic.query(this.cache, this.studentId, false);
                    this.showingAccept = true;
                    this.errorMessage = CoreConstants.EMPTY;
                    repaint = true;
                } catch (final SQLException ex) {
                    Log.warning(ex);
                    this.studentId = CoreConstants.EMPTY;
                    this.errorMessage = "ERROR:  Student not found.";
                    repaint = true;
                }
            } else {
                if (this.selection == 9) {
                    this.mainWindow.quit();
                } else if (this.selection == 0) {
                    this.student = null;
                    this.showingPick = true;
                    this.showingAccept = false;
                    this.errorMessage = CoreConstants.EMPTY;
                    repaint = true;
                }
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
            final int lockLen = this.lockPassword.length();

            if (lockLen < 8 && character >= '0' && character <= '9') {
                this.lockPassword = this.lockPassword + character;
                repaint = true;
            }
        } else if (this.showingPick && !this.showingAccept) {
            final int stuIdLen = this.studentId.length();

            if (stuIdLen < 9 && character >= '0' && character <= '9') {
                this.studentId = this.studentId + character;
                repaint = true;
            }
        }

        return repaint;
    }
}
