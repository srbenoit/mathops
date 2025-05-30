package dev.mathops.web.site.lti.oauth;


import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A base class for LTI web pages.
 */
public enum LtiPage {
    ;

    /** Show no bars. */
    public static final int NO_BARS = 0x00;

    /** Show the user/date bar (could be combined with '|' with ADMIN_BAR). */
    public static final int USER_DATE_BAR = 0x01;

    /** Show the admin bar (could be combined with '|' with USER_DATE_BAR). */
    public static final int ADMIN_BAR = 0x02;

    /** The MIME type text/html. */
    public static final String MIME_TEXT_HTML = "text/html";

    /**
     * Sends a response with a particular content type and content.
     *
     * @param req         the request
     * @param resp        the response
     * @param contentType the content type
     * @param reply       the reply content
     * @throws IOException if there was an exception writing the response
     */
    public static void sendReply(final ServletRequest req, final ServletResponse resp,
                          final String contentType, final byte[] reply) throws IOException {

        resp.setContentType(contentType);
        resp.setCharacterEncoding("UTF-8");
        resp.setContentLength(reply.length);
        resp.setLocale(req.getLocale());

        try (final OutputStream out = resp.getOutputStream()) {
            out.write(reply);
        } catch (final IOException ex) {
            if (!"ClientAbortException".equals(ex.getClass().getSimpleName())) {
                throw ex;
            }
        }
    }

    /**
     * Appends the start of the HTML page (the DOCTYPE declaration, opening of the &lt;html&gt; element, &lt;head&gt;
     * block, opening of &lt;body&gt;, and two surrounding &lt;div&gt;s) to an {@code HtmlBuilder}.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the title (to be followed by " | Department of Mathematics")
     */
    public static void startPage(final HtmlBuilder htm, final String title) {

        htm.addln("<!DOCTYPE html>")
                .addln("<html>")
                .addln("<head>");

        htm.addln(" <meta name=\"robots\" content=\"noindex\">")
                .addln(" <meta http-equiv='Content-Type' ",
                        "content='text/html;charset=utf-8'/>")
                .addln(" <link rel='stylesheet' href='style.css' type='text/css'>")
                .addln(" <link rel='icon' type='image/x-icon' ",
                        "href='/www/images/favicon.ico'>")
                .addln(" <title>", title, "</title>");

        htm.addln("</head>");
        htm.addln("<body>");
    }

    /**
     * Appends the end of the HTML page (the closure of the two surrounding &lt;div&gt;s and the &lt;body&gt; and
     * &lt;html&gt; elements) to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    public static void endPage(final HtmlBuilder htm) {

        htm.addln("</body>");
        htm.addln("</html>");
    }
}
