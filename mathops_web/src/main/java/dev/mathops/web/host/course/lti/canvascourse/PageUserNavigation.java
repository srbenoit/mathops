package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.db.schema.term.rec.LtiContextRec;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page with the "user_navigation" placement.
 */
enum PageUserNavigation {
    ;

    /**
     * Shows the page.
     *
     * @param cache    the data cache
     * @param req      the request
     * @param resp     the response
     * @param redirect the redirect
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final ServletRequest req, final HttpServletResponse resp,
                         final LtiSite.PendingTargetRedirect redirect) throws IOException, SQLException {

        final PageUtils.LtiContextData ltiData = PageUtils.lookupLtiContext(cache, redirect);

        if (ltiData == null) {
            PageUtils.showCourseNotConfigured(req, resp);
        } else {
            // TODO: Show real content here

            final JSONObject payload = redirect.idTokenPayload();
            LTITarget.showDefault(ltiData, payload, req, resp, "CSU LTI Tool User Navigation");
        }
    }
}
