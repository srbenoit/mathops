package dev.mathops.web.site.cfm;

import dev.mathops.text.builder.HtmlBuilder;

/**
 * Utilities that generate content common to multiple pages.
 */
enum PageUtilities {
    ;

    /**
     * Generates the heading for the top of the page, with the CFM logo (a link to the main landing page), and an icon
     * and title in a larger font.
     *
     * @param htm          the {@code HtmlBuilder} to which to append
     * @param title        the title text
     * @param iconFilename the icon filename
     */
    static void emitSubpageTitle(final HtmlBuilder htm, final String title, final String iconFilename) {

        htm.add("<a href='index.html'>");
        htm.sDiv("left")
                .add("<img style='padding-left:20px; padding-right:12px; height:70px;' ",
                        "src='/www/images/cfm/logo_transparent_256.png' alt=''/>")
                .eDiv();

        htm.sDiv("left");
        htm.add("<h1 style='font-family:prox-regular; padding-right:15px; margin:0; border-right:3px solid #59595B;'>");
        htm.sP(null, "style='font-size:11.3pt; margin:1px;'").addln("THE CENTER FOR").eP();
        htm.sP(null, "style='font-size:12pt; margin:1px;'").addln("FOUNDATIONAL").eP();
        htm.sP(null, "style='font-size:13.2pt; margin:1px;'").addln("MATHEMATICS").eP();
        htm.eH(1).eDiv().add("</a>");
        htm.sH(2).add(
                "<img style='width:56px;margin-left:15px;padding-top:9px;' src='/www/images/cfm/", iconFilename, "'/>",
                "<span style='margin-left:14px; font-size:36px; position:relative; bottom:8px; color:#59595B;'>",
                title, "</span>").eH(2);

        htm.div("clear");
        htm.div("vgap");
    }

    /**
     * Generates the navigation bar with icons that link to the main pages in the site.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    static void emitNavigationBar(final HtmlBuilder htm) {

        htm.addln("<table style='margin:30px auto; width:90%; max-width:800px; font-size:14pt;'><tr>");
        htm.addln("  <td style='text-align:center;'><a href='info.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/info.svg'/>").br().add("Information");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='contact.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/contact.svg'/>").br().add("Contact Us");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='analytics.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/analytics.svg'/>").br().add("Analytics");
        htm.addln("  </td>");
        htm.addln("  <td style='text-align:center;'><a href='strategy.html'>");
        htm.addln("    <img style='width:40px;' src='/www/images/cfm/strategy.svg'/>").br().add("Strategy");
        htm.addln("  </td>");
        htm.addln("</tr></table>");
    }
}
