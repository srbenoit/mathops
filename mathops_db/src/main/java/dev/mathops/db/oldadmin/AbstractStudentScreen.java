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
 * A base class for screens that track a current student.
 */
abstract class AbstractStudentScreen extends AbstractScreen {

    /** The current student ID. */
    private final Field studentIdField;

    /** Flag indicating "Pick student" is being shown. */
    private boolean showingPick = false;

    /** Flag indicating "Press RETURN to select, or F5 to cancel" is being shown. */
    private boolean showingAccept = false;

    /** The current student record. */
    private RawStudent student = null;

    /**
     * Constructs a new {@code AbstractStudentScreen}.
     *
     * @param theCache      the cache
     * @param theMainWindow the main window
     */
    AbstractStudentScreen(final Cache theCache, final MainWindow theMainWindow) {

        super(theCache, theMainWindow);

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
    public final boolean isAcceptingPick() {

        return this.showingAccept;
    }

    /**
     * Tests whether the user is picking a new student.
     *
     * @return true if picking a new student
     */
    public final boolean isPicking() {

        return this.showingPick;
    }

    /**
     * Draws the box where the user can enter a student ID.
     */
    protected final void drawPickBox() {

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

        if (this.showingAccept) {
            console.print("Press RETURN to select or F5 to cancel...", 15, 16);
        }
    }

    /**
     * Processes a typed key when accepting a picked student.
     *
     * @param key the key
     * @return true if the pick was accepted
     */
    protected final boolean processKeyPressInAcceptingPick(final int key) {

        boolean accepted = false;

        if (key == KeyEvent.VK_ENTER) {
            this.showingPick = false;
            this.showingAccept = false;
            clearErrors();
            this.studentIdField.clear();
            getConsole().setCursor(-1, -1);

            accepted = true;
        } else if (key == KeyEvent.VK_F5) {
            this.student = null;
            this.showingPick = false;
            this.showingAccept = false;
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
    protected final void processKeyPressInPick(final int key, final int modifiers) {

        if (key == KeyEvent.VK_ENTER) {
            final String entered = this.studentIdField.getValue();
            try {
                this.student = RawStudentLogic.query(getCache(), entered, false);
                this.showingAccept = true;
                clearErrors();
            } catch (final SQLException ex) {
                Log.warning(ex);
                this.studentIdField.clear();
                setError("ERROR:  Student not found.");
            }
        } else if (key == KeyEvent.VK_C && (modifiers & KeyEvent.CTRL_DOWN_MASK) == KeyEvent.CTRL_DOWN_MASK) {
            this.student = null;
            this.showingPick = false;
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
    protected final void processKeyTypedInPick(final char character) {

        this.studentIdField.processChar(character);
    }

    /**
     * Starts a pick operation.
     */
    protected final void doPick() {

        this.student = null;
        this.showingPick = true;
        this.showingAccept = false;
        clearErrors();
        this.studentIdField.clear();
        this.studentIdField.activate();
    }
}
