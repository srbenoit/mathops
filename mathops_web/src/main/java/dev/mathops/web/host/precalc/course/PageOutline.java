package dev.mathops.web.host.precalc.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.enums.EProctoringOption;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.logic.course.MilestoneLogic;
import dev.mathops.db.old.rawlogic.RawSpecialStusLogic;
import dev.mathops.db.old.rawlogic.RawStcourseLogic;
import dev.mathops.db.old.rawlogic.RawStexamLogic;
import dev.mathops.db.schema.legacy.RawCourse;
import dev.mathops.db.schema.legacy.RawCsection;
import dev.mathops.db.schema.legacy.RawCunit;
import dev.mathops.db.schema.legacy.RawCuobjective;
import dev.mathops.db.schema.legacy.RawCusection;
import dev.mathops.db.schema.legacy.RawExam;
import dev.mathops.db.schema.legacy.RawLesson;
import dev.mathops.db.schema.legacy.RawLessonComponent;
import dev.mathops.db.schema.legacy.RawMilestone;
import dev.mathops.db.schema.legacy.RawSpecialStus;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.schema.legacy.RawStexam;
import dev.mathops.db.schema.legacy.RawStmilestone;
import dev.mathops.db.schema.legacy.RawStterm;
import dev.mathops.db.schema.legacy.RawStudent;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.db.type.TermKey;
import dev.mathops.session.ExamWriter;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourse;
import dev.mathops.session.sitelogic.data.SiteDataCfgCourseStatus;
import dev.mathops.session.sitelogic.data.SiteDataCfgExamStatus;
import dev.mathops.session.sitelogic.data.SiteDataCfgUnit;
import dev.mathops.session.sitelogic.data.SiteDataCourse;
import dev.mathops.session.sitelogic.data.SiteDataMilestone;
import dev.mathops.session.sitelogic.data.SiteDataStatus;
import dev.mathops.session.sitelogic.servlet.CourseLesson;
import dev.mathops.session.sitelogic.servlet.StudentCourseStatus;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.text.builder.SimpleBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.html.unitexam.UnitExamSessionStore;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.time.chrono.ChronoZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * Generates the content of the web page that displays the outline of a course tailored to a student's position and
 * status in the course.
 */
enum PageOutline {
    ;

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param site     the owning site
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final ESiteType siteType, final CourseSite site,
                      final ServletRequest req, final HttpServletResponse resp,
                      final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String mode = req.getParameter("mode");
        final String errorExam = req.getParameter("errorExam");
        final String error = req.getParameter("error");

        if (AbstractSite.isParamInvalid(course) || AbstractSite.isParamInvalid(mode)
                || AbstractSite.isParamInvalid(errorExam) || AbstractSite.isParamInvalid(error)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            Log.warning("  mode='", mode, "'");
            Log.warning("  errorExam='", errorExam, "'");
            Log.warning("  error='", error, "'");
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else if (course == null || mode == null) {
            PageError.doGet(cache, site, req, resp, session,
                    "No course and mode provided for course outline");
        } else {
            emitOutlineContent(cache, siteType, site, req, resp, session, logic, course, mode, errorExam, error, null);
        }
    }

    /**
     * Emits the outline content.
     *
     * @param cache     the data cache
     * @param siteType  the site type
     * @param site      the owning site
     * @param req       the request
     * @param resp      the response
     * @param session   the user's login session information
     * @param logic     the course site logic
     * @param course    the course ID
     * @param mode      the page mode
     * @param errorExam the error exam
     * @param error     the error
     * @param preMsg    an optional message to show at the top of the page
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitOutlineContent(final Cache cache, final ESiteType siteType, final CourseSite site,
                                           final ServletRequest req, final HttpServletResponse resp,
                                           final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                           final String course, final String mode, final String errorExam,
                                           final String error, final String preMsg) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null,
                false, true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        doOutline(cache, siteType, site, session, logic, course, mode, errorExam, error, htm, null, preMsg);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }

    /**
     * Processes a POST request when the user wants to apply an available accommodation extension.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param site     the owning site
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doRequestAccomExtension(final Cache cache, final ESiteType siteType, final CourseSite site,
                                        final ServletRequest req, final HttpServletResponse resp,
                                        final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String stu = req.getParameter("stu");
        final String track = req.getParameter("track");
        final String pace = req.getParameter("pace");
        final String index = req.getParameter("index");
        final String unit = req.getParameter("unit");
        final String type = req.getParameter("type");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            PageError.doGet(cache, site, req, resp, session, "No course and mode provided for course outline");
        } else if (AbstractSite.isParamInvalid(stu) || AbstractSite.isParamInvalid(track)
                || AbstractSite.isParamInvalid(pace) || AbstractSite.isParamInvalid(index)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(type)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stu='", stu, "'");
            Log.warning("  track='", track, "'");
            Log.warning("  pace='", pace, "'");
            Log.warning("  index='", index, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  type='", type, "'");

            final String msg = """
                    We were unable to apply your accommodation extension.
                    Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
            emitOutlineContent(cache, siteType, site, req, resp, session, logic, course, "course", null, null, msg);
        } else {
            String msg = null;

            if (stu != null && track != null && pace != null && index != null && unit != null && type != null) {
                if (session.getEffectiveUserId().equals(stu)) {
                    try {
                        final int paceValue = Integer.parseInt(pace);
                        final int indexValue = Integer.parseInt(index);
                        final int unitValue = Integer.parseInt(unit);

                        final int days = MilestoneLogic.applyLegacyAccommodationExtension(cache, stu, track,
                                paceValue, indexValue, unitValue, type);

                        logic.gatherData();

                        final String typeStr = "RE".equals(type) ? "Review Exam" :
                                ("FE".equals(type) ? "Final Exam" : "Exam");

                        if (days == 0) {
                            msg = """
                                    We were unable to apply your accommodation extension.
                                    Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                        } else {
                            if (days > 100) {
                                final int granted = days % 100;
                                final String grantedStr = Integer.toString(granted);
                                final int available = days / 100;
                                final String availableStr = Integer.toString(available);
                                msg = SimpleBuilder.concat("You had an extension of ", availableStr,
                                        " days available for the Unit ", unit, " ", typeStr,
                                        " based on your accommodation, but there were only ", grantedStr,
                                        " days before the end of the term, so we moved your deadline to the end of " +
                                                "the ",
                                        "term.  If you cannot finish the course by the end of the term, please stop " +
                                                "in to ",
                                        "the Precalculus Center (Weber 137) or send an email to ",
                                        "precalc_math@colostate.edu to discuss your situation.");
                            } else if (days > 0 || days == -1) {
                                msg = SimpleBuilder.concat("Your accommodation extension on the Unit ", unit, " ",
                                        typeStr,
                                        " has been applied.");
                            }
                        }
                    } catch (final NumberFormatException ex) {
                        Log.warning("Attempt to apply accommodation extension for ", stu, " with invalid parameters.");
                        msg = """
                                We were unable to apply your accommodation extension.
                                Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                    }
                } else {
                    Log.warning("Attempt to apply accommodation extension for ", stu,
                            " from session with effective user ID ", session.getEffectiveUserId());
                    msg = """
                            We were unable to apply your accommodation extension.
                            Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                }
            } else {
                Log.warning("Attempt to apply accommodation extension for ", stu, " with missing required parameters.");
                msg = """
                        We were unable to apply your accommodation extension.
                        Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
            }

            emitOutlineContent(cache, siteType, site, req, resp, session, logic, course, "course", null, null, msg);
        }
    }

    /**
     * Processes a POST request when the user wants to apply an available free extension.
     *
     * @param cache    the data cache
     * @param siteType the site type
     * @param site     the owning site
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param logic    the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doRequestFreeExtension(final Cache cache, final ESiteType siteType, final CourseSite site,
                                       final ServletRequest req, final HttpServletResponse resp,
                                       final ImmutableSessionInfo session, final CourseSiteLogic logic)
            throws IOException, SQLException {

        final String course = req.getParameter("course");
        final String stu = req.getParameter("stu");
        final String track = req.getParameter("track");
        final String pace = req.getParameter("pace");
        final String index = req.getParameter("index");
        final String unit = req.getParameter("unit");
        final String type = req.getParameter("type");

        if (AbstractSite.isParamInvalid(course)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  course='", course, "'");
            PageError.doGet(cache, site, req, resp, session, "No course and mode provided for course outline");
        } else if (AbstractSite.isParamInvalid(stu) || AbstractSite.isParamInvalid(track)
                || AbstractSite.isParamInvalid(pace) || AbstractSite.isParamInvalid(index)
                || AbstractSite.isParamInvalid(unit) || AbstractSite.isParamInvalid(type)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  stu='", stu, "'");
            Log.warning("  track='", track, "'");
            Log.warning("  pace='", pace, "'");
            Log.warning("  index='", index, "'");
            Log.warning("  unit='", unit, "'");
            Log.warning("  type='", type, "'");

            final String msg = """
                    We were unable to apply your free extension.
                    Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
            emitOutlineContent(cache, siteType, site, req, resp, session, logic, course, "course", null, null, msg);
        } else {
            String msg = null;

            if (stu != null && track != null && pace != null && index != null && unit != null && type != null) {
                if (session.getEffectiveUserId().equals(stu)) {
                    try {
                        final int paceValue = Integer.parseInt(pace);
                        final int indexValue = Integer.parseInt(index);
                        final int unitValue = Integer.parseInt(unit);

                        final int days = MilestoneLogic.applyLegacyFreeExtension(cache, stu, track,
                                paceValue, indexValue, unitValue, type);

                        logic.gatherData();

                        final String typeStr = "RE".equals(type) ? ("Unit " + unit + " Review Exam") :
                                ("FE".equals(type) ? "Final Exam" : "Exam");

                        if (days > 0 || days == -1) {
                            msg = SimpleBuilder.concat("Your free extension on the ", typeStr, " has been applied.");
                        } else if (days == 0) {
                            msg = """
                                    We were unable to apply your free extension.
                                    Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                        }
                    } catch (final NumberFormatException ex) {
                        Log.warning("Attempt to apply free extension for ", stu, " with invalid parameters.");
                        msg = """
                                We were unable to apply your free extension.
                                Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                    }
                } else {
                    Log.warning("Attempt to apply free extension for ", stu,
                            " from session with effective user ID ", session.getEffectiveUserId());
                    msg = """
                            We were unable to apply your free extension.
                            Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
                }
            } else {
                Log.warning("Attempt to apply free extension for ", stu, " with missing required parameters.");
                msg = """
                        We were unable to apply your free extension.
                        Please send an email to precalc_math@colostate.edu to let us know of this issue.""";
            }

            emitOutlineContent(cache, siteType, site, req, resp, session, logic, course, "course", null, null, msg);
        }
    }

    /**
     * Creates the HTML of the course outline.
     *
     * @param cache              the data cache
     * @param siteType           the site type
     * @param site               the owning site
     * @param session            the user's login session information
     * @param logic              the course site logic
     * @param courseId           the course for which to generate the status page
     * @param mode               the mode - one of "course" (normal access), "practice" (not in course, just using
     *                           e-text), "locked" (in course, but past all deadlines so practice access only)
     * @param errorExam          the exam ID with which an error message is associated
     * @param error              the error message
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @param preMsg             an optional message to show at the top of the page
     * @throws SQLException if there is an error accessing the database
     */
    static void doOutline(final Cache cache, final ESiteType siteType, final CourseSite site,
                          final ImmutableSessionInfo session, final CourseSiteLogic logic, final String courseId,
                          final String mode, final String errorExam, final String error, final HtmlBuilder htm,
                          final String skillsReviewCourse, final String preMsg) throws SQLException {

        final ZonedDateTime now = session.getNow();
        final String stuId = session.getEffectiveUserId();

        final boolean isPractice = !"course".equals(mode);

        final SystemData systemData = cache.getSystemData();
        final TermRec activeTerm = systemData.getActiveTerm();
        final RawCourse course = systemData.getCourse(courseId);

        String defaultSect;
        if ("Y".equals(course.isTutorial)) {
            defaultSect = "1";
        } else {
            defaultSect = "001";
            final List<RawCsection> csections = systemData.getCourseSections(activeTerm.term);
            csections.sort(null);

            for (final RawCsection test : csections) {
                if (test.course.equals(courseId)) {
                    defaultSect = test.sect;
                    break;
                }
            }
        }

        RawStcourse studentCourse = RawStcourseLogic.getRegistration(cache, stuId, courseId);
        RawCsection csection;

        if (studentCourse == null) {
            csection = systemData.getCourseSection(courseId, defaultSect, activeTerm.term);
            if (csection != null) {
                studentCourse = new RawStcourse();
                studentCourse.stuId = stuId;
                studentCourse.course = courseId;
                studentCourse.sect = defaultSect;
                studentCourse.paceOrder = Integer.valueOf(1);
                studentCourse.openStatus = "Y";
                studentCourse.completed = "N";
                studentCourse.prereqSatis = "Y";
                studentCourse.finalClassRoll = "Y";
                studentCourse.iInProgress = "N";
                studentCourse.instrnType = "CE";
                studentCourse.synthetic = true;
            }
        } else {
            csection = systemData.getCourseSection(courseId, studentCourse.sect, activeTerm.term);
        }

        if (csection != null && "MAS".equals(csection.gradingStd)) {
            PageStdsCourse.masteryCoursePanel(cache, logic, course, studentCourse, csection, htm);

            if ("888888888".equals(stuId)) {
                htm.div("vgap");
                htm.sP().add("<a href='course_media.html?course=", courseId.replace(CoreConstants.SPC, "%20"),
                        "'>All Course Media</a>").eP();
            }
        } else {
            final StudentCourseStatus courseStatus = new StudentCourseStatus(site.site.profile);

            if (courseStatus.gatherData(cache, session, stuId, courseId, false, isPractice)
                    && courseStatus.getCourse().courseName != null) {

                csection = courseStatus.getCourseSection();
                if ("course".equals(mode)) {

                    if (preMsg != null) {
                        htm.sDiv("box");
                        htm.add("<b>", preMsg, "</b>");
                        htm.eDiv();
                        htm.div("vgap2");
                    }

                    if (skillsReviewCourse == null) {

                        // Normal course display (user's exam, e-text, status, course outline)
                        doUsersExamLink(cache, now, courseStatus, htm);

                        if (courseStatus.isStudentLicensed()) {

                            final String section = csection.sect;

                            htm.sH(2, "title");
                            if ("Y".equals(courseStatus.getCourseSection().courseLabelShown)) {
                                htm.add(courseStatus.getCourse().courseLabel);
                                htm.add(": ");
                            }
                            htm.add(courseStatus.getCourse().courseName);
                            if (section != null) {
                                htm.br().add("<small>Section ", section, "</small>");
                            }
                            htm.eH(2);

                            doCourseStatus(cache, logic, courseStatus, htm);

                            doCourseOutline(cache, siteType, site, session, logic, courseStatus, mode, errorExam,
                                    error, htm, null);
                        }
                    } else if (courseStatus.gatherData(cache, session, stuId, courseId, true,
                            false) && courseStatus.getCourse().courseName != null) {

                        // Displaying the course as a Skills Review, so no checks
                        doCourseOutline(cache, siteType, site, session, logic, courseStatus, mode, errorExam, error,
                                htm, skillsReviewCourse);
                    } else {
                        htm.sP().add("FAILED TO GET COURSE DATA 2").br();
                        if (courseStatus.getErrorText() != null) {
                            htm.add(courseStatus.getErrorText());
                        }
                        htm.eP();
                    }
                } else if (skillsReviewCourse == null) {
                    // Accessing course in practice/locked out mode...

                    // No users exam in practice, but keep it in locked out mode
                    if ("locked".equals(mode)) {
                        doUsersExamLink(cache, now, courseStatus, htm);
                    }

                    if ("locked".equals(mode)) {
                        // Require licensed in locked mode
                        if (courseStatus.isStudentLicensed()) {

                            // TODO: Locked status display at top

                            doCourseOutline(cache, siteType, site, session, logic, courseStatus, mode, errorExam,
                                    error, htm, null);
                        }
                    } else {
                        // Don't require licensed in practice mode
                        doCourseOutline(cache, siteType, site, session, logic, courseStatus, mode, errorExam, error,
                                htm, null);
                    }
                } else {
                    // Accessing course as a Skills Review in practice/locked out mode...
                    doCourseOutline(cache, siteType, site, session, logic, courseStatus, mode, errorExam, error, htm,
                            skillsReviewCourse);
                }
            } else {
                htm.sP().add("FAILED TO GET COURSE DATA 4").br();
                if (courseStatus.getErrorText() != null) {
                    htm.add(courseStatus.getErrorText());
                }
                htm.eP();
            }
        }
    }

    /**
     * Tests whether the student is licensed, and if not, presents the user's exam link.
     *
     * @param cache        the data cache
     * @param now          the date/time to consider as "now"
     * @param courseStatus the available lessons
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUsersExamLink(final Cache cache, final ChronoZonedDateTime<LocalDate> now,
                                        final StudentCourseStatus courseStatus, final HtmlBuilder htm)
            throws SQLException {

        if (!courseStatus.isStudentLicensed()) {
            htm.div("vgap");

            htm.sDiv("indent11");
            htm.sDiv("advice", "style='text-align:center;'");
            htm.addln("  <span class='why_unavail'>");
            htm.addln("   <strong>You have not yet passed the User's Exam.</strong>");
            htm.addln("  </span>").br();
            htm.addln("  You must pass the User's Exam before you may access course");
            htm.addln("  materials.").br().br();

            final SystemData systemData = cache.getSystemData();
            final TermRec active = systemData.getActiveTerm();

            final RawCusection cusection = systemData.getCourseUnitSection(RawRecordConstants.M100U, "1",
                    Integer.valueOf(1), active.term);
            final boolean avail;
            if (cusection == null) {
                htm.eP();
                htm.sP().add("Information on User's Exam not found.");
                avail = false;
            } else if ((cusection.firstTestDt == null)
                    || !cusection.firstTestDt.isAfter(now.toLocalDate())) {
                avail = true;
            } else {
                htm.eP();
                htm.sP().add("The User's Exam will be available ",
                        TemporalUtils.FMT_MDY.format(cusection.firstTestDt), CoreConstants.DOT);
                avail = false;
            }

            if (avail) {
                htm.addln("<form method='get' action='run_review.html'>");
                htm.addln(" <input type='hidden' name='mode' value='course'/>");
                htm.addln(" <input type='hidden' name='exam' value='UOOOO'/>");
                htm.addln(" <button class='btn' type='submit'>Take User's Exam</button>");
                htm.addln("</form>");
            }

            htm.br();
            htm.eDiv();
            htm.eDiv();
        }
    }

    /**
     * Generates the course status display at the top of the outline.
     *
     * @param cache        the data cache
     * @param logic        the course site logic
     * @param courseStatus the student's status in the course
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCourseStatus(final Cache cache, final CourseSiteLogic logic,
                                       final StudentCourseStatus courseStatus,
                                       final HtmlBuilder htm) throws SQLException {

        htm.sDiv("coursestatus");

        final RawStcourse reg = courseStatus.getStudentCourse();

        if ("F".equals(reg.courseGrade)) {
            htm.sP("red");
            htm.addln(" A progress report for this course is not available.  ", //
                    "Please contact the Precalculus Center at ",
                    "<a class='ulink2' href='mailto:precalc_math@colostate.edu'>precalc_math@colostate.edu",
                    "</a> with questions concerning your grade in this course.");
            htm.sP();
        } else {
            statusContent(cache, logic, reg, htm);
        }

        htm.eDiv();
    }

    /**
     * Show the content of the course status page.
     *
     * @param cache the data cache
     * @param logic the course site logic
     * @param reg   the course registration
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void statusContent(final Cache cache, final CourseSiteLogic logic,
                                      final RawStcourse reg, final HtmlBuilder htm) throws SQLException {

        final String courseId = reg.course;
        final String sect = reg.sect;

        // Show incomplete status if this is an incomplete.
        if (reg.iTermKey != null) {
            final TermRec incTerm = logic.data.registrationData.getRegistrationTerm(courseId, sect);

            htm.sDiv("indent11");
            htm.sP("red");
            htm.add("<strong>This course is an incomplete from the ", incTerm.term.longString, " semester.</strong>");
            htm.eP();
            htm.eDiv();
        }

        final SiteDataCourse courseData = logic.data.courseData;
        final SiteDataCfgCourse cfgCourse = courseData.getCourse(courseId, sect);
        final Integer maxUnit = courseData.getMaxUnit(courseId);

        if (cfgCourse != null && maxUnit != null) {
            final SiteDataStatus status = logic.data.statusData;
            final SiteDataMilestone msData = logic.data.milestoneData;

            // Print all pace deadlines, including student override dates
            final SystemData systemData = cache.getSystemData();
            final TermRec term = systemData.getActiveTerm();
            final TermKey termKey = reg.iTermKey == null ? (term == null ? null : term.term) : reg.iTermKey;

            // Log.info("Querying milestones for ", termKey);

            final List<RawMilestone> allMilestones = msData.getMilestones(termKey);
            final List<RawStmilestone> stMilestones = msData.getStudentMilestones(termKey);

            // Get the scores and display...

            htm.sDiv("indent22");
            htm.sH(3).add("Your Current Status:").eH(3);

            boolean allProctoredPassed = true;

            htm.sTable("scoretable");

            htm.sTr();
            htm.sTh().eTh();
            htm.sTh().add("Review Exam").eTh();
            htm.sTh().add("Proctored Exam").eTh();
            htm.sTh().add("Points").eTh();
            htm.eTr();

            final int maxInt = maxUnit.intValue();
            for (int i = 0; i <= maxInt; ++i) {

                final SiteDataCfgUnit unitCfg = courseData.getCourseUnit(courseId, Integer.valueOf(i));
                if (unitCfg == null) {
                    continue;
                }

                final RawCunit cu = unitCfg.courseUnit;
                final RawCusection cusect = unitCfg.courseSectionUnit;

                htm.sTr();

                // There is no "course unit" record for unit 0 presently, so treat
                // any such condition as being a gateway unit
                final String unitType = cu == null ? "SR" : cu.unitType;

                if (cusect != null && "INST".equals(unitType)) {

                    Integer rePoints = null;
                    Integer uePoints = null;

                    // Unit column:
                    htm.sTd("scoreh").add("Unit&nbsp;", Integer.toString(i), ":").eTd();

                    final SiteDataCfgExamStatus reStatus =
                            status.getExamStatus(courseId, Integer.valueOf(i), "R");

                    // Review exam column:
                    htm.sTd("scored");

                    if (reStatus == null) {
                        htm.add("<span class='dim'>(None)</span>");
                    } else {
                        // Get this student's deadline for the review exam
                        String reDeadline = null;
                        if (reg.paceOrder != null) {
                            for (final RawMilestone test : allMilestones) {
                                final int msNumber = test.msNbr.intValue();
                                if (msNumber / 10 % 10 != reg.paceOrder.intValue()) {
                                    continue;
                                }

                                if (msNumber % 10 == i && "RE".equals(test.msType)) {
                                    reDeadline = TemporalUtils.FMT_MD.format(test.msDate).replace(" ", "&nbsp;");
                                }
                            }
                            for (final RawStmilestone test : stMilestones) {
                                final int msNumber = test.msNbr.intValue();
                                if (msNumber / 10 % 10 != reg.paceOrder.intValue()) {
                                    continue;
                                }

                                if (msNumber % 10 == i && "RE".equals(test.msType) && reDeadline != null) {
                                    reDeadline = TemporalUtils.FMT_MD.format(test.msDate).replace(" ", "&nbsp;");
                                }
                            }
                        }

                        if (reStatus.totalAttemptsSoFar == 0) {
                            if (reDeadline == null) {
                                htm.add("<span class='dim'>Not&nbsp;taken</span>");
                            } else {
                                htm.add("<span class='dim'>Not&nbsp;taken - </span><strong>due&nbsp;", reDeadline,
                                        "</strong>");
                            }
                        } else if (reStatus.firstPassingDate == null) {
                            if (reDeadline == null) {
                                htm.add("Not&nbsp;yet&nbsp;passed");
                            } else {
                                htm.add("Not&nbsp;yet&nbsp;passed - <strong>due&nbsp;", reDeadline, "</strong>");
                            }
                        } else if (cusect.rePointsOntime != null
                                && cusect.rePointsOntime.intValue() > 0) {

                            if (reStatus.passedOnTime) {
                                rePoints = Integer.valueOf(reStatus.onTimePoints);
                                htm.add("<strong>Passed (on&nbsp;time)</strong>");
                            } else {
                                htm.add("<strong>Passed (late)</strong>");
                            }
                        } else {
                            htm.add("<strong>Passed</strong>");
                        }
                    }

                    htm.eTd(); // scored (review exam column)

                    final RawExam exam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(i), "U");
                    final SiteDataCfgExamStatus ueStatus = status.getExamStatus(courseId, Integer.valueOf(i), "U");

                    // Proctored exam column:
                    htm.sTd("scored");

                    // If the unit exam carries a score, show that status
                    if (exam == null || ueStatus == null) {
                        htm.add("(None)");
                    } else if (ueStatus.totalAttemptsSoFar == 0) {
                        htm.add("<span class='dim'>Not&nbsp;taken</span>");
                    } else if (ueStatus.firstPassingDate == null) {
                        htm.add("Not yet passed");
                    } else {
                        uePoints = Integer.valueOf(ueStatus.countedScore);
                        htm.add("<strong>Passed</strong>");
                    }

                    htm.eTd(); // scored (proctored exam column)

                    // Points column:
                    htm.sTd("scoret");
                    if (rePoints == null) {
                        if (uePoints != null) {
                            htm.add(uePoints);
                        }
                    } else // RE points is not null
                        if (uePoints == null) {
                            htm.add(rePoints);
                        } else {
                            htm.add(rePoints).add('+').add(uePoints);
                        }
                    htm.eTd();

                } else if ("FIN".equals(unitType)) {

                    // Unit column:
                    htm.sTd("scoreh").add("Final Exam:").eTd();

                    // Review exam column:
                    htm.sTd("scored").eTd();

                    final SiteDataCfgExamStatus feStatus = status.getExamStatus(courseId, Integer.valueOf(i), "F");

                    // Proctored exam column:
                    htm.sTd("scored");

                    // Get this student's deadline for the final exam
                    String feDeadline = null;
                    if ("Y".equals(reg.iInProgress) && !"Y".equals(reg.iCounted) && reg.iDeadlineDt != null) {
                        feDeadline = TemporalUtils.FMT_MD.format(reg.iDeadlineDt).replace(" ", "&nbsp;");
                    } else if (reg.paceOrder != null) {
                        for (final RawMilestone test : allMilestones) {
                            final int msNumber = test.msNbr.intValue();
                            if (msNumber / 10 % 10 != reg.paceOrder.intValue()) {
                                continue;
                            }

                            if (msNumber % 10 == i && "FE".equals(test.msType)) {
                                feDeadline = TemporalUtils.FMT_MD.format(test.msDate).replace(" ", "&nbsp;");
                            }
                        }
                        for (final RawStmilestone test : stMilestones) {
                            final int msNumber = test.msNbr.intValue();
                            if (msNumber / 10 % 10 != reg.paceOrder.intValue()) {
                                continue;
                            }

                            if (msNumber % 10 == i && "FE".equals(test.msType) && feDeadline != null) {
                                feDeadline = TemporalUtils.FMT_MD.format(test.msDate).replace(" ", "&nbsp;");
                            }
                        }
                    }

                    if (feStatus == null || feStatus.totalAttemptsSoFar == 0) {
                        if (feDeadline == null) {
                            htm.add("<span class='dim'>Not&nbsp;taken</span>");
                        } else {
                            htm.add("<span class='dim'>Not&nbsp;taken - </span><strong>due&nbsp", feDeadline,
                                    "</strong>");
                        }
                        htm.sTd("scoret").eTd();
                    } else {
                        final int pts = feStatus.countedScore;

                        if (pts == 0) {
                            if (feDeadline == null) {
                                htm.add("Not&nbsp;yet&nbsp;passed");
                            } else {
                                htm.add("Not&nbsp;yet&nbsp;passed - <strong>due&nbsp;", feDeadline, "</strong>");
                            }
                        } else {
                            htm.add("<strong>Passed</strong>");
                        }
                        htm.sTd("scoret").add(Integer.toString(pts)).eTd();
                    }

                    htm.eTd(); // scored (proctored exam column)

                    if (feStatus == null || feStatus.firstPassingDate == null) {
                        allProctoredPassed = false;
                    }
                }

                htm.eTr();
            }

            // Show total score:
            final SiteDataCfgCourseStatus courseStatus = status.getCourseStatus(courseId);
            final int total = courseStatus == null ? 0 : courseStatus.totalScore;

            htm.sTr("totals");

            // Unit column:
            htm.sTd("scoreh", "colspan='3'").add("<strong>Total Points Earned:</strong>").eTd();
            htm.sTd("scoret").add("<strong>", Integer.toString(total), "</strong>").eTd();
            htm.eTr();

            htm.eTable();

            htm.eDiv(); // indent22

            // If the final exam has been passed but the course grade is not sufficient to earn
            // a passing grade, WARN the student to keep retaking exams to improve score
            if (allProctoredPassed) {

                final RawCsection csection = cfgCourse.courseSection;
                final Integer minPassing;
                final Integer minD = csection.dMinScore;
                if (minD == null) {
                    final Integer minC = csection.cMinScore;
                    if (minC == null) {
                        final Integer minB = csection.bMinScore;
                        if (minB == null) {
                            minPassing = csection.aMinScore;
                        } else {
                            minPassing = minB;
                        }
                    } else {
                        minPassing = minC;
                    }
                } else {
                    minPassing = minD;
                }

                if (minPassing != null && total < minPassing.intValue()) {
                    htm.sP("indent22")
                            .add("<strong class='red'>Your score is not yet sufficient to earn a ",
                                    "passing grade in the course.  You may retake Unit and Final ",
                                    "Exams to improve your total score.</strong>")
                            .eP();
                }
            }

            // Show the grading scale if the section indicates to do so.
            final RawCsection csection = cfgCourse.courseSection;
            if ("Y".equals(csection.displayGradeScale)) {

                final int maxPossible;
                if (csection.aMinScore.intValue() == 65) {
                    maxPossible = 72;
                } else if (csection.aMinScore.intValue() == 43) {
                    maxPossible = 48;
                } else {
                    maxPossible = Math.round(csection.aMinScore.floatValue() * 0.9f);
                }

                htm.div("vgap");

                htm.sDiv("indent22");
                htm.add("<strong>Grading Scale</strong>: &nbsp;");

                htm.add(csection.aMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(maxPossible), "&nbsp;=&nbsp;A, &nbsp");

                htm.add(csection.bMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(csection.aMinScore.intValue() - 1), //
                        "&nbsp;=&nbsp;B, &nbsp");

                htm.add(csection.cMinScore, "&nbsp;-&nbsp;",
                        Integer.toString(csection.bMinScore.intValue() - 1), //
                        "&nbsp;=&nbsp;C, &nbsp");

                if (csection.dMinScore == null) {
                    htm.add("&lt;", csection.cMinScore,
                            "&nbsp;=&nbsp;U");
                } else {
                    htm.add(csection.dMinScore, "&nbsp;-&nbsp;",
                            Integer.toString(csection.cMinScore.intValue() - 1), //
                            "&nbsp;=&nbsp;D, &nbsp");

                    htm.add("&lt;", csection.dMinScore,
                            "&nbsp;=&nbsp;U");
                }

                htm.eDiv(); // indent22
            }

            if (RawRecordConstants.M124.equals(courseId)
                || RawRecordConstants.M126.equals(courseId)) {
                htm.div("vgap");
                htm.sDiv("indent22");
                htm.sDiv("blue");
                htm.addln("Please note: The prerequisites for <b>MATH 156</b> ",
                        "(Mathematics for Computational Science I) and <b>MATH 160</b> ",
                        "(Calculus for Physical Scientists I) <b>require</b> a grade of ",
                        "<b>B or higher</b> in both MATH 124 and MATH 126.");
                htm.eDiv();
                htm.eDiv();
            }

            // Show incomplete deadline date if applicable
            if (reg.iDeadlineDt != null //
                    && !"Y".equals(reg.iCounted)
                    && "Y".equals(reg.iInProgress)) {

                htm.div("vgap");
                htm.sDiv("indent11");
                htm.sDiv("red");
                htm.addln(" <strong>Course Deadline:</strong><br/>");
                htm.addln(" &nbsp; &nbsp; You have until ",
                        TemporalUtils.FMT_MDY.format(reg.iDeadlineDt), " to complete the course.");
                htm.eDiv();
                htm.eDiv();
            }

            htm.div("vgap");
        }
    }

    /**
     * Generates the course outline.
     *
     * @param cache              the data cache
     * @param siteType           the site type
     * @param site               the course site
     * @param session            the user's login session information
     * @param logic              the course site logic
     * @param courseStatus       the student's status in the course
     * @param mode               the mode ("course", "practice", or "locked")
     * @param errorExam          the exam ID with which an error message is associated
     * @param error              the error message
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCourseOutline(final Cache cache, final ESiteType siteType, final CourseSite site,
                                        final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                        final StudentCourseStatus courseStatus, final String mode,
                                        final String errorExam, final String error, final HtmlBuilder htm,
                                        final String skillsReviewCourse) throws SQLException {

        // This is essentially a table of contents for the e-text, with student-specific status
        // on each entry, and selected enable/disable.

        htm.sH(2).add("E-Text Table of Contents").eH(2);

        // Top-level course media
        doCourseMedia(courseStatus, htm);

        // Link to SR course if present
        if (skillsReviewCourse != null) {
            htm.sDiv("nav");
            htm.sDiv("aslines");
            htm.addln("  <a href='course.html?course=", skillsReviewCourse,
                    "&mode=", mode, "'><em>Return to the Course Outline</em></a>");
            htm.eDiv();
            htm.eDiv();
            htm.div("clear");
        }

        // Course topmatter
        doCourseTopMatter(session, courseStatus, mode, htm);

        htm.div("clear");
        htm.div("vgap0").hr().div("vgap0");

        // Units
        doUnits(cache, siteType, site, session, logic, courseStatus, mode, errorExam, error, htm,
                skillsReviewCourse);
    }

    /**
     * Present the course media.
     *
     * @param courseStatus the student's status in the course
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doCourseMedia(final StudentCourseStatus courseStatus,
                                      final HtmlBuilder htm) {

        final Map<String, Map<String, String>> media = courseStatus.getMedia();

        if (!media.isEmpty()) {
            for (final Map.Entry<String, Map<String, String>> entry : media.entrySet()) {
                htm.sDiv("vlines");
                final String title = entry.getKey();
                htm.sH(5).add(title).eH(5);

                final Map<String, String> values = entry.getValue();

                for (final Map.Entry<String, String> e : values.entrySet()) {
                    final String url = e.getValue();

                    htm.sDiv();

                    if (url.endsWith(".pdf")) {
                        htm.add("<img src='/images/pdf.png' alt=''/>");
                    } else if (url.endsWith(".xls")) {
                        htm.add("<img src='/images/excel.png' alt=''/>");
                    } else if (url.endsWith(".txt")) {
                        htm.add("<img src='/images/text.png' alt=''/>");
                    } else if (url.endsWith(".html")) {
                        htm.add("<img src='/images/html.png' alt=''/>");
                    } else if (url.endsWith(".doc")) {
                        htm.add("<img src='/images/word.png' alt=''/>");
                    } else if (url.endsWith(".zip")) {
                        htm.add("<img src='/images/zip.png' alt=''/>");
                    }

                    final String name = e.getKey();
                    htm.addln(" <a class='ulink' href='", url, "'>", name, "</a>").eDiv();
                }

                htm.eDiv();
            }

            htm.div("clear");
        }
    }

    /**
     * Present the course top-matter.
     *
     * @param session      the user's login session information
     * @param courseStatus the student's status in the course
     * @param mode         the access mode
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doCourseTopMatter(final ImmutableSessionInfo session,
                                          final StudentCourseStatus courseStatus, final String mode,
                                          final HtmlBuilder htm) {

        final RawCsection csection = courseStatus.getCourseSection();
        final String topMatter = RawCsection.getTopmatter(csection.course);

        if ("practice".equals(mode)) {

            if (session.getEffectiveRole() == ERole.STUDENT) {
                htm.br();
                htm.sDiv("advice");
                htm.addln("<strong class='red'>");
                htm.addln("You have purchased access to this e-text, but since you are not currently working in the ",
                        "course, exams and assignments will NOT be recorded.  You may practice objective problems or ",
                        "review exams as often as you like.");
                htm.addln("</strong>");
                htm.eDiv().br();
                htm.div("vgap");
            }
        } else if ("locked".equals(mode)) {

            if (session.getEffectiveRole() == ERole.STUDENT) {
                htm.br();
                htm.sDiv("advice");
                htm.addln("<strong class='red'>");
                htm.addln("You have purchased access to this e-text, but since you are past the deadline date for ",
                        "finishing the course, exams and assignments will NOT be recorded.  You may practice ",
                        "objective problems or review exams as often as you like.");
                htm.addln("</strong>");
                htm.eDiv().br();
                htm.div("vgap");
            }
        } else if (topMatter != null) {
            htm.sP().add(topMatter).eP();
        }
    }

    /**
     * Present each unit's topics with status.
     *
     * @param cache              the data cache
     * @param siteType           the site type
     * @param site               the course site
     * @param session            the user's login session information
     * @param logic              the course site logic
     * @param courseStatus       the student's status in the course
     * @param mode               the mode ("course", "practice", or "locked")
     * @param errorExam          the exam ID with which an error message is associated
     * @param error              the error message
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUnits(final Cache cache, final ESiteType siteType, final CourseSite site,
                                final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                final StudentCourseStatus courseStatus, final String mode, final String errorExam,
                                final String error, final HtmlBuilder htm, final String skillsReviewCourse)
            throws SQLException {

        final int maxUnit = courseStatus.getMaxUnit();
        final RawCusection gwSecUnit = courseStatus.getGatewaySectionUnit();
        boolean told = false;

        for (int i = 0; i <= maxUnit; ++i) {

            final RawCunit unit = courseStatus.getCourseUnit(i);

            final String type = unit == null ? "SR" : unit.unitType;

            if ("SR".equals(type) && gwSecUnit != null) {
                doSkillsReviewUnit(cache, logic, courseStatus, mode, gwSecUnit, htm);
            } else if ("INST".equals(type)) {
                told = doInstructionUnit(cache, siteType, site, session, logic, courseStatus, unit,
                        mode, gwSecUnit, told, htm, skillsReviewCourse);
            } else if ("FIN".equals(type)) {
                doFinalUnit(cache, siteType, session, logic, courseStatus, unit, mode,
                        gwSecUnit, told, errorExam, error, htm);
            }
        }
    }

    /**
     * Present the link to access the Skills Review exam, if configured.
     *
     * @param cache        the data cache
     * @param logic        the course site logic
     * @param courseStatus the student's status in the course
     * @param mode         the mode ("course", "practice", or "locked")
     * @param gwSecUnit    the gateway course section unit, if present, {@code null} if not
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doSkillsReviewUnit(final Cache cache, final CourseSiteLogic logic,
                                           final StudentCourseStatus courseStatus, final String mode,
                                           final RawCusection gwSecUnit,
                                           final HtmlBuilder htm) throws SQLException {

        final String courseId = courseStatus.getCourse().course;
        final int unitNum = gwSecUnit.unit.intValue();
        final RawStcourse stcourse = courseStatus.getStudentCourse();

        final SystemData systemData = cache.getSystemData();

        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, gwSecUnit.unit, "R");
        final String examLabel;
        final String examTitle;
        String examId;
        if (unitExam == null) {
            examLabel = null;
            examTitle = null;
            examId = null;
        } else {
            examLabel = unitExam.buttonLabel;
            examTitle = unitExam.title;
            examId = unitExam.version;
        }

        htm.sDiv(null, "style='float:left;padding-right:5px;'");
        htm.addln(" <img src='/images/stock-jump-to-32.png' alt=''/>");
        htm.eDiv();

        htm.sH(3).add("<span style='position:relative;top:3px;'>Skills Review</span>").eH(3);
        htm.div("clear");

        if ("course".equals(mode) && courseStatus.isCourseGatewayPassed()
                && !courseStatus.isCourseGatewayAttempted() && unitExam != null) {
            htm.sDiv(null, "style='padding-left:37px;'");
            htm.add("Based on your course status, you are not required to pass the ", examTitle,
                    ". However, you may practice the exam.");
            htm.eDiv();
        }

        htm.div("vgap");

        // FIXME: Hack here - if a student is in M 117, section 401/801, and prerequisite satisfied
        //  is provisional (as set in RegistrationCache for this population), then we make the
        //  gateway course M 100T to force a larger Skills Review.

        String gwCourse = null;
        if (RawRecordConstants.M117.equals(stcourse.course)
                && ("801".equals(stcourse.sect) || "809".equals(stcourse.sect) || "401".equals(stcourse.sect))
                && "P".equals(stcourse.prereqSatis)) {

            gwCourse = RawRecordConstants.M100T;
            examId = "17ELM";
        }

        if ("course".equals(mode)) {

            if (unitExam == null) {
                htm.sDiv("indent2");
                gatewayLinks(courseId, mode, gwCourse, unitNum, courseStatus, htm);
                htm.eDiv();
            } else if (courseStatus.isCourseGatewayPassed()) {

                htm.sDiv("indent2");
                gatewayLinks(courseId, mode, gwCourse, unitNum, courseStatus, htm);
                htm.eDiv();

                if (examId != null) {
                    htm.sDiv("indent");
                    htm.addln("<form method='get' action='run_review.html'>");
                    htm.sDiv("exambtn");
                    htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                    htm.addln("<input type='hidden' name='exam' value='", examId, "'/>");
                    htm.addln("<input type='hidden' name='mode' value='course'/>");

                    if (courseStatus.isCourseGatewayAttempted()) {
                        htm.addln("<button class='btn' type='submit'>", examLabel, "</button> &nbsp;");
                        htm.addln("<span class='why_done'><img src='/images/check.png' ",
                                "style='position:relative;top:-2px;' alt=''/> Passed</span>");
                    } else if (examLabel != null && examLabel.contains("Practice")) {
                        htm.addln("<button class='btn' type='submit'>", examLabel, "</button>");
                    } else {
                        htm.addln("<button class='btn' type='submit'>Practice ", examLabel, "</button>");
                    }

                    htm.eDiv();
                    htm.addln("</form>");
                    htm.eDiv(); // indent
                }
            } else if (courseStatus.isCourseGatewayAttempted()) {

                htm.sDiv("indent2");
                gatewayLinks(courseId, mode, gwCourse, unitNum, courseStatus, htm);
                htm.eDiv();

                if (examId != null) {
                    htm.addln("<form method='get' action='run_review.html'>");
                    htm.sDiv("exambtn");
                    htm.addln("<input type='hidden' name='mode' value='course'/>");
                    htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                    htm.addln("<input type='hidden' name='exam' value='", examId, "'/>");
                    htm.addln("<button class='btn' type='submit'>", examLabel, "</button> &nbsp;");
                    htm.addln("<span class='why_unavail'>Not Yet Passed</span>");
                    htm.eDiv();
                    htm.addln("</form>");
                }
            } else if (examId != null) {
                htm.sDiv("indent");
                htm.add("Please try the Skills Review Exam first.  If you pass that exam, you can move into Unit 1.  ",
                        "If not, review materials will become available so you can refresh your skills before ",
                        "moving on to the main course content.");
                htm.eDiv(); // indent

                htm.sDiv("indent");
                htm.addln("<form method='get' action='run_review.html'>");
                htm.sDiv("exambtnlow");
                htm.addln("<input type='hidden' name='mode' value='course'/>");
                htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                htm.addln("<input type='hidden' name='exam' value='", examId, "'/>");
                htm.addln("<button class='btn' type='submit'>", examLabel, "</button> &nbsp;");
                htm.addln("<span class='why_unavail'>Not Yet Attempted</span>");
                htm.eDiv();
                htm.addln("</form>");
                htm.eDiv(); // indent
            }

        } else {
            htm.sDiv("indent2");
            gatewayLinks(courseId, mode, gwCourse, unitNum, courseStatus, htm);
            htm.eDiv();

            if (unitExam != null && examId != null) {
                htm.sDiv("indent");
                htm.addln("<form method='get' action='run_review.html'>");
                htm.sDiv("exambtn");
                htm.addln("<input type='hidden' name='mode' value='practice'/>");
                htm.addln("<input type='hidden' name='course' value='", courseId, "'/>");
                htm.addln("<input type='hidden' name='exam' value='", examId, "'/>");
                if (examLabel != null && examLabel.contains("Practice")) {
                    htm.addln("<button class='btn' type='submit'>", examLabel, "</button/> &nbsp;");
                } else {
                    htm.addln("<button class='btn' type='submit'>Practice ", examLabel, "</button> &nbsp;");
                }
                if ("locked".equals(mode)) {
                    htm.addln("<span class='why_unavail'>Past final exam deadline, practice only</span>");
                }
                htm.eDiv();
                htm.addln("</form>");
                htm.eDiv(); // indent
            }
        }

        final String studentId = logic.data.studentData.getStudent().stuId;

        final TermRec active = cache.getSystemData().getActiveTerm();

        final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, courseId, gwSecUnit.unit, false,
                RawStexamLogic.ALL_EXAM_TYPES);

        htm.sDiv("indent2");
        if (exams.size() > 5) {
            htm.addln("<details>");
            htm.addln("<summary>Review your exams and solutions</summary>");
        }

        for (final RawStexam exam : exams) {
            if (!"SY".equals(exam.examSource)) {
                final String path = ExamWriter.makeWebExamPath(active.term.shortString, studentId,
                        exam.serialNbr.longValue());
                final String course = courseId.replace(CoreConstants.SPC, "%20");

                htm.sDiv("indent1");
                htm.addln(" <a class='ulink' href='see_past_exam.html?course=", course,
                        "&mode=", mode,
                        "&exam=", exam.version,
                        "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                        "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                        "'>Review your ", exam.getExamLabel(), "</a>");
                htm.eDiv();
            }
        }

        if (exams.size() > 5) {
            htm.addln("</details>");
        }

        htm.eDiv();

        htm.hr();
    }

    /**
     * Present the link(s) to the gateway review materials (which may be either a single lesson or a full course).
     *
     * @param courseId     the course ID
     * @param mode         the mode ("course", "practice", or "locked")
     * @param gwCourse     the number of the course to use as gateway material ({@code null} if gateway material is a
     *                     single lesson)
     * @param unit         the gateway unit
     * @param courseStatus the student's status in the course
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void gatewayLinks(final String courseId, final String mode,
                                     final String gwCourse, final int unit, final StudentCourseStatus courseStatus,
                                     final HtmlBuilder htm) {

        if (gwCourse == null) {
            final int count = courseStatus.getNumLessons(unit);

            for (int i = 0; i < count; ++i) {
                if (i > 0) {
                    htm.br();
                }
                final RawCuobjective cuobjective = courseStatus.getCourseUnitObjective(unit, i);
                final RawLesson lesson = courseStatus.getLesson(unit, i);

                htm.addln(" <a class='linkbtn' href='lesson.html?course=", courseId, "&unit=", Integer.toString(unit),
                        "&lesson=", cuobjective.objective, "&mode=", mode, "'>", lesson.descr, "</a>");
            }
        } else {
            htm.addln(" <a class='linkbtn' href='skills_review.html?course=", courseId, "&mode=", mode,
                    "'>Skills Review materials</a>");
        }
    }

    /**
     * Present a unit of instruction.
     *
     * @param cache              the data cache
     * @param siteType           the site type
     * @param site               the course site
     * @param session            the user's login session information
     * @param logic              the course site logic
     * @param courseStatus       the student's status in the course
     * @param unit               the unit model
     * @param mode               the mode ("course", "practice", or "locked")
     * @param gwSecUnit          the gateway course section unit, if present, {@code null} if not
     * @param told               {@code true} if the user has already been told why they cannot proceed
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @return the new value for {@code told} @ throws SQLException if there is an error accessing the database
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean doInstructionUnit(final Cache cache, final ESiteType siteType, final CourseSite site,
                                             final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                             final StudentCourseStatus courseStatus, final RawCunit unit,
                                             final String mode, final RawCusection gwSecUnit, final boolean told,
                                             final HtmlBuilder htm, final String skillsReviewCourse)
            throws SQLException {

        boolean dimmed = false;

        final String courseId = courseStatus.getCourse().course;
        String actualMode = mode;

        if (courseStatus.isStudentVisiting() && courseStatus.isVisitingPracticeMode()) {
            actualMode = "practice";
        }

        final SystemData systemData = cache.getSystemData();

        // Unit materials dimmed if gateway exam not passed
        if ("course".equals(actualMode) && gwSecUnit != null && !courseStatus.isStudentVisiting()) {

            final RawCusection gw = courseStatus.getGatewaySectionUnit();
            final RawExam gwExam = gw == null ? null
                    : systemData.getActiveExamByCourseUnitType(courseId, unit.unit, "R");

            if (gwExam != null && !courseStatus.isCourseGatewayPassed()) {
                dimmed = true;
            }
        }

        final int unitNum = unit.unit.intValue();

        doUnitTitle(courseStatus, unitNum, htm);
        doUnitTopmatter(courseStatus, unitNum, htm);

        if (!courseStatus.isProctoredPassed(unitNum)) {
            final LocalDate deadline = courseStatus.getUnitExamDeadline(unitNum);
            if (deadline != null) {

                htm.sDiv("indent2");
                htm.addln("<img src='/images/info.png' alt=''/> ");
                htm.add("<strong class='blue' style='position:relative;top:2px;'>The Unit ",
                        Integer.toString(unitNum), " Exam is due ");

                final LocalDate today = LocalDate.now();
                if (today.equals(deadline)) {
                    htm.add("TODAY");
                } else if (today.plusDays(1L).equals(deadline)) {
                    htm.add("TOMORROW");
                } else {
                    final String deadlineStr = TemporalUtils.FMT_MDY.format(deadline);
                    htm.add(deadlineStr);
                }

                htm.addln(".</strong>").eDiv(); // indent2

                htm.div("vgap");
            }
        }
        if (!courseStatus.isReviewPassed(unitNum)) {
            final LocalDate deadline = courseStatus.getReviewExamDeadline(unitNum);
            if (deadline != null) {

                htm.sDiv("indent2");
                htm.addln("<img src='/images/info.png' alt=''/> ");
                htm.add("<strong class='blue' style='position:relative;top:2px;'>The Unit ",
                        Integer.toString(unitNum), " Review Exam is due ");

                final LocalDate today = LocalDate.now();
                if (today.equals(deadline)) {
                    htm.add("TODAY");
                } else if (today.plusDays(1L).equals(deadline)) {
                    htm.add("TOMORROW");
                } else {
                    final String deadlineStr = TemporalUtils.FMT_MDY.format(deadline);
                    htm.add(deadlineStr);
                }

                htm.addln(".</strong>").eDiv(); // indent2

                htm.div("vgap");
            }
        }

        // Show the links to the lessons in the unit, with student's status as needed
        final boolean newTold = doUnitLessons(cache, site, courseStatus, unitNum, htm, actualMode, dimmed,
                told, skillsReviewCourse);

        if (skillsReviewCourse == null) {
            // Sequence for an instructional unit:
            // - Lessons (must be done in sequence as rule set rules dictate)
            // - Unit Review Exam (done after homeworks, as rule set rules dictate)
            // - Unit Exam (proctored, or online if "online unit exams" indicated)
            // - After two failed units, must re-pass review exam

            final RawCusection cusect = courseStatus.getCourseSectionUnit(unitNum);
            if (cusect == null) {
                Log.warning("No course section unit for unit " + unitNum);
            } else {
                doUnitReviewExam(cache, courseStatus, session, unitNum, dimmed, actualMode, htm);

                final String range = courseStatus.getProctoredRange(unitNum);
                if (range != null) {
                    htm.sDiv("indent2").add(range).eDiv();
                }

                // Show a link to take the exam with proctoring service
                if (RawRecordConstants.M117.equals(courseId)
                        || RawRecordConstants.M118.equals(courseId)
                        || RawRecordConstants.M124.equals(courseId)
                        || RawRecordConstants.M125.equals(courseId)
                        || RawRecordConstants.M126.equals(courseId)) {
                    doCanvasUnitExam(cache, siteType, session, courseStatus, unitNum, actualMode, htm);
                }
            }
        }

        final String studentId = logic.data.studentData.getStudent().stuId;

        final TermRec active = cache.getSystemData().getActiveTerm();

        final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, courseId, unit.unit,
                false, RawStexamLogic.ALL_EXAM_TYPES);

        htm.sDiv("indent2");
        if (exams.size() > 5) {
            htm.addln("<details>");
            htm.addln("<summary>Review your exams and solutions</summary>");
        }

        for (final RawStexam exam : exams) {
            if (!"SY".equals(exam.examSource)) {
                final String path = ExamWriter.makeWebExamPath(active.term.shortString, studentId,
                        exam.serialNbr.longValue());
                final String course = courseId.replace(CoreConstants.SPC, "%20");

                htm.sDiv("indent1");
                htm.addln(" <a class='ulink' href='see_past_exam.html?course=", course,
                        "&mode=", mode,
                        "&exam=", exam.version,
                        "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                        "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                        "'>Review your ", exam.getExamLabel(), "</a>");
                htm.eDiv();
            }
        }

        if (exams.size() > 5) {
            htm.addln("</details>");
        }
        htm.eDiv();

        htm.hr();

        return newTold;
    }

    /**
     * Present a final exam unit.
     *
     * @param cache        the data cache
     * @param siteType     the site type
     * @param session      the session
     * @param logic        the course site logic
     * @param courseStatus the student's status in the course
     * @param unit         the unit model
     * @param mode         the mode ("course", "practice", or "locked")
     * @param gwSecUnit    the gateway course section unit, if present, {@code null} if not
     * @param told         {@code true} if the user has already been told why they cannot proceed
     * @param errorExam    the exam ID with which an error message is associated
     * @param error        the error message
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doFinalUnit(final Cache cache, final ESiteType siteType, final ImmutableSessionInfo session,
                                    final CourseSiteLogic logic, final StudentCourseStatus courseStatus,
                                    final RawCunit unit, final String mode, final RawCusection gwSecUnit,
                                    final boolean told, final String errorExam, final String error,
                                    final HtmlBuilder htm) throws SQLException {

        final String courseId = courseStatus.getCourse().course;

        boolean dimmed = false;

        final SystemData systemData = cache.getSystemData();

        if ("course".equals(mode)) {

            // Unit materials dimmed if gateway exam not passed
            if (gwSecUnit != null && !courseStatus.isStudentVisiting()) {
                final RawExam finalExam = systemData.getActiveExamByCourseUnitType(courseId, unit.unit, "F");
                if (finalExam != null && !courseStatus.isCourseGatewayPassed()) {
                    dimmed = true;
                }
            }

            final int unitNum = unit.unit.intValue();

            doUnitTitle(courseStatus, unitNum, htm);

            if (courseStatus.isPassing(unitNum)) {
                final LocalDate deadline = courseStatus.getUnitExamDeadline(unitNum);
                if (deadline != null) {
                    htm.sDiv("indent");
                    htm.addln(" <img src='/images/info.png' alt=''/>");
                    htm.add(" <strong class='blue' style='position:relative;top:2px;'>The Final Exam ");
                    if (deadline.isBefore(session.getNow().toLocalDate())) {
                        htm.add("was");
                    } else {
                        htm.add("is");
                    }
                    htm.addln(" due ", TemporalUtils.FMT_WMDY.format(deadline), ".</strong>");
                    htm.eDiv();
                    htm.div("vgap");
                }
            }

            doUnitTopmatter(courseStatus, unitNum, htm);

            doFinalExam(cache, siteType, session, courseStatus, unitNum, mode, dimmed, htm,
                    session.getNow());

            final String studentId = logic.data.studentData.getStudent().stuId;

            final TermRec active = cache.getSystemData().getActiveTerm();

            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, courseId, unit.unit, false,
                    RawStexamLogic.ALL_EXAM_TYPES);

            htm.sDiv("indent2");
            if (exams.size() > 5) {
                htm.addln("<details>");
                htm.addln("<summary>Review your exams and solutions</summary>");
            }

            for (final RawStexam exam : exams) {
                if (!"SY".equals(exam.examSource)) {
                    final String path = ExamWriter.makeWebExamPath(active.term.shortString, studentId,
                            exam.serialNbr.longValue());
                    final String course = courseId.replace(CoreConstants.SPC, "%20");

                    htm.sDiv("indent1");
                    htm.addln(" <a class='ulink' href='see_past_exam.html?course=", course,
                            "&mode=", mode,
                            "&exam=", exam.version,
                            "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                            "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                            "'>Review your ", exam.getExamLabel(), "</a>");
                    htm.eDiv();
                }
            }
            if (exams.size() > 5) {
                htm.addln("</details>");
            }
        } else {

            // Unit materials dimmed if gateway exam not passed
            if (gwSecUnit != null && !courseStatus.isStudentVisiting()) {
                final RawExam finalExam = systemData.getActiveExamByCourseUnitType(courseId, unit.unit, "F");
                if (finalExam != null && !courseStatus.isCourseGatewayPassed()) {
                    dimmed = true;
                }
            }

            final int unitNum = unit.unit.intValue();

            doUnitTitle(courseStatus, unitNum, htm);

            if (courseStatus.isPassing(unitNum)) {
                final LocalDate deadline = courseStatus.getUnitExamDeadline(unitNum);
                if (deadline != null) {
                    htm.sDiv("indent");
                    htm.addln(" <img src='/images/info.png' alt=''/>");
                    htm.add(" <strong class='blue' style='position:relative;top:2px;'>The Final Exam ");
                    if (deadline.isBefore(session.getNow().toLocalDate())) {
                        htm.add("was");
                    } else {
                        htm.add("is");
                    }
                    htm.addln(" due ", TemporalUtils.FMT_WMDY.format(deadline), ".</strong>");
                    htm.eDiv();
                    htm.div("vgap");
                }
            }

            doFinalExam(cache, siteType, session, courseStatus, unitNum, mode, dimmed, htm,
                    session.getNow());

            final String studentId = logic.data.studentData.getStudent().stuId;

            final TermRec active = cache.getSystemData().getActiveTerm();

            final List<RawStexam> exams = RawStexamLogic.getExams(cache, studentId, courseId, unit.unit, false,
                    RawStexamLogic.ALL_EXAM_TYPES);

            htm.sDiv("indent2");
            if (exams.size() > 5) {
                htm.addln("<details>");
                htm.addln("<summary>Review your exams and solutions</summary>");
            }

            for (final RawStexam exam : exams) {
                if (!"SY".equals(exam.examSource)) {
                    final String path = ExamWriter.makeWebExamPath(active.term.shortString, studentId,
                            exam.serialNbr.longValue());
                    final String course = courseId.replace(CoreConstants.SPC, "%20");

                    htm.sDiv("indent1");
                    htm.addln(" <a class='ulink' href='see_past_exam.html?course=", course,
                            "&mode=", mode,
                            "&exam=", exam.version,
                            "&xml=", path, CoreConstants.SLASH, ExamWriter.EXAM_FILE,
                            "&upd=", path, CoreConstants.SLASH, ExamWriter.ANSWERS_FILE,
                            "'>Review your ", exam.getExamLabel(), "</a>");
                    htm.eDiv();
                }
            }
            if (exams.size() > 5) {
                htm.addln("</details>");
            }
        }

        htm.eDiv();
        htm.hr();
    }

    /**
     * Present the title in the course outline for a unit.
     *
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit number
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitTitle(final StudentCourseStatus courseStatus, final int unitNum,
                                    final HtmlBuilder htm) {

        final RawCunit courseUnit = courseStatus.getCourseUnit(unitNum);

        htm.sDiv(null, "style='float:left;padding-right:5px;'");
        htm.addln(" <img src='/images/stock-jump-to-32.png' alt=''/>");
        htm.eDiv();

        htm.sDiv();
        htm.addln(" <a name='unit", Integer.toString(unitNum), "'></a>");

        htm.sH(3).add("<span class='green' style='position:relative;top:3px;'>");

        if (courseUnit.unit != null) {
            htm.add(courseUnit.unit, ": ");
        }
        if (courseUnit.unitDesc != null) {
            htm.add(courseUnit.unitDesc);
        }
        htm.addln("</span>").eH(3).eDiv();
    }

    /**
     * Present the topmatter in the course outline for a unit.
     *
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit number
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     */
    private static void doUnitTopmatter(final StudentCourseStatus courseStatus, final int unitNum,
                                        final HtmlBuilder htm) {

        final RawCusection courseSecUnit = courseStatus.getCourseSectionUnit(unitNum);

        if (courseSecUnit != null) {
            final String top = RawCusection.getTopmatter(courseSecUnit);
            if (top != null) {
                htm.add(top);
            }
        }

        htm.div("vgap");
    }

    /**
     * Present the list of lessons in a unit.
     *
     * @param cache              the data cache
     * @param site               the course site
     * @param courseStatus       the student's status in the course
     * @param unit               the unit
     * @param htm                the {@code HtmlBuilder} to which to append the HTML
     * @param mode               the mode
     * @param dimmed             {@code true} if unit lessons should be dimmed (disabled)
     * @param told               {@code true} if user has already been told why he can't move on
     * @param skillsReviewCourse the course for which this course is being presented as a skills review, {@code null} if
     *                           this course is being presented on its own
     * @return the new value for the {@code told} input
     * @throws SQLException if there is an error accessing the database
     */
    private static boolean doUnitLessons(final Cache cache, final CourseSite site,
                                         final StudentCourseStatus courseStatus, final int unit, final HtmlBuilder htm,
                                         final String mode, final boolean dimmed, final boolean told,
                                         final String skillsReviewCourse) throws SQLException {

        final int count = courseStatus.getNumLessons(unit);
        final Profile dbProfile = site.site.profile;
        final CourseLesson less = new CourseLesson(dbProfile);
        final String courseId = courseStatus.getCourse().course;
        boolean newTold = told;

        htm.sDiv("indent2");
        htm.addln("<table>");

        for (int i = 0; i < count; ++i) {

            final RawCuobjective cuobj = courseStatus.getCourseUnitObjective(unit, i);

            final RawLesson lesson = courseStatus.getLesson(unit, i);
            final Integer seqNum = cuobj.objective;
            final String lessNum = cuobj.lessonNbr;

            final boolean avail;
            avail = (!courseStatus.hasHomework(unit, seqNum.intValue())
                    || courseStatus.isHomeworkAvailable(unit, seqNum.intValue()))
                    || "Instructor Lecture not yet viewed."
                    .equals(courseStatus.getHomeworkReason(unit, seqNum.intValue()));
            final String status = courseStatus.getHomeworkStatus(unit, seqNum.intValue());

            // Show any "PREMED" media components for the lesson in the outline page
            if (less.gatherData(cache, courseId, Integer.valueOf(unit), cuobj.objective)) {
                final int numComp = less.getNumComponents();

                boolean hasPre = false;
                for (int j = 0; j < numComp; ++j) {
                    final RawLessonComponent comp = less.getLessonComponent(j);
                    if ("PREMED".equals(comp.type)) {
                        hasPre = true;
                        break;
                    }
                }

                if (hasPre) {
                    htm.add("<tr><td style='height:8px;'></td></tr>");

                    for (int j = 0; j < numComp; ++j) {
                        final RawLessonComponent comp = less.getLessonComponent(j);
                        if ("PREMED".equals(comp.type)) {
                            final String xml = comp.xmlData;
                            htm.add("<tr><td colspan='2' class='open' style='white-space:nowrap;'>");
                            htm.add(xml.replace("%%MODE%%", mode));
                            htm.addln("</td></tr>");
                        }
                    }

                    htm.add("<tr><td style='height:4px;'></td></tr>");
                }
            }

            htm.addln("<tr>");

            if (dimmed) {
                htm.add("<td class='open dim' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                if (lessNum != null) {
                    htm.add(lessNum, ":&nbsp;");
                }
                htm.add("</td><td class='dim' style='white-space:nowrap;'>");
                htm.addln(lesson.descr, " &nbsp; </td>");

                if (!newTold) {
                    htm.addln("<td class='why_unavail'>Skills Review Exam not yet passed.</td>");
                    newTold = true;
                }
            } else if ("course".equals(mode)) {

                if (avail) {
                    htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                    if (lessNum != null) {
                        htm.add(lessNum, ":&nbsp;");
                    }
                    htm.add("</td><td style='white-space:nowrap;'>");
                    htm.add("<a class='ulink' href='lesson.html?course=", courseStatus.getCourse().course, "&unit=",
                            Integer.toString(unit), "&lesson=", seqNum, "&mode=", mode);
                    if (skillsReviewCourse != null) {
                        htm.add("&srcourse=", skillsReviewCourse);
                    }
                    htm.add("'>");
                    htm.addln(lesson.descr, "</a> &nbsp; </td>");
                } else {
                    htm.add("<td class='open dim' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                    if (lessNum != null) {
                        htm.add(lessNum, ":&nbsp;");
                    }
                    htm.add("</td><td class='dim' style='white-space:nowrap;'>");
                    htm.addln(lesson.descr, "&nbsp; </td>");

                    final String reason = courseStatus.getHomeworkReason(unit, seqNum.intValue());

                    if (!newTold && reason != null && !"May Move On".equals(status) && !"Completed".equals(status)) {
                        htm.add("<td><span class='why_unavail'>", reason, "</span></td>");
                        newTold = true;
                    }
                }

                if ("May Move On".equals(status) || "Completed".equals(status)) {
                    htm.add("<td class='why_done'><img src='/images/check.png' alt=''/>", CoreConstants.SPC,
                            status, "</td>");
                } else if (!newTold && status != null) {
                    htm.add("<td class='why_unavail'>", status, "</td>");
                    newTold = true;
                }
            } else if ("locked".equals(mode) || "practice".equals(mode)) {

                htm.add("<td class='open' style='text-align:right;font-family: factoria-medium,sans-serif;'>");
                if (lessNum != null) {
                    htm.add(lessNum, ":&nbsp;");
                }
                htm.add("</td><td style='white-space:nowrap;'>");
                htm.add("<a class='ulink' href='lesson.html?course=", courseStatus.getCourse().course, "&unit=",
                        Integer.toString(unit), "&lesson=", seqNum, "&mode=", mode);
                if (skillsReviewCourse != null) {
                    htm.add("&srcourse=", skillsReviewCourse);
                }
                htm.add("'>");

                htm.addln(lesson.descr, "</a> &nbsp; </td>");
            }
            htm.addln("</tr>");
        }

        htm.addln("</table>");
        htm.eDiv(); // indent2

        return newTold;
    }

    /**
     * Present the button for the unit review exam along with status.
     *
     * @param cache        the data cache
     * @param courseStatus the student's status in the course
     * @param session      the user's login session information
     * @param unitNum      the unit
     * @param dimmed       {@code true} if the button should be dimmed regardless of status
     * @param mode         the mode
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doUnitReviewExam(final Cache cache, final StudentCourseStatus courseStatus,
                                         final ImmutableSessionInfo session, final int unitNum,
                                         final boolean dimmed, final String mode, final HtmlBuilder htm)
            throws SQLException {

        final ZonedDateTime sessionNow = session.getNow();

        final RawCourse course = courseStatus.getCourse();
        final String courseId = course.course;
        final boolean reviewAvail = courseStatus.isReviewExamAvailable(unitNum);
        final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);

        final SystemData systemData = cache.getSystemData();

        final Integer unitNumObj = Integer.valueOf(unitNum);
        final RawExam revExam = systemData.getActiveExamByCourseUnitType(courseId, unitNumObj, "R");
        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, unitNumObj, "U");

        String unitLabel = "Unit Exam";
        if (unitExam != null) {
            unitLabel = unitExam.buttonLabel;
        }

        final String label;
        final String version;
        final LocalDate dueDate = courseStatus.getReviewExamDeadline(unitNum);

        if (revExam == null) {
            label = null;
            version = null;
        } else {
            if (dueDate == null) {
                label = revExam.buttonLabel;
            } else {
                label = revExam.buttonLabel + "<br/><small>(due " + TemporalUtils.FMT_MDY.format(dueDate) + ")</small>";
            }
            version = revExam.version;
        }

        if ("course".equals(mode) && version != null) {
            htm.sDiv("indent");
            htm.addln("<form method='get' action='run_review.html'>");
            htm.sDiv("exambtn");

            if (dimmed) {
                htm.addln(" <button class='btn' type='submit' disabled='disabled'>", label, "</button> &nbsp;");
                htm.eDiv(); // indent2
                htm.addln("</form>");
                htm.eDiv();

                if (dueDate != null) {
                    emitReviewExamExtensions(cache, courseStatus, dueDate, unitNum, htm);
                }
            } else if (reviewAvail) {
                htm.addln(" <input type='hidden' name='mode' value='course'/>");
                htm.addln(" <input type='hidden' name='course' value='", course.course, "'/>");
                htm.addln(" <input type='hidden' name='exam' value='", version, "'/>");
                htm.addln(" <button class='btn' type='submit'>", label, "</button> &nbsp;");

                final String status = courseStatus.getReviewStatus(unitNum);

                boolean passedOnTime = false;
                if ("Passed".equals(status)) {
                    htm.add(" <span class='why_done'>",
                            "<img src='/images/check.png' style='position:relative;top:-2px;' alt=''/> Passed ");

                    if (courseStatus.isReviewPassedOnTime(unitNum)) {
                        htm.add("(On-Time)");
                        passedOnTime = true;
                    } else {
                        htm.add("(Late)");
                    }
                    htm.addln("</span>").br();
                } else {
                    htm.addln(" <span class='why_unavail'>", status, "</span>").br();
                }

                htm.eDiv();
                htm.addln("</form>");

                if (dueDate != null && !passedOnTime) {
                    emitReviewExamExtensions(cache, courseStatus, dueDate, unitNum, htm);
                }

                htm.eDiv(); // indent2
            } else {
                htm.add(" <button class='btn' type='submit' disabled='disabled'>", label, "</button> &nbsp; ");

                final String reason = courseStatus.getReviewReason(unitNum);

                if (reason != null) {
                    htm.add("<span class='why_unavail'>", reason, "</span>").br();
                }
                htm.addln();

                htm.eDiv();
                htm.addln("</form>");

                if (dueDate != null) {
                    emitReviewExamExtensions(cache, courseStatus, dueDate, unitNum, htm);
                }

                htm.eDiv(); // indent
            }

            if (unitExam != null) {
                final int timesTaken = courseStatus.getProctoredTimesTaken(unitNum);
                final int curScore = courseStatus.getScores().getRawUnitExamScore(unitNum);
                final int perfectScore = courseStatus.getPerfectScore(unitNum);
                final boolean isPassing = courseStatus.isPassing(unitNum);

                if (timesTaken > 0) {
                    htm.div("gap2");
                    htm.sDiv("indent");
                    htm.add(" You have taken the <b>", unitLabel, "</b> ");
                    if (timesTaken == 1) {
                        htm.addln("one time. ");
                    } else {
                        htm.addln(Integer.toString(timesTaken), " times. ");
                    }
                    htm.addln(" Your current score is <b>", Integer.toString(curScore), "</b> out of <b>",
                            Integer.toString(perfectScore), "</b>");

                    if (curScore == perfectScore) {
                        htm.addln(" (a perfect score). &nbsp;");
                    } else if (isPassing) {
                        htm.addln(" (a passing score). &nbsp;");

                        if (unitAvail) {
                            htm.addln("You may retake the <b>", unitExam.buttonLabel,
                                    "</b> to try to improve this score. &nbsp; ");
                        }
                    } else {
                        htm.addln(" (not a passing score). &nbsp;");

                        final int availProc = courseStatus.getProctoredAttemptsAvailable(unitNum);
                        final String reviewLabel;
                        reviewLabel = revExam.buttonLabel;

                        if (unitLabel != null && reviewLabel != null) {
                            if (availProc == 0) {
                                htm.addln("You must take and pass the <b>", reviewLabel,
                                        "</b> before attempting the <b>", unitLabel, "</b> again. &nbsp; ");
                            } else if (availProc == 1) {
                                htm.addln("You have 1 attempt on the <b>", unitLabel, "</b> available. &nbsp; ");
                            } else if (availProc > 1 && availProc < 90) {
                                htm.addln("You have ", Integer.toString(availProc), " attempts on the <b>",
                                        unitLabel, "</b> available. &nbsp; ");
                            }
                        }
                    }

                    htm.addln();
                    htm.eDiv();
                } else if (unitAvail) {
                    final int nextUnit = unitNum + 1;

                    if ("Y".equals(courseStatus.getPacingStructure().requireUnitExams)
                            && nextUnit <= courseStatus.getMaxUnit()) {
                        final RawCusection nextSecUnit = courseStatus.getCourseSectionUnit(nextUnit);

                        if (nextSecUnit != null) {
                            final String type = courseStatus.getCourseUnit(nextUnit).unitType;

                            // Determine which exam the user would take after passing the unit exam
                            final RawExam nextExam;
                            if ("FIN".equals(type)) {
                                nextExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(nextUnit),
                                        "U");
                            } else {
                                nextExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(nextUnit),
                                        "R");
                            }

                            if (nextExam != null) {
                                // See what proctoring options are available
                                final RawCsection courseSection = courseStatus.getCourseSection();
                                final List<EProctoringOption> proctoringOptions =
                                        RawCsection.getProctoringOptions(courseSection);

                                final int count = proctoringOptions == null ? 0 : proctoringOptions.size();

                                if (count > 0) {
                                    htm.div("gap2");
                                    htm.sDiv("indent1");
                                    htm.addln(" You must pass the <b>", unitLabel, "</b>");
                                    if (count == 1 && proctoringOptions.contains(EProctoringOption.DEPT_TEST_CENTER)) {
                                        htm.addln("in the Precalculus Center");
                                    }
                                    htm.addln("before you can move on to the <b>", nextExam.buttonLabel, "</b>.");
                                    htm.eDiv();
                                }
                            }
                        }
                    }
                }
            }
        } else if ("practice".equals(mode) && version != null) {
            htm.sDiv("indent");
            htm.addln("<form method='get' action='run_review.html'>");

            htm.sDiv("reviewbtn");
            htm.addln(" <input type='hidden' name='course' value='", course.course, "'/>");
            htm.addln(" <input type='hidden' name='exam' value='", version, "'/>");
            htm.addln(" <input type='hidden' name='mode' value='practice'/>");
            if (label != null && label.contains("Practice")) {
                htm.addln(" <button class='btn' type='submit'>", label, "</button> &nbsp;");
            } else {
                htm.addln(" <button class='btn' type='submit'>Practice ", label, "</button> &nbsp;");
            }
            htm.eDiv();
            htm.addln("</form>");
            htm.eDiv(); // indent
        } else if ("locked".equals(mode) && version != null) {
            htm.sDiv("indent");
            htm.addln("<form method='get' action='run_review.html'>");

            htm.sDiv("reviewbtn");
            htm.addln(" <input type='hidden' name='course' value='", course.course, "'/>");
            htm.addln(" <input type='hidden' name='exam' value='", version, "'/>");
            htm.addln(" <input type='hidden' name='mode' value='practice'/>");
            if (label != null && label.contains("Practice")) {
                htm.addln(" <button class='btn' type='submit'>", label, "</button> &nbsp;");
            } else {
                htm.addln(" <button class='btn' type='submit'>Practice ", label, "</button> &nbsp;");
            }
            htm.addln(" <span class='why_unavail'>Past final exam deadline, practice only</span>");
            htm.eDiv();
            htm.addln("</form>");
            htm.eDiv(); // indent
        }
    }

    /**
     * Checks whether the student is eligible for automatic extensions on a Review Exam, and emits buttons to request
     * those.
     *
     * @param cache        the data cache
     * @param courseStatus the course status object
     * @param dueDate      the current review exam due date
     * @param unitNum      the unit number
     * @param htm          the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitReviewExamExtensions(final Cache cache, final StudentCourseStatus courseStatus,
                                                 final LocalDate dueDate, final int unitNum, final HtmlBuilder htm)
            throws SQLException {

        final RawStterm stterm = courseStatus.getStudentTerm();
        final RawStcourse stcourse = courseStatus.getStudentCourse();

        final RawStudent stu = courseStatus.getStudent();
        final String course = courseStatus.getCourse().course;

        if (stcourse != null && stcourse.paceOrder != null && stterm != null && stterm.pace != null
                && stterm.paceTrack != null) {

            final int index = stcourse.paceOrder.intValue();
            final int paceInt = stterm.pace.intValue();

            try {
                final int accommodationExtensionDays = paceInt < 1 ? 0 :
                        MilestoneLogic.daysAvailableLegacyAccommodationExtension(
                                cache, stu.stuId, stterm.paceTrack, paceInt, index, unitNum, "RE");

                if (accommodationExtensionDays == 0) {
                    htm.sP("indent");
                    htm.add("Your SDC accommodation extension has already been applied to this due date.");
                    htm.eP();
                } else if (accommodationExtensionDays > 0) {
                    // If the due date is in the past or near future, show SDC accommodation
                    final LocalDate today = LocalDate.now();
                    final LocalDate soon = today.plusDays(4L);
                    if (dueDate.isBefore(soon)) {
                        final String daysStr = Integer.toString(accommodationExtensionDays);

                        htm.addln("<form method='POST' action='request_accom_extension.html'>");

                        htm.sP("indent").add("You have an extension of ", daysStr,
                                " days available based on your SDC accommodation.").br();
                        htm.addln(" <input type='hidden' name='course' value='", course, "'/>");
                        htm.addln(" <input type='hidden' name='stu' value='", stu.stuId, "'/>");
                        htm.addln(" <input type='hidden' name='track' value='", stterm.paceTrack, "'/>");
                        htm.addln(" <input type='hidden' name='pace' value='", stterm.pace, "'/>");
                        htm.addln(" <input type='hidden' name='index' value='", index, "'/>");
                        htm.addln(" <input type='hidden' name='unit' value='", unitNum, "'/>");
                        htm.addln(" <input type='hidden' name='type' value='RE'/>");

                        htm.addln(" &nbsp; <button class='smallbtn' type='submit'>",
                                "Apply my accommodation extension</button>");
                        htm.eP();
                        htm.addln("</form>");
                    }
                }
            } catch (final IllegalArgumentException ex) {
                Log.warning(ex);
            }

            try {
                final int freeExtensionDays = MilestoneLogic.daysAvailableLegacyFreeExtension(cache, stu.stuId,
                        stterm.paceTrack, paceInt, index, unitNum, "RE");

                if (freeExtensionDays == 0) {
                    htm.sP("indent");
                    htm.add("Your free extension has already been applied to this due date.");
                    htm.eP();
                } else if (freeExtensionDays > 0) {
                    // If the due date is in the past or near future, show SDC accommodation
                    final LocalDate today = LocalDate.now();
                    final LocalDate soon = today.plusDays(2L);

                    if (dueDate.isBefore(soon)) {
                        final String daysStr = Integer.toString(freeExtensionDays);
                        htm.addln("<form method='POST' action='request_free_extension.html'>");
                        htm.sP("indent");
                        htm.add("All students are allowed a ", daysStr,
                                "-day free extension to account for unexpected situations that may arise.").br();

                        htm.addln(" <input type='hidden' name='course' value='", course, "'/>");
                        htm.addln(" <input type='hidden' name='stu' value='", stu.stuId, "'/>");
                        htm.addln(" <input type='hidden' name='track' value='", stterm.paceTrack, "'/>");
                        htm.addln(" <input type='hidden' name='pace' value='", stterm.pace, "'/>");
                        htm.addln(" <input type='hidden' name='index' value='", index, "'/>");
                        htm.addln(" <input type='hidden' name='unit' value='", unitNum, "'/>");
                        htm.addln(" <input type='hidden' name='type' value='RE'/>");
                        htm.addln(" &nbsp; <button class='smallbtn' type='submit'>Apply my free extension</button>");
                        htm.eP();
                        htm.addln("</form>");
                    }
                }
            } catch (final IllegalArgumentException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Present the button for the unit exam along with status.
     *
     * @param cache        the data cache
     * @param siteType     the site type
     * @param session      the login session
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit
     * @param mode         the mode ("course", "practice", or "locked")
     * @param dimmed       {@code true} if the button should be dimmed regardless of status
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @param now          the date/time to consider as "now"
     * @throws SQLException if there is an error accessing the database
     */
    private static void doFinalExam(final Cache cache, final ESiteType siteType, final ImmutableSessionInfo session,
                                    final StudentCourseStatus courseStatus, final int unitNum, final String mode,
                                    final boolean dimmed, final HtmlBuilder htm,
                                    final ChronoZonedDateTime<LocalDate> now) throws SQLException {

        final String courseId = courseStatus.getCourse().course;
        final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);

        final SystemData systemData = cache.getSystemData();

        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(unitNum), "F");

        final String label;
        if (unitExam == null) {
            label = null;
        } else {
            label = unitExam.buttonLabel;
        }

        final RawStcourse stcourse = courseStatus.getStudentCourse();

        final LocalDate deadline;
        final LocalDate lastTry;
        final Integer lastTryAttempts;

        if ("Y".equals(stcourse.iInProgress) && !"Y".equals(stcourse.iCounted) && stcourse.iDeadlineDt != null) {
            deadline = stcourse.iDeadlineDt;
            lastTry = stcourse.iDeadlineDt;
            lastTryAttempts = null;
        } else {
            deadline = courseStatus.getUnitExamDeadline(unitNum);
            lastTry = courseStatus.getUnitExamLastTry(unitNum);
            lastTryAttempts = courseStatus.getUnitExamLastTryAttempts(unitNum);
        }

        final boolean pastDeadline = deadline != null && deadline.isBefore(now.toLocalDate());

        final int maxUnit = courseStatus.getMaxUnit();

        // Buttons to practice each unit exam
        if ("course".equals(mode)) {
            htm.sDiv("indent");
            for (int i = 0; i <= maxUnit; ++i) {
                final RawCunit tryUnit = courseStatus.getCourseUnit(i);
                final String type = tryUnit == null ? "SR" : tryUnit.unitType;

                if ("INST".equals(type)) {
                    final Integer unitInt = Integer.valueOf(i);
                    final RawExam rev = systemData.getActiveExamByCourseUnitType(courseId, unitInt, "R");

                    if (rev != null) {
                        final String pastLabel = rev.buttonLabel;

                        if (dimmed || (!"Passed".equals(courseStatus.getProctoredStatus(i)) || (rev.version == null))) {
                            htm.sDiv("reviewbtn");
                            if (pastLabel != null && pastLabel.contains("Practice")) {
                                htm.addln(" <button class='btn' type='submit' disabled='disabled'>", pastLabel,
                                        "</button>");
                            } else {
                                htm.addln(" <button class='btn' type='submit' disabled='disabled'>Practice ",
                                        pastLabel, "</button>");
                            }
                            htm.eDiv();
                        } else {
                            htm.addln("<form method='get' action='run_review.html'>");
                            htm.addln(" <input type='hidden' name='course' value='", courseStatus.getCourse().course,
                                    "'/>");
                            htm.addln(" <input type='hidden' name='exam' value='", rev.version, "'/>");
                            htm.addln(" <input type='hidden' name='mode' value='", mode, "'/>");

                            htm.sDiv("reviewbtn");
                            if (pastLabel != null && pastLabel.contains("Practice")) {
                                htm.addln(" <button class='btn' type='submit'>", pastLabel, "</button>");
                            } else {
                                htm.addln(" <button class='btn' type='submit'>Practice ", pastLabel, "</button>");
                            }
                            htm.eDiv();

                            htm.addln("</form>");
                        }
                    }
                }
            }
            htm.eDiv();
        }

        final boolean isPassing = courseStatus.isPassing(unitNum);
        if (!isPassing && deadline != null) {
            if (pastDeadline) {
                htm.sP("indent").add("<strong class='redred'>");
                htm.addln(" The deadline to pass the Final Exam was ",
                        TemporalUtils.FMT_WMDY.format(deadline), "</strong>.");
                htm.eP();

                if (lastTry != null && !lastTry.isBefore(now.toLocalDate())) {
                    // Student eligible for "last try" and within last try deadline

                    // Need to see if student has used all "last try" attempts
                    final List<RawStexam> exams = RawStexamLogic.getExams(cache, courseStatus.getStudent().stuId,
                            courseStatus.getStudentCourse().course, false, "F");

                    int count = 0;
                    for (final RawStexam test : exams) {
                        if (test.examDt.isAfter(deadline)) {
                            ++count;
                        }
                    }

                    htm.sP("indent").add("<strong>", "<span class='redred'>");
                    final String lastTryText;
                    if (lastTryAttempts == null || lastTryAttempts.intValue() == 1) {
                        lastTryText = "ONE additional attempt";
                    } else {
                        lastTryText = lastTryAttempts + " additional attempts";
                    }

                    if (count == 0) {
                        htm.addln(" Because you were eligible for the Final Exam by its due date, you have a total of ",
                                lastTryText, " on the Final Exam by ", TemporalUtils.FMT_MDY.format(lastTry),
                                CoreConstants.DOT);
                    } else if (lastTryAttempts != null && count < lastTryAttempts.intValue()) {
                        htm.addln(" Because you were eligible for the Final Exam by its due date, you were given ");

                        if (lastTryAttempts.intValue() == 1) {
                            htm.addln(lastTryText);
                        } else {
                            htm.addln("a total of ", lastTryText);
                        }

                        htm.addln(" on the Final Exam by ", TemporalUtils.FMT_MDY.format(lastTry),
                                ". You have already used ");
                        if (count == 1) {
                            htm.addln("ONE attempt,");
                        } else {
                            htm.addln(Integer.toString(count), " attempts,");
                        }

                        htm.addln(" and have ", Integer.valueOf(lastTryAttempts.intValue() - count), " remaining.");
                    } else {
                        htm.addln(" You have used the ", lastTryText,
                                " on the Final Exam for which you were eligible.");
                    }
                    htm.addln("</span></strong>").eP();
                }
            } else if (label != null) {

                final RawCsection csection = courseStatus.getCourseSection();
                final int minToPass;
                if (csection.dMinScore == null) {
                    if (csection.cMinScore == null) {
                        if (csection.bMinScore == null) {
                            minToPass = csection.aMinScore.intValue();
                        } else {
                            minToPass = csection.bMinScore.intValue();
                        }

                    } else {
                        minToPass = csection.cMinScore.intValue();
                    }
                } else {
                    minToPass = csection.dMinScore.intValue();
                }

                htm.sP("indent blue");
                htm.addln(" To complete this course, you must <strong>PASS</strong> the ", label);
                htm.addln(" by <strong>", TemporalUtils.FMT_WMDY.format(deadline),
                        "</strong> and you must earn <strong>at least ", Integer.toString(minToPass),
                        " total points</strong>.");
                htm.eP();

                final boolean nonCountedIncomplete = courseStatus.isIncompleteInProgress()
                        && "N".equals(courseStatus.getStudentCourse().iCounted);

                if (!nonCountedIncomplete) {
                    // Don't show "Last try" prompt for a student with a non-counted incomplete who has a fixed I
                    // Deadline Date.

                    final String lastTryText;
                    final String lastTryText2;
                    if (lastTryAttempts == null || lastTryAttempts.intValue() == 1) {
                        lastTryText = "ONE more opportunity";
                        lastTryText2 = "that one attempt";
                    } else {
                        lastTryText = lastTryAttempts + " more opportunities";
                        lastTryText2 = "those attempts";
                    }

                    htm.sP("indent");
                    htm.addln("If you become eligible for, but do not pass the <b>", label,
                            "</b> by this date, you will be given ", lastTryText, " to take the <b>", label,
                            "</b> on THE VERY NEXT DAY DAY THE PRECALCULUS CENTER IS OPEN. If you do not pass on ",
                            lastTryText2, ", you cannot complete the course.");
                    htm.eP();
                }
            }

            emitFinalExamExtensions(cache, courseStatus, deadline, htm);
        }

        final String range = courseStatus.getProctoredRange(unitNum);
        if (range != null) {
            htm.sDiv("indent2").add(range).eDiv();
        }

        // Show a link to take the exam with proctoring service
        if (RawRecordConstants.M117.equals(courseId) || RawRecordConstants.M118.equals(courseId)
                || RawRecordConstants.M124.equals(courseId) || RawRecordConstants.M125.equals(courseId)
                || RawRecordConstants.M126.equals(courseId)) {
            doCanvasFinalExam(cache, siteType, session, courseStatus, unitNum, mode, htm);
        }

        if ("course".equals(mode)) {
            examStatus(courseStatus, unitNum, label, unitAvail, htm);
        }
    }

    /**
     * Checks whether the student is eligible for automatic extensions on a Final Exam, and emits buttons to request
     * those.
     *
     * @param cache        the data cache
     * @param courseStatus the course status object
     * @param dueDate      the current review exam due date
     * @param htm          the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void emitFinalExamExtensions(final Cache cache, final StudentCourseStatus courseStatus,
                                                final LocalDate dueDate, final HtmlBuilder htm)
            throws SQLException {

        final RawStterm stterm = courseStatus.getStudentTerm();
        final RawStcourse stcourse = courseStatus.getStudentCourse();

        final RawStudent stu = courseStatus.getStudent();
        final String course = courseStatus.getCourse().course;

        if (stcourse != null && stcourse.paceOrder != null && stterm != null && stterm.pace != null
                && stterm.paceTrack != null) {

            final int index = stcourse.paceOrder.intValue();
            final int paceInt = stterm.pace.intValue();

            try {
                final int accommodationExtensionDays = paceInt < 1 ? 0 :
                        MilestoneLogic.daysAvailableLegacyAccommodationExtension(
                                cache, stu.stuId, stterm.paceTrack, paceInt, index, 5, "FE");

                if (accommodationExtensionDays == 0) {
                    htm.sP("indent");
                    htm.add("Your SDC accommodation extension has already been applied to this due date.");
                    htm.eP();
                } else if (accommodationExtensionDays > 0) {
                    // If the due date is in the past or near future, show SDC accommodation
                    final LocalDate today = LocalDate.now();
                    final LocalDate soon = today.plusDays(4L);
                    if (dueDate.isBefore(soon)) {
                        final String daysStr = Integer.toString(accommodationExtensionDays);

                        htm.addln("<form method='POST' action='request_accom_extension.html'>");

                        htm.sP("indent").add("You have an extension of ", daysStr,
                                " days available based on your SDC accommodation.").br();
                        htm.addln(" <input type='hidden' name='course' value='", course, "'/>");
                        htm.addln(" <input type='hidden' name='stu' value='", stu.stuId, "'/>");
                        htm.addln(" <input type='hidden' name='track' value='", stterm.paceTrack, "'/>");
                        htm.addln(" <input type='hidden' name='pace' value='", stterm.pace, "'/>");
                        htm.addln(" <input type='hidden' name='index' value='", index, "'/>");
                        htm.addln(" <input type='hidden' name='unit' value='5'/>");
                        htm.addln(" <input type='hidden' name='type' value='FE'/>");

                        htm.addln(" &nbsp; <button class='smallbtn' type='submit'>",
                                "Apply my accommodation extension</button>");
                        htm.eP();
                        htm.addln("</form>");
                    }
                }
            } catch (final IllegalArgumentException ex) {
                Log.warning(ex);
            }

            try {
                final int freeExtensionDays = MilestoneLogic.daysAvailableLegacyFreeExtension(cache, stu.stuId,
                        stterm.paceTrack, paceInt, index, 5, "FE");

                if (freeExtensionDays == 0) {
                    htm.sP("indent");
                    htm.add("Your free extension has already been applied to this due date.");
                    htm.eP();
                } else if (freeExtensionDays > 0) {
                    // If the due date is in the past or near future, show SDC accommodation
                    final LocalDate today = LocalDate.now();
                    final LocalDate soon = today.plusDays(2L);

                    if (dueDate.isBefore(soon)) {
                        final String daysStr = Integer.toString(freeExtensionDays);
                        htm.addln("<form method='POST' action='request_free_extension.html'>");
                        htm.sP("indent");
                        htm.add("All students are allowed a ", daysStr,
                                "-day free extension to account for unexpected situations that may arise.").br();

                        htm.addln(" <input type='hidden' name='course' value='", course, "'/>");
                        htm.addln(" <input type='hidden' name='stu' value='", stu.stuId, "'/>");
                        htm.addln(" <input type='hidden' name='track' value='", stterm.paceTrack, "'/>");
                        htm.addln(" <input type='hidden' name='pace' value='", stterm.pace, "'/>");
                        htm.addln(" <input type='hidden' name='index' value='", index, "'/>");
                        htm.addln(" <input type='hidden' name='unit' value='5'/>");
                        htm.addln(" <input type='hidden' name='type' value='FE'/>");
                        htm.addln(" &nbsp; <button class='smallbtn' type='submit'>Apply my free extension</button>");
                        htm.eP();
                        htm.addln("</form>");
                    }
                }
            } catch (final IllegalArgumentException ex) {
                Log.warning(ex);
            }
        }
    }

    /**
     * Presents the student's current status with respect to an exam, including the number of attempts the student has
     * used, the current score, and information if any attempts remain to try to improve the score.
     *
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit number
     * @param label        the exam label
     * @param unitAvail    {@code true} if the exam is available; {@code false} if not
     * @param htm          the {@code HtmlBuilder} to which to append
     */
    private static void examStatus(final StudentCourseStatus courseStatus, final int unitNum,
                                   final String label, final boolean unitAvail, final HtmlBuilder htm) {

        final int timesTaken = courseStatus.getProctoredTimesTaken(unitNum);
        final int curScore = courseStatus.getScores().getRawUnitExamScore(unitNum);
        final int perfectScore = courseStatus.getPerfectScore(unitNum);
        final boolean isPassing = courseStatus.isPassing(unitNum);

        // This method may only be called when mode = 'course'
        if (timesTaken > 0) {

            htm.sDiv("indent");

            if (!"Final Exam".equals(label)) {
                htm.addln(" You have taken the <b>", label, "</b> ");
                if (timesTaken == 1) {
                    htm.addln("one time.");
                } else {
                    htm.addln(Integer.toString(timesTaken), " times.");
                }
            }

            htm.addln("Your current score on the ", label, " is <b>", Integer.toString(curScore), "</b> out of <b>",
                    Integer.toString(perfectScore), "</b>");

            if (curScore == perfectScore) {
                htm.addln(" (a perfect score).");
            } else if (isPassing) {
                htm.addln(" (a passing score).");

                if (unitAvail) {
                    htm.addln(" You may retake the <b>", label, "</b> to try to improve this score.");
                }
            } else {
                htm.addln(" (not a passing score).");

                if (unitAvail) {
                    htm.addln(" You must retake the <b>", label,
                            "</b> and earn a passing score to complete this course.");
                }
            }

            htm.eDiv();

            htm.div("vgap");
        }
    }

    /**
     * Show instructions for taking a proctored final exam using an online service, with the password input box for the
     * proctor.
     *
     * @param cache        the data cache
     * @param siteType     the site type
     * @param session      the session
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit
     * @param mode         the mode
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCanvasUnitExam(final Cache cache, final ESiteType siteType,
                                         final ImmutableSessionInfo session, final StudentCourseStatus courseStatus,
                                         final int unitNum, final String mode, final HtmlBuilder htm)
            throws SQLException {

        final String courseId = courseStatus.getCourse().course;
        final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);
        final boolean unitPassed = courseStatus.isProctoredPassed(unitNum);

        final SystemData systemData = cache.getSystemData();

        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(unitNum), "U");
        final String version;

        if (unitExam == null) {
            version = null;
        } else {
            version = unitExam.version;
        }

        if ("course".equals(mode) && version != null && unitAvail) {

            final UnitExamSessionStore uess = UnitExamSessionStore.getInstance();
            final String examCode = uess.makeExamCode(session);
            if (examCode.length() == 6) {
                // TODO:
            }

            final LocalDate today = session.getNow().toLocalDate();
            final boolean isRamwork = RawSpecialStusLogic.isSpecialType(cache, session.getEffectiveUserId(), today,
                    RawSpecialStus.RAMWORK);

            final boolean isCE = "CE".equals(courseStatus.getCourseSection().instrnType);

            htm.div("vgap");

            final String examName = courseId.replace("M ", "MATH ") + " - Unit " + unitNum + " Exam";

            if (unitPassed) {
                htm.sP(null, "style='margin:0 0 14px 50px;'")
                        .add(" <span class='why_done'><img src='/images/check.png' ",
                                "style='position:relative;top:-2px;' alt=''/> ", examName + " Passed</span>").eP();
            }

            htm.addln("<div style='max-width:380pt;margin:0 30pt;padding:4pt 8pt 0 8pt;border:3px double #004f39;",
                    "background-color:#ffffc6;'>");

            htm.add("To take the proctored <b>Unit ", Integer.toString(unitNum), " Exam</b>:");

            if (isRamwork || isCE) {
                final String url = siteType == ESiteType.PROD
                        ? "https://course.math.colostate.edu/mps/index.html"
                        : "https://coursedev.math.colostate.edu/mps/index.html";

                htm.addln("<ul>");
                htm.addln("<li> Log in to the <a href='", url, "' class='ulink' target='_blank'><strong>",
                        "Mathematics Proctoring System</strong></a>, and allow your browser to use ",
                        "your webcam and share your screen (please share your ENTIRE SCREEN).</li>");
                htm.addln("<li> Select <strong>Choose Exam</strong>, then <strong>", examName, "</strong>.</li>");
                htm.addln("<li> Follow the instructions to complete your exam.</li>");
                htm.addln("<li> You can use scratch paper and a personal calculator or the ",
                        "Desmos calculator; a link to Desmos should appear above the exam.");
                htm.addln("</ul>");

                htm.sDiv("center");
                htm.addln("<a href='", url, "' class='btn' target='_blank'>Take the ", examName, "</a>");
                htm.eDiv();
                htm.hr();
            } else {
                // Not CE or authorized to use RamWork
                htm.addln("<li> Come to the Precalculus Center testing area (Weber 138).</li>");
                htm.addln("<li> Store any personal items in lockers provided (locks are available to lend), and ",
                        "take only your RamCard and a pencil or pen into the testing area. No other resources are ",
                        "allowed. Scratch paper will be provided.</li>");
                htm.addln("<li> Request the <b>", examName, "</b>.</li>");
                htm.addln("<li> An on-screen TI-84 calculator will be provided on the testing computer.</li>");
                htm.addln("<li> When the exam is completed, return to this site to check your status.</li>");
            }

            htm.eDiv();

            htm.div("vgap");
        }
    }

    /**
     * Show instructions for taking a proctored final exam using an online service, with the password input box for the
     * proctor.
     *
     * @param cache        the data cache
     * @param siteType     the site type
     * @param session      the session
     * @param courseStatus the student's status in the course
     * @param unitNum      the unit
     * @param mode         the mode
     * @param htm          the {@code HtmlBuilder} to which to append the HTML
     * @throws SQLException if there is an error accessing the database
     */
    private static void doCanvasFinalExam(final Cache cache, final ESiteType siteType,
                                          final ImmutableSessionInfo session, final StudentCourseStatus courseStatus,
                                          final int unitNum, final String mode, final HtmlBuilder htm)
            throws SQLException {

        final String courseId = courseStatus.getCourse().course;
        final boolean unitAvail = courseStatus.isProctoredExamAvailable(unitNum);

        final SystemData systemData = cache.getSystemData();

        final RawExam unitExam = systemData.getActiveExamByCourseUnitType(courseId, Integer.valueOf(unitNum), "F");
        final String version;

        if (unitExam == null) {
            version = null;
        } else {
            version = unitExam.version;
        }

        // Log.info("Student ", courseStatus.getStudent().stuId, ": Course=", courseId, ", mode=",
        // mode, ", examId=", version, ", avail=", Boolean.toString(unitAvail));

        if ("course".equals(mode) && version != null && unitAvail) {

            final LocalDate today = session.getNow().toLocalDate();
            final boolean isRamwork = RawSpecialStusLogic.isSpecialType(cache, session.getEffectiveUserId(), today,
                    RawSpecialStus.RAMWORK);

            htm.div("vgap");
            htm.addln("<div style='max-width:380pt;margin:0 30pt;padding:4pt 8pt 0 8pt;border:3px double #004f39;",
                    "background-color:#ffffc6;'>");

            final String examName = courseId.replace("M ", "MATH ") + " - Final Exam";

            final boolean isCE = "CE".equals(courseStatus.getCourseSection().instrnType);

            htm.add("To take the proctored <b>Final Exam</b>:");

            if (isRamwork || isCE) {
                final String url = siteType == ESiteType.PROD
                        ? "https://course.math.colostate.edu/mps/index.html"
                        : "https://coursedev.math.colostate.edu/mps/index.html";

                htm.addln("<ul>");
                htm.addln("<li> Log in to the <a href='", url, "' class='ulink' target='_blank'><strong>Mathematics ",
                        "Proctoring System</strong></a>, and allow your browser to use your webcam and share your ",
                        "screen.</li>");
                htm.addln("<li> Select <strong>Choose Exam</strong>, then <strong>", examName, "</strong>.</li>");
                htm.addln("<li> Follow the instructions to complete your exam.</li>");
                htm.addln("<li> You can use scratch paper and a personal calculator or the Desmos calculator; a ",
                        "link to Desmos should appear above the exam.");
                htm.addln("</ul>");

                htm.sDiv("center");
                htm.addln("<a href='", url, "' class='btn' target='_blank'>Take the ", examName, "</a>");
                htm.eDiv();
                htm.hr();
            } else {
                htm.addln("<li> Come to the Precalculus Center testing area (Weber 138).</li>");
                htm.addln("<li> Store any personal items in lockers provided (locks are available to lend), and ",
                        "take only your RamCard and a pencil or pen into the testing area. No other resources are ",
                        "allowed. Scratch paper will be provided.</li>");
                htm.addln("<li> Request the <b>", examName, "</b>.</li>");
                htm.addln("<li> An on-screen TI-84 calculator will be provided on the testing computer.</li>");
                htm.addln("<li> When the exam is completed, return to this site to check your status.</li>");
            }
            htm.addln("</ul>");

            htm.eDiv();
            htm.div("vgap");
        }
    }
}
