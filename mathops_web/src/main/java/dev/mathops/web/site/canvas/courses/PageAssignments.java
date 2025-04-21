package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.old.rawlogic.RawSttermLogic;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.old.rawrecord.RawStterm;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.rec.main.StandardAssignmentRec;
import dev.mathops.db.rec.main.StandardsCourseModuleRec;
import dev.mathops.db.rec.term.StandardsCourseGradingSystemRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.db.rec.term.StandardsMilestoneRec;
import dev.mathops.db.rec.term.StudentCourseMasteryRec;
import dev.mathops.db.rec.term.StudentStandardsMilestoneRec;
import dev.mathops.db.reclogic.TermLogic;
import dev.mathops.db.reclogic.term.StudentCourseMasteryLogic;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.canvas.CanvasPageUtils;
import dev.mathops.web.site.canvas.CanvasSite;
import dev.mathops.web.site.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * This page shows the "Assignments" content.
 */
public enum PageAssignments {
    ;

    /**
     * Starts the page that shows the status of all assignments and grades.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final Metadata metadata) throws IOException,
            SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MetadataCourse metaCourse = metadata.getCourse(registration.course);
            if (metaCourse == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                final TermData termData = cache.getTermData();
                final StandardsCourseSectionRec section = termData.getStandardsCourseSection(
                        registration.course, registration.sect);

                if (section == null) {
                    // TODO: Error display, section not part of this system rather than a redirect to Home
                    final String homePath = site.makeRootPath("home.html");
                    resp.sendRedirect(homePath);
                } else {
                    final StandardsCourseGradingSystemRec gradingSystem =
                            termData.getStandardsCourseGradingSystem(section.gradingSystemId);
                    if (gradingSystem == null) {
                        // TODO: Error display, section not part of this system rather than a redirect to Home
                        final String homePath = site.makeRootPath("home.html");
                        resp.sendRedirect(homePath);
                    } else {
                        presentAssignments(cache, site, req, resp, session, registration, section, gradingSystem,
                                metaCourse);
                    }
                }
            }
        }
    }

    /**
     * Presents the "Assignments" information.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @param section      the course section information
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentAssignments(final Cache cache, final CanvasSite site, final ServletRequest req,
                                   final HttpServletResponse resp, final ImmutableSessionInfo session,
                                   final RawStcourse registration, final StandardsCourseSectionRec section,
                                   final StandardsCourseGradingSystemRec gradingSystem,
                                   final MetadataCourse metaCourse) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();

        CanvasPageUtils.startPage(htm, siteTitle);

        // Emit the course number and section at the top
        CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, section);

        htm.sDiv("pagecontainer");

        CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, null, ECanvasPanel.ASSIGNMENTS);

        htm.sDiv("flexmain");

        htm.sH(2).add("Assignments").eH(2);
        htm.sP().add("This page shows every assignment in the course, grouped by module.").eP();
        htm.hr();

        // Determine the student's pace and track
        final TermRec active = TermLogic.get(cache).queryActive(cache);
        final RawStterm stterm = RawSttermLogic.query(cache, active.term, registration.stuId);
        final Integer index = registration.paceOrder;

        if (stterm == null || index == null) {
            htm.sP().add("Error: Unable to determine your deadline schedule.  Please contact the Precalculus Center ",
                    "at <a class='ulink' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu</a> to ",
                    "report this error (please include your CSU ID number and the course number).").eP();
        } else {
            final MainData mainData = cache.getMainData();
            final TermData termData = cache.getTermData();

            // Load all milestones for the student's pace/track/index and any student overrides
            final List<StandardsMilestoneRec> milestones = termData.getStandardsMilestonesByTrackAndPaceAndIndex(
                    stterm.paceTrack, stterm.pace, index);
            final List<StudentStandardsMilestoneRec> stmilestones =
                    termData.getStudentStandardsMilestonesByTrackAndPaceAndIndex(registration.stuId, stterm.paceTrack,
                            stterm.pace, index);

            // Load all the assignments associated with the course
            final List<StandardAssignmentRec> assignments = mainData.getStandardAssignments(registration.course);

            // Get the list of course modules
            final List<StandardsCourseModuleRec> modules = mainData.getStandardsCourseModules(registration.course);

            StudentCourseMasteryRec courseMastery = termData.getStudentCourseMastery(registration.stuId,
                    registration.course);
            if (courseMastery == null) {
                courseMastery = StudentCourseMasteryLogic.buildCourseMastery(cache, registration.stuId,
                        registration.course);
            }
            // Show each module's homework assignments with their status

            final int nbrModules = modules.size();
            for (final StandardsCourseModuleRec module : modules) {
                final int moduleNbr = module.moduleNbr.intValue();

                startModule(htm, "Module " + module.moduleNbr + " Homework Assignments");

                htm.sDiv("module-item");
                for (int standardNbr = 1; standardNbr <= module.nbrStandards; ++standardNbr) {
                    emitHw(htm, courseMastery, moduleNbr, standardNbr);
                }
                htm.eDiv(); // module-item

                endModule(htm);
            }

            // Show the overall status of the course mastery exam.

            startModule(htm, "Course Mastery Exam");

            htm.sDiv("module-item");

            final String nbrModulesStr = Integer.toString(nbrModules);
            final int totalQuestions = gradingSystem.nbrStandards.intValue() << 1;
            final String totalQuestionsStr = Integer.toString(totalQuestions);
            htm.sP().add("The Course Mastery Exam has two questions for each standard in each of the ", nbrModulesStr,
                    " modules, or ", totalQuestionsStr, " questions total.").eP();

            htm.sP().add("There is a <u>deadline date</u> for each module's questions.").eP();

            htm.sP().add("Your score on the Course Mastery Exam is <b>", gradingSystem.onTimeMasteryPts,
                    " points</b> if you answer both questions for a standard correctly \"on time\" (on or before its ",
                    "deadline), and <b>", gradingSystem.lateMasteryPts,
                    " points</b> if get both questions correct \"late\" (after its deadline).").eP();

            htm.sP().add("When a Homework Assignment is passed, the corresponding questions on the Course Mastery ",
                    "Exam are unlocked.").eP();

            htm.sP().add("Any time you have questions unlocked, you can ask to take the Course Mastery Exam in the ",
                    "Precalculus Center (Weber 138).  You will be given an exam with all unlocked questions that ",
                    "you have not already answered correctly.").eP();

            htm.sP().add("You have unlimited attempts on the Course Mastery Exam, through the last day of ",
                    "classes, and your score increases as you complete more standards.").eP();

            final int maxScore = gradingSystem.nbrStandards.intValue() * gradingSystem.onTimeMasteryPts.intValue();
            final String maxScoreStr = Integer.toString(maxScore);
            htm.sP().add("The maximum possible score is ", maxScoreStr,
                    " points.  Your grade in the course is based on your score on this exam.").eP();

            htm.sTable("grades", "style='margin-left: 20px;'");
            htm.sTr().sTh().add("Score").eTh().sTh().add("Earned Grade").eTh().eTr();

            htm.sTr().sTd().add(gradingSystem.aMinScore, " - ", maxScoreStr).eTh().sTh().add("A").eTh().eTr();
            int bound = gradingSystem.aMinScore.intValue() - 1;
            if (gradingSystem.bMinScore != null) {
                final String boundStr = Integer.toString(bound);
                htm.sTr().sTd().add(gradingSystem.bMinScore, " - ", boundStr).eTh().sTh().add("B").eTh().eTr();
                bound = gradingSystem.bMinScore.intValue() - 1;
            }
            if (gradingSystem.cMinScore != null) {
                final String boundStr = Integer.toString(bound);
                htm.sTr().sTd().add(gradingSystem.cMinScore, " - ", boundStr).eTh().sTh().add("C").eTh().eTr();
                bound = gradingSystem.cMinScore.intValue() - 1;
            }
            if (gradingSystem.dMinScore != null) {
                final String boundStr = Integer.toString(bound);
                htm.sTr().sTd().add(gradingSystem.dMinScore, " - ", boundStr).eTh().sTh().add("D").eTh().eTr();
                bound = gradingSystem.dMinScore.intValue() - 1;
            }
            if (gradingSystem.uMinScore != null) {
                final String boundStr = Integer.toString(bound);
                htm.sTr().sTd().add(gradingSystem.uMinScore, " - ", boundStr).eTh().sTh().add("D").eTh().eTr();
                bound = gradingSystem.uMinScore.intValue() - 1;
                final String boundStr2 = Integer.toString(bound);
                htm.sTr().sTd().add(boundStr2, " or less").eTh().sTh().add("F").eTh().eTr();
            } else {
                final String boundStr = Integer.toString(bound);
                htm.sTr().sTd().add(boundStr, " or less").eTh().sTh().add("U").eTh().eTr();
            }
            htm.eTable();

            htm.eDiv(); // module-item

            endModule(htm);
        }

        CanvasPageUtils.endPage(htm);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Finds an assignment with a specified module number, standard number, and type iin a list of assignments.
     *
     * @param moduleNbr   the module number for which to search
     * @param standardNbr the standard number for which to search
     * @param type        the assignment type for which to search
     * @param assignments the list of assignments
     * @return the matching assignment; {@code null} if none found
     */
    private static StandardAssignmentRec find(final int moduleNbr, final int standardNbr, final String type,
                                              final Iterable<StandardAssignmentRec> assignments) {
        StandardAssignmentRec result = null;

        for (final StandardAssignmentRec test : assignments) {
            if (test.moduleNbr.intValue() == moduleNbr && test.standardNbr.intValue() == standardNbr
                && test.assignmentType.equals(type)) {
                result = test;
                break;
            }
        }

        return result;
    }

    /**
     * Emits an assignment entry for a homework.
     *
     * @param htm           the {@code HtmlBuilder} to which to append
     * @param courseMastery the course mastery status
     * @param moduleNbr     the module number
     * @param standardNbr   the standard number
     */
    private static void emitHw(final HtmlBuilder htm, final StudentCourseMasteryRec courseMastery,
                               final int moduleNbr, final int standardNbr) {

        htm.addln("<img class='assignment-icon' src='/www/images/etext/video_icon22.png' alt=''/>");
        htm.sDiv("assignment-title");

        htm.sDiv("module-item-block");
        htm.addln("<a class='ulink2' href='homework.html'><b>Homework ", moduleNbr, ".", standardNbr, "</b></a>").br();

        htm.add("<small>");
        htm.add("&nbsp;</small>");
        htm.eDiv();

        htm.eDiv(); // assignment-title
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the module title
     */
    private static void startModule(final HtmlBuilder htm, final String title) {

        htm.addln("<details open class='module'>");
        htm.addln("  <summary class='module-summary'>", title, "</summary>");
    }

    /**
     * Emits the HTML to end a module.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endModule(final HtmlBuilder htm) {

        htm.addln("</details>");
    }
}
