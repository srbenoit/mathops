package dev.mathops.web.host.testing.adminsys.office;

import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStudentLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.host.testing.adminsys.AdminSite;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pages that allow the user to pick a single student. If pick criteria yield one student, that student's data is shown.
 * If it yields multiple students, the list is shown to choose from.
 */
enum PageStudentPick {
    ;

    /**
     * Handles a POST from the page.
     *
     * @param cache   the data cache
     * @param site    the site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doPost(final Cache cache, final AdminSite site, final ServletRequest req,
                       final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        String stu = req.getParameter("pick_stu");
        if (stu != null) {
            stu = stu.trim();

            while (!stu.isEmpty() && !Character.isLetterOrDigit(stu.charAt(0))) { // stu changes in loop
                stu = stu.substring(1);
            }
            while (!stu.isEmpty() && !Character.isLetterOrDigit(stu.charAt(stu.length() - 1))) { // stu changes in loop
                stu = stu.substring(0, stu.length() - 1);
            }
        }

        if (stu == null || stu.isEmpty()) {
            final String path = site.site.path;
            resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH) //
                    ? "home.html" : "/home.html"));
        } else {
            // Try first as a student ID

            final String stuId = stu.replace(CoreConstants.DASH, CoreConstants.EMPTY)
                    .replace(CoreConstants.SPC, CoreConstants.EMPTY);

            // Log.info("Querying for ", stuId);

            final RawStudent student = RawStudentLogic.query(cache, stuId, false);

            if (student == null) {
                // Not a valid student ID, try as a name

                final String first;
                final String last;
                final int comma = stu.indexOf(CoreConstants.COMMA_CHAR);
                if (comma == -1) {
                    final int spc = stu.indexOf(' ');
                    if (spc == -1) {
                        // No space or comma - assume just last name
                        first = null;
                        last = stu;
                    } else {
                        // Assume "First Last" (could also be a two-word last name)
                        first = stu.substring(0, spc).trim();
                        last = stu.substring(spc + 1).trim();
                    }
                } else {
                    // Assume "Doe, John"
                    last = stu.substring(0, comma).trim();
                    first = stu.substring(comma + 1).trim();
                }

                if ((first == null || first.isEmpty()) && last.isEmpty()) {
                    // Nothing provided - re-display the query form
                    PageHome.doGet(cache, site, req, resp, session, null);
                } else {
                    // Get list of students that match name
                    List<RawStudent> students = RawStudentLogic.queryAllByName(cache, first == null
                                    || first.isEmpty() ? "%" : first,
                            last.isEmpty() ? "%" : last);

                    if (first != null && !first.isEmpty() && !last.isEmpty()) {
                        students.addAll(RawStudentLogic.queryAllByName(cache, "%",
                                first + CoreConstants.SPC + last));
                    }

                    if (students.isEmpty() && ((first != null) && !first.isEmpty())) {
                        // Try with first/last reversed
                        students = RawStudentLogic.queryAllByName(cache, last.isEmpty() ? "%" : last, first);
                    }

                    if (students.isEmpty()) {
                        PageHome.doGet(cache, site, req, resp, session, "No matching students found.");
                    } else if (students.size() == 1) {
                        // Show the page for the selected student
                        PageStudentInfo.doStudentInfoPage(cache, site, req, resp, session, students.getFirst());
                    } else {
                        // Present a list of the matching students, allow user to choose
                        showListOfStudents(cache, site, req, resp, session, students);
                    }
                }
            } else {
                final String path = site.site.path;
                resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH)
                        ? "office/student_info.html?stu=" + student.stuId
                        : "/office/student_info.html?stu=" + student.stuId));

                // Show the page for the selected student
                // PageStudentInfo.doStudentInfoPage(cache, site, req, resp, session, student);
            }
        }
    }

    /**
     * Displays a list of matching students and allows the user to select one.
     *
     * @param cache    the data cache
     * @param site     the site
     * @param req      the request
     * @param resp     the response
     * @param session  the login session
     * @param students the list of students from which to choose
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void showListOfStudents(final Cache cache, final AdminSite site, final ServletRequest req,
                                           final HttpServletResponse resp, final ImmutableSessionInfo session,
                                           final List<RawStudent> students)
            throws IOException, SQLException {

        Collections.sort(students);

        final HtmlBuilder htm = OfficePage.startOfficePage(cache, site, session, true);

        htm.addln("<h2>Student Status</h2>");

        htm.sP().add(Integer.toString(students.size()), " students matched your query:").eP();

        htm.sTable("report");
        htm.sTr();
        htm.sTh().add("First Name").eTh();
        htm.sTh().add("Last Name").eTh();
        htm.sTh().add("Student ID").eTh();
        htm.sTh().add("E-mail").eTh();
        htm.sTh().add("Registrations").eTh();
        htm.sTh().eTh();
        htm.eTr();

        final TermRec active = cache.getSystemData().getActiveTerm();

        for (final RawStudent student : students) {
            final List<RawStcourse> regs = active == null ? new ArrayList<>(0)
                    : RawStcourseLogic.getActiveForStudent(cache, student.stuId, active.term);
            Collections.sort(regs);

            htm.sTr();
            htm.sTd().add(student.firstName).eTd();
            htm.sTd().add(student.lastName).eTd();
            htm.sTd().add(student.stuId).eTd();
            htm.sTd().add(student.stuEmail).eTd();
            if (regs.isEmpty()) {
                htm.sTd().add("(none)").eTd();
            } else {
                htm.sTd();
                boolean comma = false;
                for (final RawStcourse reg : regs) {
                    if (comma) {
                        htm.add(", ");
                    }
                    htm.add(reg.course, " (", reg.sect, ")");
                    comma = true;
                }
                htm.eTd();
            }

            htm.sTd().add("<form action='student_info.html' method='post'>")
                    .add("<input type='hidden' name='stu' value='", student.stuId, "'/>")
                    .add("<input type='submit' value='Choose'></form>").eTd();
            htm.eTr();
        }
        htm.eTable();

        htm.div("vgap0");
        htm.sP().add("<a class='btn' href='home.html'>Back</a>").eP();

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm);
    }
}
