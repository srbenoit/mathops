package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.host.course.lti.LtiSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * An error page that can be used for any LTI target.
 */
public enum PageError {
    ;

    /**
     * Shows a page that displays an error message.
     *
     * @param req      the request
     * @param resp     the response
     * @param subtitle the subtitle for the error display
     * @param msg      the error message
     */
    static void showErrorPage(final ServletRequest req, final HttpServletResponse resp,
                              final String subtitle, final String msg) throws IOException {

        Log.warning(msg);

        final HtmlBuilder htm = new HtmlBuilder(1000);

        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head>");
        htm.addln(" <meta http-equiv='Content-Type' content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='ltistyle.css' type='text/css'>")
                .addln(" <title>", LtiSite.TOOL_NAME, "</title>");
        htm.addln("</head>");
        htm.addln("<body style='background:white; padding:20px;'>");

        htm.sH(1).add(LtiSite.TOOL_NAME).eH(1);
        htm.sH(2).add(subtitle).eH(2);

        htm.sDiv("indent");
        htm.sP("error").add("An error has occurred:").eP();
        htm.sP(null, "style='color:steelblue;'").add(msg).eP();
        htm.eDiv();

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
