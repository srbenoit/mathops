package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.CfgCodeContext;
import dev.mathops.db.config.CfgDataProfile;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

/**
 * A mutable model of a code context, which maps a code context ID (to which code can refer) to a data profile.
 *
 * <p>
 * To create a new {@code MutableCfgCodeContext}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class MutableCfgCodeContext {

    /** The ID. */
    private StringProperty id;

    /** The profile ID. */
    private StringProperty profileId;

    /**
     * Constructs a new {@code MutableCfgCodeContext}.
     *
     * @param theId        the ID
     * @param theProfileId the data profile ID
     */
    public MutableCfgCodeContext(final String theId, final String theProfileId) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Context ID may not be null or blank");
        }
        if (theProfileId == null) {
            throw new IllegalArgumentException("Data profile ID may not be null");
        }

        this.id = new SimpleStringProperty(theId);
        this.profileId = new SimpleStringProperty(theProfileId);
    }

    /**
     * Constructs a new {@code MutableCfgCodeContext} from a {@code CfgCodeContext}.
     *
     * @param source   the source {@code CfgCodeContext}
     */
    public MutableCfgCodeContext(final CfgCodeContext source) {

        this.id = new SimpleStringProperty(source.id);
        this.profileId = new SimpleStringProperty(source.profile.id);
    }

    /**
     * Gets the ID property.
     *
     * @return the ID property
     */
    public StringProperty getIdProperty() {

        return this.id;
    }

    /**
     * Gets the profile ID property.
     *
     * @return the profile ID property
     */
    public StringProperty getProfileIdProperty() {

        return this.profileId;
    }

    /**
     * Generate an immutable {@code CfgCodeContext} from this object.
     *
     * @return the generated {@code CfgCodeContext}
     */
    CfgCodeContext toCfgCodeContext(final Map<String, CfgDataProfile> profiles) {

        final String idStr = this.id.get();
        final String profileIdStr = this.profileId.get();
        final CfgDataProfile profile = profiles.get(profileIdStr);

        return new CfgCodeContext(idStr, profile);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgCodeContext{id='", this.id, "',profileId='", this.profileId, "'}");
    }
}
