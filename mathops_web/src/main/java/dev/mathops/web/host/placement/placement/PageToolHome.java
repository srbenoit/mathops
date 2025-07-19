package dev.mathops.web.host.placement.placement;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.DateRange;
import dev.mathops.db.logic.DateRangeGroups;
import dev.mathops.db.logic.course.PrerequisiteLogic;
import dev.mathops.db.logic.mathplan.MathPlanLogic;
import dev.mathops.db.logic.mathplan.StudentMathPlan;
import dev.mathops.db.logic.placement.PlacementLogic;
import dev.mathops.db.logic.tutorial.PrecalcTutorialLogic;
import dev.mathops.db.logic.tutorial.PrecalcTutorialStatus;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.session.ImmutableSessionInfo;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

/**
 * Generates the content of the home page.
 */
enum PageToolHome {
    ;

    /**
     * Generates the home page with the menu of courses and general information for the student.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error writing the response
     */
    static void doGet(final Cache cache, final MathPlacementSite site, final ServletRequest req,
                      final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();

        final HtmlBuilder htm = MPPage.startPage2(site, session);

        htm.sDiv("inset2");
        htm.sDiv("shaded2left");

        htm.sP().add("To request accommodations related to a disability for access to the Math Placement Tool, ",
                "please contact <a class='ulink' href='https://disabilitycenter.colostate.edu/'>",
                "The Student Disability Center</a>.").eP();
        htm.div("vgap");

        final RawStudent student = RawStudentLogic.query(cache, stuId, false);

        final ZonedDateTime now = session.getNow();
        final PlacementLogic logic = new PlacementLogic(cache, stuId, student == null ? null : student.aplnTerm, now);

        if (logic.status.attemptsUsed > 0) {
            PlacementReport.doPlacementReport(cache, logic.status, session, null, false, htm);
        } else {
            htm.sP("indent11", "style='padding-left:32px;'");
            htm.add("<img src='/images/orange2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");
            htm.addln("You have not completed the Math Placement Tool.");
            htm.eP();
        }

        if (student != null) {
            final StudentMathPlan plan = MathPlanLogic.queryPlan(cache, stuId);
            PagePlanNext.showNextSteps(cache, htm, plan);
        }

        htm.div("vgap2");

        htm.sDiv("indent11");
        htm.addln("<fieldset>");
        htm.add("<legend>");
        if (logic.status.attemptsUsed > 0) {
            htm.add("Your remaining options:");
        } else {
            htm.add("Your options:");
        }
        htm.addln("</legend>");
        boolean nothing = true;

        final DateRangeGroups unproc = logic.status.unproctoredDateRanges;

        String also = CoreConstants.EMPTY;

        // Print any options relating to the unproctored version

        if (!logic.status.unproctoredUsed) {
            if (!logic.status.availableUnproctoredIds.isEmpty()) {
                // Currently available
                htm.sP("indent11", "style='padding-left:32px;'");
                htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");
                htm.addln("You are eligible to complete the Math Placement Tool remotely.");
                htm.eP();

                htm.addln("<ul class='options'>");
                htm.add(" <li><a href='tool_instructions_re.html'>",
                        "Tell me how to complete the Math Placement Tool remotely.</a>");
                if (unproc.current != null && unproc.current.end != null) {
                    htm.add(" (this option is available until ",
                            TemporalUtils.FMT_MDY.format(unproc.current.end), ")");
                }
                htm.addln("</li>");
                htm.addln("</ul>");

                also = "also";
                nothing = false;
            } else if (unproc != null) {
                final List<DateRange> future = unproc.future;

                if (future != null && !future.isEmpty()) {
                    htm.sP("indent11", "style='padding-left:32px;'");
                    htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/> ");

                    htm.addln("You will be eligible to complete the Math Placement Tool remotely on ",
                            TemporalUtils.FMT_MDY.format(future.getFirst().start), CoreConstants.DOT);
                    htm.eP();

                    also = "also";
                    nothing = false;
                }
            }
        }

        if (!logic.status.availableLocalProctoredIds.isEmpty() || !logic.status.availableOnlineProctoredIds.isEmpty()) {
            htm.sP("indent11", "style='padding-left:32px;'");

            htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");
            htm.addln("You are ", also, " eligible to complete the proctored Math Placement Tool.");
            htm.eP();

            htm.addln("<ul class='options'>");

            if (logic.status.availableLocalProctoredIds
                    .contains(PlacementLogic.PROCTORED_MPT_DEPT_TC_ID)) {
                htm.addln(" <li><a href='tool_instructions_tc.html'>",
                        "Tell me how to complete the Math Placement Tool in the Precalculus Center.</a></li>");
            }

            if (logic.status.availableOnlineProctoredIds
                    .contains(PlacementLogic.PROCTORED_MPT_PROCTORU_ID)) {
                htm.addln(" <li><a href='tool_instructions_pu.html'>",
                        "Tell me how to complete the Math Placement Tool using ProctorU.</a></li>");
            }

            nothing = false;
        }

        htm.addln("</ul>");

        // Users who have completed the placement tool are eligible for ELM Tutorial,
        // but if they already qualify for MATH 117, there is no point, so only show to the
        // population with a placement attempt but no M 100C result.

        final boolean has117 = logic.status.clearedFor.contains("MATH 117")
                               || logic.status.placedOutOf.contains("MATH 117")
                               || logic.status.earnedCreditFor.contains("MATH 117");

        if (!has117 && logic.status.placementAttempted) {
            htm.sP("indent11", "style='padding-left:32px;'");
            htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

            htm.addln("You are eligible to complete the Entry Level Mathematics (ELM) Tutorial:");
            htm.eP();

            htm.addln("<ul class='options'>");
            htm.addln(" <li><a href='tool_instructions_elm.html'>Tell me about the ELM Tutorial</a></li>");
            htm.addln("</ul>");
            htm.eP();
            nothing = false;
        }

        // Users who have completed the placement tool but have not placed out of MATH
        // 124 and 126 and whose effective application term name (after possible modification by
        // special student records) is "FA" are eligible for at least one Precalculus Tutorial,
        // as long as it is open or will open in the future

        final PrerequisiteLogic prereq = new PrerequisiteLogic(cache, stuId);
        final PrecalcTutorialLogic tutLogic = new PrecalcTutorialLogic(cache, stuId, session.getNow().toLocalDate(),
                prereq);
        final PrecalcTutorialStatus tutStatus = tutLogic.status;

        if (tutStatus.eligibleForPrecalcTutorial) {

            final List<RawCampusCalendar> cal = cache.getSystemData().getCampusCalendars();
            LocalDate start = null;
            LocalDate end = null;
            LocalDate info = null;
            for (final RawCampusCalendar test : cal) {
                if (RawCampusCalendar.DT_DESC_TUT_START.equals(test.dtDesc)) {
                    start = test.campusDt;
                } else if (RawCampusCalendar.DT_DESC_TUT_END.equals(test.dtDesc)) {
                    end = test.campusDt;
                } else if (RawCampusCalendar.DT_DESC_TUT_INFO.equals(test.dtDesc)) {
                    info = test.campusDt;
                }
            }
            final LocalDate today = session.getNow().toLocalDate();

            if (start != null && end != null && !today.isAfter(end)) {

                htm.sP("indent11", "style='padding-left:32px;'");
                htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

                htm.addln("You are eligible to complete the Precalculus Tutorial:");
                htm.eP();

                htm.addln("<ul class='options'>");
                htm.addln(" <li><a href='/precalc-tutorial/home.html'>Tell me about the Precalculus Tutorial",
                        "</a></li>");
                htm.addln("</ul>");
                htm.eP();
                nothing = false;
            } else if (info != null && !today.isBefore(info)) {

                // TODO: Different messaging here?
                htm.sP("indent11", "style='padding-left:32px;'");
                htm.add("<img src='/images/blue2.png' style='margin:-2px 0 0 -32px; padding-right:10px;'/>");

                htm.addln("You are eligible to complete a Precalculus Tutorial:");
                htm.eP();

                htm.addln("<ul class='options'>");
                htm.addln(" <li><a href='/precalc-tutorial/home.html'>Tell me about the Precalculus Tutorial",
                        "</a></li>");
                htm.addln("</ul>");
                htm.eP();
                nothing = false;
            }
        }

        if (nothing) {
            htm.sP("indent11");
            htm.addln("You have no attempts remaining on the Math Placement Tool.");
        } else {
            htm.sP("indent11", "style='font-size:80%;'");
            htm.addln("<strong>PLEASE NOTE</strong>: A one-time <strong>$15.00 fee</strong> for ",
                    "administration of the Math Placement Tool will be charged to your student account",
                    " on your first attempt.  This fee covers both allowed attempts on the Math ",
                    "Placement Tool and access to the ELM Tutorial if needed.");
        }
        htm.eP();

        htm.addln("</fieldset>");

        htm.eP();
        htm.eDiv();

        htm.eDiv(); // shaded2left
        htm.eDiv(); // inset2

        MPPage.emitScripts(htm);
        MPPage.endPage(htm, req, resp);
    }
}
