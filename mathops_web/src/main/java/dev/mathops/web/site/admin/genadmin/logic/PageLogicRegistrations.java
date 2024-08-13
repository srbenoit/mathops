package dev.mathops.web.site.admin.genadmin.logic;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Cache;
import dev.mathops.db.old.logic.PaceTrackLogic;
import dev.mathops.db.old.logic.RegistrationsLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * A page to test logic related to registrations.
 */
public enum PageLogicRegistrations {
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

            if ("ALL".equals(stuId)) {
                scanAllStudents(cache, htm);
            } else {
                scanOneStudent(cache, htm, stuId);
            }
        }

        endPage(cache, site, req, resp, htm);
    }

    /**
     * Performs a scan of all students who have a registration in the active term, printing summary statistics and all
     * warnings encountered.  This will skip students whose only registrations in the active term have been dropped.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void scanAllStudents(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final TermRec active = cache.getSystemData().getActiveTerm();
        if (active == null) {
            htm.sH(4).add("Unable to query the active term.").eH(4);
        } else {
            // Compile a list of IDs for all students who have active registrations this term
            final List<RawStcourse> allRegs = RawStcourseLogic.queryByTerm(cache, active.term, true, false);
            final int numTotalRegs = allRegs.size();
            final Set<String> studentIds = new HashSet<>(numTotalRegs * 2 / 3);
            for (final RawStcourse stc : allRegs) {
                studentIds.add(stc.stuId);
            }

            int numUncountedIncompletes = 0;
            int numIgnored = 0;
            int numChallengeCredit = 0;
            int numPaced = 0;
            final List<Map<String, Integer>> paceTrackCounts = new ArrayList<>(5);
            for (int i = 0; i < 5; ++i) {
                paceTrackCounts.add(new TreeMap<>());
            }

            // Scan those students, emit warnings as encountered, and gather summary statistics
            for (final String stuId : studentIds) {
                final RegistrationsLogic.ActiveTermRegistrations regs =
                        RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);

                numIgnored += regs.ignored().size();
                numUncountedIncompletes += regs.uncountedIncompletes().size();
                numChallengeCredit += regs.creditByExam().size();

                final List<RawStcourse> inPaceRegs = regs.inPace();
                numPaced += inPaceRegs.size();

                if (!inPaceRegs.isEmpty()) {
                    final int pace = PaceTrackLogic.determinePace(inPaceRegs);
                    final String track = PaceTrackLogic.determinePaceTrack(inPaceRegs, pace);
                    final RawStterm stterm = RawSttermLogic.query(cache, active.term, stuId);
                    if (stterm == null) {
                        regs.warnings().add("No STTERM record found");
                    } else if (stterm.pace.intValue() != pace || !stterm.paceTrack.equals(track)) {
                        regs.warnings().add("STTERM record has pace = " + stterm.pace + " and pace track "
                                            + stterm.paceTrack + " but logic calculated pace = " + pace
                                            + " and pace track = " + track);
                    }

                    final Map<String, Integer> paceMap = paceTrackCounts.get(pace - 1);
                    final Integer current = paceMap.get(track);
                    if (current == null) {
                        paceMap.put(track, Integer.valueOf(1));
                    } else {
                        paceMap.put(track, Integer.valueOf(current.intValue() + 1));
                    }
                }

                if (!regs.warnings().isEmpty()) {
                    htm.sP().add("<strong style='color:red'>*** WARNINGS for student ", stuId, " ***)</strong>").eP();
                    htm.addln("<ul style='margin-top:0;'>");
                    for (final String row : regs.warnings()) {
                        htm.add("<li>").add(row).addln("</li>");
                    }
                    htm.addln("</ul>");
                }

                // Try not to make this method blast the production server's load up to 100%
                try {
                    Thread.sleep(5L);
                } catch (final InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

            htm.sH(4).add("Summary Statistics:").eH(4);
            htm.sP("indent");
            htm.add("Number of students with registrations: ", studentIds.size()).br();
            htm.add("Number of uncounted Incompletes: ", numUncountedIncompletes).br();
            htm.add("Number of Ignored registrations: ", numIgnored).br();
            htm.add("Number of Challenge Credit registrations: ", numChallengeCredit).br();
            htm.add("Number of 'in-pace' registrations: ", numPaced).br();
            htm.eP();

            htm.sDiv("indent2");
            htm.sTable("report");
            htm.sTr().sTh().add("Pace").eTh().sTh().add("Track").eTh().sTh().add("Registrations").eTh().eTr();
            for (int i = 0; i < 5; ++i) {
                final String paceStr = Integer.toString(i + 1);

                final Map<String, Integer> tracks = paceTrackCounts.get(i);
                for (final Map.Entry<String, Integer> entry : tracks.entrySet()) {
                    final String key = entry.getKey();
                    final Integer value = entry.getValue();
                    htm.sTr().sTd().add(paceStr).eTd().sTd().add(key).eTd().sTd().add(value).eTd().eTr();
                }
            }
            htm.eTable();
            htm.eDiv();
        }
    }

    /**
     * Performs a scan of a single student, printing that student's registration status, calculated pace and pace track,
     * and information from STTERM, if found.
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
            final String screenName = student.getScreenName();
            htm.sH(4).add("Registrations for student ", stuId, " (", screenName, ")").eH(4);

            final RegistrationsLogic.ActiveTermRegistrations regs =
                    RegistrationsLogic.gatherActiveTermRegistrations(cache, stuId);

            final List<RawStcourse> inPaceRegs = regs.inPace();

            if (!inPaceRegs.isEmpty()) {
                htm.sP().add("<strong>Counted toward pace</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final RawStcourse row : inPaceRegs) {
                    htm.add("<li>").add(row.course, " (", row.sect, "), pace order = ", row.paceOrder,
                            ", open = ", row.openStatus, ", completed = ", row.completed).addln("</li>");
                }
                htm.addln("</ul>");

                final int pace = PaceTrackLogic.determinePace(inPaceRegs);
                final String track = PaceTrackLogic.determinePaceTrack(inPaceRegs, pace);

                htm.sP("indent").add("Calculated pace = ", pace + " and pace track = ", track).eP();

                final TermRec active = cache.getSystemData().getActiveTerm();
                final RawStterm stterm = RawSttermLogic.query(cache, active.term, stuId);
                if (stterm == null) {
                    htm.sP("indent").add("(There is no STTERM record for this student yet").eP();
                } else if (stterm.pace.intValue() == pace && stterm.paceTrack.equals(track)) {
                    htm.sP("indent").add("STTERM record found and has correct pace and pace track.").eP();
                } else {
                    htm.sP("indent").add("*** WARNING: STTERM record found but has pace = ", stterm.pace,
                            " and pace track ", stterm.paceTrack).eP();
                }
            }

            if (!regs.uncountedIncompletes().isEmpty()) {
                htm.sP().add("<strong>Incompletes that do not count toward pace</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final RawStcourse row : regs.uncountedIncompletes()) {
                    htm.add("<li>").add(row.course, " (", row.sect, "), from = ", row.iTermKey, ", open = ",
                                    row.openStatus, ", completed = ", row.completed, ", deadline = ",
                                    row.iDeadlineDt)
                            .addln("</li>");
                }
                htm.addln("</ul>");
            }

            if (!regs.dropped().isEmpty()) {
                htm.sP().add("<strong>Dropped registrations</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final RawStcourse row : regs.dropped()) {
                    htm.add("<li>").add(row.course, " (", row.sect, ")").addln("</li>");
                }
                htm.addln("</ul>");
            }

            if (!regs.ignored().isEmpty()) {
                htm.sP().add("<strong>Ignored registrations (forfeit with open status = 'G')</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final RawStcourse row : regs.ignored()) {
                    htm.add("<li>").add(row.course, " (", row.sect, ")").addln("</li>");
                }
                htm.addln("</ul>");
            }

            if (!regs.creditByExam().isEmpty()) {
                htm.sP().add("<strong>Challenge credit (instruction type = 'OT')</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final RawStcourse row : regs.creditByExam()) {
                    htm.add("<li>").add(row.course, " (", row.sect, ")").addln("</li>");
                }
                htm.addln("</ul>");
            }

            if (!regs.warnings().isEmpty()) {
                htm.sP().add("<strong style='color:red'>*** WARNINGS ***)</strong>").eP();
                htm.addln("<ul style='margin-top:0;'>");
                for (final String row : regs.warnings()) {
                    htm.add("<li>").add(row).addln("</li>");
                }
                htm.addln("</ul>");
            }
        }
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

        PageLogicTesting.emitNavMenu(htm, EAdmSubtopic.LOGIC_REGISTRATIONS);
        htm.hr().div("vgap");

        htm.sP().add("This page tests logic contained in the <code>RegistrationsLogic</code> and ",
                "<code>PaceTrackLogic</code> classes in the <code>dev.mathops.db.old.logic</code> package.").eP();
        htm.hr().div("vgap0");

        htm.sDiv("indent");
        htm.addln("<form action='logic_registrations.html' method='POST'>");
        htm.addln("  Student ID: <input id='stu_id' name='stu_id' type='text' size='7'/>");
        htm.addln("  <input type='submit' value='Gather and classify current-term registrations...'/>");
        htm.addln("</form>");
        htm.eDiv();
        htm.div("vgap0");

        htm.sDiv("indent");
        htm.addln("<form action='logic_registrations.html' method='POST'>");
        htm.addln("  <input type='hidden' id='stu_id' name='stu_id' value='ALL'/>");
        htm.addln("  <input type='submit' value='Scan registrations for all students and report warnings...'/>");
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
        final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
    }
}
