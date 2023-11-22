package dev.mathops.app.ops.snapin.messaging;

import dev.mathops.db.Cache;
import dev.mathops.db.DbContext;
import dev.mathops.app.ops.snapin.AbstractFullPanel;
import dev.mathops.app.ops.snapin.AbstractSnapIn;

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
     * @param theContext     the database context
     * @param theLiveContext the live database context
     * @param theCache       the data cache
     * @param theFrame       the owning frame
     */
    public MessagingSnapIn(final DbContext theContext, final DbContext theLiveContext,
                           final Cache theCache, final JFrame theFrame) {

        super(theContext, theLiveContext, theCache);

        this.thumbnail = new MessagingThumbnail();
        this.dashboardTile = new MessagingDashboard();
        this.full = new MessagingFull(theCache, this, theFrame);
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
