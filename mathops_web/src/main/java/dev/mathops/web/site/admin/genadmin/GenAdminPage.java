package dev.mathops.web.site.admin.genadmin;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminPage;
import dev.mathops.web.site.admin.AdminSite;

import java.sql.SQLException;

/**
 * A base class for pages in the administrative system site.
 */
public enum GenAdminPage {
    ;

    /**
     * Creates an {@code HtmlBuilder} and starts a system administration page, emitting the page start and the top level
     * header.
     *
     * @param cache    the data cache
     * @param site     the owning site
     * @param session  the login session
     * @param showHome true to show a "Home" link
     * @return the created {@code HtmlBuilder}
     * @throws SQLException if there is an error accessing the database
     */
    public static HtmlBuilder startGenAdminPage(final Cache cache, final AdminSite site,
                                                final ImmutableSessionInfo session, final boolean showHome)
            throws SQLException {

        final RawWhichDb whichDb = cache.getSystemData().getWhichDb();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, false, null, "home.html", Page.NO_BARS, null, false, true);

        AdminPage.emitPageHeader(htm, session, whichDb, showHome);

        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        return htm;
    }

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param selected the currently selected topic; {@code null} if none
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    public static void emitNavBlock(final EAdminTopic selected, final HtmlBuilder htm) {

        htm.addln("<nav>");

        navButton(htm, selected, "first", EAdminTopic.STUDENT_STATUS);
        navButton(htm, selected, null, EAdminTopic.MONITOR_SYSTEM);
        navButton(htm, selected, null, EAdminTopic.TEST_STUDENTS);
        navButton(htm, selected, "last", EAdminTopic.UTILITIES);

        navButton(htm, selected, "first", EAdminTopic.SERVER_ADMIN);
        navButton(htm, selected, null, EAdminTopic.DB_ADMIN);
        navButton(htm, selected, null, EAdminTopic.SITE_ADMIN);
        navButton(htm, selected, "last", EAdminTopic.PROCTORING);

        htm.addln("</nav>");
        htm.addln("<hr class='orange'/>");
    }

    /**
     * Starts a navigation button.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the selected topic
     * @param id       the button ID ("first" or "last" to adjust margins)
     * @param topic    the topic
     */
    private static void navButton(final HtmlBuilder htm, final EAdminTopic selected, final String id,
                                  final EAdminTopic topic) {

        htm.add("<button");
        if (selected == topic) {
            htm.add(" class='nav4 selected'");
        } else {
            htm.add(" class='nav4'");
        }
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", topic.url, "\");'>", topic.label, "</button>");
    }

    /**
     * Starts a small navigation button.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the selected subtopic
     * @param subtopic the subtopic
     * @param query    optional query string (not including leading '?')
     */
    public static void navButtonSmall(final HtmlBuilder htm, final EAdmSubtopic selected, final EAdmSubtopic subtopic,
                                      final String query) {

        htm.add("<button");
        if (selected == subtopic) {
            htm.add(" class='nav8 selected'");
        } else {
            htm.add(" class='nav8'");
        }
        htm.add(" onclick='pick(\"", subtopic.url);
        if (query != null) {
            htm.add('?').add(query);
        }
        htm.add("\");'>", subtopic.label, "</button>");
    }

    /**
     * Formats a duration in milliseconds as a string of the form "#:##:##".
     *
     * @param duration the duration
     * @return the formatted string
     */
    public static String formatMsDuration(final long duration) {

        final String result;

        if (duration < 0L) {
            result = "negative";
        } else {
            final long sec = (duration + 500L) / 1000L;
            final long ss = sec % 60L;
            final long mm = sec / 60L % 60L;
            final long hr = sec / 3600L;

            final StringBuilder sb = new StringBuilder(20);
            sb.append(hr).append(':');
            if (mm < 10L) {
                sb.append('0');
            }
            sb.append(mm).append(':');
            if (ss < 10L) {
                sb.append('0');
            }
            sb.append(ss);

            result = sb.toString();
        }

        return result;
    }
}
