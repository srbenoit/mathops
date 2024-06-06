package dev.mathops.web.site.help.student;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.logic.Cache;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;
import dev.mathops.web.site.help.HelpSite;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;

/**
 * Generates the live help session page.
 */
public enum PageStudentHelp {
    ;

    /**
     * Generates the page.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    public static void doGet(final Cache cache, final HelpSite site, final HttpServletRequest req,
                             final HttpServletResponse resp, final ImmutableSessionInfo session)
            throws IOException, SQLException {

        final HelpContext context = new HelpContext(req);
        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS,
                "helpmax.html" + context.makeQueryString(), false, true);

        htm.add("<div style='width:100%; height:calc(100vh - 197px); display:grid; ",
                "grid-template-columns:30% 35% 35%; grid-template-rows:calc(100% - 186px) 184px; ",
                "grid-gap: 2px 2px;'>");

        emitPageContent(htm, context);

        htm.add("</div>");

        Page.endOrdinaryPage(cache, site, htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Generates the page in maximized form.
     *
     * @param req     the request
     * @param resp    the response
     * @param session the session
     * @throws IOException if there is an error writing the response
     */
    public static void doGetMax(final HttpServletRequest req, final HttpServletResponse resp,
                                final ImmutableSessionInfo session) throws IOException {

        final HelpContext context = new HelpContext(req);
        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryMaxPage(htm, Res.get(Res.SITE_TITLE), session, false, Page.NO_BARS,
                "help.html" + context.makeQueryString(), false, true);

        htm.add("<div style='width:100%; height:calc(100vh - 60px); display:grid; ",
                "grid-template-columns:30% 35% 35%; grid-template-rows:calc(100% - 186px) 184px; ",
                "grid-gap: 2px 2px;'>");

        emitPageContent(htm, context);

        htm.add("</div>");

        Page.endOrdinaryMaxPage(htm, true);
        AbstractSite.sendReply(req, resp, Page.MIME_TEXT_HTML, htm.toString().getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Emits page content.
     *
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param context the help context
     */
    private static void emitPageContent(final HtmlBuilder htm, final HelpContext context) {

        final HtmlBuilder title = new HtmlBuilder(40);

        if (context.course == null) {
            title.add("General Questions");
        } else if (!context.course.isEmpty() && context.course.charAt(0) == '1') {
            title.add("MATH ", context.course);
            if (context.unit == 0) {
                title.add(", Skills Review Materials");
            } else if (context.unit != -1) {
                title.add(", Unit ", Integer.toString(context.unit));
                if (context.obj != -1) {
                    title.add(", Objective ", Integer.toString(context.obj));
                }
            }
        } else if ("ELM".equals(context.course)) {
            title.add("ELM Tutorial");
            if (context.unit != -1) {
                title.add(", Unit ", Integer.toString(context.unit));
                if (context.obj != -1) {
                    title.add(", Objective ", Integer.toString(context.obj));
                }
            }
        } else if ("PCT".equals(context.course)) {
            title.add("PCT Tutorial");
        } else if ("MPR".equals(context.course)) {
            title.add("Math Placement Review");
        }

        PanelChat.emitPanel(htm);
        PanelWhiteboard.emitPanel(htm, title.toString());
        PanelTutorVideo.emitPanel(htm);
        PanelStudentVideo.emitPanel(htm);
    }
}
