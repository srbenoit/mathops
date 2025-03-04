package dev.mathops.web.site.canvas.courses;

import dev.mathops.db.Cache;
import dev.mathops.db.old.rawlogic.RawCsectionLogic;
import dev.mathops.db.old.rawrecord.RawCourse;
import dev.mathops.db.old.rawrecord.RawCsection;
import dev.mathops.db.old.rawrecord.RawRecordConstants;
import dev.mathops.db.old.rawrecord.RawStcourse;
import dev.mathops.db.rec.TermRec;
import dev.mathops.db.reclogic.TermLogic;
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
 * This page shows a module Topic.
 */
public enum PageTopic {
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
     * @param topic    the topic number
     * @param req      the request
     * @param resp     the response
     * @param session  the user's login session information
     * @param metadata the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final CanvasSite site, final String courseId, final int module,
                             final int topic, final ServletRequest req, final HttpServletResponse resp,
                             final ImmutableSessionInfo session, final Metadata metadata)
            throws IOException, SQLException {

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
                presentTopicPage(cache, site, req, resp, session, module, topic, registration, metaCourse);
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
     * @param session      the login session
     * @param module       the module number
     * @param topic        the topic number
     * @param registration the student's registration record
     * @param metaCourse   the metadata object with course structure data
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void presentTopicPage(final Cache cache, final CanvasSite site, final ServletRequest req,
                                 final HttpServletResponse resp, final ImmutableSessionInfo session, final int module,
                                 final int topic, final RawStcourse registration, final MetadataCourse metaCourse)
            throws IOException, SQLException {

        final TermRec active = TermLogic.get(cache).queryActive(cache);
        final List<RawCsection> csections = RawCsectionLogic.queryByTerm(cache, active.term);

        RawCsection csection = null;
        for (final RawCsection test : csections) {
            if (registration.course.equals(test.course) && registration.sect.equals(test.sect)) {
                csection = test;
                break;
            }
        }

        if (csection == null) {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        } else {
            final HtmlBuilder htm = new HtmlBuilder(2000);
            final String siteTitle = site.getTitle();

            CanvasPageUtils.startPage(htm, siteTitle);

            // Emit the course number and section at the top
            CanvasPageUtils.emitCourseTitleAndSection(htm, metaCourse, csection);

            htm.sDiv("pagecontainer");

            CanvasPageUtils.emitLeftSideMenu(htm, metaCourse, ECanvasPanel.MODULES);

            htm.sDiv("flexmain");

            final String courseId = registration.course;

            // TODO: Make topics data-driven

            if (RawRecordConstants.MATH122.equals(courseId)) {
                presentMATH122Topic(cache, site, req, resp, session, module, topic, registration, htm);
            } else if (RawRecordConstants.MATH125.equals(courseId)) {
                presentMATH125Topic(cache, site, req, resp, session, module, topic, registration, htm);
            } else if (RawRecordConstants.MATH126.equals(courseId)) {
                presentMATH126Topic(cache, site, req, resp, session, module, topic, registration, htm);
            }

            htm.eDiv(); // flexmain
            htm.eDiv(); // pagecontainer

            CanvasPageUtils.endPage(htm);

            AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
        }
    }

    /**
     * Presents a MATH 122 topic.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param topic        the topic number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH122Topic(final Cache cache, final CanvasSite site, final ServletRequest req,
                                    final HttpServletResponse resp, final ImmutableSessionInfo session,
                                    final int module, final int topic, final RawStcourse registration,
                                    final HtmlBuilder htm) throws IOException {

        if (topic == 1) {
            presentMath125Topic1(cache, site, req, resp, session, registration, htm);
        } else if (topic == 2) {
            presentMath125Topic2(cache, site, req, resp, session, registration, htm);
        } else if (topic == 3) {
            presentMath125Topic3(cache, site, req, resp, session, registration, htm);
        } else if (topic == 4) {
            presentMath125Topic4(cache, site, req, resp, session, registration, htm);
        } else if (topic == 5) {
            presentMath125Topic5(cache, site, req, resp, session, registration, htm);
        } else if (topic == 6) {
            presentMath125Topic6(cache, site, req, resp, session, registration, htm);
        } else if (topic == 7) {
            presentMath125Topic7(cache, site, req, resp, session, registration, htm);
        } else if (topic == 8) {
            presentMath125Topic8(cache, site, req, resp, session, registration, htm);
        } else if (topic == 9) {
            presentMath126Topic1(cache, site, req, resp, session, registration, htm);
        } else if (topic == 10) {
            presentMath126Topic2(cache, site, req, resp, session, registration, htm);
        } else if (topic == 11) {
            presentMath126Topic3(cache, site, req, resp, session, registration, htm);
        } else if (topic == 12) {
            presentMath126Topic4(cache, site, req, resp, session, registration, htm);
        } else if (topic == 13) {
            presentMath126Topic5(cache, site, req, resp, session, registration, htm);
        } else if (topic == 14) {
            presentMath126Topic6(cache, site, req, resp, session, registration, htm);
        } else if (topic == 15) {
            presentMath126Topic7(cache, site, req, resp, session, registration, htm);
        } else if (topic == 16) {
            presentMath126Topic8(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents a MATH 125 topic.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param topic        the topic number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH125Topic(final Cache cache, final CanvasSite site, final ServletRequest req,
                                    final HttpServletResponse resp, final ImmutableSessionInfo session,
                                    final int module, final int topic, final RawStcourse registration,
                                    final HtmlBuilder htm) throws IOException {

        if (topic == 1) {
            presentMath125Topic1(cache, site, req, resp, session, registration, htm);
        } else if (topic == 2) {
            presentMath125Topic2(cache, site, req, resp, session, registration, htm);
        } else if (topic == 3) {
            presentMath125Topic3(cache, site, req, resp, session, registration, htm);
        } else if (topic == 4) {
            presentMath125Topic4(cache, site, req, resp, session, registration, htm);
        } else if (topic == 5) {
            presentMath125Topic5(cache, site, req, resp, session, registration, htm);
        } else if (topic == 6) {
            presentMath125Topic6(cache, site, req, resp, session, registration, htm);
        } else if (topic == 7) {
            presentMath125Topic7(cache, site, req, resp, session, registration, htm);
        } else if (topic == 8) {
            presentMath125Topic8(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents MATH 125 topic 1.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic1(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

        htm.sH(2).add("Topic 1:&nbsp; <span style='color:#D9782D'>Angles</span>").eH(2);
        htm.hr();
    }

    /**
     * Presents MATH 125 topic 2.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic2(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

        htm.sH(2).add("Topic 2:&nbsp; <span style='color:#D9782D'>Triangles</span>").eH(2);
        htm.hr();
    }

    /**
     * Presents MATH 125 topic 3.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic3(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 125 topic 4.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic4(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 125 topic 5.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic5(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 125 topic 6.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic6(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 125 topic 7.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic7(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 125 topic 8.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath125Topic8(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents a MATH 126 topic
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param module       the module number
     * @param topic        the topic number
     * @param registration the student's registration record
     * @throws IOException if there is an error writing the response
     */
    static void presentMATH126Topic(final Cache cache, final CanvasSite site, final ServletRequest req,
                                    final HttpServletResponse resp, final ImmutableSessionInfo session,
                                    final int module, final int topic, final RawStcourse registration,
                                    final HtmlBuilder htm) throws IOException {

        if (topic == 1) {
            presentMath126Topic1(cache, site, req, resp, session, registration, htm);
        } else if (topic == 2) {
            presentMath126Topic2(cache, site, req, resp, session, registration, htm);
        } else if (topic == 3) {
            presentMath126Topic3(cache, site, req, resp, session, registration, htm);
        } else if (topic == 4) {
            presentMath126Topic4(cache, site, req, resp, session, registration, htm);
        } else if (topic == 5) {
            presentMath126Topic5(cache, site, req, resp, session, registration, htm);
        } else if (topic == 6) {
            presentMath126Topic6(cache, site, req, resp, session, registration, htm);
        } else if (topic == 7) {
            presentMath126Topic7(cache, site, req, resp, session, registration, htm);
        } else if (topic == 8) {
            presentMath126Topic8(cache, site, req, resp, session, registration, htm);
        } else {
            final String homePath = site.makeRootPath("home.html");
            resp.sendRedirect(homePath);
        }
    }

    /**
     * Presents MATH 126 topic 1.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic1(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 2.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic2(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 3.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic3(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 4.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic4(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 5.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic5(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 6.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic6(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 7.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic7(final Cache cache, final CanvasSite site, final ServletRequest req,
                                     final HttpServletResponse resp, final ImmutableSessionInfo session,
                                     final RawStcourse registration, final HtmlBuilder htm) {

    }

    /**
     * Presents MATH 126 topic 8.
     *
     * @param cache        the data cache
     * @param site         the owning site
     * @param req          the request
     * @param resp         the response
     * @param session      the login session
     * @param registration the student's registration record
     */
    static void presentMath126Topic8(final Cache cache, final CanvasSite site, final ServletRequest req,
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

        htm.sP().addln(
                "<form class='hidden' id='error_rpt" + key + "' action='example_feedback.html' method='post'>");
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
