package dev.mathops.web.host.placement.placement;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.mathplan.MathPlanConstants;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.mathplan.StudentStatus;
import dev.mathops.db.logic.mathplan.types.ECourse;
import dev.mathops.db.old.rawrecord.RawFfrTrns;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;

/**
 * Generates the page that presents the student's work history.
 */
enum PagePlanHistory {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final StudentMathPlan mathPlan = MathPlanLogic.queryPlan(cache, stuId);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        final String title = site.getTitle();
        Page.startNofooterPage(htm, title, session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        MathPlacementSite.emitLoggedInAs2(htm, session);
        htm.sDiv("inset2");

        htm.sDiv("shaded2left");
        htm.sP().add("Let's review what you've already done, so we can design a plan that's right for you.").eP();
        htm.eDiv();

        htm.div("vgap");

        htm.sDiv("shaded2");
        emitHistory(htm, mathPlan);

        htm.div("clear");
        htm.div("vgap");

        htm.addln("<form action='plan_view.html' method='post'>");
        htm.sDiv("center");
        htm.addln("<input type='hidden' name='cmd' value='", MathPlanConstants.EXISTING_PROFILE, "'/>");

        htm.addln("<button type='submit' class='btn'>Go to the next step...</button>");
        htm.eDiv();
        htm.addln("</form>");
        htm.eDiv(); // shaded2

        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Emits the history.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param mathPlan the student math plan
     */
    private static void emitHistory(final HtmlBuilder htm, final StudentMathPlan mathPlan) {

        htm.sDiv("center");
        emitTransferCredit(htm, mathPlan);
        emitPlacement(htm, mathPlan);
        emitEligibility(htm, mathPlan);
        htm.eDiv();
    }

    /**
     * Emits the transfer credit history box.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param mathPlan the student math plan
     */
    private static void emitTransferCredit(final HtmlBuilder htm, final StudentMathPlan mathPlan) {

        // Query student's transfer credit and print out
        final List<RawFfrTrns> xfer = mathPlan.stuStatus.transferCredit;
        final List<RawStcourse> courses = mathPlan.stuStatus.completedCourses;
        final Collection<String> courseIds = new ArrayList<>(10);
        for (final RawFfrTrns row : xfer) {
            courseIds.add(row.course);
        }
        for (final RawStcourse row : courses) {
            courseIds.add(row.course);
        }

        htm.sDiv("historyblock first");
        htm.sDiv("historycontent");
        htm.addln("<strong>Mathematics credit on your record</strong>");

        htm.div("vgap");
        htm.addln(Res.get(Res.HISTORY_XFER_CREDIT_SUB));

        boolean has002 = false;
        boolean has055 = false;
        boolean has093 = false;
        boolean has099 = false;
        boolean has117 = false;
        boolean has118 = false;
        boolean has124 = false;
        boolean has125 = false;
        boolean has126 = false;
        boolean has120 = false;
        boolean has127 = false;
        boolean has141 = false;
        boolean has155 = false;
        boolean has156 = false;
        boolean has160 = false;
        boolean has161 = false;
        boolean hasSTAT100 = false;
        boolean hasSTAT201 = false;
        boolean hasSTAT204 = false;
        boolean hasFIN200 = false;
        boolean has1B = false;
        boolean has101 = false;
        boolean has105 = false;

        for (final String id : courseIds) {
            if (id.endsWith("+1B") || id.endsWith("+B")) {
                has1B = true;
            } else if ("FIN200".equals(id)) {
                hasFIN200 = true;
            } else if ("M 002".equals(id)) {
                has002 = true;
            } else if ("M 055".equals(id)) {
                has055 = true;
            } else if ("M 093".equals(id)) {
                has093 = true;
            } else if ("M 099".equals(id)) {
                has099 = true;
            } else if ("M 117".equals(id) || "MATH 117".equals(id)) {
                has117 = true;
            } else if ("M 118".equals(id) || "MATH 118".equals(id)) {
                has118 = true;
            } else if ("M 124".equals(id) || "MATH 124".equals(id)) {
                has124 = true;
            } else if ("M 125".equals(id) || "MATH 125".equals(id)) {
                has125 = true;
            } else if ("M 126".equals(id) || "MATH 126".equals(id)) {
                has126 = true;
            } else if ("M 120".equals(id) || "MATH 120".equals(id)) {
                has120 = true;
            } else if ("M 127".equals(id) || "MATH 127".equals(id)) {
                has127 = true;
            } else if ("M 101".equals(id) || "MATH 101".equals(id) || "M 130".equals(id)
                       || "MATH 130".equals(id)) {
                has101 = true;
            } else if ("M 105".equals(id) || "MATH 105".equals(id)) {
                has105 = true;
            } else if ("M 141".equals(id) || "MATH 141".equals(id)) {
                has141 = true;
            } else if ("M 155".equals(id) || "MATH 155".equals(id)) {
                has155 = true;
            } else if ("M 155".equals(id) || "MATH 155".equals(id)) {
                has155 = true;
            } else if ("M 156".equals(id) || "MATH 156".equals(id)) {
                has156 = true;
            } else if ("M 160".equals(id) || "MATH 160".equals(id) || "M 159".equals(id)
                       || "MATH 159".equals(id)) {
                has160 = true;
            } else if ("M 161".equals(id) || "MATH 161".equals(id)) {
                has161 = true;
            } else if ("STAT100".equals(id)) {
                hasSTAT100 = true;
            } else if ("STAT201".equals(id)) {
                hasSTAT201 = true;
            } else if ("STAT204".equals(id)) {
                hasSTAT204 = true;
            }
        }

        final boolean hasAny = has002 || has055 || has093 || has099 || has117 || has118 || has124 || has125 || has126
                               || has120 || has127 || has141 || has155 || has156 || has160 || has161 || hasSTAT100
                               || hasSTAT201 || hasSTAT204 || hasFIN200 || has1B || has101 || has105;

        if (hasAny) {
            htm.addln(" <ul style='list-style: none;'>");
            if (has1B) {
                htm.add("<li>General AUCC 1B Math credit</li>");
            } else if (has002) {
                htm.add("<li>MATH 002</li>");
            } else if (has055) {
                htm.add("<li>MATH 055</li>");
            } else if (has093) {
                htm.add("<li>MATH 093</li>");
            } else if (has099) {
                htm.add("<li>MATH 099</li>");
            } else if (has101) {
                htm.add("<li>MATH 101</li>");
            } else if (has105) {
                htm.add("<li>MATH 105</li>");
            } else if (has117) {
                htm.add("<li>MATH 117</li>");
            } else if (has118) {
                htm.add("<li>MATH 118</li>");
            } else if (has124) {
                htm.add("<li>MATH 124</li>");
            } else if (has120) {
                htm.add("<li>MATH 120</li>");
            } else if (has125) {
                htm.add("<li>MATH 125</li>");
            } else if (has126) {
                htm.add("<li>MATH 126</li>");
            } else if (has127) {
                htm.add("<li>MATH 127</li>");
            } else if (has141) {
                htm.add("<li>MATH 141</li>");
            } else if (has155) {
                htm.add("<li>MATH 155</li>");
            } else if (has156) {
                htm.add("<li>MATH 156</li>");
            } else if (has160) {
                htm.add("<li>MATH 160</li>");
            } else if (has161) {
                htm.add("<li>MATH 161</li>");
            } else if (hasSTAT100) {
                htm.add("<li>STAT 100</li>");
            } else if (hasSTAT201) {
                htm.add("<li>STAT 201</li>");
            } else if (hasSTAT204) {
                htm.add("<li>STAT 204</li>");
            } else if (hasFIN200) {
                htm.add("<li>FIN 200</li>");
            }
            htm.addln(" </ul>");
        } else {
            htm.sDiv("center");
            htm.sDiv("boxed");
            final String msg = Res.get(Res.HISTORY_NONE_ON_FILE);
            htm.addln(msg);
            htm.eDiv();
            htm.eDiv();
        }
        htm.eDiv(); // historycontent

        htm.sDiv("center");
        final String label = Res.get(Res.HISTORY_ANY_COURSES_MISSING);
        htm.addln("<a class='linksm' href='missing.html'>", label, "</a>");
        htm.eDiv();

        htm.eDiv(); // historyblock first
    }

    /**
     * Emits placement results, if any.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param mathPlan the student math plan
     */
    private static void emitPlacement(final HtmlBuilder htm, final StudentMathPlan mathPlan) {

        htm.sDiv("historyblock");
        htm.addln("<strong>Math Placement Results</strong>");
        htm.div("vgap");

        if (mathPlan.stuStatus.isPlacementCompleted()) {
            final List<RawMpeCredit> placement = mathPlan.stuStatus.placementCredit;
            boolean cleared117 = false;
            final Collection<String> placed = new TreeSet<>();
            final Collection<String> credit = new TreeSet<>();

            boolean did120 = false;
            for (final RawMpeCredit p : placement) {
                final String courseId = p.course;

                if (RawRecordConstants.M100C.equals(courseId)) {
                    cleared117 = true;
                } else if ("M 100A".equals(courseId)) {
                    cleared117 = true;
                } else if (RawRecordConstants.M117.equals(courseId)) {
                    placed.add("MATH 117");
                    if ("C".equals(p.examPlaced)) {
                        placed.add("MATH 117");
                    }
                } else if (RawRecordConstants.M118.equals(courseId)) {
                    placed.add("MATH 118");
                    if ("C".equals(p.examPlaced)) {
                        credit.add("MATH 118");
                    }
                } else if ("M 120".equals(courseId) || "M 120A".equals(courseId) || "M 121".equals(courseId)) {

                    if (!did120) {
                        did120 = true;
                        placed.add("MATH 117");
                        placed.add("MATH 118");

                        if ("C".equals(p.examPlaced)) {
                            credit.add("MATH 117");
                            credit.add("MATH 118");
                        }
                    }
                } else if (RawRecordConstants.M124.equals(courseId)) {
                    placed.add("MATH 124");
                    if ("C".equals(p.examPlaced)) {
                        credit.add("MATH 124");
                    }
                } else if (RawRecordConstants.M125.equals(courseId)) {
                    placed.add("MATH 125");
                    if ("C".equals(p.examPlaced)) {
                        credit.add("MATH 125");
                    }
                } else if (RawRecordConstants.M126.equals(courseId)) {
                    placed.add("MATH 126");
                    if ("C".equals(p.examPlaced)) {
                        credit.add("MATH 126");
                    }
                }
            }

            Collections.sort(placement);

            if (placed.isEmpty() && credit.isEmpty()) {
                // Must be cleared for something...
                htm.addln("You are cleared to register for:");
                htm.addln(" <ul style='list-style: none;'>");
                htm.addln(" <li>MATH&nbsp;101, MATH&nbsp;105, MATH&nbsp;112, STAT&nbsp;100, STAT&nbsp;201, STAT&nbsp;204, and FIN&nbsp;200</li>");
                if (cleared117) {
                    htm.addln(" <li>MATH 117</li>");
                }
                htm.addln(" </ul>");
            }

            if (!placed.isEmpty()) {
                htm.div("vgap");
                htm.addln("You have placed out of:");
                htm.addln(" <ul style='list-style: none;'>");
                for (final String label : placed) {
                    htm.addln(" <li>", label, "</li>");
                }
                htm.addln(" </ul>");
            }

            if (!credit.isEmpty()) {
                htm.div("vgap");
                htm.addln("You have earned Challenge credit in:");
                htm.addln(" <ul>");
                for (final String label : credit) {
                    htm.addln(" <li>", label, "</li>");
                }
                htm.addln(" </ul>");
            }
            htm.div("vgap");

        } else {
            htm.addln("You have not completed the Math&nbsp;Placement&nbsp;Tool.");
        }

        htm.eDiv();
    }

    /**
     * Emits eligibility for math courses.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param mathPlan the student math plan
     */
    private static void emitEligibility(final HtmlBuilder htm, final StudentMathPlan mathPlan) {

        htm.sDiv("historyblock last");
        htm.addln("<strong>Mathematics Eligibility</strong>");
        htm.div("vgap");

        htm.addln("You have satisfied the prerequisites for the following courses:");

        htm.addln("<ul style='list-style: none;'>");

        final StudentStatus status = mathPlan.stuStatus;

        if (status.isEligible(ECourse.M_117) && !status.hasCompleted(ECourse.M_117)) {
            htm.addln("<li>", catLink("MATH 117"), "</li>");
        }
        if (status.isEligible(ECourse.M_118) && !status.hasCompleted(ECourse.M_118)) {
            htm.addln("<li>", catLink("MATH 118"), "</li>");
        }
        if (status.isEligible(ECourse.M_124) && !status.hasCompleted(ECourse.M_124)) {
            htm.addln("<li>", catLink("MATH 124"), "</li>");
        }
        if (status.isEligible(ECourse.M_125) && !status.hasCompleted(ECourse.M_125)) {
            htm.addln("<li>", catLink("MATH 125"), "</li>");
        }
        if (status.isEligible(ECourse.M_126) && !status.hasCompleted(ECourse.M_126)) {
            htm.addln("<li>", catLink("MATH 126"), "</li>");
        }
        if (status.isEligible(ECourse.M_141) && !status.hasCompleted(ECourse.M_141)) {
            htm.addln("<li>", catLink("MATH 141"), "</li>");
        }
        if (status.isEligible(ECourse.M_155) && !status.hasCompleted(ECourse.M_155)) {
            htm.addln("<li>", catLink("MATH 155"), "</li>");
        }
        if (status.isEligible(ECourse.M_156) && !status.hasCompleted(ECourse.M_156)) {
            htm.addln("<li>", catLink("MATH 156"), "</li>");
        }
        if (status.isEligible(ECourse.M_160) && !status.hasCompleted(ECourse.M_160)) {
            htm.addln("<li>", catLink("MATH 160"), "</li>");
        }

        // Show the AUCC core courses regardless
        htm.addln("<li>", catLink("MATH 101"), ", ", catLink("MATH 105"), " or ", catLink("MATH 112"), "</li>");
        htm.addln("<li>", catLink("FIN 200"), ", ", catLink("STAT 100"), ", ", catLink("STAT 201"), ", or ",
                catLink("STAT 204"), "</li>");
        htm.addln("</ul>");

        htm.addln("<small>These courses each apply toward the All-University Core Curriculum 1B requirement in ",
                "Quantitative Reasoning.</small>");

        htm.eDiv();
    }

    /**
     * Creates a catalog link for a course.
     *
     * @param course the course
     * @return the link
     */
    private static String catLink(final String course) {

        final String plussed = course.replace(' ', '+');

        return SimpleBuilder.concat("<a target='_blank' href='https://catalog.colostate.edu/search/?search=",
                plussed, "' style='white-space:nowrap;'>", course, "</a>");
    }
}
