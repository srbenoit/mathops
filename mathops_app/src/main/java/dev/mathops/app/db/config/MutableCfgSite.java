package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.CfgDataProfile;
import dev.mathops.db.config.CfgSite;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Map;

/**
 * A mutable model of a site (as specified by an absolute path) within a web context, which specifies the
 * data profile to use for that site.
 *
 * <p>
 * To create a new {@code MutableCfgSite}, the UI would present a field to enter
 * a path, and a dropdown to select a profile, with a button to execute the addition, which would fail if the path
 * were empty or not unique, or if the profile had not been selected.  Creating a new site adds this object to the list
 * of dependencies on the selected profile.
 *
 * <p>
 * If the path is updated and an "apply" button pressed in a GUI, the new path is tested for uniqueness within the
 * owning web context.
 */
public final class MutableCfgSite {

    /** The owning web context. */
    private final MutableCfgWebContext owner;

    /** The path. */
    private StringProperty path;

    /** The profile ID. */
    private StringProperty profileId;

    /**
     * Constructs a new {@code MutableCfgSite}.
     *
     * @param theOwner     the owning mutable web context
     * @param thePath      the path
     * @param theProfileId the data profile ID
     */
    public MutableCfgSite(final MutableCfgWebContext theOwner, final String thePath, final String theProfileId) {

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
     * Constructs a new {@code MutableCfgSite} from a {@code CfgSite}.
     *
     * @param theOwner the owning mutable web context
     * @param source   the source {@code CfgSite}
     */
    public MutableCfgSite(final MutableCfgWebContext theOwner, final CfgSite source) {

        if (theOwner == null) {
            throw new IllegalArgumentException("Owning web context may not be null");
        }

        this.owner = theOwner;
        this.path = new SimpleStringProperty(source.path);
        this.profileId = new SimpleStringProperty(source.profile.id);
    }

    /**
     * Gets the owning mutable web context.
     *
     * @return the owning mutable web context
     */
    public MutableCfgWebContext getOwner() {

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
     * Generate an immutable {@code CfgSite} from this object.
     *
     * @param profiles a map from string profile ID to the data profile object
     * @return the generated {@code CfgSite}
     */
    CfgSite toCfgSite(final Map<String, CfgDataProfile> profiles) {

        final String pathStr = this.path.get();
        final String profileIdStr = this.profileId.get();
        final CfgDataProfile profile = profiles.get(profileIdStr);

        return new CfgSite(pathStr, profile);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgSite{path='", this.path, "',profileId='", this.profileId, "'}");
    }
}
