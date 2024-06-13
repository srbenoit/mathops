package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawlogic.RawCusectionLogic;
import dev.mathops.db.old.rawlogic.RawPacingStructureLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawCusection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.rec.RecBase;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;
import oracle.sql.ARRAY;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * The Course screen.
 */
final class ScreenCourse extends AbstractStudentScreen {

    /** A single instance. */
    private static final StCourseSort COURSE_SORT = new StCourseSort();

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

    /** The display. */
    private ScreenCourseDisplay display = ScreenCourseDisplay.NONE;

    /** Flag indicating there is an active "Press any key to continue" prompt. */
    private boolean clearPressAnyKey;

    /**
     * Constructs a new {@code ScreenCourse}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    ScreenCourse(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow, 6);

        this.clearPressAnyKey = false;
    }

    /**
     * Draws the screen to a console.
     */
    public void draw() {

        final Console console = getConsole();

        console.clear();
        console.print("COURSE OPTIONS:   History  Current  homework_rpt  Pick  locK  QUIT", 0, 0);

        switch (getSelection()) {
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
        } else if (getStudent() != null) {
            drawStudentNameId();

            switch (this.display) {
                case HISTORY -> drawHistory();
            }
        }

        drawErrors();

        console.commit();
    }

    /**
     * Draws the history display.
     */
    private void drawHistory() {

        final Console console = getConsole();
        final RawStudent stu = getStudent();

        final Cache cache = getCache();
        try {
            final TermRec active = TermLogic.get(cache).queryActive(cache);

            final List<RawStcourse> stcFull = RawStcourseLogic.queryByStudent(cache, stu.stuId, true, true);
            final List<RawStcourse> stcCurr = RawStcourseLogic.queryByStudent(cache, stu.stuId, active.term,
                    true, true);
            final Iterator<RawStcourse> iter = stcCurr.iterator();
            while (iter.hasNext()) {
                final RawStcourse row = iter.next();
                if ("Y".equals(row.iInProgress) || row.iInProgress == null) {
                    iter.remove();
                }
            }

            final int stcCount = stcFull.size() - stcCurr.size();
            if (stcCount < 1) {
                final TermKey oTerm = new TermKey(active.term.name, active.term.year.intValue() - 8);
                final String msg = SimpleBuilder.concat(stu.firstName, " has not taken any PACe courses since ",
                        oTerm.longString);
                console.print(msg, 13, 10);
                console.reverse(13, 10, msg.length());

                setError("Press any key to continue...");
                this.clearPressAnyKey = true;
            } else {
                // Sort full list by course, then by term
                stcFull.sort(COURSE_SORT);

                final List<String> rowsToShow = new ArrayList<>(10);
                final StringBuilder builder = new StringBuilder(40);

                for (final RawStcourse stc : stcFull) {
                    String pacing = null;
                    final RawCsection csect = RawCsectionLogic.query(cache, stc.course, stc.sect, stc.termKey);
                    if (csect != null) {
                        pacing = csect.pacingStructure;
                    }

                    final String pacingName;
                    if ("M".equals(pacing)) {
                        pacingName = "PACe ";
                    } else if ("O".equals(pacing)) {
                        pacingName = "CSUOnline ";
                    } else if ("K".equals(pacing)) {
                        pacingName = "KEY Acad ";
                    } else if ("I".equals(pacing)) {
                        pacingName = "Instr Led ";
                    } else {
                        pacingName = "Unknown ";
                    }

                    final TermKey dispTerm;
                    if (active.equals(stc.termKey)) {
                        if ("Y".equals(stc.iInProgress)) {
                            dispTerm = stc.iTermKey;
                        } else {
                            dispTerm = stc.termKey;
                        }
                    } else {
                        dispTerm = stc.termKey;
                    }

                    builder.append(stc.course);
                    for (int i = stc.course.length(); i < 10; ++i) {
                        builder.append(' ');
                    }
                    builder.append(stc.sect);
                    for (int i = stc.sect.length(); i < 10; ++i) {
                        builder.append(' ');
                    }
                    builder.append(dispTerm.name.termName);
                    builder.append("  ");
                    builder.append(dispTerm.year);
                    builder.append("     ");
                    if (stc.courseGrade == null) {
                        builder.append("      ");
                    } else {
                        builder.append(stc.courseGrade);
                        for (int i = stc.courseGrade.length(); i < 6; ++i) {
                            builder.append(' ');
                        }
                    }
                    builder.append(pacingName);

                    final String regString = builder.toString();
                    rowsToShow.add(regString);

                    builder.setLength(0);
                }

                setError("F5 (or ctrl-e) = Exit");

                drawBox(6, 6, 54, 15);
                console.print("PACe COURSE REGISTRATION HISTORY", 17, 8);
                console.print("Course   Section   Term Year   Grade   Format", 9, 10);
                console.print("Use the arrow keys to view more rows...", 8, 19);

                int row = 11;
                for (final String str : rowsToShow) {
                    console.print(str, 9, row);
                    ++row;
                }
                console.setCursor(9, 11);
            }
        } catch (final SQLException ex) {
            final String msg = ex.getMessage();
            setError("Unable to query course history", msg);
        }
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
                final int sel = getSelection();

                if (sel == 0) {
                    doHistory();
                } else if (sel == 1) {
                    doCurrent();
                } else if (sel == 2) {
                    doHomework();
                } else if (sel == 3) {
                    doPick();
                }
            }
            repaint = true;
        } else if (isPicking()) {
            processKeyPressInPick(key, modifiers);
            repaint = true;
        } else if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_KP_RIGHT) {
            incrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_KP_LEFT) {
            decrementSelection();
            repaint = true;
        } else if (key == KeyEvent.VK_ENTER) {
            final int sel = getSelection();

            if (sel == 0) {
                doHistory();
            } else if (sel == 1) {
                doCurrent();
            } else if (sel == 2) {
                doHomework();
            } else if (sel == 3) {
                doPick();
            } else if (sel == 4) {
                doLock();
            } else if (sel == 5) {
                doQuit();
            }
            repaint = true;
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

        boolean repaint = true;

        if (isLocked()) {
            processKeyTypedInLocked(character);
        } else if (isPicking()) {
            processKeyTypedInPick(character);
        } else if (this.clearPressAnyKey) {
            clearErrors();
            this.clearPressAnyKey = false;
        } else if ((int) character == (int) HISTORY_CHAR) {
            setSelection(0);
            doHistory();
        } else if ((int) character == (int) CURRENT_CHAR) {
            setSelection(1);
            doCurrent();
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            setSelection(2);
            doHomework();
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(3);
            doPick();
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(4);
            doLock();
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(5);
            doQuit();
        } else {
            repaint = false;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "History" item.
     */
    private void doHistory() {

        if (getStudent() == null) {
            doPick();
        } else {
            this.display = ScreenCourseDisplay.HISTORY;
        }
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

    /**
     * Possible displays on this screen.
     */
    private enum ScreenCourseDisplay {

        NONE,
        HISTORY,
    }

    /**
     * A comparator that sorts {@code RawStcourse} records on course ID then term.
     */
    private static class StCourseSort implements Comparator<RawStcourse> {

        /**
         * Constructs a new {@code StCourseSort}.
         */
        private StCourseSort() {

            // No action
        }

        /**
         * Compares two records for order.
         *
         * @param o1 the first object to be compared
         * @param o2 the second object to be compared
         * @return a negative integer, zero, or a positive integer as the first object is less than, equal to, or
         *         greater than the second object
         */
        @Override
        public final int compare(final RawStcourse o1, final RawStcourse o2) {

            int result = RecBase.compareAllowingNull(o1.course, o2.course);

            if (result == 0) {
                result = RecBase.compareAllowingNull(o1.termKey, o2.termKey);
            }

            return result;
        }
    }
}
