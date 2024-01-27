package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.db.EDbProduct;

import java.util.Objects;

/**
 * Information on an installed database product on a database server.
 *
 * <pre>
 *   &lt;db-product product='...' version='...'/&gt;
 * </pre>
 * <p>
 * where product is the name of a EDbInstallationType value.
 */
final class DataDbInstalledProduct {

    /** The product. */
    public final EDbProduct product;

    /** The version. */
    final String version;

    /**
     * Constructs a new {@code DataDbInstalledProduct}.
     *
     * @param theProduct the product
     * @param theVersion the version
     */
    DataDbInstalledProduct(final EDbProduct theProduct, final CharSequence theVersion) {

        this.product = Objects.requireNonNullElse(theProduct, EDbProduct.INFORMIX);

        if (theVersion == null) {
            this.version = CoreConstants.EMPTY;
        } else {
            this.version = sanitize(theVersion);
        }
    }

    /**
     * Attempts to parse a {@code DbInstalledProductInfo} from an XML element.
     *
     * @param elem the element
     * @return the parsed data; {@code null} if parsing failed
     */
    static DataDbInstalledProduct parse(final IElement elem) {

        DataDbInstalledProduct info = null;

        final EDbProduct product = EDbProduct.forName(elem.getStringAttr("product"));
        final String version = elem.getStringAttr("version");

        if (product != null && version != null) {
            info = new DataDbInstalledProduct(product, version);
        }

        return info;
    }

    /**
     * Appends the XML representation of this object to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder htm) {

        htm.addln("  <db-product product='", this.product.name, "' version='", this.version, "'/>");
    }

    /**
     * Sanitizes a string, replacing all characters that are not [A-Za-z0-9.-/() ] with '_'.
     *
     * @param toSanitize the string to sanitize
     * @return the sanitized string
     */
    public static String sanitize(final CharSequence toSanitize) {

        final int len = toSanitize.length();
        final char[] san = new char[len];
        for (int i = 0; i < len; ++i) {
            final char ch = toSanitize.charAt(i);
            if (ch == '.' || ch == '-' || ch == '/' || ch == '(' || ch == ')' || ch == ' '
                    || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9')) {
                san[i] = ch;
            } else {
                // Replacement is safe in URLs, but not a real hostname character
                san[i] = '_';
            }
        }

        return new String(san);
    }
}
