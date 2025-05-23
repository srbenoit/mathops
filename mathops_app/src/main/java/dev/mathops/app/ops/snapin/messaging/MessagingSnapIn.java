package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.app.ops.snapin.AbstractFullPanel;
import dev.mathops.app.ops.snapin.AbstractSnapIn;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Facet;

import javax.swing.JFrame;

/**
 * A snap-in that interacts with the messaging system.
 */
public final class MessagingSnapIn extends AbstractSnapIn {

    /** The thumbnail panel. */
    private final MessagingThumbnail thumbnail;

    /** The dashboard panel. */
    private final MessagingDashboard dashboardTile;

    /** The full-window panel. */
    private final MessagingFull full;

    /**
     * Constructs a new {@code MessagingSnapIn}.
     *
     * @param theSchema     the database schema
     * @param theLiveSchema the live database schema
     * @param theCache      the data cache
     * @param theFrame      the owning frame
     * @param accessToken   the Canvas access token
     */
    public MessagingSnapIn(final Facet theSchema, final Facet theLiveSchema, final Cache theCache,
                           final JFrame theFrame, final String accessToken) {

        super(theSchema, theLiveSchema, theCache);

        this.thumbnail = new MessagingThumbnail();
        this.dashboardTile = new MessagingDashboard();
        this.full = new MessagingFull(theCache, this, theFrame, accessToken);
    }

    /**
     * Gets the snap-in title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return ("Messaging");
    }

    /**
     * Gets the thumbnail panel.
     *
     * @return the thumbnail panel
     */
    @Override
    public MessagingThumbnail getThumbnail() {

        return this.thumbnail;
    }

    /**
     * Gets the dashboard tile panel.
     *
     * @return the dashboard tile panel
     */
    @Override
    public MessagingDashboard getDashboardTile() {

        return this.dashboardTile;
    }

    /**
     * Gets the full-window panel.
     *
     * @return the full-window panel
     */
    @Override
    public AbstractFullPanel getFull() {

        return this.full;
    }
}
