package dev.mathops.web.site.html.item;

import dev.mathops.assessment.InstructionalCache;
import dev.mathops.assessment.htmlgen.ProblemConverter;
import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import jakarta.servlet.ServletRequest;

import java.sql.SQLException;
import java.time.ZonedDateTime;

/**
 * A user session used to take homework assignments online. It takes as arguments a session ID, student name, and
 * assignment ID and presents the homework assignment to the student. When the assignment is complete, the student
 * submits it for grading, after which the student is shown a summary page of the results.
 */
public final class ItemSession {

    /** The timeout duration (2 hours), in milliseconds. */
    private static final long TIMEOUT = (long) (2 * 60 * 60 * 1000);


    /** The ID of the student doing the homework. */
    public final String studentId;

    /** The GUID of the item placement. */
    public final String guid;

    /** The tree reference of the item. */
    public final String treeRef;

    /** The assessment item itself. */
    private AbstractProblemTemplate item;

    /** The state of the assignment. */
    private EItemState state;

    /** Timestamp when exam will time out. */
    private long timeout;

    /**
     * Constructs a new {@code ItemSession}. This is called when the user loads a page with an embedded item. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param theStudentId the student ID
     * @param theGuid      the GUID of the item placement
     * @param theTreeRef   the tree reference of the item
     */
    public ItemSession(final String theStudentId, final String theGuid, final String theTreeRef) {

        if (theStudentId == null) {
            throw new IllegalArgumentException("Student ID may not be null");
        }
        if (theGuid == null) {
            throw new IllegalArgumentException("GUID may not be null");
        }
        if (theTreeRef == null) {
            throw new IllegalArgumentException("Item tree reference may not be null");
        }

        this.studentId = theStudentId;
        this.guid = theGuid;
        this.treeRef = theTreeRef;

        this.state = EItemState.INTERACTING;
        this.timeout = System.currentTimeMillis() + TIMEOUT;

        final AbstractProblemTemplate theItem = InstructionalCache.getProblem(this.treeRef);

        if (theItem == null) {
            Log.warning("Unable to load item for ", this.treeRef);
        } else if (theItem.realize(theItem.evalContext)) {
            this.item = theItem;
        } else {
            Log.warning("Unable to generate item for ", this.treeRef);
        }
    }

    /**
     * Constructs a new {@code ItemSession}. This is called when the user clicks a button to start an assignment. It
     * stores data but does not generate the HTML until the page is actually generated.
     *
     * @param theStudentId the student ID
     * @param theGuid      the GUID of the item placement
     * @param theTreeRef   the tree reference of the item
     * @param theState     the session state
     * @param theTimeout   the timeout
     * @param theItem      the assessment item
     */
    ItemSession(final String theStudentId, final String theGuid, final String theTreeRef, final EItemState theState,
                final long theTimeout, final AbstractProblemTemplate theItem) {

        this(theStudentId, theGuid, theTreeRef);

        this.state = theState;
        this.timeout = theTimeout;
        this.item = theItem;
    }

    /**
     * Gets the homework state.
     *
     * @return the homework state
     */
    public EItemState getState() {

        return this.state;
    }

    /**
     * Tests whether the session has a valid item.
     *
     * @return true if there is a valid item
     */
    public boolean hasItem() {

        return this.item != null;
    }

    /**
     * Gets the realized item.
     *
     * @return the realized item
     */
    public AbstractProblemTemplate getItem() {

        return this.item;
    }

    /**
     * Gets the time remaining in the exam.
     *
     * @return the time remaining, in milliseconds (0 if the exam has not been started)
     */
    public long getTimeRemaining() {

        return this.timeout == 0L ? 0L : this.timeout - System.currentTimeMillis();
    }

    /**
     * Tests whether this session is timed out.
     *
     * @return {@code true} if timed out
     */
    public boolean isTimedOut() {

        return System.currentTimeMillis() >= this.timeout;
    }

    /**
     * Generates HTML for the item based on its current state.
     *
     * <p>
     * Inline items do not do a "form submit" when the user clicks the submit button, since that would reload the entire
     * page.  They cannot (yet) process their own correctness locally, so they send (via AJAX) a request to an item
     * session service to submit updated state, and receive display instructions.
     *
     * <p>
     * This method, then, generates HTML with JavaScript to do handle these things.  The initial state of the displayed
     * HTML is set based on this session's state (so if the page is actually refreshed, the correct item state shows).
     *
     * @param cache the data cache
     * @param now   the date/time to consider "now"
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public void generateHtml(final Cache cache, final ZonedDateTime now, final HtmlBuilder htm)
            throws SQLException {

        this.timeout = System.currentTimeMillis() + TIMEOUT;

        switch (this.state) {
            case INTERACTING:
            case INTERACTING_HINTS:
                appendItemHtml(htm);
                break;

            case CORRECT:
                appendCorrectMsgHtml(htm, "next", "Go to the next problem...");
                break;

            case CORRECT_SHOW_ANSWER:
                appendHomeworkAnswerHtml(htm);
                break;

            case CORRECT_SHOW_SOLUTION:
                appendHomeworkSolutionHtml(htm);
                break;

            case INCORRECT:
                appendIncorrectMsgHtml(htm, "next", "Try another problem...");
                break;

            case INCORRECT_SHOW_ANSWER:
                appendIncorrectMsgHtml(htm, "answer", "View answer...");
                break;

            case INCORRECT_SHOW_SOLUTION:
                appendIncorrectMsgHtml(htm, "solution", "View solution...");
                break;

            default:
                appendHeader(htm);
                htm.addln("<div style='text-align:center; color:navy;'>");
                htm.addln("Unsupported state.");
                htm.addln("</div>");
                appendFooter(htm, "close", "Close");
                break;
        }
    }

    /**
     * Appends the HTML for the assignment.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendItemHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        if (this.item.questionHtml == null) {
            ProblemConverter.populateProblemHtml(this.item, new int[]{1});
        }
        htm.addln(this.item.insertAnswers(this.item.questionHtml));

        appendFooter(htm, "grade", "Submit this problem for grading.");
    }

    /**
     * Appends the HTML for the assignment after an incorrect response has been entered.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private void appendIncorrectMsgHtml(final HtmlBuilder htm, final String command, final String label) {

        appendHeader(htm);

        if (this.item.questionHtml == null) {
            ProblemConverter.populateProblemHtml(this.item, new int[]{1});
        }
        htm.addln(this.item.insertAnswers(this.item.disabledHtml));

        htm.addln("<div style='text-align:center; color:FireBrick;'>");
        htm.addln("Your answer is not correct.");
        htm.addln("</div>");

        // TODO: Add the Live Help request button here if available.

        appendFooter(htm, command, label);
    }

    /**
     * Appends the HTML for the assignment after a correct response has been entered.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private void appendCorrectMsgHtml(final HtmlBuilder htm, final String command, final String label) {

        appendHeader(htm);

        if (this.item.questionHtml == null) {
            ProblemConverter.populateProblemHtml(this.item, new int[]{1});
        }
        htm.addln(this.item.insertAnswers(this.item.disabledHtml));

        htm.addln("<div style='text-align:center; color:green;'>");
        htm.addln("Your answer is correct.");
        htm.addln("</div>");

        appendFooter(htm, command, label);
    }

    /**
     * Appends the HTML for the assignment with answer shown.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHomeworkAnswerHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        if (this.item.questionHtml == null) {
            ProblemConverter.populateProblemHtml(this.item, new int[]{1});
        }

        htm.addln(this.item.insertAnswers(this.item.answerHtml));

        appendFooter(htm, "try-again", "Try another problem...");
    }

    /**
     * Appends the HTML for the assignment with solution shown.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHomeworkSolutionHtml(final HtmlBuilder htm) {

        appendHeader(htm);

        if (this.item.questionHtml == null) {
            ProblemConverter.populateProblemHtml(this.item, new int[]{1});
        }
        htm.addln(this.item.insertAnswers(this.item.solutionHtml));

        appendFooter(htm, "try-again", "Try another problem...");
    }

    /**
     * Appends the header.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private void appendHeader(final HtmlBuilder htm) {

        // TOOD:
    }

    /**
     * Appends the footer.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param command the submit button name (command)
     * @param label   the button label
     */
    private static void appendFooter(final HtmlBuilder htm, final String command, final String label) {

        // TOOD:
    }

    /**
     * Called when a POST is received on the page hosting the homework.
     *
     * @param cache   the data cache
     * @param session the user session
     * @param req     the servlet request
     * @param htm     the {@code HtmlBuilder} to which to append
     * @return a URL to which to redirect; {@code null} to present the generated HTML
     * @throws SQLException if there is an error accessing the database
     */
    public String processPost(final Cache cache, final ImmutableSessionInfo session,
                              final ServletRequest req, final HtmlBuilder htm) throws SQLException {

        String redirect = null;

        // TODO:

        return redirect;
    }

    /**
     * Appends the XML representation of this session to an {@code HtmlBuilder}.
     *
     * @param xml the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder xml) {

        if (this.item != null) {
            xml.addln("<item-session>");
            xml.addln(" <student>", this.studentId, "</student>");
            xml.addln(" <guid>", this.guid, "</guid>");
            xml.addln(" <tree-ref>", this.treeRef, "</tree-ref>");
            xml.addln(" <state>", this.state.name(), "</state>");
            xml.addln(" <timeout>", Long.toString(this.timeout), "</timeout>");
            this.item.appendXml(xml, 1);
            xml.addln("</item-session>");
        }
    }

    /**
     * Performs a forced abort of a homework session.
     *
     * @param session the login session requesting the forced abort
     */
    public void forceAbort(final ImmutableSessionInfo session) {

        if (session.getEffectiveRole().canActAs(ERole.ADMINISTRATOR)) {
            this.item = null;
            final ItemSessionStore store = ItemSessionStore.getInstance();
            store.removeItemSession(this.studentId, this.guid);
        }
    }
}
