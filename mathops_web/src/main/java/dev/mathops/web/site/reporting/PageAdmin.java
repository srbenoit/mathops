package dev.mathops.web.site.reporting;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.ReportPermsRec;
import dev.mathops.db.reclogic.ReportPermsLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the home page.
 */
enum PageAdmin {
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
    static void showPage(final Cache cache, final ReportingSite site, final ServletRequest req,
                         final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);

        final ERole role = session.getEffectiveRole();
        final boolean isAdmin = role.canActAs(ERole.ADMINISTRATOR);

        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, isAdmin ? Page.ADMIN_BAR : Page.NO_BARS,
                null, false, true);
        htm.sH(2).add(Res.get(Res.HOME_HEADING)).eH(2);
        htm.sH(3).add("<a href='reports_admin.html'>Report Management</a>").eH(3);
        htm.hr();

        final String screenName = session.getEffectiveScreenName();
        htm.sP().add("Logged in as <strong>", screenName, "</strong>").eP();
        htm.div("vgap");

        if (isAdmin) {
            try {
                final String rptId = req.getParameter("rpt");
                final EDefinedReport whichReport = EDefinedReport.forId(rptId);

                final List<ReportPermsRec> allPerms = ReportPermsLogic.get(cache).queryAll(cache);
                allPerms.sort(null);
                final Map<String, RawStudent> studentRecs = lookupStudents(cache, allPerms);

                htm.sDiv("indent");
                generatePageContent(htm, whichReport, allPerms, studentRecs);
                htm.eDiv();
            } catch (final SQLException ex) {
                htm.sP().addln("There was an error querying the database for report permissions:").eP();
                htm.sP().addln(ex.getLocalizedMessage()).eP();
            }
        } else {
            htm.sP().addln("You are not authorized to manage reports.").eP();
        }

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }

    /**
     * Looks up student records for all students that exist in a report permissions record.
     *
     * @param cache    the data cache
     * @param allPerms the list of all report permissions records
     * @return a map from student ID to the student record (or to {@code null} if student was not found)
     * @throws SQLException if there is an error accessing the database
     */
    private static Map<String, RawStudent> lookupStudents(final Cache cache, final List<ReportPermsRec> allPerms)
            throws SQLException {

        final int numPerms = allPerms.size();
        final Map<String, RawStudent> studentRecs = new HashMap<>(numPerms);

        for (final ReportPermsRec rec : allPerms) {
            final String stuId = rec.stuId;
            if (!studentRecs.containsKey(stuId)) {
                final RawStudent stu = RawStudentLogic.query(cache, stuId, false);
                studentRecs.put(stuId, stu);
            }
        }

        return studentRecs;
    }

    /**
     * Generates the content of the page.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param whichReport if not {@code null}, this is the ID of a report whose details to "expand"
     * @param allPerms    the list of all permissions
     * @param studentRecs a map from student ID to student record
     */
    private static void generatePageContent(final HtmlBuilder htm, final EDefinedReport whichReport,
                                            final List<ReportPermsRec> allPerms,
                                            final Map<String, RawStudent> studentRecs) {

        // Count the number of users authorized for each report
        final Map<EDefinedReport, Integer> counts = new HashMap<>();
        for (final ReportPermsRec rec : allPerms) {
            final EDefinedReport key = EDefinedReport.forId(rec.rptId);

            if (key != null) {
                final Integer cur = counts.get(key);
                if (cur == null) {
                    counts.put(key, Integer.valueOf(1));
                } else {
                    counts.put(key, Integer.valueOf(cur.intValue() + 1));
                }
            }
        }

        htm.sH(4).add("Math Placement Reports").eH(4);
        htm.sDiv("indent0");

        // --------------------
        htm.addln(EDefinedReport.MPT_BY_CATEGORY == whichReport ? "<details open='open'>" : "<details>");
        final Integer count1 = counts.get(EDefinedReport.MPT_BY_CATEGORY);
        emitSummary(htm, count1, "Math Placement progress by special category");
        htm.sP("rptdesc");
        htm.addln(" This report shows the Math Placement status for the collection of students who exist in the ",
                "<code>special_stus</code> table under a specified student categories.  Examples include student ",
                "athletes or incoming engineering students, but categories can be created as needed.");
        htm.eP();
        emitControls(htm, count1, EDefinedReport.MPT_BY_CATEGORY, "students in categories", allPerms, studentRecs);
        htm.addln("</details>");

        htm.div("vgap0");

        // --------------------
        htm.addln(EDefinedReport.MPT_BY_IDS == whichReport ? "<details open='open'>" : "<details>");
        final Integer count2 = counts.get(EDefinedReport.MPT_BY_IDS);
        emitSummary(htm, count2, "Math Placement progress for specified students");
        htm.sP("rptdesc");
        htm.addln(" This report allows a user to paste in an arbitrary list of CSU IDs, then generates the Math ",
                "Placement status for those students.");
        htm.eP();
        emitControls(htm, count2, EDefinedReport.MPT_BY_IDS, null, allPerms, studentRecs);
        htm.addln("</details>");

        htm.div("vgap2");

        htm.sH(4).add("Precalculus Course Reports").eH(4);

        // --------------------
        htm.addln(EDefinedReport.PROGRESS_BY_SECTION == whichReport ? "<details open='open'>" : "<details>");
        final Integer count3 = counts.get(EDefinedReport.PROGRESS_BY_SECTION);
        emitSummary(htm, count3, "Precalculus Course progress by course and/or section");
        htm.sP("rptdesc");
        htm.addln(" This report allows a user to select from all current Precalculus courses and sections, then ",
                "generates a report of the progress in those courses for those students.");
        htm.eP();
        emitControls(htm, count3, EDefinedReport.PROGRESS_BY_SECTION, null, allPerms, studentRecs);
        htm.addln("</details>");

        htm.div("vgap0");

        // --------------------
        htm.addln(EDefinedReport.PROGRESS_BY_IDS == whichReport ? "<details open='open'>" : "<details>");
        final Integer count4 = counts.get(EDefinedReport.PROGRESS_BY_IDS);
        emitSummary(htm, count4, "Precalculus Course progress for specified students");
        htm.sP("rptdesc");
        htm.addln(" This report allows a user to paste in an arbitrary list of CSU IDs, then generates a report of ",
                "the progress in Precalculus courses for those students.");
        htm.eP();
        emitControls(htm, count4, EDefinedReport.PROGRESS_BY_IDS, null, allPerms, studentRecs);
        htm.addln("</details>");

        htm.div("vgap2");

        htm.eDiv();
    }

    /**
     * Emits a summary line for a report.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param count the count of users authorized for the report; {@code null} if none
     * @param title the report title
     */
    private static void emitSummary(final HtmlBuilder htm, final Integer count, final String title) {

        htm.add(" <summary><span style='color:Firebrick;'>", title, "</span> <span style='color:gray;'>(",
                (count == null ? "no" : count), " users authorized)</span></summary>");
    }

    /**
     * Emits controls for configuring the report.
     *
     * @param htm         the {@code HtmlBuilder} to which to append
     * @param count       the count of users authorized for the report; {@code null} if none
     * @param whichReport the report for which to emit controls
     * @param drivingType the name of a driving type that an administrator can control; {@code null} if none
     * @param allPerms    the list of all permissions
     * @param studentRecs a map from student ID to student record
     */
    private static void emitControls(final HtmlBuilder htm, final Integer count, final EDefinedReport whichReport,
                                     final String drivingType, final List<ReportPermsRec> allPerms,
                                     final Map<String, RawStudent> studentRecs) {

        htm.addln("<form action='reports_admin.html' method='POST'>");
        htm.addln("  <input type='hidden' name='rpt' id='rpt' value='", whichReport.id, "'/>");

        htm.sP("rptcontrols");
        htm.add("  Currently Authorized users:").br();
        if (count == null) {
            htm.add("(none)").br();
        } else {
            htm.addln("  <table>");
            for (final ReportPermsRec rec : allPerms) {
                if (whichReport.id.equals(rec.rptId)) {
                    htm.add("    <tr><td>");
                    final String stuId = rec.stuId;
                    htm.add(stuId);
                    final RawStudent stu = studentRecs.get(stuId);
                    if (stu != null) {
                        htm.add(" (", stu.getScreenName(), ")");
                    }

                    final int perm = rec.permLevel;
                    final String inputName = "PERM_" + stuId;
                    htm.add("</td><td><select name='", inputName, "' id='", inputName, "'>");

                    htm.add("<option value='1'");
                    if (perm == 1) {
                        htm.add(" selected='selected'");
                    }
                    htm.add(">Can run report</option>");

                    if (drivingType != null) {
                        htm.add("<option value='2'");
                        if (perm == 2) {
                            htm.add(" selected='selected'");
                        }
                        htm.add(">Can run report and manage ", drivingType, "</option>");
                    }

                    htm.add("<option value='3'");
                    if (perm == 3) {
                        htm.add(" selected='selected'");
                    }
                    if (drivingType == null) {
                        htm.add(">Can run report and manage users");
                    } else {
                        htm.add(">Can run report, manage ", drivingType, ", and manage authorized users");
                    }
                    htm.add("</option>");

                    htm.add("<option value='0'");
                    htm.add(">(Remove user's authorization)</option>");

                    htm.addln("</select></td></tr>");
                }
            }
            htm.addln("  </table>");
        }
        htm.eP();
        htm.sP("rptcontrols");
        htm.add(" Paste CSU IDs of new users to authorize in the box below:").br();
        htm.addln("  <textarea name='newids' id='newids' rows='8' cols='20'></textarea>").br();
        htm.addln("  <input type='submit' value='Update report settings'/>");
        htm.eP();

        htm.addln("</form>");
    }

    /**
     * Generates the page.
     *
     * @param cache the data cache
     * @param req   the request
     * @param resp  the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void processPost(final Cache cache, final ServletRequest req, final HttpServletResponse resp)
            throws IOException, SQLException {

        final String rptId = req.getParameter("rpt");
        final EDefinedReport whichReport = EDefinedReport.forId(rptId);

        if (whichReport == null) {
            resp.sendRedirect("reports_admin.html");
        } else {
            final ReportPermsLogic logic = ReportPermsLogic.get(cache);

            final List<ReportPermsRec> allPerms = ReportPermsLogic.get(cache).queryAll(cache);

            // Process permission updates on existing students
            for (final ReportPermsRec toCheck : allPerms) {
                if (whichReport.id.equals(toCheck.rptId)) {
                    final String stuId = toCheck.stuId;
                    final String inputName = "PERM_" + stuId;

                    final String setting = req.getParameter(inputName);
                    if (setting != null) {
                        try {
                            final Integer parsedSetting = Integer.valueOf(setting);
                            final int parsedInt = parsedSetting.intValue();

                            if (parsedInt == 0) {
                                logic.delete(cache, toCheck);
                            } else if (!parsedSetting.equals(toCheck.permLevel) &&
                                       parsedInt >= 1 && parsedInt <= 3) {
                                toCheck.permLevel = parsedSetting;
                                logic.updatePermLevel(cache, toCheck);
                            }
                        } catch (final NumberFormatException ex) {
                            Log.warning("Unable to parse user permission setting: ", setting);
                        }
                    }
                }
            }

            // Add new students
            final String newIdList = req.getParameter("newids");
            if (newIdList != null && !newIdList.isBlank()) {
                final List<String> newIds = ReportingSite.extractIds(newIdList);

                for (final String newId : newIds) {
                    final String cleaned = ReportingSite.cleanId(newId);
                    if (newId.length() == 9) {
                        boolean exists = false;
                        for (final ReportPermsRec toCheck : allPerms) {
                            if (whichReport.id.equals(toCheck.rptId)
                                && cleaned.equals(toCheck.stuId)) {
                                exists = true;
                                break;
                            }
                        }

                        if (!exists) {
                            final ReportPermsRec newRec = new ReportPermsRec(cleaned, whichReport.id,
                                    Integer.valueOf(1));
                            logic.insert(cache, newRec);
                        }
                    }
                }
            }

            resp.sendRedirect("reports_admin.html?rpt=" + whichReport.id);
        }
    }
}
