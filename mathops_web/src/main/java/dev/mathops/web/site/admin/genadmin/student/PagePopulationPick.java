package dev.mathops.web.site.admin.genadmin.student;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.logic.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.WebViewData;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminSite;
import dev.mathops.web.site.admin.genadmin.EAdminTopic;
import dev.mathops.web.site.admin.genadmin.GenAdminPage;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Pages that allow the user to choose a population of students.
 */
public enum PagePopulationPick {
    ;

    /**
     * Handles a POST from the page.
     *
     * @param data    the web view data
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doPost(final WebViewData data, final AdminSite site, final ServletRequest req,
                              final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        String course = req.getParameter("pick_course");
        String section = req.getParameter("pick_sect");
        if (course != null && course.isEmpty()) {
            course = null;
        }
        if (section != null && section.isEmpty()) {
            section = null;
        }

        final String pace = req.getParameter("pick_pace");
        Integer paceInt = null;
        if (pace != null) {
            try {
                paceInt = Integer.valueOf(pace);
            } catch (final NumberFormatException ex) {
                Log.warning("Invalid pace: ", pace, ex);
            }
        }

        final Cache cache = data.getCache();
        final List<RawStcourse> list = RawStcourseLogic.queryActiveForActiveTerm(cache);

        // If pace is specified, count the pace for each student, then remove all with other pace
        // (we have to do this filter before removing based on course/section)
        if (paceInt != null) {
            // Get pace for each student ID
            final Map<String, Integer> counts = new HashMap<>(list.size());
            for (final RawStcourse reg : list) {
                final String stuId = reg.stuId;
                final Integer cur = counts.get(stuId);
                if (cur == null) {
                    counts.put(stuId, Integer.valueOf(1));
                } else {
                    counts.put(stuId, Integer.valueOf(cur.intValue() + 1));
                }
            }

            // Remove all regs for students with pace that does not match desired pace
            final Iterator<RawStcourse> iter1 = list.iterator();
            while (iter1.hasNext()) {
                final RawStcourse rec = iter1.next();

                final Integer actualPace = counts.get(rec.stuId);
                if (!paceInt.equals(actualPace)) {
                    iter1.remove();
                }
            }
        }

        // Filter list by course and section, if specified
        final Iterator<RawStcourse> iter2 = list.iterator();
        while (iter2.hasNext()) {
            final RawStcourse rec = iter2.next();

            if ((course != null && !course.equals(rec.course)) || (section != null && !section.equals(rec.sect))) {
                iter2.remove();
            }
        }

        // What remains in 'list' is the records we want to return

        final Map<String, RawStudent> students = new HashMap<>(list.size());
        final Map<String, RawStcourse> sorted = new TreeMap<>();

        for (final RawStcourse rec : list) {
            final RawStudent stu = RawStudentLogic.query(cache, rec.stuId, false);
            if (stu == null) {
                Log.warning("Cannot look up student ", rec.stuId);
                continue;
            }

            students.put(rec.stuId, stu);
            final String key = stu.lastName + CoreConstants.SPC + stu.firstName + CoreConstants.SPC + stu.stuId;
            sorted.put(key, rec);
        }

        final SystemData systemData = data.getSystemData();

        if (sorted.isEmpty()) {
            PageStudent.doGet(data, site, req, resp, session, "No matching students found.");
        } else {
            final TermRec active = systemData.getActiveTerm();

            if (active == null) {
                PageStudent.doGet(data, site, req, resp, session, "Unable to query the active term.");
            } else {
                final HtmlBuilder htm = GenAdminPage.startGenAdminPage(data, site, session, true);

                GenAdminPage.emitNavBlock(EAdminTopic.STUDENT_STATUS, htm);

                if (course == null && section == null && pace == null) {
                    htm.sH(1).add("All registered students").eH(1);
                } else {
                    htm.sH(1).add("Students in ");
                    if (course != null) {
                        htm.add(course);
                    }
                    if (section != null) {
                        htm.add(" section ", section);
                    }
                    if (paceInt != null) {
                        htm.add(" pace ", paceInt);
                    }
                    htm.eH(1);
                }

                htm.sP().add(Integer.toString(sorted.size()), " students matched your query:").eP();

                htm.sTable("report");
                htm.sTr();
                htm.sTh().add("First Name").eTh();
                htm.sTh().add("Last Name").eTh();
                htm.sTh().add("Student ID").eTh();
                htm.sTh().add("E-mail").eTh();
                htm.sTh().eTh();
                htm.eTr();

                for (final RawStcourse reg : sorted.values()) {
                    final RawStudent student = students.get(reg.stuId);

                    htm.sTr();
                    htm.sTd().add(student.firstName).eTd();
                    htm.sTd().add(student.lastName).eTd();
                    htm.sTd().add(student.stuId).eTd();
                    htm.sTd().add(student.stuEmail).eTd();

                    htm.sTd().add("<form action='student_info.html' method='post'>")
                            .add("<input type='hidden' name='stu' value='", student.stuId, "'/>")
                            .add("<input type='submit' value='Choose'></form>").eTd();
                    htm.eTr();
                }
                htm.eTable();

                htm.div("vgap0");
                htm.sP().add("<a href='student.html'>Return to student selection.</a>").eP();

                Page.endOrdinaryPage(systemData, site, htm, true);

                final byte[] bytes = htm.toString().getBytes(StandardCharsets.UTF_8);
                AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, bytes);
            }
        }
    }
}
