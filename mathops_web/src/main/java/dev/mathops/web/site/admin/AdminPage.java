package dev.mathops.web.site.admin;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.db.enums.ERole;
import dev.mathops.db.old.rawrecord.RawWhichDb;
import dev.mathops.session.ImmutableSessionInfo;

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
            htm.add("<button class='floatnav'>", Res.get(Res.HOME_BTN_LBL), "</button>");
            htm.addln("</form>");

            if (session != null && session.role == ERole.SYSADMIN) {
                htm.addln("<form style='display:inline;' method='get' action='/adminsys/home.html'>");
                htm.add("<button class='floatnav'>", Res.get(Res.ROOT_BTN_LBL), "</button>");
                htm.addln("</form>");
            }

            htm.eDiv(); // floatnav
        } else if (session != null && session.role == ERole.SYSADMIN) {
            htm.sDiv("floatnav");

            htm.addln("<form style='display:inline;' method='get' action='/adminsys/home.html'>");
            htm.add("<button class='floatnav'>", Res.get(Res.ROOT_BTN_LBL), "</button>");
            htm.addln("</form>");

            htm.eDiv(); // floatnav
        }

        htm.sH(1).add(Res.get(Res.SITE_TITLE)).eH(1);

        if (session != null && whichDb != null) {
            htm.sH(3).add(Res.fmt(Res.LOGGED_IN_TO_AS, whichDb.descr, session.screenName)).eH(3);
        }

        htm.div("clear");
        htm.hr("orange");
    }
}
