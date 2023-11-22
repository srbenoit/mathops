package dev.mathops.web.site.course;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.PlacementLogic;
import dev.mathops.db.logic.PlacementStatus;
import dev.mathops.db.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Generates the content of the home page.
 */
enum PagePlacementReport {
    ;

    /**
     * Generates the home page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null,
                Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        final PlacementStatus pstatus = new PlacementLogic(cache, session.getEffectiveUserId(),
                logic.data.studentData.getStudent().aplnTerm, session.getNow()).status;

        doPlacementReport(pstatus, logic.data.studentData.getStudent(), htm);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML,
                htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Creates the HTML of the placement report.
     *
     * @param status  the placement tool status for the student
     * @param student the student record
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doPlacementReport(final PlacementStatus status, final RawStudent student,
                                          final HtmlBuilder htm) {

        htm.sDiv("indent11");

        if (status.attemptsUsed == 0) {

            htm.sP("indent11", "style='padding-left:32px;'");
            htm.add("<img src='/images/orange2.png' ",
                    "style='margin:0 0 0 -32px; padding-right:10px;'/>");
            htm.addln(" You have not yet completed the Math Placement Tool.");
            htm.eP();

            htm.sP("indent11");
            htm.addln(" Visit our");
            htm.addln(" <a class='ulink' href='https://placement.", Contexts.DOMAIN,
                    "/welcome/welcome.html'>Math Placement web site</a>");
            htm.addln(" for information on using the Math Placement Tool.");
            htm.eP();
        } else {
            appendYouHaveTaken(htm, status);

            htm.div("vgap");

            htm.addln("<fieldset>");
            htm.addln("<legend>Your best results so far:</legend>");

            // Display list of courses the student is qualified to register for
            boolean heading = true;
            boolean comma = false;
            boolean noneBut101 = true;

            final List<String> list = new ArrayList<>(status.clearedFor);
            Collections.sort(list);
            for (final String course : list) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp;");
                    htm.addln("You are cleared to register for:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");

                if ((!"MATH 101".equals(course) && !"STAT 100".equals(course))) {
                    noneBut101 = false;
                }
            }

            if (!heading) {
                htm.eP();
            }

            // Display list of courses the student has placed out of
            heading = true;
            comma = false;

            for (final String course : status.placedOutOf) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp;");
                    htm.addln("You have placed out of:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");
                noneBut101 = false;
            }

            if (!heading) {
                htm.eP();
            }

            // Display list of courses the student has earned credit for of
            heading = true;
            comma = false;

            for (final String course : status.earnedCreditFor) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp;");
                    htm.addln("You have earned placement credit for:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");
                noneBut101 = false;
            }

            if (!heading) {
                htm.eP();
            }

            if (noneBut101) {
                htm.addln("<p class='indent11' style='margin-bottom:0;margin-top:10pt;'>");
                htm.addln("<img style='position:relative; top:-1px' src='/images/error.png'/> &nbsp; ");
                htm.addln("MATH 101 and STAT 100 do not satisfy the degree requirements for many ",
                        "majors.  Consult the <a class='ulink' href='https://www.catalog.colostate.edu/'>University ",
                        " Catalog</a> to see if these courses are appropriate for your desired major.");
                htm.eP();

                if (!status.transferSatisfied) {
                    htm.addln("<p class='indent11' style='margin-bottom:0;margin-top:10pt;'>");
                    htm.addln("<img style='position:relative; top:-1px' src='/images/error.png'/> &nbsp; ");
                    htm.addln("Transfer Students: This placement result does not satisfy the ",
                            "<a class='ulink' href='https://admissions.colostate.edu/requirementinmathematics'>",
                            "admission requirement in mathematics</a>.  You should complete ",
                            "units 1-3 of the ELM Tutorial if you need to satisfy this admission ",
                            "requirement.");
                    htm.eP();
                }

                htm.addln("<p class='indent11' style='margin-top:10pt;'>");
                htm.addln("<img style='position:relative; top:-1px' src='/images/info.png'/> &nbsp; ");
                htm.addln("<span class='blue'>What should I do now?</span>");
                htm.eP();

                if (status.attemptsRemaining > 0) {
                    htm.sP("indent33");
                    htm.add("To become eligible to register for just MATH 117, MATH 120, or ",
                            "MATH 127, you may complete the ELM Tutorial and take the ELM Exam.");
                    htm.eP();

                    htm.sP("indent33");
                    htm.add("Alternatively, to become eligible to register for additional ",
                            "mathematics courses, including MATH 117, MATH 120, or MATH 127, you ",
                            "could use the <a class='ulink' href='https://placement.", Contexts.DOMAIN,
                            "/mpe-review/index.html' ",
                            "class='ulink'>Math Placement Review materials</a> to study, and ",
                            "try the Math Placement Tool again.");
                } else {
                    htm.sP("indent33");
                    htm.addln(" To become eligible to register for MATH 117117, MATH 120, or ",
                            "MATH 127, complete the ELM Tutorial and take the ELM Exam.");
                }
                htm.eP();
            }

            // Only show adviser comment if the student is known to have an adviser
            if (student != null && student.adviserEmail != null) {
                htm.div("vgap");
                htm.sP("indent11");
                htm.addln(" You should check with your adviser for complete information ",
                        "concerning your math requirements.");
                htm.eP();
            }

            htm.addln("</fieldset>");
        }

        htm.eDiv(); // indent11
    }

    /**
     * Appends a message describing the number of times the student has completed the Math Placement Tool in both
     * proctored and unproctored settings, and how many tries are available in each.
     *
     * @param htm    the {@code HtmlBuilder} to which to append
     * @param status the student's placement status
     */
    private static void appendYouHaveTaken(final HtmlBuilder htm, final PlacementStatus status) {

        htm.sP("indent11", "style='padding-left:32px;'");
        htm.add("<img src='/images/orange2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

        if (status.attemptsUsed == 0) {
            htm.addln(" You have not yet completed the Math Placement Tool.");
        } else {
            htm.add(" You have completed the Math Placement Tool ");
            appendNumTimes(htm, status.attemptsUsed);
            htm.add(". ");

            if (status.attemptsRemaining == 0) {
                htm.addln(" You have no attempts remaining.");
            } else if (status.attemptsRemaining == 1) {
                htm.addln(" You have 1 attempt remaining.");
            } else {
                htm.addln(" You have " + status.attemptsRemaining + " attempts remaining.");
            }
        }

        htm.eP();
    }

    /**
     * Appends a string telling the user how many times they have attempted the exam or how many attempts they may use.
     * Based on the number of times passed in, one of the following is appended to the {@code HtmlBuilder}:
     *
     * <pre>
     *   0 times
     *   1 time
     *   N times (where N is a number larger than 1)
     * </pre>
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param numTimes the number of times
     */
    private static void appendNumTimes(final HtmlBuilder htm, final int numTimes) {

        if (numTimes == 1) {
            htm.add("one time");
        } else {
            htm.add(Integer.toString(numTimes), " times");
        }
    }
}
