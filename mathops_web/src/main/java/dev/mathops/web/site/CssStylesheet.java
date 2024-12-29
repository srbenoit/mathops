package dev.mathops.web.site;

import dev.mathops.commons.CoreConstants;

import dev.mathops.text.builder.HtmlBuilder;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A container for CSS styles that can generate the CSS representation of the stylesheet.
 */
public class CssStylesheet {

    /** Font list to use within the stylesheet. */
    private final String fonts;

    /** Map from identifier to a list of styles that apply to that identifier. */
    private final Map<String, List<String>> styles;

    /** The length of the longest identifier (drives spacing on the output file). */
    private int maxIdentifier;

    /**
     * Constructs a new {@code CssStylesheet}.
     *
     * @param theFonts the font list to use within the stylesheet (this can be
     */
    public CssStylesheet(final String theFonts) {

        this.fonts = "font-family:" + theFonts + ";";
        this.styles = new LinkedHashMap<>(100);
        this.maxIdentifier = 0;
    }

    /**
     * Adds the list of fonts as a style within a particular identifier. For example, if the fonts list provided to the
     * constructor was "geneva,sans-serif", calling this method with identifier "p" would result in a CSS entry of the
     * form "p {font-family:geneva,sans-serif;}".
     * <p>
     * Styles will be written out in the CSS in the order in which they are added.
     *
     * @param identifier the identifier
     */
    final void addFontsToStyle(final String identifier) {

        getListForId(identifier).add(this.fonts);
    }

    /**
     * Adds a new style to a particular identifier. For example, adding style "color" with value "red" to identifier "p"
     * would result in a CSS entry of the form "p {color:red;}".
     * <p>
     * Styles will be written out in the CSS in the order in which they are added.
     *
     * @param identifier the identifier
     * @param style      the name of the style to add
     * @param value      the value of the style to add
     */
    protected final void addStyle(final String identifier, final String style, final String value) {

        getListForId(identifier).add(style + CoreConstants.COLON + value + ";");
    }

    /**
     * Gets the list of styles associated with a particular identifier, creating it if needed.
     *
     * @param identifier the identifier
     * @return the associated list of styles
     */
    private List<String> getListForId(final String identifier) {

        final List<String> list = this.styles.computeIfAbsent(identifier, s -> new ArrayList<>(10));

        if (identifier.length() > this.maxIdentifier) {
            this.maxIdentifier = identifier.length();
        }

        return list;
    }

    /**
     * Generates the stylesheet in CSS format suitable for transmitting to a web browser.
     *
     * @param css the {@code HtmlBuilder} to which to append the stylesheet
     */
    private void appendCss(final HtmlBuilder css) {

        for (final Map.Entry<String, List<String>> entry : this.styles.entrySet()) {
            final String key = entry.getKey();

            final List<String> list = entry.getValue();
            final int len = key.length();

            css.add(key);
            for (int j = 0; j < this.maxIdentifier + 2 - len; ++j) {
                css.add(' ');
            }
            css.add("{", list.getFirst());

            final int size = list.size();
            for (int i = 1; i < size; i++) {
                css.addln();
                for (int j = 0; j < this.maxIdentifier + 3; ++j) {
                    css.add(' ');
                }
                css.add(list.get(i));
            }

            css.addln('}');
        }
    }

    /**
     * Serves the CSS stylesheet.
     *
     * @param req  the request
     * @param resp the response
     * @throws IOException if there is an error writing the response
     */
    public final void serveCss(final ServletRequest req, final HttpServletResponse resp) throws IOException {

        final HtmlBuilder css = new HtmlBuilder(500);

        appendCss(css);
        AbstractSite.sendReply(req, resp, AbstractSite.MIME_TEXT_CSS, css.toString().getBytes(StandardCharsets.UTF_8));
    }
}
