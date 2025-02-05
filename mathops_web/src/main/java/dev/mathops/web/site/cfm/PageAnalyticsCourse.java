package dev.mathops.web.site.cfm;

import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * Generates a page with analytics for a single course.
 */
enum PageAnalyticsCourse {
    ;

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void showPage(final Cache cache, final CfmSite site, final ServletRequest req,
                         final HttpServletResponse resp) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, "The Center for Foundational Mathematics - Analytics", null, true, 0, null, false,
                false);

        PageUtilities.emitSubpageTitle(htm, "Analytics", "analytics.svg");

        final String course = req.getParameter("course");

        if ("MATH101".equals(course)) {
            htm.sH(3).add("MATH 101: Math in the Social Sciences (GT-MA1)").eH(3);
        } else if ("MATH105".equals(course)) {
            htm.sH(3).add("MATH 105: Patterns of Phenomena (GT-MA1)").eH(3);
        } else {
            resp.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            final String path = site.site.path;
            resp.setHeader("Location",
                    path + (path.endsWith(Contexts.ROOT_PATH) ? "analytics.html" : "/analytics.html"));

//            htm.addln("<option value='MATH116'>MATH 116: Precalculus Supplement for Success in Math</option>");
//            htm.addln("<option value='MATH117'>MATH 117: College Algebra in Context I (GT-MA1)</option>");
//            htm.addln("<option value='MATH118'>MATH 118: College Algebra in Context II (GT-MA1)</option>");
//            htm.addln("<option value='MATH120'>MATH 120: College Algebra (GT-MA1)</option>");
//            htm.addln("<option value='MATH124'>MATH 124: Logarithmic and Exponential Functions (GT-MA1)</option>");
//            htm.addln("<option value='MATH125'>MATH 125: Numerical Trigonometry (GT-MA1)</option>");
//            htm.addln("<option value='MATH126'>MATH 126: Analytic Trigonometry (GT-MA1)</option>");
//            htm.addln("<option value='MATH127'>MATH 127: Precalculus (GT-MA1)</option>");
//            htm.addln("<option value='MATH141'>MATH 141: Calculus in Management Sciences (GT-MA1)</option>");
//            htm.addln("<option value='MATH155'>MATH 155: Calculus for Biological Scientists I (GT-MA1)</option>");
//            htm.addln("<option value='MATH156'>MATH 156: Mathematics for Computational Science I (GT-MA1)</option>");
//            htm.addln("<option value='MATH157'>MATH 157: One Year Calculus IA (GT-MA1)</option>");
//            htm.addln("<option value='MATH159'>MATH 159: One Year Calculus IB (GT-MA1)</option>");
//            htm.addln("<option value='MATH160'>MATH 160: Calculus for Physical Scientists I (GT-MA1)</option>");
//            htm.addln("<option value='MATH161'>MATH 161: Calculus for Physical Scientists II (GT-MA1)</option>");
//            htm.addln("<option value='MATH255'>MATH 255: Calculus for Biological Scientists II (GT-MA1)</option>");
//            htm.addln("<option value='MATH256'>MATH 256: Mathematics for Computational Science II (GT-MA1)</option>");
//            htm.addln("<option value='MATH261'>MATH 261: Calculus for Physical Scientists III (GT-MA1)</option>");
//            htm.addln("<option value='MATH340'>MATH 340: Intro to Ordinary Differential Equations (GT-MA1)</option>");
//            htm.addln("<option value='STAT100'>STAT 100: Statistical Literacy (GT-MA1)</option>");
        }

        PageUtilities.emitNavigationBar(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
