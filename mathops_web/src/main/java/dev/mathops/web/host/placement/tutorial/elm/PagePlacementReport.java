package dev.mathops.web.host.placement.tutorial.elm;

import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.placement.PlacementStatus;
import dev.mathops.db.logic.tutorial.ELMTutorialStatus;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
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
     * @param status  the student status with respect to the ELM Tutorial
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ElmTutorialSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final ELMTutorialStatus status) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, "Entry Level Mathematics Tutorial",
                "/elm-tutorial/home.html", Page.ADMIN_BAR, null, false, true);

        htm.sDiv("menupanel");
        TutorialMenu.buildMenu(cache, session, status, htm);
        htm.sDiv("panel");

        final PlacementStatus pstatus = new PlacementLogic(cache, session.getEffectiveUserId(), status.student.aplnTerm,
                session.getNow()).status;

        doPlacementReport(pstatus, status.student, htm);

        htm.eDiv(); // (end "panel" div)
        htm.eDiv(); // (end "menupanel" div)

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Creates the HTML of the placement report.
     *
     * @param status  the placement tool status for the student
     * @param student the student
     * @param htm     the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doPlacementReport(final PlacementStatus status, final RawStudent student,
                                          final HtmlBuilder htm) {

        htm.sDiv("indent11");

        if (status.attemptsUsed == 0) {

            htm.sP("indent11", "style='padding-left:32px;'");
            htm.add("<img src='/images/orange2.png' style='margin:0 0 0 -32px; padding-right:10px;'/>");
            htm.addln(" You have not yet completed the Math Placement Tool.");
            htm.eP();

            htm.sP("indent11");
            htm.addln(" Visit our <a href='https://placement.", Contexts.DOMAIN,
                    "/welcome/welcome.html'>Math Placement Directory</a>  for information on using the Math ",
                    "Placement Tool.");
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
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You are cleared to register for:");
                }

                if (comma) {
                    htm.add(", ");
                }

                comma = true;
                htm.add("<strong>", course, "</strong>");

                if (!("MATH 101".equals(course) || "MATH 105".equals(course) || "STAT 100".equals(course)
                      || ("STAT 201").equals(course) || "STAT 204".equals(course))) {
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
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You have placed out of:");
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

            // Display list of courses the student has earned credit for
            heading = true;
            comma = false;

            for (final String course : status.earnedCreditFor) {

                if (heading) {
                    heading = false;
                    htm.sP("indent11");
                    htm.addln(" <img src='/images/check.png'/> &nbsp; You have earned placement credit for:");
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
                htm.addln("MATH 101, MATh 105, STAT 100, STAT 201, and STAT 204 do not satisfy the degree ",
                        "requirements for some majors.  Consult the ",
                        "<a href='https://www.catalog.colostate.edu/'>University Catalog</a> to see if these courses ",
                        "are appropriate for your desired major.");
                htm.eP();

                htm.addln("<p class='indent11' style='margin-top:10pt;'>");
                htm.addln("<img style='position:relative; top:-1px' src='/images/info.png'/> &nbsp; ");
                htm.addln("<span class='blue'>What should I do now?</span>");
                htm.eP();

                if (status.attemptsRemaining > 0) {
                    htm.sP("indent33");
                    htm.add("To become eligible to register for just MATH 117, MATH 120, or MATH 127, you may ",
                            "complete the ELM Tutorial and take the ELM Exam.");
                    htm.eP();

                    htm.sP("indent33");
                    htm.add("Alternatively, to become eligible to register for additional mathematics courses, ",
                            "including MATH 117, MATH 120, or MATH 127, you could use the ",
                            "<a href='https://placement.", Contexts.DOMAIN, "/welcome/placement.html' class='ulink'>",
                            "Math Placement Review materials</a> to study, and try the Math Placement Tool again.");
                } else {
                    htm.sP("indent33");
                    htm.addln(" To become eligible to register for MATH 117, MATH 120, or MATH 127, complete the ",
                            "ELM Tutorial and take the ELM Exam.");
                }
                htm.eP();
            }

            // Only show adviser comment if the student is known to have an adviser
            if (student != null && student.adviserEmail != null) {
                htm.div("vgap");
                htm.sP("indent11");
                htm.addln("You should check with your adviser for complete information concerning your math ",
                        "requirements.");
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
