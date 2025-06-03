package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A page with the "discussion_topic_menu" placement.
 */
enum PageDiscussionTopicMenu {
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
        LTITarget.showDefault(payload, req, resp, "CSU LTI Tool Discussion Topic Menu");
    }
}
