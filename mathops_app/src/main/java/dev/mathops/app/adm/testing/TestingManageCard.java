package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.AdminPanelBase;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.TemporalUtils;
import dev.mathops.core.log.Log;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.old.Cache;
import dev.mathops.db.old.rawlogic.RawClientPcLogic;
import dev.mathops.db.old.rawrecord.RawClientPc;
import dev.mathops.session.scramsha256.ScramClientStub;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
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
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * A card panel that allows an administrator to turn blocks of testing machines or individual machines on and off.
 */
final class TestingManageCard extends AdminPanelBase implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6444481681579133884L;

    /** An action command. */
    private static final String POWER_UP = "POWER_UP";

    /** An action command. */
    private static final String POWER_DOWN = "POWER_DOWN";

    /** An action command. */
    private static final String REFRESH = "REFRESH";

    /** The data cache. */
    private final Cache cache;

    /** The list of client computers to present. */
    private final List<RawClientPc> clients;

    /** The server site URL to use when constructing a ScramClientStub. */
    private final String serverSiteUrl;

    /** The map. */
    private final TestingCenterManagePanel managePane;

    /**
     * Constructs a new {@code TestingManageCard}.
     *
     * @param theCache         the data cache
     * @param theServerSiteUrl the server site URL to use when constructing a ScramClientStub
     * @param theOwner         the owning pane
     */
    TestingManageCard(final Cache theCache, final String theServerSiteUrl, final TestingTabPane theOwner) {

        super();

        this.cache = theCache;
        this.serverSiteUrl = theServerSiteUrl;

        final JPanel panel = new JPanel(new BorderLayout(5, 5));
        panel.setBackground(Skin.OFF_WHITE_RED);
        final Border myBorder = getBorder();
        panel.setBorder(myBorder);

        setBackground(Skin.LT_RED);
        final Border padding = BorderFactory.createEmptyBorder(3, 3, 3, 3);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        setBorder(newBorder);
        add(panel, BorderLayout.CENTER);

        final JPanel top = new JPanel(new StackedBorderLayout(10, 0));
        top.setBackground(Skin.OFF_WHITE_RED);
        panel.add(top, BorderLayout.PAGE_START);
        final JLabel header = makeHeader("Manage Testing Center", false);
        top.add(header, StackedBorderLayout.WEST);

        final JButton powerRoomOn = new JButton("Power Room Up");
        powerRoomOn.setFont(Skin.BIG_BUTTON_16_FONT);
        powerRoomOn.setActionCommand(POWER_UP);
        powerRoomOn.addActionListener(this);

        final JButton powerRoomOff = new JButton("Power Room Down");
        powerRoomOff.setFont(Skin.BIG_BUTTON_16_FONT);
        powerRoomOff.setActionCommand(POWER_DOWN);
        powerRoomOff.addActionListener(this);

        final JButton refresh = new JButton("Refresh");
        refresh.setFont(Skin.BIG_BUTTON_16_FONT);
        refresh.setActionCommand(REFRESH);
        refresh.addActionListener(this);

        top.add(refresh, StackedBorderLayout.EAST);
        top.add(powerRoomOff, StackedBorderLayout.EAST);
        top.add(powerRoomOn, StackedBorderLayout.EAST);

        final JPanel center = new JPanel(new BorderLayout(10, 10));
        center.setBackground(Skin.OFF_WHITE_RED);
        panel.add(center, BorderLayout.CENTER);

        this.clients = new ArrayList<>(100);
        loadClients();

        this.managePane = new TestingCenterManagePanel(theOwner, this.clients);
        center.add(this.managePane, BorderLayout.CENTER);
    }

    /**
     * Called when the "Loan" or "Cancel" button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (POWER_UP.equals(cmd)) {
            doPowerUp();
        } else if (POWER_DOWN.equals(cmd)) {
            if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Power Room Down",
                    JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
                doPowerDown();
            }
        } else if (REFRESH.equals(cmd)) {
            this.managePane.refresh();
        }
    }

    /**
     * Power up all stations that are not "Paper only" usage.
     */
    private void doPowerUp() {

        final LocalTime now = LocalTime.now();
        final int recent = TemporalUtils.secondOfDay(now) - 40;
        final ScramClientStub stub = new ScramClientStub(this.serverSiteUrl);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];

        final String handshakeError = stub.handshake("sbenoit", "thinflation");
        if (handshakeError == null) {
            try {
                final List<RawClientPc> stations = RawClientPcLogic.INSTANCE.queryAll(this.cache);

                for (final RawClientPc station : stations) {

                    if (RawClientPc.USAGE_PAPER.equals(station.pcUsage)) {
                        // Skip stations with "paper" usage when powering up room
                        continue;
                    }

                    if (RawClientPc.POWER_REPORTING_ON.equals(station.powerStatus)
                            && station.lastPing != null && station.lastPing.intValue() > recent) {
                        // Station is already on and reporting - skip
                        continue;
                    }

                    final String center = station.testingCenterId;
                    if ("1".equals(center) || "4".equals(center)) {
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
                    }

                    try {
                        Thread.sleep(200L);
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
    }

    /**
     * Power down all stations that are not currently in use.
     */
    private void doPowerDown() {

        Log.info("POWERING DOWN!");

        final ScramClientStub stub = new ScramClientStub(this.serverSiteUrl);
        final ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        final byte[] buffer = new byte[1024];

        final String handshakeError = stub.handshake("sbenoit", "thinflation");
        if (handshakeError == null) {
            try {
                final List<RawClientPc> stations = RawClientPcLogic.INSTANCE.queryAll(this.cache);

                for (final RawClientPc station : stations) {
                    if (station.currentStuId == null) {
                        final String center = station.testingCenterId;
                        if ("1".equals(center) || "4".equals(center)) {
                            Log.info("Powering off ", station.stationNbr);

                            try {
                                final URI uri = new URI(this.serverSiteUrl + "testing-power-station-off.ws?token="
                                        + stub.getToken() + "&computer-id=" + station.computerId);
                                final URL url = uri.toURL();

                                final URLConnection conn = url.openConnection();
                                final Object content = conn.getContent();
                                if (content == null) {
                                    Log.warning("Server response from 'testing-power-station-off.ws' was null");
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
                                    Log.warning("Server response from 'testing-power-station-off.ws' was ",
                                            contentClassName);
                                }
                            } catch (final URISyntaxException | IOException ex) {
                                Log.warning(ex);
                            }
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
    }

    /**
     * Refreshes the card.
     */
    void refresh() {

        loadClients();
        this.managePane.refresh();
    }

    /**
     * Loads the list of clients.
     */
    private void loadClients() {

        synchronized (this.clients) {
            this.clients.clear();

            try {
                final List<RawClientPc> stations = RawClientPcLogic.INSTANCE.queryAll(this.cache);

                for (final RawClientPc station : stations) {
                    final String center = station.testingCenterId;
                    if ("1".equals(center) || "4".equals(center)) {
                        this.clients.add(station);
                    }
                }
            } catch (final SQLException ex) {
                Log.warning("Failed to query client PC records", ex);
            }
        }
    }
}
