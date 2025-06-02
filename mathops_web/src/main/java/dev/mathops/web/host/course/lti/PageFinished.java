package dev.mathops.web.host.course.lti;

import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Shows a message once the exam is finished.
 */
enum PageFinished {
    ;

    /**
     * Generates the page.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    static void showPage(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final String title = "Exam Completed";

        Page.startPage(htm, title, false, false);

        htm.addln("<body style='background:white'>");

        htm.sH(1).add(title + ": Canvas Portal").eH(1);

        htm.sP().add("Results will be updated in the Precalculus course web site.").eP();

        htm.addln("</body>");
        htm.addln("</html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
