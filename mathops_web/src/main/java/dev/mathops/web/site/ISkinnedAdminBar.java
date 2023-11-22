package dev.mathops.web.site;

import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.session.ImmutableSessionInfo;
import dev.mathops.web.site.help.HelpSite;

/**
 * A class that emits an administrative bar at the top of a page.
 */
public interface ISkinnedAdminBar {

    /**
     * Gets the height of the admin bar, in CSS pixels.
     *
     * @param session the session
     * @return the height
     */
    int getAdminBarHeight(ImmutableSessionInfo session);

    /**
     * Emits the HTML for the admin bar.
     *
     * @param site    the owning site
     * @param htm     the {@code HtmlBuilder} to which to append
     * @param session the session
     */
    void emitAdminBar(HelpSite site, HtmlBuilder htm, ImmutableSessionInfo session);
}
