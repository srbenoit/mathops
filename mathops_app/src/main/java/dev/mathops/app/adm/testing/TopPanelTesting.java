package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdmMainWindow;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.TemporalUtils;
import dev.mathops.commons.builder.SimpleBuilder;
import dev.mathops.commons.log.Log;
import dev.mathops.db.Cache;
import dev.mathops.db.logic.SystemData;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.session.scramsha256.ScramClientStub;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalTime;
import java.util.List;

/**
 * The "Testing" pane.
 */
public final class TopPanelTesting extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String MAP_CMD = "MAP";

    /** A button action command. */
    private static final String MANAGE_CMD = "MANAGE";

    /** A button action command. */
    private static final String ISSUE_CMD = "ISSUE";

    /** A button action command. */
    private static final String CANCEL_CMD = "CANCEL";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9054536124109728232L;

    /** The data cache. */
    private final Cache cache;

    /** The server site URL to use when constructing a ScramClientStub. */
    private final String serverSiteUrl;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The "map" card. */
    private final TestingMapCard mapCard;

    /** The "manage" card. */
    private final TestingManageCard manageCard;

    /** The "issue" card. */
    private final TestingIssueCard issueCard;

    /** The "issue" card. */
    private final TestingCancelCard cancelCard;

    /**
     * Constructs a new {@code TestingTabPane}.
     *
     * @param theCache         the data cache
     * @param theServerSiteUrl the server site URL to use when constructing a ScramClientStub
     * @param fixed            the fixed data
     * @param theFrame         the owning frame
     */
    public TopPanelTesting(final Cache theCache, final String theServerSiteUrl, final FixedData fixed,
                           final JFrame theFrame) {

        // Functions:
        // [ Map ]
        // [ Manage ]
        // [ Issue ]

        super(new BorderLayout(5, 5));
        setPreferredSize(AdmMainWindow.PREF_SIZE);

        this.cache = theCache;
        this.serverSiteUrl = theServerSiteUrl;

        setBackground(Skin.OFF_WHITE_GRAY);
        final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final Border etching = BorderFactory.createEtchedBorder();
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(etching, padding);
        setBorder(newBorder);

        final JPanel menu = new JPanel();
        menu.setBackground(Skin.OFF_WHITE_GRAY);
        menu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));

        if (fixed.getClearanceLevel("TST_MAP") != null) {
            final JPanel mapButton = makeTopButton("Map", MAP_CMD, Skin.LT_GREEN);
            menu.add(mapButton);
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("TST_MANAG") != null) {
            final JPanel manageButton = makeTopButton("Manage", MANAGE_CMD, Skin.LT_RED);
            menu.add(manageButton);
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("TST_ISSUE") != null) {
            final JPanel issueExamButton = makeTopButton("Issue Exam", ISSUE_CMD, Skin.LT_CYAN);
            menu.add(issueExamButton);
            menu.add(new JLabel(CoreConstants.SPC));

            final JPanel cancelExamButton = makeTopButton("Cancel Exam", CANCEL_CMD, Skin.LT_MAGENTA);
            menu.add(cancelExamButton);
            menu.add(new JLabel(CoreConstants.SPC));
        }

        add(menu, BorderLayout.PAGE_START);

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.OFF_WHITE_GREEN);
        add(this.cardPane, BorderLayout.CENTER);

        this.mapCard = new TestingMapCard(theCache);
        this.cardPane.add(this.mapCard, MAP_CMD);

        this.manageCard = new TestingManageCard(theCache, theServerSiteUrl, this);
        this.cardPane.add(this.manageCard, MANAGE_CMD);

        this.issueCard = new TestingIssueCard(theCache, theFrame);
        this.cardPane.add(this.issueCard, ISSUE_CMD);

        this.cancelCard = new TestingCancelCard(theCache);
        this.cardPane.add(this.cancelCard, CANCEL_CMD);

        this.cards.show(this.cardPane, MAP_CMD);
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title            the button title
     * @param command          the action command
     * @param background       the background color
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background) {

        final JButton btn = new JButton(title);
        btn.setActionCommand(command);
        btn.addActionListener(this);
        btn.setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        final Border padding = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        menuBox.setBorder(newBorder);
        menuBox.add(btn, BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // No action
    }

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    public void clearDisplay() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (MAP_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, MAP_CMD);
            this.mapCard.refresh();
        } else if (MANAGE_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, MANAGE_CMD);
            this.manageCard.refresh();
        } else if (ISSUE_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, ISSUE_CMD);
            this.issueCard.reset();
        } else if (CANCEL_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, CANCEL_CMD);
            this.cancelCard.reset();
        }
    }

    /**
     * Disables (sets usage to paper-only) a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    void disableStations(final String[] stationIds) {

        try (final Statement stmt = this.cache.conn.createStatement()) {

            for (final String id : stationIds) {

                final String sql1 = SimpleBuilder.concat(
                        "UPDATE client_pc SET pc_usage='P' WHERE testing_center_id='1' AND station_nbr='", id,
                        "' and pc_usage = 'O'");

                final String sql2 = SimpleBuilder.concat(
                        "UPDATE client_pc SET current_status='5' WHERE testing_center_id='1' AND station_nbr='", id,
                        "' AND current_status='4'");

                stmt.executeUpdate(sql1);
                stmt.executeUpdate(sql2);
            }

            this.cache.conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Enables (sets usage to Online) on a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    void enableStations(final String[] stationIds) {

        try (final Statement stmt = this.cache.conn.createStatement()) {

            for (final String id : stationIds) {

                final String sql1 = SimpleBuilder.concat(
                        "UPDATE client_pc SET pc_usage='O' WHERE testing_center_id='1' AND station_nbr='", id,
                        "' AND pc_usage = 'P'");

                final String sql2 = SimpleBuilder.concat(
                        "UPDATE client_pc SET current_status='4' WHERE testing_center_id='1' AND station_nbr='", id,
                        "' AND current_status='5'");

                stmt.executeUpdate(sql1);
                stmt.executeUpdate(sql2);
            }

            this.cache.conn.commit();
        } catch (final SQLException ex) {
            Log.warning(ex);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Powers on a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    void powerOnStations(final String[] stationIds) {

        final LocalTime now = LocalTime.now();
        final int recent = TemporalUtils.secondOfDay(now) - 40;

        final ScramClientStub stub = new ScramClientStub(this.serverSiteUrl);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];

        final String handshakeError = stub.handshake("sbenoit", "thinflation");
        if (handshakeError == null) {
            try {
                final RawClientPc[] toPowerOn = findStations(stationIds);

                // Power those stations on
                for (final RawClientPc station : toPowerOn) {

                    if (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)
                            && station.lastPing != null && station.lastPing.intValue() > recent) {
                        // Station is already on and reporting - skip
                        continue;
                    }

                    Log.info("Powering on ", station.stationNbr);

                    try {
                        final URI uri = new URI(this.serverSiteUrl + "testing-power-station-on.ws?token="
                                + stub.getToken() + "&computer-id=" + station.computerId);
                        final URL url = uri.toURL();

                        final URLConnection conn = url.openConnection();
                        final Object content = conn.getContent();
                        if (content == null) {
                            Log.warning("Server response from 'testing-power-station-on.ws' was null");
                        } else if (content instanceof InputStream) {
                            try (final InputStream in = (InputStream) content) {
                                baos.reset();
                                int count = in.read(buffer);
                                while (count > 0) {
                                    baos.write(buffer, 0, count);
                                    count = in.read(buffer);
                                }

                                final String result = baos.toString();
                                if (!"OK".equals(result)) {
                                    Log.info(result);
                                }
                            }
                        } else {
                            final Class<?> contentClass = content.getClass();
                            final String contentClassName = contentClass.getName();
                            Log.warning("Server response from 'testing-power-station-on.ws' was ", contentClassName);
                        }
                    } catch (final URISyntaxException | IOException ex) {
                        Log.warning(ex);
                    }

                    try {
                        Thread.sleep(100L);
                    } catch (final InterruptedException ex) {
                        Log.warning(ex);
                        Thread.currentThread().interrupt();
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query testing stations.", ex);
            }
        } else {
            Log.info("Web services handshake error: ", handshakeError);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Powers off a list of stations by ID.
     *
     * @param stationIds the list of station IDs
     */
    void powerOffStations(final String[] stationIds) {

        final ScramClientStub stub = new ScramClientStub(this.serverSiteUrl);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];

        final String handshakeError = stub.handshake("sbenoit", "thinflation");
        if (handshakeError == null) {
            try {
                final RawClientPc[] toPowerOff = findStations(stationIds);

                // Power those stations off (skip any that are currently in use)
                for (final RawClientPc station : toPowerOff) {

                    if (station.currentStuId == null) {
                        Log.info("Powering off ", station.stationNbr);

                        try {
                            final URI uri = new URI(this.serverSiteUrl + "testing-power-station-off.ws?token="
                                    + stub.getToken() + "&computer-id=" + station.computerId);
                            final URL url = uri.toURL();

                            final URLConnection conn = url.openConnection();
                            final Object content = conn.getContent();
                            if (content == null) {
                                Log.warning(
                                        "Server response from 'testing-power-station-off.ws' was null");
                            } else if (content instanceof InputStream) {
                                try (final InputStream in = (InputStream) content) {
                                    baos.reset();
                                    int count = in.read(buffer);
                                    while (count > 0) {
                                        baos.write(buffer, 0, count);
                                        count = in.read(buffer);
                                    }

                                    final String result = baos.toString();
                                    if (!"OK".equals(result)) {
                                        Log.info(result);
                                    }
                                }
                            } else {
                                final Class<?> contentClass = content.getClass();
                                final String contentClassName = contentClass.getName();
                                Log.warning("Server response from 'testing-power-station-off.ws' was ", contentClassName);
                            }
                        } catch (final URISyntaxException | IOException ex) {
                            Log.warning(ex);
                        }
                    } else {
                        Log.warning("Skipping station ", station.stationNbr, " (currently in use)");
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query testing stations.", ex);
            }
        } else {
            Log.info("Web services handshake error: ", handshakeError);
        }

        this.mapCard.refresh();
        this.manageCard.refresh();
    }

    /**
     * Given a list of station IDs, assumed to be in testing centers 1 (main) or 4 (quiet testing), retrieves the {@code
     * RawClientPc} records for each and returns that list.
     *
     * @param stationIds the station numbers
     * @return the array of corresponding {@code RawClientPc} records (if a station number cannot be found, this array
     *         will have {@code null} at the corresponding index)
     * @throws SQLException if there is an error accessing the database
     */
    private RawClientPc[] findStations(final String[] stationIds) throws SQLException {

        final int numStations = stationIds.length;
        final RawClientPc[] result = new RawClientPc[numStations];

        final SystemData systemData = this.cache.getSystemData();
        final List<RawClientPc> stations = systemData.getClientPcs();

        for (int i = 0; i < numStations; ++i) {
            for (final RawClientPc station : stations) {
                final String center = station.testingCenterId;
                if (station.stationNbr.equals(stationIds[i]) && ("1".equals(center) || "4".equals(center))) {
                    result[i] = station;
                    break;
                }
            }
        }

        return result;
    }
}
