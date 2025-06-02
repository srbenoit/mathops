package dev.mathops.web.host.precalc.course;

import dev.mathops.db.Cache;
import dev.mathops.text.builder.HtmlBuilder;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.Page;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * A page that displays a maintenance message.
 */
enum PageMaintenance {
    ;

    /**
     * Generates the page with contact information.
     *
     * @param cache   the data cache
     * @param site    the owning site
     * @param req     the request
     * @param resp    the response
     * @param message the maintenance message
     * @throws IOException  if there is an error writing the response
     * @throws SQLException if there is an error accessing the database
     */
    static void doGet(final Cache cache, final CourseSite site, final ServletRequest req,
                      final HttpServletResponse resp, final String message) throws IOException, SQLException {

        final HtmlBuilder htm = new HtmlBuilder(2000);
        Page.startOrdinaryPage(htm, site.getTitle(), null, true, Page.ADMIN_BAR | Page.USER_DATE_BAR, null, false,
                true);

        htm.div("vgap2");

        htm.sDiv("center");
        htm.sDiv("hours");
        htm.sH(4, "center red").add("SYSTEM UNDERGOING MAINTENANCE").eH(4);
        htm.sP().add("<strong>", message, "</strong>").eP();
        htm.eDiv();
        htm.eDiv();

        htm.div("vgap2");

        htm.div("vgap2");
        htm.sDiv("indent44");

        htm.sDiv("advice");
        htm.sDiv("center").add("<img width='64' height='64' src='/images/dialog-warning-2.png'/>").eDiv().hr();

        htm.sDiv("largerbox");
        htm.addln(" <ul class='boxlist'><li class='boxlist'>",
                "Precalculus courses have deadlines that must be met to avoid losing points. ",
                "These are not \"self-paced\" courses!</li>");
        htm.addln("</ul>");
        htm.eDiv();

        htm.addln(" <hr/>");

        htm.sDiv("largerbox");
        htm.addln(" <ul class='boxlist'>");
        htm.addln(" <li class='boxlist'>");
        // FIXME: Rename (or convert to HTML)
        htm.addln(" Please review the <a class='ulink' ",
                "href='https://www.math.colostate.edu/Precalc/Precalc-Student-Guide.pdf'>",
                "Student Guide</a> for more information.</li>");

//        htm.addln("<li class='boxlist'>");
//        htm.addln(" Please review the <a class='ulink' ", "href='/instruction/orientation.html'>",
//                "Online Orientation</a> for an online orientation to the Precalculus Program.");
//        htm.addln(" </li>");

        htm.addln("</ul>");
        htm.eDiv();

        htm.eDiv();
        htm.eDiv();

        Page.endOrdinaryPage(cache, site, htm, true);

        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_HTML, htm);
    }
}
