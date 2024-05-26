package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawStudent;

import java.awt.event.KeyEvent;
import java.util.Objects;

/**
 * The main screen.
 */
final class ScreenMain extends AbstractStudentScreen {

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

    /** The current selection (0 through 9). */
    private int selection;

    /**
     * Constructs a new {@code ScreenMain}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenMain(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

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
                console.print(idMsg, 40, 4);
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
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
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
                doPick();
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

        if (isLocked()) {
            processKeyTypedInLocked(character);
            repaint = true;
        } else if (isPicking()) {
            processKeyTypedInPick(character);
            repaint = true;
        } else if ((int) character == (int) PICK_CHAR) {
            doPick();
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
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToCourse(stu);
            }
        }
    }

    /**
     * Handles the selection of the "Schedule" item.
     */
    private void doSchedule() {

        if (isClearedFor("PACING", 5)) {
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToSchedule(stu);
            }
        }
    }

    /**
     * Handles the selection of the "Discipline" item.
     */
    private void doDiscipline() {

        if (isClearedFor("DISCIP", 3)) {
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToDiscipline(stu);
            }
        }
    }

    /**
     * Handles the selection of the "Holds" item.
     */
    private void doHolds() {

        if (isClearedFor("ADHOLD", 5)) {
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToHolds(stu);
            }
        }
    }

    /**
     * Handles the selection of the "Exams" item.
     */
    private void doExams() {

        if (isClearedFor("PENDEX", 5)) {
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToExams(stu);
            }
        }
    }

    /**
     * Handles the selection of the "MPE" item.
     */
    private void doMpe() {

        if (isClearedFor("PLACE", 5)) {
            final RawStudent stu = getStudent();

            if (stu == null) {
                doPick();
            } else {
                getMainWindow().goToMPE(stu);
            }
        }
    }

    /**
     * Handles the selection of the "Resource" item.
     */
    private void doResource() {

        if (isClearedFor("LOAN", 5)) {
            getMainWindow().goToResource();
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
