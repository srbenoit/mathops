package dev.mathops.web.site.cfm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates a page with analytics.
 */
enum PageAnalytics {
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

        htm.sDiv("inset2");

        htm.addln("<form method='get' action='course-report.html' ",
                "style='background:#f2f2f2; padding:10px; border:1px solid #ddd;'>");
        htm.sH(3).add("Analyses of individual foundational Mathematics courses:").eH(3);
        htm.sDiv("indent");
        htm.addln("<label for='course-select'>Choose a Course:</label> <select name='course' id='course-select'>");
        htm.addln("  <option value='''>--Please choose a course--</option>");
        htm.addln("<option value='MATH117'>MATH 117</option>");
        htm.addln("<option value='MATH118'>MATH 118</option>");
        htm.addln("</select> ");
        htm.addln("<button type='submit'>Go</button>");
        htm.eDiv(); // indent
        htm.addln("</form>");

        htm.div("vgap2");

        htm.addln("<form method='get' action='majors-report.html' ",
                "style='background:#f2f2f2; padding:10px; border:1px solid #ddd;'>");

        htm.sH(3).add("Analyses of outcomes by Major").eH(3);
        htm.sDiv("indent");

        htm.sP().add("This analysis is designed to assess the impact of non-success in MATH courses on student ",
                "persistence in majors, and to examine enrollment and success rates in individual MATH courses ",
                "for students in a major or concentration.").eP();

        htm.addln("<details><summary>Expand for a description of the methodology...</summary>");
        htm.sDiv(null, "style='font-size:smaller;'");
        htm.sP().add("For each major, and for each set of concentrations with consistent MATH requirements, an ",
                "analysis was performed over all students who took a MATH course in a semester while having one of ",
                "the corresponding programs declared. Data was collected over the preceding ten years.").eP();
        htm.sP().add("The students found for each group were then classified into four categories:").eP();
        htm.addln("<ul>");
        htm.addln("<li>Those that persisted in program until the MATH requirements were met,</li>");
        htm.addln("<li>those who remained in program but never finished the MATH requirements,</li>");
        htm.addln("<li>those who finished program requirements but exited the program (were no longer in the program ",
                "when the last MATH course was taken), and</li>");
        htm.addln("<li>those who exited the program without finishing requirements.</li>");
        htm.addln("</ul>");
        htm.sP().add("In addition, the number of semesters in which they took a MATH course, and the number of ",
                "credits of MATH taken were compared to the major’s expectations as listed in the University Catalog, ",
                "and the number of credits of MATH with a DFW result was calculated.  DFW outcomes (raw counts and ",
                "percentage) for foundational MATH courses were computed for those participating in the major. ",
                "DFW percentages are not reported when the number of students attempting a course was 5 or less.").eP();
        htm.sP().add(
                "Finally, within the set of students who did not complete the program, the last MATH course that had ",
                "a DFW outcome was found. Then, for each such course, the percentage of this sub-population that had ",
                "a DFW result in their last semester with any MATH class is found. This may indicate that the failure ",
                "in the MATH course caused the failure to complete the major requirements (for example, by causing a ",
                "change in major), but such a change could be due to many other factors, so these percentages should ",
                "not be interpreted as purely causal.").eP();
        htm.eDiv(); // small
        htm.addln("</details>");
        htm.div("vgap");

        htm.addln("<label for='major-select'>Choose a Major:</label> <select name='major' id='major-select'>");
        htm.addln("  <option value='''>--Please choose a major--</option>");
        htm.addln("<option value='AGBU'>Agricultural Business</option>");
        htm.addln("<option value='AGED'>Agricultural Education</option>");
        htm.addln("</select> ");
        htm.addln("<button type='submit'>Go</button>");
        htm.eDiv(); // indent
        htm.addln("</form>");

        htm.eDiv(); // inset2

        PageUtilities.emitNavigationBar(htm);

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }
}
