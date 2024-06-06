package dev.mathops.web.site.admin.testing;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.StudentData;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.admin.AdminPage;
import dev.mathops.web.site.admin.AdminSite;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * A base class for pages in the testing center management site.
 */
enum TestingPage {
    ;

    /**
     * Creates an {@code HtmlBuilder} and starts a testing management page, emitting the page start and the top level
     * header.
     *
     * @param studentData the student data object
     * @param site        the owning site
     * @param session     the login session
     * @return the created {@code HtmlBuilder}
     * @throws SQLException if there is an error accessing the database
     */
    static HtmlBuilder startTestingPage(final StudentData studentData, final AdminSite site,
                                        final ImmutableSessionInfo session)
            throws SQLException {

        final RawWhichDb whichDb = studentData.getWhichDb();

        final HtmlBuilder htm = new HtmlBuilder(2000);
        final String siteTitle = site.getTitle();
        Page.startOrdinaryPage(htm, siteTitle, null, false, null, "home.html", Page.NO_BARS, null, false, true);
        AdminPage.emitPageHeader(htm, session, whichDb, false);

        return htm;
    }

    /**
     * Ends a testing management page.
     *
     * @param studentData the student data object
     * @param htm         the {@code HtmlBuilder} to which to write
     * @param site        the owning site
     * @param req         the request
     * @param resp        the response
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void endTestingPage(final StudentData studentData, final HtmlBuilder htm, final AdminSite site,
                               final ServletRequest req,
                               final HttpServletResponse resp) throws IOException, SQLException {

        Page.endOrdinaryPage(studentData, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page that prompts the user to log in.
     *
     * @param selected the currently selected topic; {@code null} if none
     * @param htm      the {@code HtmlBuilder} to which to append
     */
    static void emitNavBlock(final ETestingTopic selected, final HtmlBuilder htm) {

        htm.addln("<nav>");
        htm.addln("<script>");
        htm.addln(" function pick(target) {");
        htm.addln("  window.location.assign(target);");
        htm.addln(" }");
        htm.addln("</script>");

        navButton(htm, selected, "first", ETestingTopic.POWER_ON_OFF);
        navButton(htm, selected, null, ETestingTopic.ENABLE_DISABLE);
        navButton(htm, selected, null, ETestingTopic.ISSUE);
        navButton(htm, selected, "last", ETestingTopic.CANCEL);

        htm.addln("</nav>");
        htm.hr("orange").div("vgap");

    }

    /**
     * Starts a navigation button.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param selected the selected topic
     * @param id       the button ID ("first" or "last" to adjust margins)
     * @param topic    the topic
     */
    private static void navButton(final HtmlBuilder htm, final ETestingTopic selected, final String id,
                                  final ETestingTopic topic) {

        htm.add("<button");
        if (selected == topic) {
            htm.add(" class='nav4 selected'");
        } else {
            htm.add(" class='nav4'");
        }
        if (id != null) {
            htm.add(" id='", id, "'");
        }
        htm.add(" onclick='pick(\"", topic.getUrl(), "\");'>", topic.getLabel(), "</button>");
    }
}
