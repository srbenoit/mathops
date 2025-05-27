package dev.mathops.web.site.lti.canvascourse;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.lti.LtiSite;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;

/**
 * The page that manages the CSU Precalculus Program as an LTI 1.3 tool.
 *
 * <p>
 * The process followed here is documented at <a
 * href='https://www.imsglobal.org/spec/lti-dr/v1p0#overview'>https://www.imsglobal.org/spec/lti-dr/v1p0#overview</a>.
 */
public enum PageLaunch {
    ;

    /**
     * Responds to a POST of "lti13_launch".  This generates content that will be embedded in Canvas.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException if there is an error writing the response
     */
    public static void doPost(final Cache cache, final LtiSite site, final HttpServletRequest req,
                              final HttpServletResponse resp) throws IOException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        htm.addln("<!DOCTYPE html>").addln("<html>").addln("<head></head><body>");

        htm.sP().addln("Call to LTI Launch:").eP();

        htm.sDiv("indent");

        final Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String name = headerNames.nextElement();
            final String value = req.getHeader(name);
            htm.sP().addln("Header '", name, "': ", value).eP();
        }

        final Enumeration<String> parameterNames = req.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            final String name = parameterNames.nextElement();
            final String value = req.getParameter(name);
            htm.sP().addln("Parameter '", name, "': ", value).eP();
        }

        final InputStream input = req.getInputStream();
        final BufferedReader buf = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8));
        String inputLine;
        final StringBuilder buffer = new StringBuilder(1000);
        while ((inputLine = buf.readLine()) != null) {
            buffer.append(inputLine);
        }
        buf.close();
        final String requestContent = buffer.toString();

        htm.sP().addln("Request content:").eP();
        htm.sP().addln(requestContent).eP();
        htm.eDiv();

        htm.addln("</body></html>");

        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);

    }
}
