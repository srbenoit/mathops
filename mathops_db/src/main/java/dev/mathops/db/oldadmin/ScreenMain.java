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
 * The main screen.
 */
final class ScreenMain extends AbstractScreen {

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
     * Constructs a new {@code ScreenMain}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenMain(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

        this.lockPassword = theMainWindow.getUserData().getClearPassword("LOCK");

        final Console console = getConsole();
        this.lockPasswordField = new Field(console, 21, 11, 8, true, null);
        this.studentIdField = new Field(console, 28, 11, 9, false, "0123456789");

        this.selection = 0;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("MAIN ADMIN:   Pick  Course  Schedule  Discipline  Holds  Exams  MPE  Resource  locK  QUIT", 0,
                0);

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
            console.print(idMsg, 40, 4);
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

                if (this.selection == 1) {
                    doCourse();
                } else if (this.selection == 2) {
                    doSchedule();
                } else if (this.selection == 3) {
                    doDiscipline();
                } else if (this.selection == 4) {
                    doHolds();
                } else if (this.selection == 5) {
                    doExams();
                } else if (this.selection == 6) {
                    doMpe();
                } else if (this.selection == 7) {
                    doResource();
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
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
                repaint = true;
            } else if (this.selection == 1) {
                doCourse();
                repaint = true;
            } else if (this.selection == 2) {
                doSchedule();
                repaint = true;
            } else if (this.selection == 3) {
                doDiscipline();
                repaint = true;
            } else if (this.selection == 4) {
                doHolds();
                repaint = true;
            } else if (this.selection == 5) {
                doExams();
                repaint = true;
            } else if (this.selection == 6) {
                doMpe();
                repaint = true;
            } else if (this.selection == 7) {
                doResource();
                repaint = true;
            } else if (this.selection == 8) {
                doLock();
                repaint = true;
            } else if (this.selection == 9) {
                getMainWindow().quit();
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
        } else if ((int) character == (int) COURSE_CHAR) {
            this.selection = 1;
            doCourse();
            repaint = true;
        } else if ((int) character == (int) SCHEDULE_CHAR) {
            this.selection = 2;
            doSchedule();
            repaint = true;
        } else if ((int) character == (int) DISCIPLINE_CHAR) {
            this.selection = 3;
            doDiscipline();
            repaint = true;
        } else if ((int) character == (int) HOLDS_CHAR) {
            this.selection = 4;
            doHolds();
            repaint = true;
        } else if ((int) character == (int) EXAMS_CHAR) {
            this.selection = 5;
            doExams();
            repaint = true;
        } else if ((int) character == (int) MPE_CHAR) {
            this.selection = 6;
            doMpe();
            repaint = true;
        } else if ((int) character == (int) RESOURCE_CHAR) {
            this.selection = 7;
            doResource();
            repaint = true;
        } else if ((int) character == (int) LOCK_CHAR) {
            this.selection = 8;
            doLock();
            repaint = true;
        } else if ((int) character == (int) QUIT_CHAR) {
            this.selection = 9;
            getMainWindow().quit();
        }

        return repaint;
    }

    /**
     * Handles the selection of the "Course" item.
     */
    private void doCourse() {

        if (isClearedFor("COURSE", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToCourse(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "Schedule" item.
     */
    private void doSchedule() {

        if (isClearedFor("PACING", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToSchedule(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "Discipline" item.
     */
    private void doDiscipline() {

        if (isClearedFor("DISCIP", 3)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToDiscipline(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "Holds" item.
     */
    private void doHolds() {

        if (isClearedFor("ADHOLD", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToHolds(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "Exams" item.
     */
    private void doExams() {

        if (isClearedFor("PENDEX", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToExams(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "MPE" item.
     */
    private void doMpe() {

        if (isClearedFor("PLACE", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToMPE(this.student);
            }
        }
    }

    /**
     * Handles the selection of the "Resource" item.
     */
    private void doResource() {

        if (isClearedFor("LOAN", 5)) {
            if (this.student == null) {
                this.showingPick = true;
                this.showingAccept = false;
                clearErrors();
                this.studentIdField.clear();
                this.studentIdField.activate();
            } else {
                getMainWindow().goToResource(this.student);
            }
        }
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
     * Tests whether the logged-in user is cleared to access a specified function.
     *
     * @param clearFunction   the function
     * @param comparisonValue the value to which to compare
     * @return true if cleared
     */
    private boolean isClearedFor(final String clearFunction, final int comparisonValue) {

        boolean ok = false;

        final Integer clearType = getMainWindow().getUserData().getClearType(clearFunction);

        if (clearType == null || clearType.intValue() >= comparisonValue) {
            setError("You don't have clearance to use this option.", " Press any key to continue...");
        } else {
            ok = true;
        }

        return ok;
    }
}
