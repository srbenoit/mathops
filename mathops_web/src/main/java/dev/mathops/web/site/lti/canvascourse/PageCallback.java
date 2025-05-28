package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.rec.main.LtiRegistrationRec;
import dev.mathops.db.reclogic.main.LtiRegistrationLogic;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Enumeration;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0#overview'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
public enum PageCallback {
    ;

    /**
     * Responds to a GET or POST to "lti13_callback" with an Authentication Response as describned in
     * <a href='https://www.imsglobal.org/spec/security/v1p0/'>https://www.imsglobal.org/spec/security/v1p0/</a>.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException if there is an error writing the response
     */
    public static void doCallback(final Cache cache, final LtiSite site, final HttpServletRequest req,
                                  final HttpServletResponse resp) throws IOException {

        final Enumeration<String> paramNames = req.getParameterNames();
        while (paramNames.hasMoreElements()) {
            final String name = paramNames.nextElement();
            final String value = req.getParameter(name);
            Log.info("Callback param '", name, "' = ", value);
        }

        final String authenticityToken = req.getParameter("authenticity_token");
        final String idToken = req.getParameter("id_token");
        final String state = req.getParameter("state");
        final String ltiStorageTarget = req.getParameter("lti_storage_target");

        JSONWebSignature.validate(idToken);

        //

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")
                .addln(" <link rel='icon' type='image/x-icon' href='/www/images/favicon.ico'>")
                .addln(" <title>CSU Mathematics Program</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add("CSU Mathematics Program").eH(1);

        htm.sH(2).add("Callback").eH(2);

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
