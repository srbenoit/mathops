package dev.mathops.app.checkout;

import dev.mathops.app.checkin.TestingCenterMap;
import dev.mathops.core.log.Log;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.DbConnection;
import dev.mathops.db.old.DbContext;
import dev.mathops.db.old.cfg.DbProfile;
import dev.mathops.db.old.cfg.ESchemaUse;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
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
 * A panel to appear in the center of the check-in application. This panel displays a map of the testing center,
 * showing the status of all stations, and is used for buttons when selecting course/unit for exams.
 */
final class CenterPanel extends JPanel implements Runnable {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -7029995559760361198L;

    /** Application version. */
    private static final String VERSION = "v2.3.5 (Jan 1, 2024)";

    /** The database profile. */
    private final DbProfile dbProfile;

    /** The testing center ID being managed. */
    private final String testingCenterId;

    /** The testing center map. */
    private final TestingCenterMap map;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /**
     * Constructs a new {@code CenterPanel}.
     *
     * @param theDbProfile       the database profile
     * @param theTestingCenterId the testing center ID being managed
     */
    CenterPanel(final DbProfile theDbProfile, final String theTestingCenterId) {

        super();

        // NOTE: This constructor is called from the GUI builder in the main application, which
        // runs in the AWT thread, so we are safe to do AWT operations.

        this.dbProfile = theDbProfile;
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

            final DbContext ctx = this.dbProfile.getDbContext(ESchemaUse.PRIMARY);
            try {
                final DbConnection conn = ctx.checkOutConnection();
                final Cache cache = new Cache(this.dbProfile, conn);

                try {
                    final List<RawClientPc> stations = RawClientPcLogic.queryByTestingCenter(cache,
                            this.testingCenterId);
                    this.map.updateTestingStations(stations);
                    repaint();
                } finally {
                    ctx.checkInConnection(conn);
                }
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
