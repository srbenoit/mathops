package dev.mathops.web.site.admin.office;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminPage;
import dev.mathops.web.site.admin.AdminSite;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.chrono.ChronoLocalDateTime;

/**
 * A base class for pages in the Precalculus Center Office site.
 */
enum OfficePage {
    ;

    /**
     * Gets the site title.
     *
     * @return the title
     */
    static String getSiteTitle() {

        return Res.get(Res.SITE_TITLE);
    }

    /**
     * Creates an {@code HtmlBuilder} and starts a system administration page, emitting the page start and the top level
     * header.
     *
     * @param site     the owning site
     * @param session  the login session
     * @param showHome true to show a "Home" link
     * @return the created {@code HtmlBuilder}
     */
    public static HtmlBuilder startOfficePage(final AdminSite site, final ImmutableSessionInfo session,
                                              final boolean showHome) {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, false, null, "home.html", Page.NO_BARS, null, false, true);

        AdminPage.emitPageHeader(htm, session, showHome);

        return htm;
    }

    /**
     * Generates a string representation of a duration.
     *
     * @param start  the start time
     * @param finish the finish time
     * @return the duration string
     */
    static String durationString(final ChronoLocalDateTime<LocalDate> start,
                                 final ChronoLocalDateTime<LocalDate> finish) {

        final String result;

        if (start == null) {
            result = "(no start time)";
        } else if (finish == null) {
            result = "(no finish time)";
        } else if (start.isAfter(finish)) {
            result = "(neg)";
        } else {
            final StringBuilder dur = new StringBuilder(12);

            final int seconds = (int) (finish.toEpochSecond(ZoneOffset.UTC) - start.toEpochSecond(ZoneOffset.UTC));

            final int min = seconds / 60;

            if (min == 0) {
                dur.append(seconds).append(" sec");
            } else {
                dur.append(min);
            }

            result = dur.toString();
        }

        return result;
    }

    /**
     * Generates a string representation of a duration.
     *
     * @param start  the start time
     * @param finish the finish time
     * @return the duration string
     */
    static String durationString(final Integer start, final Integer finish) {

        final String result;

        if (start == null) {
            result = "(no start time)";
        } else if (finish == null) {
            result = "(no finish time)";
        } else if (start.intValue() > finish.intValue()) {
            result = "(neg)";
        } else {
            result = Integer.toString(finish.intValue() - start.intValue());
        }

        return result;
    }
}
