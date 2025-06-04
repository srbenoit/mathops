package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A page with the "link_selection" placement.
 */
enum PageLinkSelection {
    ;

    /**
     * Shows the page.
     *
     * @param req      the request
     * @param resp     the response
     * @param redirect the redirect
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp,
                         final LtiSite.PendingTargetRedirect redirect) throws IOException {

        final JSONObject payload = redirect.idTokenPayload();

        final Object custom = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/custom");
        if (custom instanceof final JSONObject customObj) {
            final String assignmentId = customObj.getStringProperty("canvas_assignment_id");

            // TODO: See of this resource link ID has already been linked to a CSU assignment
            //   IF SO:
            //       If the user has teacher role, show the assignment configuration (allow edits)
            //       Otherwise, show the user's view of the assignment
            //   IF NOT:
            //       If the user has teacher role, let them pick an assignment from a list
            //       Otherwise, show a message indicating the assignment is not configured
        }






        LTITarget. showDefault(payload, req, resp, "CSU LTI Tool Link Selection");
    }
}
