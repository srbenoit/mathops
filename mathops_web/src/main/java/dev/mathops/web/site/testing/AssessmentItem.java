package dev.mathops.web.site.testing;

import dev.mathops.assessment.problem.template.AbstractProblemTemplate;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.html.item.EItemState;
import dev.mathops.web.site.html.item.ItemSession;
import dev.mathops.web.site.html.item.ItemSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Handles servlet requests for embedded assessment items in pages.
 */
enum AssessmentItem {
    ;

    /**
     * Processes a GET request.  The response to this request is not a complete web page, but a snipped that can be
     * incorporated into other pages (hence the ".ws" rather than ".html" extension)
     *
     * <p>
     * When a hosting page is loaded, a request to this service is made, passing the student ID, GUID of the item
     * placement, and tree reference of the item.  If the student already has an item session for that GUID, that
     * session is used; if not, a new session is created and brought to the INTERACTING state.  The session then
     * generates the HTML content.
     *
     * <p>
     * The generated HTML includes scripts that will process actions like submitting the item for scoring.  These will
     * call this service again, passing the item session ID, an action code, and any other needed parameters.  The
     * response will depend on the action code, but will allow the hosting page to update itself properly.
     *
     * <p>
     * Request parameters:
     * <ol>
     *     <li>action: "display | submit | ..."</li>
     *     <li>student_id: sent on page load when item session ID is not known</li>
     *     <li>guid: sent on page load when item session ID is not known</li>
     *     <li>tree_ref: sent on page load when item session ID is not known</li>
     *     <li>session_id: item session id, sent on actions after page load</li>
     *     <li>response: comma-separated responses</li>
     * </ol>
     *
     * @param cache   the data cache
     * @param subpath the portion of the path beyond that which was used to select this site
     * @param type    the site type
     * @param req     the request
     * @param resp    the response
     * @throws IOException if there is an error writing the response
     */
    static void doGet(final Cache cache, final String subpath, final ESiteType type, final HttpServletRequest req,
                      final HttpServletResponse resp) throws IOException {

        // GET https://testing.math.colostate.edu/assessment_item.ws
        final String action = req.getParameter("action");

        final HtmlBuilder htm = new HtmlBuilder(1000);

        if ("submit".equals(action)) {
            doSubmitAction(req, htm);
        } else if ("regenerate".equals(action)) {
            doRegenerateAction(req, htm);
        } else {
            // Treat "load" or any unknown action as "load"
            // Page being loaded - student ID, GUID, and tree reference must be provided
            doLoadAction(req, htm);
        }

        final String replyStr = htm.toString();
        final byte[] replyBytes = replyStr.getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_PLAIN, replyBytes);
    }

    /**
     * Handles the "submit" action, which is called the user presses the "submit" button on the item.  This button is
     * only active when the item is in an "INTERACTING" state and can be submitted.  This scores the item, and moves
     * the state to a CORRECT_* or INCORRECT_* state.
     *
     * @param req the request
     * @param htm a {@code HtmlBuilder} to which to append
     */
    private static void doSubmitAction(final ServletRequest req, final HtmlBuilder htm) {

        // TODO:
    }

    /**
     * Handles the "regenerate" action, which is called the user presses the "generate new question" button on the item.
     * This button is only active when the item is in a "CORRECT_*" or "INCORRECT_*" state and the item can be
     * regenerated.
     *
     * @param req the request
     * @param htm a {@code HtmlBuilder} to which to append
     */
    private static void doRegenerateAction(final ServletRequest req, final HtmlBuilder htm) {

        // TODO:

    }

    /**
     * Handles the "load" action, which is called when a page containing an item is loaded (or re-loaded).  If an item
     * session already exists that matches the student ID/GUID combination, that session is used to generate content;
     * otherwise, a new session is created.
     *
     * @param req the request
     * @param htm a {@code HtmlBuilder} to which to append
     */
    private static void doLoadAction(final ServletRequest req, final HtmlBuilder htm) {

        final String studentId = req.getParameter("student_id");
        final String guid = req.getParameter("guid");
        final String treeRef = req.getParameter("tree_ref");

        if (studentId == null) {
            htm.add("<p>Unable to display item: student ID not provided in request.</p>");
        } else if (guid == null) {
            htm.add("<p>Unable to display item: GUID not provided in request.</p>");
        } else if (treeRef == null) {
            htm.add("<p>Unable to display item: item identifier not provided in request.</p>");
        } else {
            final ItemSessionStore sessionStore = ItemSessionStore.getInstance();
            ItemSession session = sessionStore.getItemSession(studentId, guid);
            AbstractProblemTemplate item = null;

            if (session == null) {
                session = new ItemSession(studentId, guid, treeRef);
                if (session.hasItem()) {
                    sessionStore.setItemSession(session);
                    item = session.getItem();
                }
            } else {
                item = session.getItem();
            }

            if (item == null) {
                htm.add("<p>Unable to display item: item could not be generated.</p>");
            } else {
                // TODO: This needs scripts, a correct/incorrect/in-progress icon, submit button

                final EItemState itemState = session.getState();

                final String questionHtml = item.questionHtml;
                htm.add(questionHtml);

                if (itemState == EItemState.CORRECT_SHOW_ANSWER || itemState == EItemState.INCORRECT_SHOW_ANSWER) {
                    final String answerHtml = item.answerHtml;
                    if (answerHtml != null) {
                        htm.add(answerHtml);
                    }
                }

                if (itemState == EItemState.CORRECT_SHOW_SOLUTION || itemState == EItemState.INCORRECT_SHOW_SOLUTION) {
                    final String solutionHtml = item.solutionHtml;
                    if (solutionHtml != null) {
                        htm.add(solutionHtml);
                    }
                }
            }
        }
    }
}