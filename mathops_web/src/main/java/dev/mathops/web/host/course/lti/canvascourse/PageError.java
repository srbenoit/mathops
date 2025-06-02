package dev.mathops.web.host.course.lti.canvascourse;

import dev.mathops.commons.log.Log;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

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
        htm.sH(2).add(subtitle).eH(2);

        htm.sDiv("indent");
        htm.sP(null, "style='color:firebrick;'").add("An error has occurred:").eP();
        htm.sP(null, "style='color:steelblue;'").add(msg).eP();
        htm.eDiv();

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
