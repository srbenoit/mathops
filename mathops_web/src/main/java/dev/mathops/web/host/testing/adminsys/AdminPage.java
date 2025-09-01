package dev.mathops.web.host.testing.adminsys;

import dev.mathops.db.enums.ERole;
import dev.mathops.db.schema.legacy.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * A base class for pages in the administrative system site.
 */
public enum AdminPage {
    ;

    /**
     * Emits the heading at the top of each page site title and logged-in username.
     *
     * @param htm      the {@code HtmlBuilder} to which to append
     * @param session  the login session
     * @param whichDb  the database to which we are connected
     * @param showHome true to show a "Home" button that links to "home.html"
     */
    public static void emitPageHeader(final HtmlBuilder htm, final ImmutableSessionInfo session,
                                      final RawWhichDb whichDb, final boolean showHome) {

        if (showHome) {
            htm.sDiv("floatnav");

            htm.addln("<form style='display:inline;' method='get' action='home.html'>");
            final String homeLbl = Res.get(Res.HOME_BTN_LBL);
            htm.add("<button class='floatnav'>", homeLbl, "</button>");
            htm.addln("</form>");

            if (session != null && session.role == ERole.SYSADMIN) {
                htm.addln("<form style='display:inline;' method='get' action='/adminsys/home.html'>");
                final String rootLbl = Res.get(Res.ROOT_BTN_LBL);
                htm.add("<button class='floatnav'>", rootLbl, "</button>");
                htm.addln("</form>");
            }

            htm.eDiv(); // floatnav
        } else if (session != null && session.role == ERole.SYSADMIN) {
            htm.sDiv("floatnav");

            htm.addln("<form style='display:inline;' method='get' action='/adminsys/home.html'>");
            final String rootLbl = Res.get(Res.ROOT_BTN_LBL);
            htm.add("<button class='floatnav'>", rootLbl, "</button>");
            htm.addln("</form>");

            htm.eDiv(); // floatnav
        }

        final String title = Res.get(Res.SITE_TITLE);
        htm.sH(1).add(title).eH(1);

        if (session != null && whichDb != null) {
            final String loggedInLbl = Res.fmt(Res.LOGGED_IN_TO_AS, whichDb.descr, session.screenName);
            htm.sH(3).add(loggedInLbl).eH(3);
        }

        htm.div("clear");
        htm.hr("orange");
    }
}
