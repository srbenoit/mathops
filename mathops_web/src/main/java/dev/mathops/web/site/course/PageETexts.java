package dev.mathops.web.site.course;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.rawlogic.RawCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextCourseLogic;
import dev.mathops.db.old.rawlogic.RawEtextLogic;
import dev.mathops.db.old.rawlogic.RawStetextLogic;
import dev.mathops.db.old.rawrecord.RawEtext;
import dev.mathops.db.old.rawrecord.RawEtextCourse;
import dev.mathops.db.old.rawrecord.RawStetext;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.sitelogic.CourseInfo;
import dev.mathops.session.sitelogic.CourseSiteLogic;
import dev.mathops.session.sitelogic.CourseSiteLogicCourse;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.chrono.ChronoZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Generates the content of the web page that displays the e-texts the student has purchased.
 */
enum PageETexts {
    ;

    /** A shared date formatter. */
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US);

    /**
     * Generates a page to manage e-texts.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the user's login session information
     * @param logic   the course site logic
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doETextsPage(final Cache cache, final CourseSite site, final ServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session,
                             final CourseSiteLogic logic) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), session, false, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                true);

        htm.sDiv("menupanelu");
        CourseMenu.buildMenu(cache, site, session, logic, htm);
        htm.sDiv("panelu");

        doETextsPageContent(cache, site, session, logic, htm);

        htm.eDiv(); // panelu
        htm.eDiv(); // menupanelu

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the content of the e-text page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param session the user's login session information
     * @param logic   the course site logic
     * @param htm     the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    private static void doETextsPageContent(final Cache cache, final CourseSite site,
                                            final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                            final HtmlBuilder htm) throws SQLException {

        final String userId = session.getEffectiveUserId();

        htm.sH(2).add("<img style='width:40px;height:40px;vertical-align:middle;' ",
                "src='/images/etexts.png'/> &nbsp; My e-texts").eH(2);

        final List<RawStetext> stetexts = RawStetextLogic.queryByStudent(cache, userId);

        final int count = stetexts.size();

        htm.sDiv("indent11");

        if (count == 0) {
            final String host = site.siteProfile.host;

            final String message;

            if (Contexts.PRECALC_HOST.equals(host)) {
                message = " All Precalculus courses participate in the CSU Bookstore Inclusive "
                        + "Access program, which provides automatic access to the e-text book for "
                        + "registered students.  You <b>must not</b> \"opt out\" of that program, or "
                        + "you will lose access to the e-text book";
            } else {
                message = " You have no e-text books.";
            }

            htm.div("vgap");
            htm.addln(message);
            htm.div("vgap");

            final CourseSiteLogicCourse courseLogic = logic.course;
            if (courseLogic.requiresEText) {
                showActivateLink(htm);
            }
        } else {
            // Sort texts by status and category
            final Collection<RawStetext> refunded = new ArrayList<>(count);
            final Collection<RawStetext> expired = new ArrayList<>(count);
            final Collection<RawStetext> permanent = new ArrayList<>(count);
            final Collection<RawStetext> completed = new ArrayList<>(count);
            final Collection<RawStetext> termOnly = new ArrayList<>(count);

            for (final RawStetext stetext : stetexts) {
                if (stetext.refundDt != null) {
                    refunded.add(stetext);
                } else if (stetext.expirationDt != null
                        && stetext.expirationDt.isBefore(session.getNow().toLocalDate())) {
                    expired.add(stetext);
                } else {
                    final RawEtext theEtext = RawEtextLogic.query(cache, stetext.etextId);

                    if (theEtext != null) {
                        if ("Y".equals(theEtext.retention)) {
                            permanent.add(stetext);
                        } else if ("C".equals(theEtext.retention)) {
                            completed.add(stetext);
                        } else {
                            termOnly.add(stetext);
                        }
                    }
                }
            }

            final TermRec activeTerm = cache.getSystemData().getActiveTerm();
            if (completed.size() + termOnly.size() > 0) {

                htm.div("vgap").hr();
                if (activeTerm == null) {
                    htm.sH(3).add("Current Semester e-texts").eH(3);
                } else {
                    htm.sH(3).add("e-Texts for the ", activeTerm.term.longString, " Semester").eH(3);
                }

                htm.sDiv("indent11");

                if (!termOnly.isEmpty()) {
                    if (termOnly.size() == 1) {
                        htm.add(" This e-text provides ");
                    } else {
                        htm.add(" These e-texts provide ");
                    }
                    htm.addln("access for the current semester only:");
                    htm.div("vgap").hr();

                    startEtextTable(htm);
                    etextTable(cache, htm, termOnly, session, logic, false);
                    endEtextTable(htm);
                }

                if (!completed.isEmpty()) {
                    if (!termOnly.isEmpty()) {
                        htm.div("vgap");
                    }

                    if (completed.size() == 1) {
                        htm.add(" This e-text provides ");
                    } else {
                        htm.add(" These e-texts provide ");
                    }
                    htm.addln("access for the current semester.  Any courses completed this ",
                            "semester will be converted into permanent e-texts.");
                    htm.div("vgap").hr();

                    startEtextTable(htm);
                    etextTable(cache, htm, completed, session, logic, false);
                    endEtextTable(htm);
                }

                htm.eDiv(); // indent11
            }

            if (!permanent.isEmpty()) {

                htm.div("vgap").hr();

                htm.sH(3).add("Permanent Access e-Texts").eH(3);

                htm.sDiv("indent11");

                if (permanent.size() == 1) {
                    htm.add(" This e-text provides ");
                } else {
                    htm.add(" These e-texts provide ");
                }
                htm.addln("<strong>practice-only</strong> access for courses in which you are ",
                        "no longer enrolled.  Click on the course number to access the e-text in ",
                        "<strong>practice mode</strong>.");
                htm.div("vgap");

                startEtextTable(htm);
                etextTable(cache, htm, permanent, session, logic, true);
                endEtextTable(htm);

                htm.eDiv(); // indent11
            }

            if (expired.size() + refunded.size() > 0) {

                htm.div("vgap").hr();
                htm.sH(3).add("Expired and Returned e-Texts").eH(3);

                htm.sDiv("indent11");

                if (!refunded.isEmpty()) {
                    if (refunded.size() == 1) {
                        htm.addln(" This e-text has been returned for refund and is ",
                                "no longer available:");
                    } else {
                        htm.addln(" These e-texts have been returned for refund ",
                                "and are no longer available:");
                    }
                    htm.div("vgap");

                    startEtextTable(htm);
                    etextTable(cache, htm, refunded, session, logic, false);
                    endEtextTable(htm);
                }

                if (!expired.isEmpty()) {
                    if (!refunded.isEmpty()) {
                        htm.div("vgap");
                    }

                    if (expired.size() == 1) {
                        htm.addln(" This e-text has expired and is no longer available:");
                    } else {
                        htm.addln(" These e-texts have expired and are no longer available:");
                    }
                    htm.div("vgap");

                    startEtextTable(htm);
                    etextTable(cache, htm, expired, session, logic, false);
                    endEtextTable(htm);
                }

                htm.eDiv(); // indent11
            }

            htm.div("vgap");

            htm.sDiv("indent11");
            htm.addln(" To access your e-text materials, click on the course ",
                    "number under <strong>", activeTerm.term.longString, " Courses");
            htm.addln("</strong> on the left side of the page.");
            htm.eDiv();

            htm.div("vgap").hr();
        }

        htm.eDiv(); // indent11
    }

    /**
     * Presents a form to activate an e-text access code.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void showActivateLink(final HtmlBuilder htm) {

        htm.sDiv("indent11").hr();

        htm.addln(" <div class='left' style='padding:1em 2em 1em 2em;'>");
        htm.addln("  <img src='/images/PaceEtext.png' style='height:180px'>");
        htm.eDiv();

        htm.addln(" <div style='padding:1em 2em 1em 2em;'>");
        htm.addln("  If you have purchased an e-text access code through the <strong>");
        htm.addln("  <a target='_blank' href='",
                "https://www.bookstore.colostate.edu/shop/supplies/class-kits/csu-precalculus-tutorial'");
        htm.addln("  >Colorado State University Bookstore</a></strong>, ");
        htm.addln("  enter it here to activate:");
        htm.eDiv();

        htm.addln(" <div style='padding:1em 2em 1em 4em;'>");
        htm.addln("  <form method='POST' action='etext_key_entry.html'>");
        htm.addln("    &nbsp; &nbsp; <input type='text' id='key' name='key'>");
        htm.addln("    <input type='submit' value='Activate Access Code'>");
        htm.addln("  </form>");
        htm.eDiv();

        htm.addln(" <div class='blue' style='padding:1em 2em 1em 2em;'>");
        htm.addln("  CSU Bookstore staff will email your access code within 24 hours of");
        htm.addln("  receiving your order. Allow additional processing time when ordering");
        htm.addln("  access codes after 4 pm on Friday, and during the final two weeks of");
        htm.addln("  August and January. You will not be charged for shipping on the");
        htm.addln("  access code even though their website may display a shipping charge.");
        htm.eDiv();

        htm.div("clear");

        htm.eDiv().hr(); // indent11
    }

    /**
     * Starts a table of e-texts and prints the header row.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void startEtextTable(final HtmlBuilder htm) {

        htm.addln("<table width='100%' cellpadding='5'>");
        htm.addln(" <tr>");
        htm.addln("  <th class='medblue' style='text-align:left' width='20%'>Course(s)&nbsp;</th>");
        htm.addln("  <th class='lightblue' style='text-align:left' width='17%'>Date&nbsp;Activated&nbsp;</th>");
        htm.addln("  <th class='lightblue' style='text-align:left' width='17%'>Expiration&nbsp;Date&nbsp;</th>");
        htm.addln("  <th class='lightblue' style='text-align:left' width='46%'>Notes</th>");
        htm.addln(" </tr>");
    }

    /**
     * Ends a table of e-texts.
     *
     * @param htm the {@code HtmlBuilder} to which to append the HTML
     */
    private static void endEtextTable(final HtmlBuilder htm) {

        htm.addln("</table>");
    }

    /**
     * Prints a list of e-texts.
     *
     * @param cache     the data cache
     * @param htm       the {@code HtmlBuilder} to which to append the HTML
     * @param texts     the list of texts
     * @param session   the session
     * @param logic     the course site logic
     * @param showLinks true to show links to practice mode
     * @throws SQLException if there is an error accessing the database
     */
    private static void etextTable(final Cache cache, final HtmlBuilder htm, final Iterable<RawStetext> texts,
                                   final ImmutableSessionInfo session, final CourseSiteLogic logic,
                                   final boolean showLinks) throws SQLException {

        for (final RawStetext text : texts) {
            final List<RawEtextCourse> etcourses = RawEtextCourseLogic.queryByEtext(cache, text.etextId);
            if (etcourses.isEmpty()) {
                continue;
            }

            final HtmlBuilder builder = new HtmlBuilder(100);

            // FIXME: need a title for an e-text to use in this display
            if (etcourses.size() > 1) {
                builder.add("Precalculus Program");
            } else {
                for (final RawEtextCourse etcours : etcourses) {
                    final String crsLabel = RawCourseLogic.getCourseLabel(cache, etcours.course);
                    if (crsLabel == null) {
                        builder.add(etcours.course);
                    } else {
                        builder.add(crsLabel);
                    }
                }
            }

            htm.addln(" <tr>");
            htm.addln("  <td class='medgreen' valign='top'>");

            // See if any of the courses are in-progress
            CourseInfo hitCourse = null;
            CourseInfo inProgCourse = null;

            final CourseSiteLogicCourse courses = logic.course;

            if (!courses.lockedOut) {

                for (final RawEtextCourse etcourse : etcourses) {

                    for (final CourseInfo course : courses.inProgressCourses) {
                        if (course.course.equals(etcourse.course)) {
                            hitCourse = course;
                            inProgCourse = course;
                            break;
                        }
                    }
                    if (hitCourse == null) {
                        for (final CourseInfo course : courses.inProgressIncCourses) {
                            if (course.course.equals(etcourse.course)) {
                                hitCourse = course;
                                inProgCourse = course;
                                break;
                            }
                        }
                    }

                    if (hitCourse == null) {
                        for (final CourseInfo course : courses.completedCourses) {
                            if (course.course.equals(etcourse.course)) {
                                hitCourse = course;
                                break;
                            }
                        }
                    }

                    if (hitCourse == null) {
                        for (final CourseInfo course : courses.completedIncCourses) {
                            if (course.course.equals(etcourse.course)) {
                                hitCourse = course;
                                break;
                            }
                        }
                    }

                    if (hitCourse == null) {
                        for (final CourseInfo course : courses.pastDeadlineCourses) {
                            if (course.course.equals(etcourse.course)) {
                                hitCourse = course;
                                break;
                            }
                        }
                    }

                    if (hitCourse == null) {
                        for (final CourseInfo course : courses.pastDeadlineIncCourses) {
                            if (course.course.equals(etcourse.course)) {
                                hitCourse = course;
                                break;
                            }
                        }
                    }
                }
            }

            if (!showLinks || etcourses.size() != 1) {
                // Multi-course texts cannot give practice mode access
                htm.addln(builder.toString());
            } else if (hitCourse != null) {
                if (inProgCourse == null) {
                    htm.addln(builder.toString());
                } else {
                    // Single-course e-text for a course that's currently open
                    htm.addln("<a href='course.html?course=", etcourses.getFirst().course, "&mode=",
                            courses.lockedOut ? "locked" : "course", "'>", builder.toString(), "</a> (In progress)");
                }
            } else {
                htm.addln("<a href='course.html?course=", etcourses.getFirst().course, "&mode=",
                        courses.lockedOut ? "locked" : "practice", "'>", builder.toString(), "</a>");
            }

            htm.addln("  </td>");

            htm.addln("  <td class='lightgreen' valign='top'>", getCurPurchaseDateString(text), "</td>");

            htm.addln("  <td class='lightgreen' valign='top'>", getCurExpirationDateString(text), "</td>");

            htm.add("  <td class='lightgreen' valign='top'>");

            if (getCurRefundDateString(text) != null) {
                htm.add("Returned for refund on ", getCurRefundDateString(text));

                if (getCurRefundReason(text) != null) {
                    htm.add(CoreConstants.SPC, getCurRefundReason(text));
                }
            } else if (isCurExpired(text, session.getNow())) {
                htm.add("<span class='red'>e-text has expired.</span>");
            } else if (isCurPastRefundDeadline(text, session.getNow())) {
                htm.add("The deadline for a refund for withdrawal was ", getCurRefundDeadlineDateString(text));
            } else {
                final RawEtext etext = RawEtextLogic.query(cache, text.etextId);

                if (etext.purchaseUrl == null) {
                    htm.add("Refunds only through the CSU Bookstore");
                } else {
                    // TODO: probably should add a "refund URL" to the etext table
                    htm.add("Refund for withdrawal available until ", getCurRefundDeadlineDateString(text), ". ",
                            "<a href='https://www.kendallhunt.com/csuRefund/default.aspx", "?student_id=",
                            session.getEffectiveUserId(), "&course_id=", text.etextId, "&session_id=",
                            session.loginSessionId, "'>Return e-text for a refund.</a>");
                }
            }

            htm.addln("</td>");
            htm.addln(" </tr>");
        }
    }

    /**
     * Gets the string representation of the deadline date for requesting a refund.
     *
     * @param stetext the student e-text record
     * @return the refund deadline date string
     */
    private static String getCurRefundDeadlineDateString(final RawStetext stetext) {

        final String result;

        if (stetext == null) {
            result = null;
        } else {
            final LocalDate dt = stetext.refundDeadlineDt;

            if (dt == null) {
                result = "None";
            } else {
                result = FMT.format(dt);
            }
        }

        return result;
    }

    /**
     * Gets the string representation of the purchase date.
     *
     * @param stetext the student e-text record
     * @return the purchase date string
     */
    private static String getCurPurchaseDateString(final RawStetext stetext) {

        final String result;

        if (stetext == null) {
            result = null;
        } else {
            final LocalDate dt = stetext.activeDt;

            if (dt == null) {
                result = null;
            } else {
                result = FMT.format(dt);
            }
        }

        return result;
    }

    /**
     * Gets the string representation of the expiration date of the text purchase.
     *
     * @param stetext the student e-text record
     * @return the expiration date string ({@code null} if no expiration)
     */
    private static String getCurExpirationDateString(final RawStetext stetext) {

        final String result;

        if (stetext == null) {
            result = null;
        } else {
            final LocalDate dt = stetext.expirationDt;

            if (dt == null) {
                result = "None";
            } else {
                result = FMT.format(dt);
            }
        }

        return result;
    }

    /**
     * Gets the string representation of the refund date.
     *
     * @param stetext the student e-text record
     * @return the refund date string
     */
    private static String getCurRefundDateString(final RawStetext stetext) {

        final String result;

        if (stetext == null) {
            result = null;
        } else {
            final LocalDate dt = stetext.refundDt;

            if (dt == null) {
                result = null;
            } else {
                result = FMT.format(dt);
            }
        }

        return result;
    }

    /**
     * Gets the reason given for a refund.
     *
     * @param stetext the student e-text record
     * @return the reason given for the refund
     */
    private static String getCurRefundReason(final RawStetext stetext) {

        return stetext == null ? null : stetext.refundReason;
    }

    /**
     * Checks whether the current text purchase has expired.
     *
     * @param stetext the student e-text record
     * @param now     the date/time to consider as "now"
     * @return {@code true} if the purchase has expired; {@code false} if not
     */
    private static boolean isCurExpired(final RawStetext stetext, final ChronoZonedDateTime<LocalDate> now) {

        final boolean result;

        if (stetext == null) {
            result = false;
        } else {
            final LocalDate date = stetext.expirationDt;

            result = date != null && date.isBefore(now.toLocalDate());
        }

        return result;
    }

    /**
     * Checks whether the deadline date for requesting a refund has elapsed.
     *
     * @param stetext the student e-text record
     * @param now     the date/time to consider as "now"
     * @return {@code true} if the current date/time is past the deadline to request a refund
     */
    private static boolean isCurPastRefundDeadline(final RawStetext stetext,
                                                   final ChronoZonedDateTime<LocalDate> now) {

        final boolean result;

        if (stetext == null) {
            result = false;
        } else {
            final LocalDate date = stetext.refundDeadlineDt;

            result = date != null && date.isBefore(now.toLocalDate());
        }

        return result;
    }
}
