package dev.mathops.app.db.config.model;

import dev.mathops.core.builder.SimpleBuilder;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * The data model of a site (as specified by an absolute path) within a web context, which specifies the
 * data profile to use for that site.
 *
 * <p>
 * To create a new {@code CfgSiteModel}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class CfgSiteModel {

    /** The owning web context. */
    private final CfgWebContextModel owner;

    /** The path. */
    private final StringProperty path;

    /** The profile ID. */
    private final StringProperty profileId;

    /**
     * Constructs a new {@code CfgSiteModel}.
     *
     * @param theOwner     the owning mutable web context
     * @param thePath      the path
     * @param theProfileId the data profile ID
     */
    public CfgSiteModel(final CfgWebContextModel theOwner, final String thePath, final String theProfileId) {

        if (theOwner == null) {
            throw new IllegalArgumentException("Owning web context may not be null");
        }
        if (thePath == null || thePath.isBlank()) {
            throw new IllegalArgumentException("Path may not be null or blank");
        }
        if (theProfileId == null) {
            throw new IllegalArgumentException("Data profile ID may not be null");
        }

        this.owner = theOwner;
        this.path = new SimpleStringProperty(thePath);
        this.profileId = new SimpleStringProperty(theProfileId);
    }

    /**
     * Gets the owning mutable web context.
     *
     * @return the owning mutable web context
     */
    public CfgWebContextModel getOwner() {

        return this.owner;
    }

    /**
     * Gets the path property.
     *
     * @return the path property
     */
    public StringProperty getPathProperty() {

        return this.path;
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
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("CfgSiteModel{path='", this.path, "',profileId='", this.profileId, "'}");
    }
}
