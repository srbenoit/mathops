package dev.mathops.app.ops.snapin.activity;

import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.app.ops.snapin.AbstractDashboardPanel;
import dev.mathops.app.ops.snapin.AbstractFullPanel;
import dev.mathops.app.ops.snapin.AbstractSnapIn;
import dev.mathops.app.ops.snapin.AbstractThumbnailButton;
import dev.mathops.db.cfg.Facet;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;

/**
 * A snap-in that monitors system activity.
 */
public final class SystemActivitySnapIn extends AbstractSnapIn {

    /** The thumbnail panel. */
    private final SystemActivityThumbnail thumbnail;

    /** The dashboard panel. */
    private final SystemActivityDashboard dashboardTile;

    /** The full-window panel. */
    private final SystemActivityFull full;

    /**
     * Constructs a new {@code SystemActivitySnapIn}.
     *
     * @param theSchema     the database schema
     * @param theLiveSchema the live database schema
     * @param theCache      the data cache
     */
    public SystemActivitySnapIn(final Facet theSchema, final Facet theLiveSchema, final Cache theCache) {

        super(theSchema, theLiveSchema, theCache);

        this.thumbnail = new SystemActivityThumbnail();
        this.dashboardTile = new SystemActivityDashboard();
        this.full = new SystemActivityFull();
    }

    /**
     * Gets the snap-in title.
     *
     * @return the title
     */
    @Override
    public String getTitle() {

        return ("Activity");
    }

    /**
     * Gets the thumbnail panel.
     *
     * @return the thumbnail panel
     */
    @Override
    public SystemActivityThumbnail getThumbnail() {

        return this.thumbnail;
    }

    /**
     * Gets the dashboard tile panel.
     *
     * @return the dashboard tile panel
     */
    @Override
    public SystemActivityDashboard getDashboardTile() {

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

    /**
     * A thumbnail panel for this snap-in.
     */
    public static final class SystemActivityThumbnail extends AbstractThumbnailButton {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 6641968050867854014L;

        /** The button label. */
        private static final String BTN_LBL = "Activity";

        /** The bar color. */
        private static final Color BAR_COLOR = new Color(0, 200, 0);

        /** The bar color. */
        private static final Color BG_COLOR = new Color(240, 250, 250);

        /**
         * Constructs a new {@code SystemActivityThumbnail}.
         */
        SystemActivityThumbnail() {

            super();
        }

        /**
         * Paints the button.
         *
         * @param g the {@code Graphics} to which to draw
         */
        @Override
        public void paintComponent(final Graphics g) {

            super.paintComponent(g);

            final Graphics2D g2d = (Graphics2D) g;

            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT);

            final Dimension size = getSize();
            final int w = size.width * 3 / 8;
            final int h = size.width / 4;
            final int cx = size.width / 2;
            final int cy = (size.height - 10) / 2;

            g.setColor(BG_COLOR);
            g.fillRect(cx - w, cy - h, w << 1, h << 1);

            g.setColor(Color.BLACK);
            g.drawRect(cx - w, cy - h, w << 1, h << 1);

            g.setColor(BAR_COLOR);
            final int numColumns = (w - 2) / 2;
            final int colWidth = numColumns * 3 + (numColumns - 1);
            int x = cx - colWidth / 2;
            while (x < cx + colWidth / 2) {
                final int height = h + (int) ((double) ((h << 1) / 3) * StrictMath.sin(x));
                g.fillRect(x, cy + h - height, 3, height);
                x += 4;
            }

            g.setFont(FONT);
            final FontMetrics metr = g.getFontMetrics();
            final int txtW = metr.stringWidth(BTN_LBL);

            g.setColor(Color.BLACK);
            g.drawString(BTN_LBL, cx - txtW / 2, size.height - 5);
        }
    }

    /**
     * A dashboard panel for this snap-in.
     */
    public static final class SystemActivityDashboard extends AbstractDashboardPanel {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 4676454751232783001L;

        /**
         * Constructs a new {@code SystemActivityDashboard}.
         */
        SystemActivityDashboard() {

            super();
        }

        /**
         * Called on a timer thread to periodically refresh displays.
         */
        @Override
        public void tick() {

            Log.info("Activity dashboard tick");
        }
    }

    /**
     * A full-screen panel for this snap-in.
     */
    public static final class SystemActivityFull extends AbstractFullPanel {

        /** Version for serialization. */
        @Serial
        private static final long serialVersionUID = 877594327708121586L;

        /**
         * Constructs a new {@code SystemActivityFullscreen}.
         */
        SystemActivityFull() {

            super();
        }

        /**
         * Called on a timer thread to periodically refresh displays.
         */
        @Override
        public void tick() {

            Log.info("Activity full tick");
        }
    }
}
