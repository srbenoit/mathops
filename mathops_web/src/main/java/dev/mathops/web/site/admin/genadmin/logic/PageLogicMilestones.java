package dev.mathops.web.site.admin.genadmin.logic;

import dev.mathops.commons.TemporalUtils;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.MilestoneLogic;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.logic.RegistrationsLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawMilestoneAppeal;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdmSubtopic;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

/**
 * A page to test logic related to milestones and extensions.
 */
public enum PageLogicMilestones {
    ;

    /**
     * Generates the logic testing page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final AdminSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = startPage(cache, site, session);

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Generates the logic testing page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the login session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = startPage(cache, site, session);

        final String stuId = req.getParameter("stu_id");
        if (stuId != null && !stuId.isEmpty()) {
            htm.hr().div("vgap");

            scanOneStudent(cache, htm, stuId);
        }

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Performs a scan of a single student, printing that current deadline schedule, with any overrides already in place
     * and any overrides available on request.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param stuId the student ID
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanOneStudent(final Cache cache, final HtmlBuilder htm, final String stuId) throws SQLException {

        final RawStudent student = RawStudentLogic.query(cache, stuId, false);
        if (student == null) {
            htm.sH(4).add("Student ", stuId, " not found in database.").eH(4);
        } else {
            final TermRec active = cache.getSystemData().getActiveTerm();

            final RegistrationsLogic.ActiveTermRegistrations regs =
                    RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);
            final List<RawStcourse> inPace = regs.inPace();

            final int pace = PaceTrackLogic.determinePace(inPace);
            final String paceStr = Integer.toString(pace);
            final String paceTrack = PaceTrackLogic.determinePaceTrack(inPace, pace);

            final String screenName = student.getScreenName();
            htm.sH(4).add("Student ", stuId, " (", screenName, ") has pace ", paceStr, " and pace track ",
                    paceTrack).eH(4);

            if (regs.hasLegacy()) {
                htm.sP().add("<strong>Legacy Course Deadline Schedule</strong>").eP();

                for (int index = 1; index <= pace; ++index) {
                    final RawStcourse reg = inPace.get(index - 1);
                    final List<RawMilestoneAppeal> appeals = MilestoneLogic.getAppeals(cache, stuId, paceTrack, pace,
                            index);

                    final MilestoneLogic.ResolvedLegacyMilestones legacy =
                            MilestoneLogic.resolveLegacyMilestones(cache, active.term, stuId, paceTrack, pace, index);

                    final String indexStr = Integer.toString(index);
                    htm.sP("indent").add("Course ", indexStr, " (", reg.course, ")").eP();

                    htm.sDiv("indent");
                    htm.sTable("report");
                    htm.sTr().sTh().add("Milestone").eTh()
                            .sTh().add("Effective Date").eTh()
                            .sTh().add("Attempts").eTh()
                            .sTh().add("Active Extensions").eTh()
                            .sTh().add("Available Extensions").eTh().eTr();

                    final String activeRe1 = buildActiveLegacyAppealsString(pace, index, 1, "RE", appeals);
                    final String availRe1 = buildAvailableLegacyExtensionsString(cache, stuId, paceTrack, pace, index,
                            1, "RE");
                    final LocalDate re1Date = legacy.re1();
                    final String re1Str = TemporalUtils.FMT_MDY.format(re1Date);
                    htm.sTr().sTd().add("Unit 1 Review").eTd().sTd().add(re1Str).eTd().sTd().eTd()
                            .sTd().add(activeRe1).eTd().sTd().add(availRe1).eTd().eTr();

                    final String activeRe2 = buildActiveLegacyAppealsString(pace, index, 2, "RE", appeals);
                    final String availRe2 = buildAvailableLegacyExtensionsString(cache, stuId, paceTrack, pace, index,
                            2, "RE");
                    final LocalDate re2Date = legacy.re2();
                    final String re2Str = TemporalUtils.FMT_MDY.format(re2Date);
                    htm.sTr().sTd().add("Unit 2 Review").eTd().sTd().add(re2Str).eTd().sTd().eTd()
                            .sTd().add(activeRe2).eTd().sTd().add(availRe2).eTd().eTr();

                    final String activeRe3 = buildActiveLegacyAppealsString(pace, index, 3, "RE", appeals);
                    final String availRe3 = buildAvailableLegacyExtensionsString(cache, stuId, paceTrack, pace, index,
                            3, "RE");
                    final LocalDate re3Date = legacy.re3();
                    final String re3Str = TemporalUtils.FMT_MDY.format(re3Date);
                    htm.sTr().sTd().add("Unit 3 Review").eTd().sTd().add(re3Str).eTd().sTd().eTd()
                            .sTd().add(activeRe3).eTd().sTd().add(availRe3).eTd().eTr();

                    final String activeRe4 = buildActiveLegacyAppealsString(pace, index, 4, "RE", appeals);
                    final String availRe4 = buildAvailableLegacyExtensionsString(cache, stuId, paceTrack, pace, index,
                            4, "RE");
                    final LocalDate re4Date = legacy.re4();
                    final String re4Str = TemporalUtils.FMT_MDY.format(re4Date);
                    htm.sTr().sTd().add("Unit 4 Review").eTd().sTd().add(re4Str).eTd().sTd().eTd()
                            .sTd().add(activeRe4).eTd().sTd().add(availRe4).eTd().eTr();

                    final String activeFe = buildActiveLegacyAppealsString(pace, index, 5, "FE", appeals);
                    final String availFe = buildAvailableLegacyExtensionsString(cache, stuId, paceTrack, pace, index,
                            5, "FE");
                    final LocalDate feDate = legacy.fe();
                    final String feStr = TemporalUtils.FMT_MDY.format(feDate);
                    htm.sTr().sTd().add("Final Exam").eTd().sTd().add(feStr).eTd().sTd().eTd()
                            .sTd().add(activeFe).eTd().sTd().add(availFe).eTd().eTr();

                    final String activeF1 = buildActiveLegacyAppealsString(pace, index, 5, "F1", appeals);
                    final LocalDate f1Date = legacy.f1();
                    final String f1Str = TemporalUtils.FMT_MDY.format(f1Date);
                    final Integer attempts = legacy.numF1Tries();
                    final String attemptsStr = attempts == null ? "null" : attempts.toString();
                    htm.sTr().sTd().add("Final +1").eTd().sTd().add(f1Str).eTd().sTd().add(attemptsStr).eTd()
                            .sTd().add(activeF1).eTd().sTd().eTd().eTr();

                    htm.eTable();
                    htm.eDiv();
                }
            }

            if (regs.hasStandardsBased()) {
                htm.sP().add("<strong>Standards-Based Course Deadline Schedule</strong>").eP();

                for (int index = 1; index <= pace; ++index) {
                    final RawStcourse reg = inPace.get(index - 1);

                    final MilestoneLogic.ResolvedStandardMilestones standard =
                            MilestoneLogic.resolveStandardMilestones(cache, active.term, stuId, paceTrack, pace, index);

                    final String indexStr = Integer.toString(index);
                    htm.sP("indent").add("Course ", indexStr, " (", reg.course, ")").eP();

                    htm.sDiv("indent");
                    htm.sTable("report");
                    htm.sTr().sTh().add("Milestone").eTh()
                            .sTh().add("Effective Date").eTh()
                            .sTh().add("Active Extensions").eTh()
                            .sTh().add("Available Extensions").eTh().eTr();

                    final LocalDate[][] dates = standard.dates();
                    final int numUnits = dates.length;
                    final int numObjectives = dates[0].length;

                    for (int i = 0; i < numUnits; ++i) {
                        for (int j = 0; j < numObjectives; ++j) {
                            final LocalDate date = dates[i][j];
                            final String dateSTr = TemporalUtils.FMT_MDY.format(date);

                            final String unitStr = Integer.toString(i + 1);
                            final String objStr = Integer.toString(j + 1);

                            htm.sTr().sTd().add("Standard ", unitStr, ".", objStr, " Mastery").eTd()
                                    .sTd().add(dateSTr).eTd()
                                    .sTd().eTd().sTd().eTd().eTr();
                        }
                    }

                    htm.eTable();
                    htm.eDiv();
                }
            }
        }
    }

    /**
     * Creates a string that shows all appeals for legacy deadlines that are active for a pace, course index, unit, and
     * milestsone type.
     *
     * @param pace    the pace
     * @param index   the course index (from 1 to pace)
     * @param unit    the unit (from 1 to 5)
     * @param msType  the milestone type ("RE", "FE", or "F1");
     * @param appeals the list of milestone appeals for the student
     * @return the string (empty if there are no active extensions)
     */
    private static String buildActiveLegacyAppealsString(final int pace, final int index, final int unit,
                                                         final String msType,
                                                         final Iterable<RawMilestoneAppeal> appeals) {

        final HtmlBuilder work = new HtmlBuilder(100);

        final Integer msNbr = Integer.valueOf(pace * 100 + index * 10 + unit);
        boolean newline = false;

        for (final RawMilestoneAppeal appeal : appeals) {
            if (msNbr.equals(appeal.msNbr) && msType.equals(appeal.msType)) {
                if (newline) {
                    work.br();
                }
                final String oldDate = TemporalUtils.FMT_MDY.format(appeal.priorMsDt);
                final String newDate = TemporalUtils.FMT_MDY.format(appeal.newMsDt);
                work.add("from ", oldDate, " to ", newDate);
                if ("F1".equals(msType) && appeal.attemptsAllowed != null) {
                    work.add(", ", appeal.attemptsAllowed, " tries");
                }
                work.add(" (", appeal.appealType, ")");
                newline = true;
            }
        }

        return work.toString();
    }

    /**
     * Creates a string that describes extensions available to the student "on request" for a milestone.
     *
     * @param cache     the data cache
     * @param stuId     the student ID
     * @param paceTrack the pace track
     * @param pace      the pace
     * @param index     the course index (from 1 to pace)
     * @param unit      the unit
     * @param msType    the milestone type
     * @return the string
     */
    private static String buildAvailableLegacyExtensionsString(final Cache cache, final String stuId,
                                                               final String paceTrack, final int pace,
                                                               final int index, final int unit, final String msType) {

        final HtmlBuilder work = new HtmlBuilder(100);
        boolean newline = false;

        try {
            final int accommodationDays = MilestoneLogic.daysAvailableLegacyAccommodationExtension(cache, stuId,
                    paceTrack,
                    pace, index, unit, msType);

            if (accommodationDays == 0) {
                work.add("Accommodation already used");
                newline = true;
            } else if (accommodationDays > 0) {
                work.add(accommodationDays);
                work.add(" days of accommodation extension");
                newline = true;
            }
        } catch (final IllegalArgumentException | SQLException ex) {
            final String exMsg = ex.getMessage();
            work.add("Exception: ", exMsg);
            newline = true;
        }

        try {
            final int freeDays = MilestoneLogic.daysAvailableLegacyFreeExtension(cache, stuId, paceTrack, pace, index,
                    unit, msType);

            if (freeDays == 0) {
                if (newline) {
                    work.br();
                }
                work.add("Free extension already used");
            } else if (freeDays > 0) {
                if (newline) {
                    work.br();
                }
                work.add(freeDays);
                work.add(" days of free extension");
            }
        } catch (final IllegalArgumentException | SQLException ex) {
            if (newline) {
                work.br();
            }
            final String exMsg = ex.getMessage();
            work.add("Exception: ", exMsg);
        }

        return work.toString();
    }

    /**
     * Starts a page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the login session
     * @return an {@code HtmlBuilder} with the started page content
     * @throws SQLException if there is an error accessing the database
     */
    private static HtmlBuilder startPage(final Cache cache, final AdminSite site, final ImmutableSessionInfo session)
            throws SQLException {

        final HtmlBuilder htm = GenAdminPage.startGenAdminPage(cache, site, session, true);

        GenAdminPage.emitNavBlock(EAdminTopic.LOGIC_TESTING, htm);
        htm.sH(1).add("Logic Testing").eH(1);

        PageLogicTesting.emitNavMenu(htm, EAdmSubtopic.LOGIC_MILESTONES);
        htm.hr().div("vgap");

        htm.sP().add("This page tests logic contained in the <code>MilestoneLogic</code> class in the ",
                "<code>dev.mathops.db.old.logic</code> package.").eP();
        htm.hr().div("vgap0");

        htm.sDiv("indent");
        htm.addln("<form action='logic_milestones.html' method='POST'>");
        htm.addln("  Student ID: <input id='stu_id' name='stu_id' type='text' size='7'/>");
        htm.addln("  <input type='submit' value='Gather available extensions...'/>");
        htm.addln("</form>");
        htm.eDiv();

        return htm;
    }

    /**
     * Ends a page.
     *
     * @param cache the data cache
     * @param site  the owning site
     * @param req   the request
     * @param resp  the response
     * @param htm   the {@code HtmlBuilder} with the page content to send
     * @throws SQLException if there is an error accessing the database
     */
    private static void endPage(final Cache cache, final AdminSite site, final ServletRequest req,
                                final HttpServletResponse resp, final HtmlBuilder htm)
            throws IOException, SQLException {

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
