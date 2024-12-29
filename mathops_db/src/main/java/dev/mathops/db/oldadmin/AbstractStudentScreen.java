package dev.mathops.db.oldadmin;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.text.builder.SimpleBuilder;

import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Objects;

/**
 * A base class for screens that track a current student.
 */
abstract class AbstractStudentScreen extends AbstractScreen {

    /** The current student ID. */
    private final Field studentIdField;

    /** Flag indicating "Pick student" is being shown. */
    private boolean picking = false;

    /** Flag indicating "Press RETURN to select, or F5 to cancel" is being shown. */
    private boolean acceptingPick = false;

    /** The current student record. */
    private RawStudent student = null;

    /**
     * Constructs a new {@code AbstractStudentScreen}.
     *
     * @param theCache         the cache
     * @param theMainWindow    the main window
     * @param theNumSelections the number of selections
     */
    AbstractStudentScreen(final Cache theCache, final MainWindow theMainWindow, final int theNumSelections) {

        super(theCache, theMainWindow, theNumSelections);

        final Console console = getConsole();
        this.studentIdField = new Field(console, 28, 11, 9, false, "0123456789");
    }

    /**
     * Sets the student.
     *
     * @param theStudent the student
     */
    public final void setStudent(final RawStudent theStudent) {

        this.student = theStudent;
    }

    /**
     * Gets the student.
     *
     * @return the student
     */
    public final RawStudent getStudent() {

        return this.student;
    }

    /**
     * Tests whether the user is accepting a picked student.
     *
     * @return true if accepting a picked student
     */
    final boolean isAcceptingPick() {

        return this.acceptingPick;
    }

    /**
     * Tests whether the user is picking a new student.
     *
     * @return true if picking a new student
     */
    final boolean isPicking() {

        return this.picking;
    }

    /**
     * Draws the student's name and ID on line 4.
     */
    final void drawStudentNameId() {

        if (Objects.nonNull(this.student)) {
            final Console console = getConsole();

            final String name = getClippedStudentName();
            console.print(name, 0, 4);

            final String idMsg = SimpleBuilder.concat("Student ID: ", this.student.stuId);
            console.print(idMsg, 41, 4);
        }
    }

    /**
     * Draws the box where the user can enter a student ID.
     */
    final void drawPickBox() {

        final Console console = getConsole();

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

        if (this.acceptingPick) {
            console.print("Press RETURN to select or F5 to cancel...", 15, 16);
        }
    }

    /**
     * Processes a typed key when accepting a picked student.
     *
     * @param key the key
     * @return true if the pick was accepted
     */
    final boolean processKeyPressInAcceptingPick(final int key) {

        boolean accepted = false;

        if (key == KeyEvent.VK_ENTER) {
            this.picking = false;
            this.acceptingPick = false;
            clearErrors();
            this.studentIdField.clear();
            getConsole().setCursor(-1, -1);

            accepted = true;
        } else if (key == KeyEvent.VK_F5) {
            this.student = null;
            this.picking = false;
            this.acceptingPick = false;
            clearErrors();
            this.studentIdField.clear();
            getConsole().setCursor(-1, -1);
        }

        return accepted;
    }

    /**
     * Processes a typed key when picking a student.
     *
     * @param key the key
     * @param modifiers key modifiers
     */
    final void processKeyPressInPick(final int key, final int modifiers) {

        if (key == KeyEvent.VK_ENTER) {
            final String entered = this.studentIdField.getValue();
            try {
                final Cache cache = getCache();
                this.student = RawStudentLogic.query(cache, entered, false);
                this.acceptingPick = true;
                clearErrors();
            } catch (final SQLException ex) {
                Log.warning(ex);
                this.studentIdField.clear();
                setError("ERROR:  Student not found.");
            }
        } else if (key == KeyEvent.VK_C && (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
            this.student = null;
            this.picking = false;
            clearErrors();
            this.studentIdField.clear();
            getConsole().setCursor(-1, -1);
        } else {
            this.studentIdField.processKey(key);
        }
    }

    /**
     * Processes a typed key when picking a student.
     *
     * @param character the character
     */
    final void processKeyTypedInPick(final char character) {

        this.studentIdField.processChar(character);
    }

    /**
     * Starts a pick operation.
     */
    final void doPick() {

        this.student = null;
        this.picking = true;
        this.acceptingPick = false;
        clearErrors();
        this.studentIdField.clear();
        this.studentIdField.activate();
    }

    /**
     * Gets the student's name clipped to 34 characters.
     *
     * @return the clipped name
     */
    final String getClippedStudentName() {

        final String result;

        if (this.student == null) {
            result = CoreConstants.EMPTY;
        } else {
            final String name = SimpleBuilder.concat(this.student.lastName, ", ", this.student.firstName);
            if (name.length() > 34) {
                result = name.substring(0, 34);
            } else {
                result = name;
            }
        }

        return result;
    }
}
