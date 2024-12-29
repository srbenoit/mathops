package dev.mathops.web.site;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.Contexts;
import dev.mathops.db.old.cfg.WebSiteProfile;
import dev.mathops.db.old.rawrecord.RawCampusCalendar;
import dev.mathops.db.old.svc.term.TermRec;
import dev.mathops.session.ISessionManager;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.session.SessionManager;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * A base class for sites that serve visible pages (as opposed to sites that simply process transactions).
 */
public abstract class AbstractPageSite extends AbstractSite {

    /**
     * Constructs a new {@code AbstractCourseSite}.
     *
     * @param theSiteProfile the website profile
     * @param theSessions    the singleton user session repository
     */
    protected AbstractPageSite(final WebSiteProfile theSiteProfile, final ISessionManager theSessions) {

        super(theSiteProfile, theSessions);
    }

    /**
     * Processes a logout, then redirects the user to the site's /login.html page.
     *
     * @param req     the request
     * @param resp    the response
     * @param session the logged-in session
     * @throws IOException if there is an error writing the response
     */
    protected final void doLogout(final HttpServletRequest req, final HttpServletResponse resp,
                                  final ImmutableSessionInfo session) throws IOException {

        final SessionManager sessMgr = SessionManager.getInstance();

        // See if there is a Shibboleth session
        for (final Cookie cookie : req.getCookies()) {
            if (cookie.getName().startsWith("_shibsession_")) {
                sessMgr.storeLoggedOutSession(cookie.getValue());
            }
        }

        sessMgr.logout(session.loginSessionId);

        final String effId = session.getEffectiveUserId();
        if (effId != null && effId.startsWith("99")) {
            final String path = this.siteProfile.path;
            resp.sendRedirect(path + (path.endsWith(Contexts.ROOT_PATH)
                    ? "login_test_user_99.html"
                    : "/login_test_user_99.html"));
        } else {
            resp.sendRedirect("/Shibboleth.sso/Logout");
        }
    }

//    /**
//     * Gets the login type based on context.
//     *
//     * @return the login type
//     */
//    public final String getLoginType() {
//
//        String loginType = "Shib";
//
//        if (Contexts.CALC_REVIEW_PATH.equals(this.siteProfile.path)
//                || Contexts.CALC2_REVIEW_PATH.equals(this.siteProfile.path)
//                || Contexts.ADMINSYS_PATH.equals(this.siteProfile.path)) {
//            loginType = "Local";
//        }
//
//        return loginType;
//    }

    /**
     * Scans the request for Shibboleth attributes and uses them (if found) to establish a session, and then redirects
     * to either the secure page (if valid) or the login page (if not valid).
     *
     * @param cache       the data cache
     * @param req         the request
     * @param resp        the response
     * @param session     the user's login session information
     * @param successPath the path to which to redirect on success (should not begin with slash)
     * @throws SQLException if there was an error accessing the database
     */
    protected final void doShibbolethLogin(final Cache cache, final HttpServletRequest req,
                                           final HttpServletResponse resp, final ImmutableSessionInfo session,
                                           final String successPath) throws SQLException {

        Log.info("Shibboleth login attempt");

        ImmutableSessionInfo sess = session;

        if (sess == null) {
            sess = processShibbolethLogin(cache, req);
        }

        final String path = this.siteProfile.path;
        final String redirect;
        if (sess == null) {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? "login.html" : "/login.html");
        } else {
            redirect = path + (path.endsWith(CoreConstants.SLASH) ? successPath : CoreConstants.SLASH + successPath);

            // Install the session ID cookie in the response
            Log.info("Adding session ID cookie ", req.getServerName());
            final Cookie cook = new Cookie(SessionManager.SESSION_ID_COOKIE, sess.loginSessionId);
            cook.setDomain(req.getServerName());
            cook.setPath(CoreConstants.SLASH);
            cook.setMaxAge(-1);
            cook.setSecure(true);
            resp.addCookie(cook);
        }
        resp.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
        resp.setHeader("Location", redirect);

        Log.info("Redirecting to ", resp.getHeader("Location"));
    }

    /**
     * Processes any submissions by the role controls (call on POST).
     *
     * @param cache   the data cache
     * @param req     the HTTP request
     * @param resp    the HTTP response
     * @param session the session information
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    protected final void processRoleControls(final Cache cache, final ServletRequest req,
                                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        UserInfoBar.processRoleControls(cache, req, session);

        final String target = req.getParameter("target");

        if (isParamInvalid(target)) {
            Log.warning("Invalid request parameters - possible attack:");
            Log.warning("  target='", target, "'");
            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else if (target == null) {
            PageError.doGet(cache, this, req, resp, session, "No target provided with role control");
        } else {
            resp.sendRedirect(target);
        }
    }

    /**
     * Scans the list of calendar dates and builds the list of holidays to show the user. Strings of consecutive dates
     * are combined into a display of the form "May 19 through May 23".
     *
     * @param calendarDays the set of calendar days
     * @return the list of dates or date ranges to display
     */
    public static List<String> makeHolidayList(final Collection<RawCampusCalendar> calendarDays) {

        // Extract and sort the list of LocalDates marked HOLIDAY
        final List<LocalDate> holidays = new ArrayList<>(calendarDays.size());
        for (final RawCampusCalendar test : calendarDays) {
            if (RawCampusCalendar.DT_DESC_HOLIDAY.equals(test.dtDesc)) {
                holidays.add(test.campusDt);
            }
        }
        Collections.sort(holidays);

        final LocalDate today = LocalDate.now();

        // Build a list of holidays as a list of (1) individual days or (2) ranges of days
        final List<String> days = new ArrayList<>(5);

        LocalDate start = null;
        LocalDate prior = null;
        boolean inRange = false;

        for (final LocalDate date : holidays) {

            if (prior == null) {
                // First date - starts new range or single date
                start = date;
            } else if (prior.isEqual(date.minusDays(1L))) {
                // Continuing a range, start keeps beginning of range
                inRange = true;
            } else {
                if (inRange) {
                    // Range has ended (start has beginning of range, prior has end)
                    if (!prior.isBefore(today)) {
                        days.add(formatDate(start) + " through " + formatDate(prior));
                    }

                    inRange = false;
                } else // Prior holds single date that is not part of a range
                    if (!prior.isBefore(today)) {
                        days.add(formatDate(prior));
                    }

                // 'date' may start a new range
                start = date;
            }

            prior = date;
        }

        if (inRange) {
            // Ended with a range in progress
            if (!prior.isBefore(today)) {
                days.add(formatDate(start) + " through " + formatDate(prior));
            }
        } else // Ended with a single date left in 'start'
            if ((start != null) && !start.isBefore(today)) {
                days.add(formatDate(start));
            }

        return days;
    }

    /**
     * Generates the HTML representation of a date, in a form similar to the following.
     *
     * <pre>
     * Mar. 23
     * </pre>
     *
     * @param date the date to format
     * @return the formatted string
     */
    private static String formatDate(final LocalDate date) {

        final HtmlBuilder xml = new HtmlBuilder(30);

        if (date.getMonth() == Month.MAY) {
            xml.add(TemporalUtils.FMT_MD.format(date));
        } else {
            xml.add(TemporalUtils.FMT_MPD.format(date));
        }

        return xml.toString();
    }

    /**
     * Appends the HTML display of the operating hours of the Precalculus Center to an {@code HtmlBuilder}.
     *
     * @param cache          the data cache
     * @param htm            the {@code HtmlBuilder} to which to append
     * @param showWalkin     {@code true} to include the walk-in placement day
     * @param showRoomNumber {@code true} to include the Precalculus Center room number
     * @throws SQLException if there is an error accessing the database
     */
    public static void hours(final Cache cache, final HtmlBuilder htm, final boolean showWalkin,
                             final boolean showRoomNumber) throws SQLException {

        final TermRec term = cache.getSystemData().getActiveTerm();
        final List<RawCampusCalendar> calendarDays = cache.getSystemData().getCampusCalendars();

        if (term != null) {
            htm.sDiv("center");
            htm.sDiv("hours");

            htm.sH(3, "center").add(showRoomNumber ? "Precalculus Center Hours (Weber 137)"
                    : "Precalculus Center Hours").eH(3);
            htm.div("vgap");
            htm.sH(4).add(term.term.longString).eH(4);

            // Show date ranges and times
            htm.addln("<ul class='hours'>");

            RawCampusCalendar start1 = null;
            RawCampusCalendar end1 = null;
            RawCampusCalendar start2 = null;
            RawCampusCalendar end2 = null;
            RawCampusCalendar walkin = null;
            RawCampusCalendar start1x = null;
            RawCampusCalendar end1x = null;

            for (final RawCampusCalendar test : calendarDays) {
                if (RawCampusCalendar.DT_DESC_START_DATE_1.equals(test.dtDesc)) {
                    start1 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_1.equals(test.dtDesc)) {
                    end1 = test;
                } else if (RawCampusCalendar.DT_DESC_START_DATE_2.equals(test.dtDesc)) {
                    start2 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_2.equals(test.dtDesc)) {
                    end2 = test;
                } else if (RawCampusCalendar.DT_DESC_WALKIN_PLACEMENT.equals(test.dtDesc)) {
                    walkin = test;
                } else if (RawCampusCalendar.DT_DESC_START_DATE_1_NEXT.equals(test.dtDesc)) {
                    start1x = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_1_NEXT.equals(test.dtDesc)) {
                    end1x = test;
                }
            }

            if (start1 != null && end1 != null) {
                final LocalDate start1Date = start1.campusDt;
                final LocalDate end1Date = end1.campusDt;

                htm.add("<li>");

                if (start1Date.getYear() == end1Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start1Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start1Date));
                }
                htm.add(" - ", TemporalUtils.FMT_MDY.format(end1Date));

                final int count = start1.numTimes();

                if (count > 0) {
                    htm.br().add(start1.openTime1, " - ", start1.closeTime1, ", ")
                            .sSpan("nowrap").add(start1.weekdays1).eSpan();
                }
                if (count > 1) {
                    htm.br().add(start1.openTime2, " - ", start1.closeTime2, ", ")
                            .sSpan("nowrap").add(start1.weekdays2).eSpan();
                }

                htm.addln("</li>");
            }

            if (start2 != null && end2 != null) {
                final LocalDate start2Date = start2.campusDt;
                final LocalDate end2Date = end2.campusDt;

                htm.add("<li>");
                if (start2Date.getYear() == end2Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start2Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start2Date));
                }
                htm.addln(" - ", TemporalUtils.FMT_MDY.format(end2Date));

                final int count = start2.numTimes();

                if (count > 0) {
                    htm.br().add(start2.openTime1, " - ", start2.closeTime1, ", ")
                            .sSpan("nowrap").add(start2.weekdays1).eSpan();
                }
                if (count > 1) {
                    htm.br().add(start2.openTime2, " - ", start2.closeTime2, ", ")
                            .sSpan("nowrap").add(start2.weekdays2).eSpan();
                }

                htm.addln("</li>");
            }

            htm.addln("</ul>");

            if (showWalkin && walkin != null) {

                final LocalDate walkinDate = walkin.campusDt;
                if (!walkinDate.isBefore(LocalDate.now())) {
                    htm.sH(4).add("Walk-in Math Placement Day").eH(4);

                    // Show date ranges and times
                    htm.addln("<ul class='hours'>");
                    htm.add("<li>");

                    htm.add(TemporalUtils.FMT_MDY.format(walkinDate));

                    final int count = walkin.numTimes();

                    if (count > 0) {
                        htm.br().add(walkin.openTime1, " - ", walkin.closeTime1, ", ")
                                .sSpan("nowrap").add(walkin.weekdays1).eSpan();
                    }
                    if (count > 1) {
                        htm.br().add(walkin.openTime2, " - ", walkin.closeTime2, ", ")
                                .sSpan("nowrap").add(walkin.weekdays2).eSpan();
                    }

                    htm.addln("</li>");
                    htm.addln("</ul>");
                }
            }

            if (start1x != null && end1x != null) {
                final TermRec nextTerm = cache.getSystemData().getNextTerm();

                if (nextTerm != null) {
                    htm.sH(4).add(nextTerm.term.longString).eH(4);

                    final LocalDate start2Date = start1x.campusDt;
                    final LocalDate end2Date = end1x.campusDt;

                    htm.addln("<ul class='hours'>");
                    htm.add("<li>");
                    if (start2Date.getYear() == end2Date.getYear()) {
                        htm.add(TemporalUtils.FMT_MD.format(start2Date));
                    } else {
                        htm.add(TemporalUtils.FMT_MDY.format(start2Date));
                    }
                    htm.addln(" - ", TemporalUtils.FMT_MDY.format(end2Date));

                    final int count = start1x.numTimes();

                    if (count > 0) {
                        htm.br().add(start1x.openTime1, " - ", start1x.closeTime1, ", ")
                                .sSpan("nowrap").add(start1x.weekdays1).eSpan();
                    }
                    if (count > 1) {
                        htm.br().add(start1x.openTime2, " - ", start1x.closeTime2, ", ")
                                .sSpan("nowrap").add(start1x.weekdays2).eSpan();
                    }

                    htm.addln("</li>");
                    htm.addln("</ul>");
                }
            }

            // Show holidays
            final List<String> days = makeHolidayList(calendarDays);

            if (!days.isEmpty()) {
                htm.hr();

                htm.sP("smaller").add("The Precalculus Center will be closed ");
                // Build a list of holidays as a list of (1) individual days or (2) ranges of days
                boolean comma = false;
                final int numDays = days.size();
                for (int i = 0; i < numDays; ++i) {
                    if (comma) {
                        if (i == numDays - 1) {
                            htm.add(" and ");
                        } else {
                            htm.add(", ");
                        }
                    }
                    htm.addln(days.get(i));
                    comma = true;
                }

                htm.addln(numDays == 1 ? " (University holiday)" : " (University holidays)");
                htm.eP();
            }

            htm.hr();

            htm.addln("<ul class='hours'>");
            htm.addln("<li>  <small>Testing Center doors close 15 minutes before closing time, no ",
                    "new exams may be started after doors close.  All exams must be submitted ",
                    "by closing time.</small></li>");

            htm.addln("<li> <small>You must bring your CSU RamCard to be issued an exam in the ",
                    "Precalculus Center testing area.</small></li>");
            htm.addln("</ul>");

            htm.eDiv(); // box
            htm.eDiv(); // center
        }
    }

    /**
     * Appends the HTML display of the hours for in-person help in the Precalculus Center to an {@code HtmlBuilder}.
     *
     * @param cache the data cache
     * @param htm   the {@code HtmlBuilder} to which to append
     * @throws SQLException if there is an error accessing the database
     */
    public static void helpHours(final Cache cache, final HtmlBuilder htm) throws SQLException {

        final TermRec term = cache.getSystemData().getActiveTerm();
        final List<RawCampusCalendar> calendarDays = cache.getSystemData().getCampusCalendars();

        if (term != null) {
            htm.sDiv("indent22");
            htm.sDiv("hours");

            htm.sP().add("In-Person Help Hours in the Precalculus Center").eP();

            // Show date ranges and times
            htm.addln("<ul class='hours'>");

            RawCampusCalendar start1 = null;
            RawCampusCalendar end1 = null;
            RawCampusCalendar start2 = null;
            RawCampusCalendar end2 = null;

            for (final RawCampusCalendar test : calendarDays) {
                if (RawCampusCalendar.DT_DESC_START_DATE_1.equals(test.dtDesc)) {
                    start1 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_1.equals(test.dtDesc)) {
                    end1 = test;
                } else if (RawCampusCalendar.DT_DESC_START_DATE_2.equals(test.dtDesc)) {
                    start2 = test;
                } else if (RawCampusCalendar.DT_DESC_END_DATE_2.equals(test.dtDesc)) {
                    end2 = test;
                }
            }

            if (start1 != null && end1 != null) {
                final LocalDate start1Date = start1.campusDt;
                final LocalDate end1Date = end1.campusDt;

                htm.add("<li>");

                if (start1Date.getYear() == end1Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start1Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start1Date));
                }
                htm.add(" - ", TemporalUtils.FMT_MDY.format(end1Date));

                final int count = start1.numTimes();

                if (count > 0) {
                    htm.br().add(start1.openTime1, " - ", start1.closeTime1, ", ")
                            .sSpan("nowrap").add(start1.weekdays1).eSpan();
                }
                if (count > 1) {
                    htm.br().add(start1.openTime2, " - ", start1.closeTime2, ", ")
                            .sSpan("nowrap").add(start1.weekdays2).eSpan();
                }

                htm.addln("</li>");
            }

            if (start2 != null && end2 != null) {
                final LocalDate start2Date = start2.campusDt;
                final LocalDate end2Date = end2.campusDt;

                htm.add("<li>");
                if (start2Date.getYear() == end2Date.getYear()) {
                    htm.add(TemporalUtils.FMT_MD.format(start2Date));
                } else {
                    htm.add(TemporalUtils.FMT_MDY.format(start2Date));
                }
                htm.addln(" - ", TemporalUtils.FMT_MDY.format(end2Date));

                final int count = start2.numTimes();

                if (count > 0) {
                    htm.br().add(start2.openTime1, " - ", start2.closeTime1, ", ")
                            .sSpan("nowrap").add(start2.weekdays1).eSpan();
                }
                if (count > 1) {
                    htm.br().add(start2.openTime2, " - ", start2.closeTime2, ", ")
                            .sSpan("nowrap").add(start2.weekdays2).eSpan();
                }

                htm.addln("</li>");
            }

            htm.addln("</ul>");

            // Show holidays
            final List<String> days = makeHolidayList(calendarDays);

            if (!days.isEmpty()) {
                htm.hr();

                htm.sP("smaller")
                        .add("The Precalculus Center will be closed ");
                // Build a list of holidays as a list of (1) individual days or (2) ranges of days
                boolean comma = false;
                final int numDays = days.size();
                for (int i = 0; i < numDays; ++i) {
                    if (comma) {
                        if (i == numDays - 1) {
                            htm.add(" and ");
                        } else {
                            htm.add(", ");
                        }
                    }
                    htm.addln(days.get(i));
                    comma = true;
                }

                htm.addln(numDays == 1 ? " (University holiday)" : " (University holidays)");
                htm.eP();
            }

            htm.eDiv(); // box
            htm.eDiv(); // center
        }
    }
}
