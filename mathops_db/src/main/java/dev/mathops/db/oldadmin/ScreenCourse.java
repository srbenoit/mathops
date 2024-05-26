package dev.mathops.db.oldadmin;

import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermLogic;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.db.type.TermKey;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;

/**
 * The Course screen.
 */
final class ScreenCourse extends AbstractStudentScreen {

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
        } else {
            drawStudentNameId();
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
                repaint = true;
            } else if (sel == 1) {
                doCurrent();
                repaint = true;
            } else if (sel == 2) {
                doHomework();
                repaint = true;
            } else if (sel == 3) {
                doPick();
                repaint = true;
            } else if (sel == 4) {
                doLock();
                repaint = true;
            } else if (sel == 5) {
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
        } else if (this.clearPressAnyKey) {
            clearErrors();
            this.clearPressAnyKey = false;
            repaint = true;
        } else if ((int) character == (int) HISTORY_CHAR) {
            setSelection(0);
            doHistory();
            repaint = true;
        } else if ((int) character == (int) CURRENT_CHAR) {
            setSelection(1);
            doCurrent();
            repaint = true;
        } else if ((int) character == (int) HOMEWORK_CHAR) {
            setSelection(2);
            doHomework();
            repaint = true;
        } else if ((int) character == (int) PICK_CHAR) {
            setSelection(3);
            doPick();
            repaint = true;
        } else if ((int) character == (int) LOCK_CHAR) {
            setSelection(4);
            doLock();
            repaint = true;
        } else if ((int) character == (int) QUIT_CHAR) {
            setSelection(5);
            doQuit();
            repaint = true;
        }

        return repaint;
    }

    /**
     * Handles the selection of the "History" item.
     */
    private void doHistory() {

        final RawStudent stu = getStudent();
        if (stu == null) {
            doPick();
        } else {
            final Console console = getConsole();
            final Cache cache = getCache();
            try {
                final TermRec active = TermLogic.get(cache).queryActive(cache);
                final List<RawStcourse> stcFull = RawStcourseLogic.queryByStudent(cache, stu.stuId, true, true);
                final List<RawStcourse> stcCurr = RawStcourseLogic.queryByStudent(cache, stu.stuId, active.term, true,
                        true);
                final Iterator<RawStcourse> iter = stcCurr.iterator();
                while (iter.hasNext()) {
                    final RawStcourse row = iter.next();
                    if ("Y".equals(row.iInProgress) || row.iInProgress == null) {
                        iter.remove();
                    }
                }

                final int stcCount = stcFull.size() - stcCurr.size();
                if (stcCount < 1) {
                    final TermKey oTerm = new TermKey(active.term.name, active.term.year - 8);
                    final String msg = SimpleBuilder.concat(stu.firstName, " has not taken any PACe courses since ",
                            oTerm.longString);
                    console.print(msg, 13, 10);
                    console.reverse(13, 10, msg.length());

                    setError("Press any key to continue...");
                    this.clearPressAnyKey = true;
                } else {

                }
            } catch (final SQLException ex) {
                final String msg = ex.getMessage();
                setError("Unable to query course history", msg);
            }
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
}
