package dev.mathops.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.parser.ParsingException;
import dev.mathops.core.parser.xml.EmptyElement;

import java.util.Map;

/**
 * An immutable representation of a code context, which maps a code context ID (to which code can refer) to a data
 * profile.
 *
 * <p>
 * XML Representation:
 *
 * <pre>
 * &lt;code-context id='...' profile='...'/&gt;
 * </pre>
 */
public final class CfgCodeContext {

    /** The element tag used in the XML representation of the configuration. */
    static final String ELEM_TAG = "code-context";

    /** The ID attribute. */
    private static final String ID_ATTR = "id";

    /** The ID attribute. */
    private static final String PROFILE_ATTR = "profile";

    /** The code context ID. */
    public final String id;

    /** The data profile. */
    public final CfgDataProfile profile;

    /**
     * Constructs a new {@code CodeContext}.
     *
     * @param theId      the code context ID
     * @param theProfile a data profile
     * @throws IllegalArgumentException if either argument is null
     */
    public CfgCodeContext(final String theId, final CfgDataProfile theProfile) {

        if (theId == null) {
            throw new IllegalArgumentException("Code context ID may not be null");
        }
        if (theProfile == null) {
            throw new IllegalArgumentException("Data profile may not be null");
        }

        this.id = theId;
        this.profile = theProfile;
    }


    /**
     * Constructs a new {@code CodeContext} from its XML representation.
     *
     * @param theProfileMap a map from string data profile ID to data profile
     * @param theElem       the XML element from which to extract configuration settings.
     * @throws ParsingException if required data is missing from the element or the data that is present is invalid
     */
    CfgCodeContext(final Map<String, CfgDataProfile> theProfileMap, final EmptyElement theElem) throws ParsingException {

        final String tag = theElem.getTagName();
        if (ELEM_TAG.equals(tag)) {
            this.id = theElem.getRequiredStringAttr(ID_ATTR);
            final String profileId = theElem.getRequiredStringAttr(PROFILE_ATTR);

            this.profile = theProfileMap.get(profileId);
            if (this.profile == null) {
                final String msg = Res.fmt(Res.CODE_CTX_BAD_PROFILE, profileId, this.id);
                throw new ParsingException(theElem, msg);
            }
        } else {
            final String msg = Res.get(Res.CODE_CTX_BAD_ELEM_TAG);
            throw new ParsingException(theElem, msg);
        }
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

        if (obj instanceof final CfgCodeContext test) {
            equal = test.id.equals(this.id) && test.profile.equals(this.profile);
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

        return this.id.hashCode() + this.profile.hashCode();
    }

    /**
     * Generates the string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CodeContext{id=", this.id, ",profile=", this.profile.id, "}");
    }
}
