package dev.mathops.app.db.config;

import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.db.config.CfgDataProfile;
import dev.mathops.db.config.CfgSite;
import dev.mathops.db.config.CfgWebContext;
import javafx.beans.property.MapPropertyBase;
import javafx.beans.property.SimpleMapProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringPropertyBase;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A mutable configuration of a web context (as specified by a web server host), which defines a list of sites
 *  * within that web context.
 *
 * <p>
 * To create a new {@code MutableCfgWebContext}, the UI would present fields to enter its host name, with a button to
 * execute the addition, which would fail if entered host name is not unique.  The new instance would have no sites.
 *
 * <p>
 * If the host is updated and an "apply" button pressed in a GUI, the new host is tested for uniqueness before being
 * accepted.
 *
 * <p>
 * Once a web context is created, a GUI should allow users to add site configurations within that web context,
 * or delete existing site configurations.
 *
 * <p>
 * A GUI should support deletion of a web context.
 */
public final class MutableCfgWebContext {

    /** The host. */
    public final StringPropertyBase host;

    /** A mutable map from site path to the mutable site configuration. */
    private final MapPropertyBase<String, MutableCfgSite> sites;

    /**
     * Constructs a new, empty {@code MutableCfgWebContext}.
     */
    public MutableCfgWebContext() {

        this.host = new SimpleStringProperty();
        this.sites = new SimpleMapProperty<>();
    }

    /**
     * Constructs a new {@code MutableCfgWebContext} from a {@code CfgWebContext}.
     *
     * @param source   the source {@code CfgWebContext}
     */
    public MutableCfgWebContext(final CfgWebContext source) {

        this.host = new SimpleStringProperty(source.host);
        this.sites = new SimpleMapProperty<>();

        for (final CfgSite site : source.getSites()) {
            final MutableCfgSite mutableSite = new MutableCfgSite(this, site);
            this.sites.put(site.path, mutableSite);
        }
    }

    /**
     * Gets the host property.
     *
     * @return the host property
     */
    public StringPropertyBase getHostProperty() {

        return this.host;
    }

    /**
     * Gets the sites property.
     *
     * @return the sites property
     */
    public MapPropertyBase<String, MutableCfgSite> getSitesProperty() {

        return this.sites;
    }

    /**
     * Generate an immutable {@code CfgDataProfile} from this object.
     *
     * @param profiles a map from string profile ID to the data profile object
     * @return the generated {@code CfgDataProfile}
     */
    CfgWebContext toCfgWebContext(final Map<String, CfgDataProfile> profiles) {

        final List<CfgSite> siteList = new ArrayList<>(10);

        for (final MutableCfgSite mutableSite : this.sites.values()) {
            final CfgSite schemaLogin = mutableSite.toCfgSite(profiles);
            siteList.add(schemaLogin);
        }

        final String hostValue = this.host.getValue();

        return new CfgWebContext(hostValue, siteList);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MutableCfgWebContext{host='", this.host, "',sites=[", this.sites, "]}");
    }
}
