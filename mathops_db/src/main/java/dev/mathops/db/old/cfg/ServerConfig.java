package dev.mathops.db.old.cfg;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.EqualityTests;
import dev.mathops.core.builder.HtmlBuilder;
import dev.mathops.core.log.Log;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.INode;
import dev.mathops.core.parser.xml.NonemptyElement;
import dev.mathops.db.EDbInstallationType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a server machine with a database product running. This object does not specify a particular database on
 * the server or login credentials.
 *
 * <p>
 * There should exist one {@code ServerConfig} object for each unique database product installation on a server machine.
 * There could be multiple database products running on a single machine, in which case each would have its own object.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;server type='...' host='...' port='...' name='...'&gt;
 *   ... zero or more &lt;db&gt; child elements ...
 * &lt;/server&gt;
 * </pre>
 */
public final class ServerConfig {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "server";

    /** The server type attribute. */
    private static final String SERVER_TYPE_ATTR = "type";

    /** The host attribute. */
    private static final String HOST_ATTR = "host";

    /** The port attribute. */
    private static final String PORT_ATTR = "port";

    /** The server name attribute. */
    private static final String SERVER_NAME_ATTR = "name";

    /** A common integer. */
    private static final Integer ZERO = Integer.valueOf(0);

    /** The server type. */
    public final EDbInstallationType type;

    /** The server host name (or IP address). */
    public final String host;

    /** The TCP port on which the server accepts JDBC connections. */
    public final int port;

    /** The name of the server to which to connect (applies to Oracle and Informix). */
    public final String name;

    /** The list of databases present on the server. */
    private final List<DbConfig> databases;

    /**
     * Constructs a new {@code ServerConfig} from its XML representation.
     *
     * @param theSchemaMap the schema map
     * @param theLoginMap  the login map
     * @param theElem      the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    ServerConfig(final Map<String, SchemaConfig> theSchemaMap,
                 final Map<String, LoginConfig> theLoginMap, final NonemptyElement theElem)
            throws ParsingException {

        if (ELEM_TAG.equals(theElem.getTagName())) {
            this.type = EDbInstallationType.forName(theElem.getRequiredStringAttr(SERVER_TYPE_ATTR));
            if (this.type == null) {
                throw new ParsingException(theElem.getStart(), theElem.getEnd(),
                        Res.fmt(Res.SRV_CFG_BAD_TYPE, theElem.getRequiredStringAttr(SERVER_TYPE_ATTR)));
            }

            this.host = theElem.getRequiredStringAttr(HOST_ATTR);
            this.port = theElem.getIntegerAttr(PORT_ATTR, ZERO).intValue();
            this.name = theElem.getStringAttr(SERVER_NAME_ATTR);

            final int count = theElem.getNumChildren();
            this.databases = new ArrayList<>(count);

            for (int i = 0; i < count; ++i) {
                final INode child = theElem.getChild(i);
                if (child instanceof NonemptyElement) {
                    this.databases.add(new DbConfig(this, theSchemaMap, theLoginMap, (NonemptyElement) child));
                } else {
                    Log.warning("Unexpected child element of 'server'.");
                }
            }
        } else {
            throw new ParsingException(theElem.getStart(), theElem.getEnd(), Res.get(Res.SRV_CFG_BAD_ELEM_TAG));
        }
    }

    /**
     * Gets the list of databases on the server.
     *
     * @return the list of databases
     */
    public List<DbConfig> getDatabases() {

        return Collections.unmodifiableList(this.databases);
    }

    /**
     * Tests whether this {@code ServerConfig} is equal to another object. To be equal, the other object must be a
     * {@code ServerConfig} and must have the same type, host, port, and name.
     *
     * @param obj the object against which to compare this object for equality
     * @return {@code true} if the objects are equal; {@code false} if not
     */
    @Override
    public boolean equals(final Object obj) {

        final boolean equal;

        if (obj instanceof final ServerConfig test) {
            equal = test.type == this.type && test.host.equals(this.host) && test.port == this.port
                    && Objects.equals(test.name, this.name);
        } else {
            equal = false;
        }

        return equal;
    }

    /**
     * Generates a hash code for the object.
     *
     * @return the hash code.
     */
    @Override
    public int hashCode() {

        return this.type.hashCode() + this.host.hashCode() + this.port + EqualityTests.objectHashCode(this.name);
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add(this.type);
        if (this.name != null) {
            htm.add(" on server ", this.name);
        }
        htm.add(" at ", this.host, CoreConstants.COLON, Integer.toString(this.port));

        return htm.toString();
    }
}
