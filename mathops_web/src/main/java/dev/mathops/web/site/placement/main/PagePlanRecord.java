package dev.mathops.web.site.placement.main;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.logic.PlacementStatus;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawMpeCredit;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rec.LiveCsuCredit;
import dev.mathops.db.old.rec.LiveTransferCredit;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.mathplan.MathPlanLogic;
import dev.mathops.session.sitelogic.mathplan.data.StudentData;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Generates the page that presents majors, organized by math requirement.
 */
enum PagePlanRecord {
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

        final MathPlanLogic logic = new MathPlanLogic(site.getDbProfile());

        final String stuId = session.getEffectiveUserId();
        final StudentData data = logic.getStudentData(cache, stuId, session);

        final HtmlBuilder htm = new HtmlBuilder(8192);
        Page.startNofooterPage(htm, site.getTitle(), session, true, Page.NO_BARS, null, false, false);

        MPPage.emitMathPlanHeader(htm);

        if (data == null) {
            MPPage.emitNoStudentDataError(htm);
        } else {
            MathPlacementSite.emitLoggedInAs2(htm, session);
            htm.sDiv("inset2");

            htm.sDiv("shaded2left");
            htm.sP().add("Let's review what you've already done, so we can design a plan that's right for you.").eP();
            htm.eDiv(); // shaded2left

            htm.div("vgap");

            htm.sDiv("shaded2");
            emitHistory(htm, data, logic);

            htm.div("clear");
            htm.div("vgap");

            htm.addln("<form action='plan_view.html' method='post'>");
            htm.sDiv("center");
            htm.addln("<input type='hidden' name='cmd' value='", MathPlanLogic.EXISTING_PROFILE, "'/>");

            htm.addln("<button type='submit' class='btn'>Go to the next step...</button>");
            htm.eDiv();
            htm.addln("</form>");
            htm.eDiv(); // shaded2

            htm.eDiv(); // inset2
        }

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }

    /**
     * Emits the history.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param data  the student data
     * @param logic the site logic
     */
    private static void emitHistory(final HtmlBuilder htm, final StudentData data, final MathPlanLogic logic) {

        htm.sDiv("center");
        emitTransferCredit(htm, data, logic);
        emitPlacement(htm, data);
        emitEligibility(htm, data);
        htm.eDiv();
    }

    /**
     * Emits the transfer credit history box.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param data  the student data
     * @param logic the site logic
     */
    private static void emitTransferCredit(final HtmlBuilder htm, final StudentData data, final MathPlanLogic logic) {

        // Query student's transfer credit and print out
        final List<LiveTransferCredit> xfer = data.getLiveTransferCredit();
        final Map<String, RawCourse> courses = logic.getCourses();

        htm.sDiv("historyblock first");
        htm.sDiv("historycontent");
        htm.addln("<strong>Mathematics credit on your record</strong>");

        htm.div("vgap");
        htm.addln(Res.get(Res.HISTORY_XFER_CREDIT_SUB));

        if (xfer.isEmpty()) {
            htm.sDiv("center");
            htm.sDiv("boxed");
            htm.addln(Res.get(Res.HISTORY_NONE_ON_FILE));
            htm.eDiv();
            htm.eDiv();
        } else {
            htm.addln(" <ul style='list-style: none;'>");
            for (final LiveTransferCredit xferCredit : xfer) {
                final String courseId = xferCredit.courseId;
                if (courseId.startsWith("M 100")) {
                    // Hide these...
                    continue;
                }

                htm.add(" <li>");

                String cr = null;
                if (xferCredit.credits != null) {
                    cr = xferCredit.credits.toString();
                    if (cr.endsWith(".0")) {
                        cr = cr.substring(0, cr.length() - 2);
                    }
                }

                final RawCourse course = courses.get(courseId);
                if (course == null) {
                    if ("MATH1++1B".equals(courseId)) {
                        htm.add("AUCC 1B Math");
                    } else {
                        htm.add(xferCredit.courseId);
                    }
                } else {
                    htm.add(course.courseLabel);
                }

                if ("1".equals(cr)) {
                    htm.add(" (1 credit)");
                } else if (cr != null) {
                    htm.add(" (", cr, " credits)");
                }
                htm.addln("</li>");
            }
            htm.addln(" </ul>");
        }
        htm.eDiv();

        htm.sDiv("center");
        htm.addln("<a class='linksm' href='missing.html'>", Res.get(Res.HISTORY_ANY_COURSES_MISSING), "</a>");
        htm.eDiv();

        htm.eDiv();
    }

    /**
     * Emits placement results, if any.
     *
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data the student data
     */
    private static void emitPlacement(final HtmlBuilder htm, final StudentData data) {

        htm.sDiv("historyblock");
        htm.addln("<strong>Math Placement Results</strong>");
        htm.div("vgap");

        final PlacementStatus placementStat = data.placementStatus;

        if (placementStat.placementAttempted) {

            final List<RawMpeCredit> placement = data.getPlacementCredit();
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
                htm.addln(" <li>MATH 101, MATH 105, or STAT 100</li>");
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
     * @param htm  the {@code HtmlBuilder} to which to append
     * @param data the student data
     */
    private static void emitEligibility(final HtmlBuilder htm, final StudentData data) {

        htm.sDiv("historyblock last");
        htm.addln("<strong>Mathematics Eligibility</strong>");
        htm.div("vgap");

        htm.addln("You have satisfied the prerequisites for the following courses:");

        htm.addln("<ul style='list-style: none;'>");

        final Set<String> set = data.getCanRegisterFor();
        if (set == null || set.isEmpty()) {
            htm.addln("<li>", catLink("MATH 101"), " or ", catLink("MATH 105"), "</li>");
            htm.addln("<li>", catLink("STAT 100"), ", ", catLink("STAT 201"), ", or ", catLink("STAT 204"), "</li>");
        } else {
            // Remove courses for which the student has placement or credit already
            final List<RawMpeCredit> placement = data.getPlacementCredit();
            for (final RawMpeCredit p : placement) {
                set.remove(p.course);
            }
            final List<LiveCsuCredit> completed = data.getCompletedCourses();
            for (final LiveCsuCredit credit : completed) {
                set.remove(credit.courseId);
            }

            if (set.isEmpty()) {
                htm.addln("<li>", catLink("MATH 101"), " or ", catLink("MATH 105"), "</li>");
                htm.addln("<li>", catLink("STAT 100"), ", ", catLink("STAT 201"), ", or ", catLink("STAT 204"), "</li" +
                        ">");
            } else {
                final List<String> list = new ArrayList<>(set);
                Collections.sort(list);
                for (final String course : list) {
                    if ("M 101".equals(course)) {
                        htm.addln("<li>", catLink("MATH 101"), " or ", catLink("MATH 105"), "</li>");
                        htm.addln("<li>", catLink("STAT 100"), ", ", catLink("STAT 201"), ", or ", catLink("STAT 204"),
                                "</li>");
                    } else if (!"M 105".equals(course)) {
                        htm.add("<li>");
                        htm.add(catLink(course.replace("M ", "MATH ")));
                        htm.addln("</li>");
                    }
                }
            }
        }

        htm.addln("</ul>");

        htm.addln("<small>", "These courses each apply toward the All-University ",
                "Core Curriculum requirement in Quantitative Reasoning.", "</small>");

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
                plussed, "'>", course, "</a>");
    }
}
