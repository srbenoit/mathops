package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.db.rec.term.LtiCourseRec;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * A page with the "wiki_index_menu" placement.
 */
enum PageWikiIndexMenu {
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

        final LtiCourseRec ltiCourse = PageUtils.lookupLtiCourse(cache, redirect);

        if (ltiCourse == null) {
            PageUtils.showCourseNotConfigured(req, resp);
        } else {
            // TODO: Show real content here

            final JSONObject payload = redirect.idTokenPayload();
            LTITarget.showDefault(payload, req, resp, "CSU LTI Tool Wiki Index Menu");
        }
    }
}
