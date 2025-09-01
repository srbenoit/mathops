package dev.mathops.web.host.precalc.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.logic.MainData;
import dev.mathops.db.logic.TermData;
import dev.mathops.db.schema.legacy.RawCourse;
import dev.mathops.db.schema.legacy.RawCsection;
import dev.mathops.db.schema.legacy.RawStcourse;
import dev.mathops.db.rec.main.StandardsCourseRec;
import dev.mathops.db.rec.term.StandardsCourseSectionRec;
import dev.mathops.db.schema.RawRecordConstants;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.host.precalc.canvas.CanvasPageUtils;
import dev.mathops.web.host.precalc.canvas.CanvasSite;
import dev.mathops.web.host.precalc.canvas.ECanvasPanel;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This page shows a module Skills Review.
 */
public enum PageReview {
    ;

    /** The URL prefix for the video server. */
    private static final String VIDEO_URL = "https://nibbler.math.colostate.edu/media/";

    /**
     * Starts the page that shows the course outline with student progress.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param courseId the course ID
     * @param module   the module number
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final int module,
                             final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final String stuId = session.getEffectiveUserId();
        final RawStcourse registration = CanvasPageUtils.confirmRegistration(cache, stuId, courseId);

        if (registration == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final MainData mainData = cache.getMainData();
            final StandardsCourseRec course = mainData.getStandardsCourse(registration.course);
            if (course == null) {
                // TODO: Error display, course not part of this system rather than a redirect to Home
                final String homePath = site.makeRootPath("home.htm");
                resp.sendRedirect(homePath);
            } else {
                presentReviewPage(cache, site, req, resp, session, module, registration, course);
            }
        }
    }

    /**
     * Presents the Skills Review items for a course and module.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param registration the student's registration record
     * @param course       the course object
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentReviewPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                  final HttpServletResponse resp, final ImmutableSessionInfo session, final int module,
                                  final RawStcourse registration, final StandardsCourseRec course)
            throws IOException, SQLException {

        final TermData termData = cache.getTermData();
        final StandardsCourseSectionRec section = termData.getStandardsCourseSection(registration.course,
                registration.sect);

        if (section == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, course, section);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, course, null, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            htm.sH(2).add("Module " + module + " Skills Review").eH(2);
            htm.hr();

            final String courseId = registration.course;

            // TODO: Make skills reviews data-driven

            if (RawRecordConstants.MATH125.equals(courseId)) {
                presentMATH125Review(cache, site, req, resp, session, module, registration, htm);
            } else if (RawRecordConstants.MATH126.equals(courseId)) {
                presentMATH126Review(cache, site, req, resp, session, module, registration, htm);
            }

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Presents the Skills Review items for MATH 122.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH122Review(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final int module, final RawStcourse registration, final HtmlBuilder htm)
            throws IOException {

        if (module == 1) {
            presentMath125Review1(cache, site, req, resp, session, registration, htm);
        } else if (module == 2) {
            presentMath125Review2(cache, site, req, resp, session, registration, htm);
        } else if (module == 3) {
            presentMath125Review3(cache, site, req, resp, session, registration, htm);
        } else if (module == 4) {
            presentMath125Review4(cache, site, req, resp, session, registration, htm);
        } else if (module == 5) {
            presentMath126Review1(cache, site, req, resp, session, registration, htm);
        } else if (module == 6) {
            presentMath126Review2(cache, site, req, resp, session, registration, htm);
        } else if (module == 7) {
            presentMath126Review3(cache, site, req, resp, session, registration, htm);
        } else if (module == 8) {
            presentMath126Review4(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents the Skills Review items for MATH 125.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH125Review(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final int module, final RawStcourse registration, final HtmlBuilder htm)
            throws IOException {

        if (module == 1) {
            presentMath125Review1(cache, site, req, resp, session, registration, htm);
        } else if (module == 2) {
            presentMath125Review2(cache, site, req, resp, session, registration, htm);
        } else if (module == 3) {
            presentMath125Review3(cache, site, req, resp, session, registration, htm);
        } else if (module == 4) {
            presentMath125Review4(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents the Skills Review items for MATH 125 module 1.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentMath125Review1(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

        startReview(htm, "Review Topic 1: Unit Conversions");
        startReviewItem(htm, "Example 1", "Multi-step Unit Conversions");
        emitExample(htm, "M125", "TR01_SR1_01");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 2: Adding and Subtracting Fractions");
        startReviewItem(htm, "Example 1", "Adding Fractions");
        endReviewItem(htm);
        startReviewItem(htm, "Example 2", "Adding Fractions including Variables");
        endReviewItem(htm);
        startReviewItem(htm, "Example 3", "Subtracting Fractions including Variables");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 3: Multiplying and Dividing Fractions");
        startReviewItem(htm, "Example 1", "Multiplying Fractions");
        endReviewItem(htm);
        startReviewItem(htm, "Example 2", "Dividing Fractions");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 4: Proportion and Ratios");
        startReviewItem(htm, "Example 1", "Calculating Using Proportionality");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 5: Evaluating Roots");
        startReviewItem(htm, "Example 1", "Simplifying Radical Expressions");
        endReviewItem(htm);
        startReviewItem(htm, "Example 2", "Evaluating Radical Expressions with Variables");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 6: Exponents and the Distributive Property");
        startReviewItem(htm, "Example 1", "Exponent Properties");
        endReviewItem(htm);
        startReviewItem(htm, "Example 2", "Distributing while Multiplying Polynomials");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 7: Properties of Square Roots");
        startReviewItem(htm, "Example 1", "Properties of Roots");
        endReviewItem(htm);
        endReview(htm);

        startReview(htm, "Review Topic 8: The Cartesian Plane and Point Coordinates");
        startReviewItem(htm, "Example 1", "Point Coordinates in the Plane");
        endReviewItem(htm);
        endReview(htm);
    }

    /**
     * Presents the Skills Review items for MATH 125 module 2.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Review2(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 125 module 3.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Review3(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 125 module 4.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Review4(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 126.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH126Review(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final int module, final RawStcourse registration, final HtmlBuilder htm)
            throws IOException {

        if (module == 1) {
            presentMath126Review1(cache, site, req, resp, session, registration, htm);
        } else if (module == 2) {
            presentMath126Review2(cache, site, req, resp, session, registration, htm);
        } else if (module == 3) {
            presentMath126Review3(cache, site, req, resp, session, registration, htm);
        } else if (module == 4) {
            presentMath126Review4(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents the Skills Review items for MATH 126 module 1.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Review1(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 126 module 2.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Review2(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 126 module 3.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Review3(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents the Skills Review items for MATH 126 module 4.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Review4(final Cache cache, final CanvasSite site, final ServletRequest req,
                                      final HttpServletResponse resp, final ImmutableSessionInfo session,
                                      final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Emits the course number, title, and section number.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param course   the course record
     * @param csection the course section record
     */
    private static void emitCourseTitleAndSection(final HtmlBuilder htm, final RawCourse course,
                                                  final RawCsection csection) {

        htm.sDiv(null, "style='margin:0 24px; border-bottom:1px solid #C7CDD1;'");
        htm.sH(1, "title");
        if ("Y".equals(csection.courseLabelShown)) {
            htm.add(course.courseLabel);
            htm.add(": ");
        }
        htm.add("<span style='color:#D9782D'>", course.courseName, "</span>");
        htm.br().add("<small>Section ", csection.sect, "</small>");
        htm.eDiv();
    }

    /**
     * Emits the HTML to start a module.
     *
     * @param htm   the {@code HtmlBuilder} to which to append
     * @param title the module title
     */
    private static void startReview(final HtmlBuilder htm, final String title) {

        htm.addln("<details class='review'>");
        htm.addln("  <summary class='review-summary'>", title, "</summary>");
    }

    /**
     * Emits the HTML to end a module.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endReview(final HtmlBuilder htm) {

        htm.addln("</details>");
    }

    /**
     * Starts a review item.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param heading the heading, like "Example 1"
     * @param title   the item title, like "Proving Fermat's last theorem"
     */
    private static void startReviewItem(final HtmlBuilder htm, final String heading, final String title) {

        htm.sDiv("review-item");
        htm.sDiv("review-title");
        htm.addln(heading, ": <b class='review-topic'>", title, "</b>");
        htm.eDiv(); // review-title
        htm.sDiv("indent");
    }

    /**
     * Ends a review item.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    private static void endReviewItem(final HtmlBuilder htm) {

        htm.eDiv(); // indent
        htm.eDiv(); // review-item
    }

    /**
     * Ends an example (a video with closed captions and text-only transcript and an accessible PDF).
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param courseDir the name of the course directory
     * @param filename  the filename root for all files
     */
    private static void emitExample(final HtmlBuilder htm, final String courseDir, final String filename) {

        htm.sP().add("<img src='/www/images/etext/pdf22.png' alt=''/>&nbsp; ",
                "<a style='vertical-align:6px;' href=''>Example with solution (PDF)</a>").eP();

        final String key = courseDir + filename;

        htm.addln("<details>");
        htm.addln("<summary>Video Walkthrough</summary>");

        htm.addln("<video style='max-width:960px; width:100%; border:1px solid gray;' controls>");
        htm.addln("  <source src='", VIDEO_URL, courseDir, "/mp4/", filename, ".mp4' type='video/mp4'/>");
        htm.addln("  <track src='/media/", courseDir, "/vtt/", filename, ".vtt' kind='subtitles' srclang='en' ",
                "label='English' default/>");
        htm.addln("  Your browser does not support inline video.");
        htm.addln("</video>");

        htm.sP().add("<a href='/math/", courseDir, "/transcripts/", filename, ".txt'>",
                "Access a plain-text transcript for screen-readers.</a>").eP();

        htm.sDiv("visible", "id='error_rpt_link" + key + "'");
        htm.add("<a href='#' onClick='showReportError" + key + "();'>",
                "Report an error or recommend an improvement...</a>");
        htm.eDiv();

        htm.sP().addln("<form class='hidden' id='error_rpt" + key + "' action='example_feedback.html' method='post'>");
        htm.addln("  <input type='hidden' name='course' value='", courseDir, "'/>");
        htm.addln("  <input type='hidden' name='media' value='", filename, "'/>");
        htm.addln("  Please describe the error or recommend an improvement:").br();
        htm.addln("  <textarea rows='5' cols='40' name='comments'></textarea>").br();
        htm.addln("  <input type='submit' value='Submit'/>");
        htm.addln("</form>").eP();

        htm.addln("</details>");
        htm.addln("<script>");
        htm.addln("function showReportError" + key + "() {");
        htm.addln("  document.getElementById('error_rpt_link" + key + "').className='hidden';");
        htm.addln("  document.getElementById('error_rpt" + key + "').className='visible';");
        htm.addln("}");
        htm.addln("</script>");
    }
}
