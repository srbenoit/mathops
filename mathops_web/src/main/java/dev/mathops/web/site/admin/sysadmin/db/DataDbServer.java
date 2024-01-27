package dev.mathops.web.site.admin.sysadmin.db;

import dev.mathops.commons.builder.HtmlBuilder;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.INode;
import dev.mathops.commons.parser.xml.NonemptyElement;

import java.util.ArrayList;
import java.util.List;

/**
 * Information on one configure database server. This information will be stored in the file 'db_servers.xml' in the
 * configuration directory path on the system admin server. That file will have a top-level 'db-servers' element that
 * contains one child element for each configured database server, in the format shown here:
 *
 * <pre>
 *   &lt;db-server name='...' hostname='...'/&gt;
 * </pre>
 */
final class DataDbServer {

    /** The server name (short - used as button label). */
    public final String name;

    /** The server hostname. */
    public final String hostname;

    /** The list of products installed on the server. */
    final List<DataDbInstalledProduct> products;

    /**
     * Constructs a new {@code DataDbServer}.
     *
     * @param theName     the name (trimmed to 15 characters)
     * @param theHostname the fully qualified hostname (null or empty will be treated as 'localhost')
     */
    DataDbServer(final String theName, final CharSequence theHostname) {

        if (theName == null || theName.isEmpty()) {
            this.name = "(no name)";
        } else if (theName.length() > 15) {
            this.name = theName.substring(0, 15);
        } else {
            this.name = sanitize(theName);
        }

        if (theHostname == null || theHostname.isEmpty()) {
            this.hostname = "localhost";
        } else {
            this.hostname = sanitize(theHostname);
        }

        this.products = new ArrayList<>(10);
    }

    /**
     * Attempts to parse a {@code DbServerInfo} from an XML element.
     *
     * @param elem the element
     * @return the parsed data; {@code null} if parsing failed
     */
    static DataDbServer parse(final IElement elem) {

        DataDbServer info = null;

        final String name = elem.getStringAttr("name");
        final String hostname = elem.getStringAttr("hostname");

        if (name != null && hostname != null) {
            info = new DataDbServer(name, hostname);

            if (elem instanceof final NonemptyElement ne) {
                for (final INode child : ne.getChildrenAsList()) {
                    if (child instanceof IElement) {
                        final DataDbInstalledProduct prod = DataDbInstalledProduct.parse((IElement) child);
                        if (prod != null) {
                            info.products.add(prod);
                        }
                    }
                }
            }
        }

        return info;
    }

    /**
     * Appends the XML representation of this object to an {@code HtmlBuilder}.
     *
     * @param htm the {@code HtmlBuilder} to which to append
     */
    void appendXml(final HtmlBuilder htm) {

        htm.addln(" <db-server name='", this.name, "' hostname='", this.hostname, "'>");
        for (final DataDbInstalledProduct product : this.products) {
            product.appendXml(htm);
        }
        htm.addln(" </db-server>");
    }

    /**
     * Sanitizes a string, replacing all characters that are not [A-Za-z0-9.-] with '_'.
     *
     * @param chars the string to sanitize
     * @return the sanitized string
     */
    public static String sanitize(final CharSequence chars) {

        final String sanitized;

        if (chars == null) {
            sanitized = null;
        } else {
            final int len = chars.length();
            final char[] san = new char[len];
            for (int i = 0; i < len; ++i) {
                final char ch = chars.charAt(i);
                if (ch == '.' || ch == '-' || (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
                        || (ch >= '0' && ch <= '9')) {
                    san[i] = ch;
                } else {
                    // Replacement is safe in URLs, but not a real hostname character
                    san[i] = '_';
                }
            }

            sanitized = new String(san);
        }

        return sanitized;
    }
}
