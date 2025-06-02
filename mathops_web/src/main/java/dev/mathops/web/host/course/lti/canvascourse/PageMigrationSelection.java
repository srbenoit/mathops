package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.parser.json.JSONObject;
import dev.mathops.text.parser.xml.XmlEscaper;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.course.lti.LtiSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * A page with the "migration_selection" placement.
 */
enum PageMigrationSelection {
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

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta name=\"robots\" content=\"noindex\">");
        htm.addln(" <meta http-equiv='X-UA-Compatible' content='IE=edge,chrome=1'/>")
                .addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='basestyle.css' type='text/css'>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")
                .addln(" <title>CSU Mathematics Program</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add("CSU LTI Tool Migration Selection").eH(1);

        htm.sDiv("indent");
        htm.sP().addln("<pre>");
        htm.add(payload.toJSONFriendly(0));
        htm.addln("</pre>").eP();
        htm.eDiv();

        final Object pres = payload.getProperty("https://purl.imsglobal.org/spec/lti/claim/launch_presentation");
        if (pres instanceof JSONObject presObject) {
            final String returnUrl = presObject.getStringProperty("return_url");
            htm.addln("<form action='", XmlEscaper.escape(returnUrl), "' method='GET'>");
            htm.addln("  <input type='submit' value='Ok.'/>");
            htm.addln("</form>");
        }

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
