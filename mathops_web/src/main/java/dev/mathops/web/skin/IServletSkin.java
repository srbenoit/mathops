package dev.mathops.web.skin;

import dev.mathops.core.builder.HtmlBuilder;

/**
 * The interface for a servlet skin.
 */
public interface IServletSkin {

    /**
     * Emits a stylesheet for the skin. May be emitted inside a &lt;style&gt; element in the &lt;head&gt; element, or as
     * a separate CSS file.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    void emitStylesheet(HtmlBuilder htm);

    /**
     * Emits scripts needed by the skin.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    void emitScripts(HtmlBuilder htm);

    /**
     * Gets the top bar height, in CSS pixels.
     *
     * @param hasSubtitle true if a subtitle will be included
     * @param collapsed   true if the top bar is "collapsed" when a screen is maximized
     * @return the top bar height
     */
    int getTopBarHeight(boolean hasSubtitle, boolean collapsed);

    /**
     * Emits the top bar.
     *
     * @param htm                the {@code HtmlBuilder} to which to append
     * @param subtitle           the optional subtitle
     * @param subtitleLink       the optional URL to which the subtitle is linked (ignored if subtitle is null)
     * @param toggleCollapsedUrl non-null to include a button (that redirects to this URL) to collapse/restore the top
     *                           bar
     * @param collapsed          true if the top bar is "collapsed", meaning the screen content area is maximized
     */
    void emitTopBar(HtmlBuilder htm, String subtitle, String subtitleLink,
                    String toggleCollapsedUrl, boolean collapsed);

    /**
     * Gets the bottom bar height, in CSS pixels.
     *
     * @param collapsed true if the bottom bar is "collapsed" when a screen is maximized
     * @return the bottom bar height
     */
    int getBottomBarHeight(boolean collapsed);

    /**
     * Emits the bottom bar.
     *
     * @param htm       the {@code HtmlBuilder} to which to append
     * @param collapsed true if the bottom bar is "collapsed" when a screen is maximized
     */
    void emitBottomBar(HtmlBuilder htm, boolean collapsed);
}
