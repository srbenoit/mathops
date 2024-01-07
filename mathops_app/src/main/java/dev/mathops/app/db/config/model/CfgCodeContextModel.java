package dev.mathops.app.db.config.model;

import dev.mathops.core.builder.SimpleBuilder;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The data model of a code context, which maps a code context ID (to which code can refer) to a data profile.
 *
 * <p>
 * To create a new {@code CfgCodeContextModel}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class CfgCodeContextModel {

    /** The ID. */
    private final StringProperty id;

    /** The profile. */
    private final ObjectProperty<CfgDataProfileModel> profile;

    /**
     * Constructs a new {@code CfgCodeContextModel}.
     *
     * @param theId      the ID
     * @param theProfile the data profile
     */
    public CfgCodeContextModel(final String theId, final CfgDataProfileModel theProfile) {

        if (theId == null || theId.isBlank()) {
            throw new IllegalArgumentException("Context ID may not be null or blank");
        }
        if (theProfile == null) {
            throw new IllegalArgumentException("Data profile may not be null");
        }

        this.id = new SimpleStringProperty(theId);
        this.profile = new SimpleObjectProperty<>(theProfile);
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
    public ObjectProperty<CfgDataProfileModel> getProfileProperty() {

        return this.profile;
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgCodeContextModel{id='", this.id, "',profile='", this.profile, "'}");
    }
}
