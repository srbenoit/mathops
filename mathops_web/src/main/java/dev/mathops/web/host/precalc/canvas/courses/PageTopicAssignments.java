package dev.mathops.web.host.precalc.canvas.courses;

import dev.mathops.commons.installation.EPath;
import dev.mathops.commons.installation.PathList;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.course.MetadataCourseModule;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.rec.main.StandardAssignmentRec;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardAssignmentAttemptRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.db.reclogic.term.StandardAssignmentAttemptLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.precalc.canvas.CanvasPageUtils;
import dev.mathops.web.host.precalc.canvas.CanvasSite;
import dev.mathops.web.host.precalc.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the homework assignments associated with a topic module.
 */
public enum PageTopicAssignments {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache     the data cache
     * @param site      the owning site
     * @param courseId  the course ID
     * @param moduleNbr the module number
     * @param req       the request
     * @param resp      the response
     * @param session   the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId,
                             final Integer moduleNbr, final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            Log.warning("No registration found for student ", stuId, " in course ", courseId);
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);

            if (course == null) {
                Log.warning("No course record for ", courseId);
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                final TermData termData = cache.getTermData();
                final StandardsCourseSectionRec section = termData.getStandardsCourseSection(registration.course,
                        registration.sect);

                if (section == null) {
                    Log.warning("No course section record for ", courseId, " section ", registration.sect);
                    final String homePath = site.makeRootPath("home.html");
                    resp.sendRedirect(homePath);
                } else {
                    final StandardsCourseModuleRec module = mainData.getStandardsCourseModule(courseId, moduleNbr);
                    if (module == null) {
                        Log.warning("No course module record for module ", moduleNbr, " in ", courseId);
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    } else {
                        // Locate "media root" which is typically /opt/public/media
                        final File wwwPath = PathList.getInstance().get(EPath.WWW_PATH);
                        final File publicPath = wwwPath.getParentFile();
                        final File mediaRoot = new File(publicPath, "media");

                        presentAssignmentsPage(cache, site, req, resp, registration, section, course,
                                module, mediaRoot);
                    }
                }
            }
        }
    }

    /**
     * Presents  a course and module.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param registration the student registration record
     * @param section      the course section object
     * @param course       the course object
     * @param module       the module object
     * @param mediaRoot    the root media directory relative to which the module path is specified
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentAssignmentsPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                       final HttpServletResponse resp, final RawStcourse registration,
                                       final StandardsCourseSectionRec section, final StandardsCourseRec course,
                                       final StandardsCourseModuleRec module, final File mediaRoot)
            throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, course, "../", ECanvasPanel.MODULES);

        htm.sDiv("flexmain");

        final MetadataCourseModule meta = new MetadataCourseModule(mediaRoot, module.modulePath,
                module.moduleNbr);

        if (meta.isValid()) {
            htm.sH(2);
            if (meta.thumbnailFile != null) {
                final String imageUrl = "/media/" + meta.moduleRelPath + "/" + meta.thumbnailFile;
                if (meta.thumbnailAltText == null) {
                    htm.addln("<img class='module-thumb' src='", imageUrl, "'/>");
                } else {
                    htm.addln("<img class='module-thumb' src='", imageUrl, "' alt='", meta.thumbnailAltText, "'/>");
                }
            }
            htm.sDiv("module-title");
            htm.add("Module ", module.moduleNbr, " Homework Assignments").br();
            htm.addln("<div style='color:#D9782D; margin-top:6px;'>", meta.title, "</div>");
            htm.addln("<a class='smallbtn' href='module.html'>Open Textbook Chapter</a> &nbsp;");
            htm.addln("<a class='smallbtn' href='targets.html'>Go to Learning Target Exams</a>");
            htm.eDiv();
            htm.eH(2);

            htm.hr();
            htm.div("vgap0");

            htm.sDiv("left");
            htm.addln("<img class='module-thumb' src='/www/images/etext/required_assignment_thumb.png' ",
                    "alt='A student doing homework.'/>");
            htm.eDiv();

            htm.sH(3).add("Module Homework Assignments").eH(3);
            htm.sDiv("clear").eDiv();

            htm.sP("indent").add("There is one homework assignment for each Learning Target in the module.").eP();

            htm.sP("indent").add(
                    "You have unlimited attempts on each assignment, but you must pass each assignment to ",
                    "unlock the Learning Target Exam so you can complete the Learning Target.").eP();

            htm.div("vgap");

            try {
                final List<StandardAssignmentRec> assignments = cache.getMainData().getStandardAssignments(
                        registration.course);
                final List<StandardAssignmentAttemptRec> attempts =
                        StandardAssignmentAttemptLogic.INSTANCE.queryByStudentCourse(cache, registration.stuId,
                                registration.course);

                for (final StandardAssignmentRec assignment : assignments) {
                    if (module.moduleNbr.equals(assignment.moduleNbr)) {
                        presentAssignment(htm, assignment, attempts);
                    }
                }
                htm.hr();
            } catch (final NumberFormatException ex) {
                Log.warning(ex);
                htm.sP().add("Error: Unable to load homework assignments for module ", module.moduleNbr).eP();
            }
        } else {
            htm.sP().add("Error: Unable to load configuration of module ", module.moduleNbr).eP();
        }

        htm.eDiv(); // flexmain
        htm.eDiv(); // pagecontainer

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Presents a single homework assignment.
     *
     * @param htm        the {@code HtmlBuilder} to which to append
     * @param assignment the assignment
     * @param attempts   the set of all submitted student assignments (of all types) in this course
     */
    private static void presentAssignment(final HtmlBuilder htm, final StandardAssignmentRec assignment,
                                          final List<StandardAssignmentAttemptRec> attempts) {

        htm.hr();
        htm.sH(4).add("Homework Assignment ", assignment.moduleNbr, ".", assignment.standardNbr).eH(4);

        int numAttempts = 0;
        boolean passed = false;
        for (final StandardAssignmentAttemptRec attempt : attempts) {
            if (attempt.assignmentId.equals(assignment.assignmentId)) {
                if ("Y".equals(attempt.passed)) {
                    passed = true;
                    ++numAttempts;
                } else if ("N".equals(attempt.passed)) {
                    ++numAttempts;
                }
                // Other values for "passed" indicate an "ignored" attempt
            }
        }

        htm.sP("indent");
        if (passed) {
            htm.add("<img style='position:relative; top:3px;' src='/www/images/etext/box_checked_18.png' ",
                    "alt='Box with check mark'/>&nbsp; ");
            htm.add("You have PASSED this assignment.");
        } else {
            htm.add("<img style='position:relative; top:3px;' src='/www/images/etext/box_unchecked_18.png' ",
                    "alt='Empty box'/>&nbsp; ");

            if (numAttempts == 0) {
                htm.add("You have not yet attempted this assignment.");
            } else if (numAttempts == 1) {
                htm.add("You have attempted this assignment 1 time.");
            } else {
                final String count = Integer.toString(numAttempts);
                htm.add("You have attempted this assignment ", count, " times.");
            }
        }
        htm.eP();

        htm.sP("indent2");
        if (passed) {
            htm.add("<a class='smallbtn' href=''>Continue to practice this assignment...</a>");
        } else {
            htm.add("<a class='smallbtn' href=''>Attempt this assignment...</a>");
        }
        htm.eP();
    }
}
