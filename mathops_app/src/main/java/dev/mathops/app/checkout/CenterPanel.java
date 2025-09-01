package dev.mathops.app.checkout;

import dev.mathops.app.checkin.TestingCenterMap;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.cfg.Profile;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.schema.legacy.RawClientPc;
import dev.mathops.font.BundledFontManager;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.io.Serial;
import java.sql.SQLException;
import java.util.List;

/**
 * A panel to appear in the center of the check-in application. This panel displays a map of the testing center, showing
 * the status of all stations, and is used for buttons when selecting course/unit for exams.
 */
final class CenterPanel extends JPanel implements Runnable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7029995559760361198L;

    /** Application version. */
    private static final String VERSION = "v2.3.6 (May 13, 2024)";

    /** The database profile. */
    private final Profile profile;

    /** The testing center ID being managed. */
    private final String testingCenterId;

    /** The testing center map. */
    private final TestingCenterMap map;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /**
     * Constructs a new {@code CenterPanel}.
     *
     * @param theProfile         the database profile
     * @param theTestingCenterId the testing center ID being managed
     */
    CenterPanel(final Profile theProfile, final String theTestingCenterId) {

        super();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        this.profile = theProfile;
        this.testingCenterId = theTestingCenterId;

        setLayout(null);
        setBackground(new Color(170, 170, 200));
        setFocusable(true);
        setDoubleBuffered(true);

        this.map = new TestingCenterMap();

        final BundledFontManager bfm = BundledFontManager.getInstance();
        this.pcFont = bfm.getFont(BundledFontManager.SANS, 14.0, Font.BOLD);
    }

    /**
     * Draws the panel, including the center name and station number, if configured.
     *
     * @param g the {@code Graphics} to which to draw
     */
    @Override
    public void paintComponent(final Graphics g) {

        super.paintComponent(g);

        final Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        final int width = getWidth();
        final int height = getHeight();
        this.map.drawMap(g2d, width, height);

        g2d.setFont(this.pcFont);
        final int h = getHeight();
        g2d.setColor(Color.GRAY);
        g2d.drawString(VERSION, 8, h - 8);
    }

    /**
     * Updates the list of client computers periodically.
     */
    @Override
    public void run() {

        // Every 5 seconds, query the testing stations
        while (isVisible()) {

            final Cache cache = new Cache(this.profile);

            try {
                final List<RawClientPc> stations = RawClientPcLogic.queryByTestingCenter(cache,
                        this.testingCenterId);
                this.map.updateTestingStations(stations);
                repaint();
            } catch (final SQLException ex) {
                Log.warning(ex);
            }

            try {
                Thread.sleep(5000L);
            } catch (final InterruptedException ex) {
                Log.warning(ex);
            }
        }
    }
}
