package dev.mathops.web.site.cfm;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import oracle.net.ns.Communication;

import javax.print.attribute.standard.Media;
import javax.xml.crypto.Data;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.function.Consumer;

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
        htm.addln("<option value='MATH101'>MATH 101: Math in the Social Sciences (GT-MA1)</option>");
        htm.addln("<option value='MATH105'>MATH 105: Patterns of Phenomena (GT-MA1)</option>");
        htm.addln("<option value='MATH116'>MATH 116: Precalculus Supplement for Success in Math</option>");
        htm.addln("<option value='MATH117'>MATH 117: College Algebra in Context I (GT-MA1)</option>");
        htm.addln("<option value='MATH118'>MATH 118: College Algebra in Context II (GT-MA1)</option>");
        htm.addln("<option value='MATH120'>MATH 120: College Algebra (GT-MA1)</option>");
        htm.addln("<option value='MATH124'>MATH 124: Logarithmic and Exponential Functions (GT-MA1)</option>");
        htm.addln("<option value='MATH125'>MATH 125: Numerical Trigonometry (GT-MA1)</option>");
        htm.addln("<option value='MATH126'>MATH 126: Analytic Trigonometry (GT-MA1)</option>");
        htm.addln("<option value='MATH127'>MATH 127: Precalculus (GT-MA1)</option>");
        htm.addln("<option value='MATH141'>MATH 141: Calculus in Management Sciences (GT-MA1)</option>");
        htm.addln("<option value='MATH155'>MATH 155: Calculus for Biological Scientists I (GT-MA1)</option>");
        htm.addln("<option value='MATH156'>MATH 156: Mathematics for Computational Science I (GT-MA1)</option>");
        htm.addln("<option value='MATH157'>MATH 157: One Year Calculus IA (GT-MA1)</option>");
        htm.addln("<option value='MATH159'>MATH 159: One Year Calculus IB (GT-MA1)</option>");
        htm.addln("<option value='MATH160'>MATH 160: Calculus for Physical Scientists I (GT-MA1)</option>");
        htm.addln("<option value='MATH161'>MATH 161: Calculus for Physical Scientists II (GT-MA1)</option>");
        htm.addln("<option value='MATH255'>MATH 255: Calculus for Biological Scientists II (GT-MA1)</option>");
        htm.addln("<option value='MATH256'>MATH 256: Mathematics for Computational Science II (GT-MA1)</option>");
        htm.addln("<option value='MATH261'>MATH 261: Calculus for Physical Scientists III (GT-MA1)</option>");
        htm.addln("<option value='MATH340'>MATH 340: Intro to Ordinary Differential Equations (GT-MA1)</option>");
        htm.addln("<option value='STAT100'>STAT 100: Statistical Literacy (GT-MA1)</option>");
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
        htm.addln("  <option value='AGBU'>Agricultural Business</option>");
        htm.addln("  <option value='AGED'>Agricultural Education</option>");
        htm.addln("  <option value='ANIM'>Animal Science</option>");
        htm.addln("  <option value='ANTH'>Anthropology</option>");
        htm.addln("  <option value='APAM'>Apparel and Merchandising</option>");
        htm.addln("  <option value='ARTM'>Art</option>");
        htm.addln("  <option value='BCHM'>Biochemistry</option>");
        htm.addln("  <option value='BLSC'>Biological Science</option>");
        htm.addln("  <option value='BIOM'>Biomedical Sciences</option>");
        htm.addln("  <option value='BUSA'>Business Administration</option>");
        htm.addln("  <option value='CBEG'>Chemical And Biological Engineering</option>");
        htm.addln("  <option value='CHEM'>Chemistry</option>");
        htm.addln("  <option value='CIVE'>Civil Engineering</option>");
        htm.addln("  <option value='CMST'>Communication Studies</option>");
        htm.addln("  <option value='CPEG'>Computer Engineering</option>");
        htm.addln("  <option value='CPSC'>Computer Science</option>");
        htm.addln("  <option value='CTMG'>Construction Management</option>");
        htm.addln("  <option value='DANC'>Dance</option>");
        htm.addln("  <option value='DSCI'>Data Science</option>");
        htm.addln("  <option value='ECHE'>Early Childhood Education</option>");
        htm.addln("  <option value='ECON'>Economics</option>");
        htm.addln("  <option value='ECSS'>Ecosystem Science and Sustainability</option>");
        htm.addln("  <option value='ELEG'>Electrical Engineering</option>");
        htm.addln("  <option value='ENGL'>English</option>");
        htm.addln("  <option value='ENRE'>Environmental and Natural Resource Economics</option>");
        htm.addln("  <option value='ENVE'>Environmental Engineering</option>");
        htm.addln("  <option value='ENHR'>Environmental Horticulture</option>");
        htm.addln("  <option value='EQSC'>Equine Science</option>");
        htm.addln("  <option value='ETST'>Ethnic Studies</option>");
        htm.addln("  <option value='FACS'>Family and Consumer Sciences</option>");
        htm.addln("  <option value='FWCB'>Fish, Wildlife, and Conservation Biology</option>");
        htm.addln("  <option value='FRRS'>Forest and Rangeland Stewardship</option>");
        htm.addln("  <option value='GEOG'>Geography</option>");
        htm.addln("  <option value='GEOL'>Geology</option>");
        htm.addln("  <option value='HAES'>Health and Exercise Science</option>");
        htm.addln("  <option value='HIST'>History</option>");
        htm.addln("  <option value='HORT'>Horticulture</option>");
        htm.addln("  <option value='HEMG'>Hospitality and Event Management</option>");
        htm.addln("  <option value='HDFS'>Human Development and Family Studies</option>");
        htm.addln("  <option value='HDNR'>Human Dimensions of Natural Resources</option>");
        htm.addln("  <option value='ILAR'>Interdisciplinary Liberal Arts</option>");
        htm.addln("  <option value='IARD'>Interior Architecture and Design</option>");
        htm.addln("  <option value='INST'>International Studies</option>");
        htm.addln("  <option value='JAMC'>Journalism and Media Communication</option>");
        htm.addln("  <option value='LDAR'>Landscape Architecture</option>");
        htm.addln("  <option value='LLAC'>Languages, Literatures, and Cultures</option>");
        htm.addln("  <option value='MATH'>Mathematics</option>");
        htm.addln("  <option value='MECH'>Mechanical Engineering</option>");
        htm.addln("  <option value='MUSC'>Music</option>");
        htm.addln("  <option value='NRTM'>Natural Resource Tourism</option>");
        htm.addln("  <option value='NRMG'>Natural Resources Management</option>");
        htm.addln("  <option value='NSCI'>Natural Sciences</option>");
        htm.addln("  <option value='NERO'>Neuroscience</option>");
        htm.addln("  <option value='PHIL'>Philosophy</option>");
        htm.addln("  <option value='PHYS'>Physics</option>");
        htm.addln("  <option value='POLS'>Political Science</option>");
        htm.addln("  <option value='PSYC'>Psychology</option>");
        htm.addln("  <option value='RECO'>Restoration Ecology</option>");
        htm.addln("  <option value='SOWK'>Social Work</option>");
        htm.addln("  <option value='SOCR'>Soil and Crop Sciences</option>");
        htm.addln("  <option value='STAT'>Statistics</option>");
        htm.addln("  <option value='THTR'>Theatre</option>");
        htm.addln("  <option value='WSSS'>Watershed Science and Sustainability</option>");
        htm.addln("  <option value='WGST'>Women's and Gender Studies</option>");
        htm.addln("  <option value='ZOOL'>Zoology</option>");
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
