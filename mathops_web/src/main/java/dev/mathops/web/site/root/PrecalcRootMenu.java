package dev.mathops.web.site.root;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.db.Contexts;
import dev.mathops.web.site.AbstractSite;
import dev.mathops.web.site.ESiteType;

/**
 * A menu of options available at the top level of the site, when no user is logged in.
 */
enum PrecalcRootMenu {
    ;

    /**
     * Generates the welcome page that users see when they access the site with either the '/' or '/index.html' paths.
     *
     * @param site the owning site
     * @param type the site type
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     */
    static void buildMenu(final PrecalcRootSite site, final ESiteType type, final HtmlBuilder htm) {

        htm.addln("<nav class='menu'>");
        htm.sDiv("menubox");

        buildMenuContent(site, type, htm);

        htm.eDiv(); // menubox
        htm.add("</nav>");
    }

    /**
     * Creates the menu content.
     *
     * @param site the owning site
     * @param type the site type
     * @param htm  the {@code HtmlBuilder} to which to append the HTML
     */
    private static void buildMenuContent(final PrecalcRootSite site, final ESiteType type, final HtmlBuilder htm) {

        final String maintenanceMsg = AbstractSite.isMaintenance(site.siteProfile);

        htm.add("<a href='index.html' class='ulink'>Home</a>");
        htm.div("vgap0").hr().div("vgap0");

        final String linkhost = type == ESiteType.PROD ? Contexts.PLACEMENT_HOST : Contexts.PLACEMENTDEV_HOST;

        htm.addln("<a class='ulink' href='https://", linkhost, "/welcome/welcome.html'>Math Placement</a>").br();
        htm.div("vgap0");

        htm.addln("<a href='courses.html' class='ulink'>Courses</a>").br();
        htm.div("vgap0");

        // FIXME: Update URL path
        htm.addln("<a class='ulink' target='_blank' ",
                "href='https://www.math.colostate.edu/Precalc/Precalc-Student-Guide.pdf'>Student Guide</a>").br();
        htm.div("vgap0");

        htm.addln("<a class='ulink' href='orientation.html'>Online Orientation</a>").br();
        htm.div("vgap0");

        htm.addln("<a href='contact.html' class='ulink'>Contact Us</a>").br();
        htm.div("vgap0").hr().div("vgap0");

        if (maintenanceMsg == null) {
            htm.addln("<a href='instruction/secure/shibboleth.html' class='menubtn'/>eID Login</a>");
            htm.div("vgap0").hr().div("vgap0");
        }
    }
}
