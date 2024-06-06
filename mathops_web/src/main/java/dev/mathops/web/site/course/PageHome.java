package dev.mathops.web.site.course;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.old.rawlogic.RawAdminHoldLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawrecord.RawAdminHold;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.web.site.AbstractPageSite;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generates the content of the home page for a course site.
 */
enum PageHome {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @param logic       the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final StudentData studentData, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                      final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, null, null, Page.ADMIN_BAR | Page.USER_DATE_BAR,
                null, false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(studentData, site, session, logic, htm);
        htm.sDiv("panelu");

        emitContent(studentData, htm, session, logic);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(studentData, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates page content.
     *
     * @param studentData the student data object
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param session     the login session
     * @param logic       the course site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitContent(final StudentData studentData, final HtmlBuilder htm,
                                    final ImmutableSessionInfo session,
                                    final CourseSiteLogic logic) throws SQLException {

        final String effectiveUserId = session.getEffectiveUserId();
        final RawStudent student = studentData.getStudentRecord();

        // If there are fatal holds, go to the home page, which will show those
        if (student == null || "F".equals(student.sevAdminHold)) {
            emitBodyText(studentData, session, htm, logic);
        } else {
            // If the pace order values are not valid, force the schedule page
            final List<RawStcourse> pacedReg = studentData.getPacedRegistrations();

            // Filter courses to only those this website supports
            final List<RawStcourse> filtered;
            final List<RawStcourse> tempList = new ArrayList<>(10);

            for (final RawStcourse reg : pacedReg) {
                final String regCourseId = reg.course;
                final String regSect = reg.sect;
                final TermKey regTerm = "Y".equals(reg.iInProgress) ? reg.iTermKey : reg.termKey;

                final RawCsection csect = logic.data.contextData.getCourseSection(regCourseId, regSect, regTerm);
                if (csect != null) {
                    tempList.add(reg);
                }
            }
            filtered = new ArrayList<>(tempList);
            tempList.clear();

            if (filtered.isEmpty() || PageSchedule.sortPaceOrder(filtered)) {
                emitBodyText(studentData, session, htm, logic);
            } else {
                PageSchedule.doScheduleContent(studentData, logic, htm);
            }
        }
    }

    /**
     * Creates the home page HTML.
     *
     * @param studentData the student data object
     * @param session     the user's login session information
     * @param htm         the {@code HtmlBuilder} to which to append the HTML
     * @param logic       the course site logic
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitBodyText(final StudentData studentData, final ImmutableSessionInfo session,
                                     final HtmlBuilder htm, final CourseSiteLogic logic) throws SQLException {

        final String screenName = session.getEffectiveScreenName();
        htm.sH(2, "center").add("Welcome, ", screenName).eH(2);

        if (logic.course != null && logic.course.blocked) {
            final boolean hasPastDeadline = !logic.course.pastDeadlineCourses.isEmpty();

            if (hasPastDeadline) {
                Page.emitFile(htm, "precalc_instr_must_withdraw_past.txt");
            } else {
                Page.emitFile(htm, "precalc_instr_must_withdraw_notpast.txt");
            }
        }

        // Display all holds on the student's account that aren't hold 30
        final List<RawAdminHold> stuHolds = logic.data.studentData.getStudentHolds();

        for (final RawAdminHold hold : stuHolds) {
            if ("30".equals(hold.holdId)) {
                continue;
            }

            htm.div("vgap");
            htm.sDiv("indent22");
            htm.sDiv("errorbox");
            htm.addln(" <strong>There is a hold on your record.</strong>");
            htm.div("vgap");

            final String msg = RawAdminHoldLogic.getStudentMessage(hold.holdId);
            if (msg != null) {
                htm.addln(" &nbsp;<strong class='red'>", msg, "</strong>");
            }

            htm.div("vgap");
            if ("F".equals(hold.sevAdminHold)) {
                Page.emitFile(htm, "precalc_instr_fatal_hold.txt");
            }

            htm.eDiv(); // errorbox
            htm.eDiv(); // indent22

            htm.div("vgap");
        }

        if (logic.data.studentData.getNumLockouts() == 0) {
            if (logic.course == null) {
                Page.emitFile(htm, "precalc_instr_home_pre_hours.txt");
            } else {
                final int useOfStandardsBased = logic.course.getUseOfStandardsBased();
                if (useOfStandardsBased == 0) {
                    Page.emitFile(htm, "precalc_instr_home_pre_hours.txt");
                } else if (useOfStandardsBased == 1) {
                    Page.emitFile(htm, "precalc_instr_home_pre_hours_some_stds.txt");
                } else {
                    Page.emitFile(htm, "precalc_instr_home_pre_hours_all_stds.txt");
                }
            }
            AbstractPageSite.hours(studentData, htm, true, true);
            Page.emitFile(htm, "precalc_instr_home_post_hours.txt");
            Page.emitFile(htm, "precalc_instr_maintenance_window.txt");
        }
    }
}
