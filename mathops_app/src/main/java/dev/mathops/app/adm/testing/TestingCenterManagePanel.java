package dev.mathops.app.adm.testing;

import dev.mathops.app.adm.Skin;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.db.old.rawrecord.RawClientPc;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.Serial;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A panel that draws a map of the testing center with regions defined
 */
final class TestingCenterManagePanel extends JPanel implements ComponentListener, ActionListener {

    /** Station IDs in the "east wall" zone. */
    private static final String[] E_WALL_STATIONS = {
            "7", "8", "9", "10",
            "11", "12", "13", "14",
            "15", "16", "17", "18"};

    /** Station IDs in the "east main 1" zone. */
    private static final String[] E_MAIN1_STATIONS = {
            "37", "38", "39", "40",
            "41", "42", "43", "44",
            "45", "46", "47", "48",
            "49", "50"};

    /** Station IDs in the "east main 2" zone. */
    private static final String[] E_MAIN2_STATIONS = {
            "19", "20", "21", "22",
            "23", "24", "25", "26",
            "27", "28", "29", "30",
            "31", "32"};

    /** Station IDs in the "east aisle" zone. */
    private static final String[] E_AISLE_STATIONS = {
            "51", "52", "53", "54",
            "55", "56", "57", "58"};

    /** Station IDs in the "west aisle" zone. */
    private static final String[] W_AISLE_STATIONS = {
            "59", "60", "61", "62",
            "63", "64", "65", "66",
            "67", "68"};

    /** Station IDs in the "west main" zone. */
    private static final String[] W_MAIN_STATIONS = {
            "69", "70", "71", "72",
            "73", "74", "75", "76",
            "77", "78", "79", "80",
            "81", "82"};

    /** Station IDs in the "west wall" zone. */
    private static final String[] W_WALL_STATIONS = {
            "85", "86", "87", "88",
            "90", "91", "94", "95",
            "97", "98", "99", "100"};

    /** Station IDs in the "west wall center" zone. */
    private static final String[] W_WALL_CENTER_STATIONS = {
            "89", "92", "93", "96"};

    /** Station IDs in the "last 1" zone. */
    private static final String[] LAST1_STATIONS = {
            "33", "34", "35", "36",
            "83", "84"};

    /** Station IDs in the "last 2" zone. */
    private static final String[] LAST2_STATIONS = {
            "3", "4"};

    /** A commonly used string. */
    private static final String ENABLE = "Enable";

    /** A commonly used string. */
    private static final String DISABLE = "Disable";

    /** A commonly used string. */
    private static final String POWER_ON = "Power On";

    /** A commonly used string. */
    private static final String POWER_OFF = "Power Off";

    /** An action command. */
    private static final String E_WALL_DISABLE = "E_WALL_DISABLE";

    /** An action command. */
    private static final String E_WALL_ENABLE = "E_WALL_ENABLE";

    /** An action command. */
    private static final String E_WALL_POWER_ON = "E_WALL_POWER_ON";

    /** An action command. */
    private static final String E_WALL_POWER_OFF = "E_WALL_POWER_OFF";

    /** An action command. */
    private static final String E_MAIN1_DISABLE = "E_MAIN1_DISABLE";

    /** An action command. */
    private static final String E_MAIN1_ENABLE = "E_MAIN1_ENABLE";

    /** An action command. */
    private static final String E_MAIN1_POWER_ON = "E_MAIN1_POWER_ON";

    /** An action command. */
    private static final String E_MAIN1_POWER_OFF = "E_MAIN1_POWER_OFF";

    /** An action command. */
    private static final String E_MAIN2_DISABLE = "E_MAIN2_DISABLE";

    /** An action command. */
    private static final String E_MAIN2_ENABLE = "E_MAIN2_ENABLE";

    /** An action command. */
    private static final String E_MAIN2_POWER_ON = "E_MAIN2_POWER_ON";

    /** An action command. */
    private static final String E_MAIN2_POWER_OFF = "E_MAIN2_POWER_OFF";

    /** An action command. */
    private static final String E_AISLE_DISABLE = "E_AISLE_DISABLE";

    /** An action command. */
    private static final String E_AISLE_ENABLE = "E_AISLE_ENABLE";

    /** An action command. */
    private static final String E_AISLE_POWER_ON = "E_AISLE_POWER_ON";

    /** An action command. */
    private static final String E_AISLE_POWER_OFF = "E_AISLE_POWER_OFF";

    /** An action command. */
    private static final String W_AISLE_DISABLE = "W_AISLE_DISABLE";

    /** An action command. */
    private static final String W_AISLE_ENABLE = "W_AISLE_ENABLE";

    /** An action command. */
    private static final String W_AISLE_POWER_ON = "W_AISLE_POWER_ON";

    /** An action command. */
    private static final String W_AISLE_POWER_OFF = "W_AISLE_POWER_OFF";

    /** An action command. */
    private static final String W_MAIN_DISABLE = "W_MAIN_DISABLE";

    /** An action command. */
    private static final String W_MAIN_ENABLE = "W_MAIN_ENABLE";

    /** An action command. */
    private static final String W_MAIN_POWER_ON = "W_MAIN_POWER_ON";

    /** An action command. */
    private static final String W_MAIN_POWER_OFF = "W_MAIN_POWER_OFF";

    /** An action command. */
    private static final String W_WALL_DISABLE = "W_WALL_DISABLE";

    /** An action command. */
    private static final String W_WALL_ENABLE = "W_WALL_ENABLE";

    /** An action command. */
    private static final String W_WALL_POWER_ON = "W_WALL_POWER_ON";

    /** An action command. */
    private static final String W_WALL_POWER_OFF = "W_WALL_POWER_OFF";

    /** An action command. */
    private static final String W_WALL_CENTER_DISABLE = "W_WALL_CENTER_DISABLE";

    /** An action command. */
    private static final String W_WALL_CENTER_ENABLE = "W_WALL_CENTER_ENABLE";

    /** An action command. */
    private static final String W_WALL_CENTER_POWER_ON = "W_WALL_CENTER_POWER_ON";

    /** An action command. */
    private static final String W_WALL_CENTER_POWER_OFF = "W_WALL_CENTER_POWER_OFF";

    /** An action command. */
    private static final String LAST1_DISABLE = "LAST1_DISABLE";

    /** An action command. */
    private static final String LAST1_ENABLE = "LAST1_ENABLE";

    /** An action command. */
    private static final String LAST1_POWER_ON = "LAST1_POWER_ON";

    /** An action command. */
    private static final String LAST1_POWER_OFF = "LAST1_POWER_OFF";

    /** An action command. */
    private static final String LAST2_DISABLE = "LAST2_DISABLE";

    /** An action command. */
    private static final String LAST2_ENABLE = "LAST2_ENABLE";

    /** An action command. */
    private static final String LAST2_POWER_ON = "LAST2_POWER_ON";

    /** An action command. */
    private static final String LAST2_POWER_OFF = "LAST2_POWER_OFF";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 3966709797464460260L;

    /** The owning tab pane. */
    private final TopPanelTesting owner;

    /** The list of clients to render. */
    private final List<RawClientPc> clients;

    /** The color in which to fill in the outline of the map. */
    private final Color mapFill;

    /** The color in which to draw the outline of the map. */
    private final Color mapOutline;

    /** The color in which to draw the tables. */
    private final Color tableColor;

    /** The color in which to draw the outline of the tables. */
    private final Color tableOutline;

    /** The color in which to draw the PCs in an error state. */
    private final Color pcErrorColor;

    /** The color in which to draw the PCs in a warning state. */
    private final Color pcWarningColor;

    /** The color in which to draw the PCs in a locked state. */
    private final Color pcLockedColor;

    /** The color in which to draw the PCs in a paper-only state. */
    private final Color pcPaperColor;

    /** The color in which to draw the PCs in an await-student state. */
    private final Color pcAwaitColor;

    /** The color in which to draw the PCs in an exam-taking state. */
    private final Color pcInExamColor;

    /** The color for PC outlines. */
    private final Color pcOutline;

    /** The fill color for zone regions. */
    private final Color zoneFill;

    /** The outline color for zone regions. */
    private final Color zoneOutline;

    /** The color in which to draw numbers on the PCs. */
    private final Color pcNumber;

    /** The color for a "powered on" badge. */
    private final Color onColor;

    /** The color for a "powered off" badge. */
    private final Color offColor;

    /** The color for a "powering on" badge. */
    private final Color poweringColor;

    // /** The color for an "unknown" badge. */
    // private final Color unknownColor;

    /** The font in which to draw numbers on the PCs. */
    private final Font pcFont;

    /** The font in which to draw legend entries. */
    private final Font legendFont;

    /** Hamburger button to open "east wall" menu. */
    private final JButton eastWallHamburger;

    /** Menu item to enable the east wall. */
    private final JMenuItem eastWallEnable;

    /** Menu item disable the east wall. */
    private final JMenuItem eastWallDisable;

    /** Hamburger button to open "east main 1" menu. */
    private final JButton eastMain1Hamburger;

    /** Menu item to enable the east main 1. */
    private final JMenuItem eastMain1Enable;

    /** Menu item to disable the east main 1. */
    private final JMenuItem eastMain1Disable;

    /** Hamburger button to open "east main 2" menu. */
    private final JButton eastMain2Hamburger;

    /** Menu item to enable the east main 2. */
    private final JMenuItem eastMain2Enable;

    /** Menu item to disable the east main 2. */
    private final JMenuItem eastMain2Disable;

    /** Hamburger button to open "east aisle" menu. */
    private final JButton eastAisleHamburger;

    /** Menu item to enable the east aisle. */
    private final JMenuItem eastAisleEnable;

    /** Menu item to disable the east aisle. */
    private final JMenuItem eastAisleDisable;

    /** Hamburger button to open "west aisle" menu. */
    private final JButton westAisleHamburger;

    /** Menu item to enable the west aisle. */
    private final JMenuItem westAisleEnable;

    /** Menu item to disable the west aisle. */
    private final JMenuItem westAisleDisable;

    /** Hamburger button to open "west main" menu. */
    private final JButton westMainHamburger;

    /** Menu item to enable the west main. */
    private final JMenuItem westMainEnable;

    /** Menu item to disable the west main. */
    private final JMenuItem westMainDisable;

    /** Hamburger button to open "west wall" menu. */
    private final JButton westWallHamburger;

    /** Menu item to enable the west wall. */
    private final JMenuItem westWallEnable;

    /** Menu item to disable the west wall. */
    private final JMenuItem westWallDisable;

    /** Hamburger button to open "west wall center" menu. */
    private final JButton westWallCenterHamburger;

    /** Menu item to enable the west wall center. */
    private final JMenuItem westWallCenterEnable;

    /** Menu item to disable the west wall center. */
    private final JMenuItem westWallCenterDisable;

    /** Hamburger button to open "last 1" menu. */
    private final JButton last1Hamburger;

    /** Menu item to enable the last 1 zone. */
    private final JMenuItem last1Enable;

    /** Menu item to disable the last 1 zone. */
    private final JMenuItem last1Disable;

    /** Hamburger button to open "last 2" menu. */
    private final JButton last2Hamburger;

    /** Menu item to enable the last 2 zone. */
    private final JMenuItem last2Enable;

    /** Menu item to disable the last 2 zone. */
    private final JMenuItem last2Disable;

    /**
     * Constructs a new {@code TestingCenterMapPanel}.
     *
     * @param theOwner         the owning tab pane
     * @param theClients       the list of clients (access to this list should be synchronized on the list)
     */
    TestingCenterManagePanel(final TopPanelTesting theOwner,
                             final List<RawClientPc> theClients) {

        super(null);

        this.owner = theOwner;
        this.clients = theClients;

        setBackground(Skin.OFF_WHITE_RED);

        ImageIcon icon = null;
        final Class<? extends TestingCenterManagePanel> aClass = getClass();
        final byte[] iconBytes = FileLoader.loadFileAsBytes(aClass, "gear.png", true);
        if (iconBytes != null) {
            icon = new ImageIcon(iconBytes);
        }

        setLayout(null);
        setFocusable(true);
        setDoubleBuffered(true);

        this.mapFill = new Color(240, 240, 240);
        this.mapOutline = Color.black;
        this.tableColor = new Color(205, 112, 84);
        this.tableOutline = new Color(139, 0, 0);

        this.pcErrorColor = new Color(235, 150, 150);
        this.pcWarningColor = new Color(212, 155, 240);
        this.pcLockedColor = new Color(250, 240, 160);
        this.pcPaperColor = new Color(214, 214, 214);
        this.pcAwaitColor = new Color(135, 227, 230);
        this.pcInExamColor = new Color(172, 230, 172);

        this.pcOutline = Color.black;
        this.pcNumber = Color.black;

        this.onColor = Color.GREEN;
        this.offColor = Color.RED;
        this.poweringColor = Color.ORANGE;
        // this.unknownColor = Color.GRAY;

        this.zoneFill = new Color(144, 137, 199, 100);
        this.zoneOutline = new Color(144, 137, 199, 180);

        this.pcFont = new Font(Font.SANS_SERIF, Font.BOLD, 14);
        this.legendFont = new Font(Font.SANS_SERIF, Font.PLAIN, 18);

        //

        this.eastWallEnable = makeMenuItem(ENABLE, E_WALL_ENABLE);
        this.eastWallDisable = makeMenuItem(DISABLE, E_WALL_DISABLE);
        final JMenuItem eastWallPowerOn = makeMenuItem(POWER_ON, E_WALL_POWER_ON);
        final JMenuItem eastWallPowerOff = makeMenuItem(POWER_OFF, E_WALL_POWER_OFF);

        final JPopupMenu eastWallPopup = new JPopupMenu();
        eastWallPopup.add(this.eastWallEnable);
        eastWallPopup.add(this.eastWallDisable);
        eastWallPopup.add(eastWallPowerOn);
        eastWallPopup.add(eastWallPowerOff);

        this.eastWallHamburger = makeContextMenu(eastWallPopup, icon);
        add(this.eastWallHamburger);

        //

        this.eastMain1Enable = makeMenuItem(ENABLE, E_MAIN1_ENABLE);
        this.eastMain1Disable = makeMenuItem(DISABLE, E_MAIN1_DISABLE);
        final JMenuItem eastMain1PowerOn = makeMenuItem(POWER_ON, E_MAIN1_POWER_ON);
        final JMenuItem eastMain1PowerOff = makeMenuItem(POWER_OFF, E_MAIN1_POWER_OFF);

        final JPopupMenu eastMain1Popup = new JPopupMenu();
        eastMain1Popup.add(this.eastMain1Enable);
        eastMain1Popup.add(this.eastMain1Disable);
        eastMain1Popup.add(eastMain1PowerOn);
        eastMain1Popup.add(eastMain1PowerOff);

        this.eastMain1Hamburger = makeContextMenu(eastMain1Popup, icon);
        add(this.eastMain1Hamburger);

        //

        this.eastMain2Enable = makeMenuItem(ENABLE, E_MAIN2_ENABLE);
        this.eastMain2Disable = makeMenuItem(DISABLE, E_MAIN2_DISABLE);
        final JMenuItem eastMain2PowerOn = makeMenuItem(POWER_ON, E_MAIN2_POWER_ON);
        final JMenuItem eastMain2PowerOff = makeMenuItem(POWER_OFF, E_MAIN2_POWER_OFF);

        final JPopupMenu eastMain2Popup = new JPopupMenu();
        eastMain2Popup.add(this.eastMain2Enable);
        eastMain2Popup.add(this.eastMain2Disable);
        eastMain2Popup.add(eastMain2PowerOn);
        eastMain2Popup.add(eastMain2PowerOff);

        this.eastMain2Hamburger = makeContextMenu(eastMain2Popup, icon);
        add(this.eastMain2Hamburger);

        //

        this.eastAisleEnable = makeMenuItem(ENABLE, E_AISLE_ENABLE);
        this.eastAisleDisable = makeMenuItem(DISABLE, E_AISLE_DISABLE);
        final JMenuItem eastAislePowerOn = makeMenuItem(POWER_ON, E_AISLE_POWER_ON);
        final JMenuItem eastAislePowerOff = makeMenuItem(POWER_OFF, E_AISLE_POWER_OFF);

        final JPopupMenu eastAislePopup = new JPopupMenu();
        eastAislePopup.add(this.eastAisleEnable);
        eastAislePopup.add(this.eastAisleDisable);
        eastAislePopup.add(eastAislePowerOn);
        eastAislePopup.add(eastAislePowerOff);

        this.eastAisleHamburger = makeContextMenu(eastAislePopup, icon);
        add(this.eastAisleHamburger);

        //

        this.westAisleEnable = makeMenuItem(ENABLE, W_AISLE_ENABLE);
        this.westAisleDisable = makeMenuItem(DISABLE, W_AISLE_DISABLE);
        final JMenuItem westAislePowerOn = makeMenuItem(POWER_ON, W_AISLE_POWER_ON);
        final JMenuItem westAislePowerOff = makeMenuItem(POWER_OFF, W_AISLE_POWER_OFF);

        final JPopupMenu westAislePopup = new JPopupMenu();
        westAislePopup.add(this.westAisleEnable);
        westAislePopup.add(this.westAisleDisable);
        westAislePopup.add(westAislePowerOn);
        westAislePopup.add(westAislePowerOff);

        this.westAisleHamburger = makeContextMenu(westAislePopup, icon);
        add(this.westAisleHamburger);

        //

        this.westMainEnable = makeMenuItem(ENABLE, W_MAIN_ENABLE);
        this.westMainDisable = makeMenuItem(DISABLE, W_MAIN_DISABLE);
        final JMenuItem westMainPowerOn = makeMenuItem(POWER_ON, W_MAIN_POWER_ON);
        final JMenuItem westMainPowerOff = makeMenuItem(POWER_OFF, W_MAIN_POWER_OFF);

        final JPopupMenu westMainPopup = new JPopupMenu();
        westMainPopup.add(this.westMainEnable);
        westMainPopup.add(this.westMainDisable);
        westMainPopup.add(westMainPowerOn);
        westMainPopup.add(westMainPowerOff);

        this.westMainHamburger = makeContextMenu(westMainPopup, icon);
        add(this.westMainHamburger);

        //

        this.westWallEnable = makeMenuItem(ENABLE, W_WALL_ENABLE);
        this.westWallDisable = makeMenuItem(DISABLE, W_WALL_DISABLE);
        final JMenuItem westWallPowerOn = makeMenuItem(POWER_ON, W_WALL_POWER_ON);
        final JMenuItem westWallPowerOff = makeMenuItem(POWER_OFF, W_WALL_POWER_OFF);

        final JPopupMenu westWallPopup = new JPopupMenu();
        westWallPopup.add(this.westWallEnable);
        westWallPopup.add(this.westWallDisable);
        westWallPopup.add(westWallPowerOn);
        westWallPopup.add(westWallPowerOff);

        this.westWallHamburger = makeContextMenu(westWallPopup, icon);
        add(this.westWallHamburger);

        //

        this.westWallCenterEnable = makeMenuItem(ENABLE, W_WALL_CENTER_ENABLE);
        this.westWallCenterDisable = makeMenuItem(DISABLE, W_WALL_CENTER_DISABLE);
        final JMenuItem westWallCenterPowerOn = makeMenuItem(POWER_ON, W_WALL_CENTER_POWER_ON);
        final JMenuItem westWallCenterPowerOff = makeMenuItem(POWER_OFF, W_WALL_CENTER_POWER_OFF);

        final JPopupMenu westWallCenterPopup = new JPopupMenu();
        westWallCenterPopup.add(this.westWallCenterEnable);
        westWallCenterPopup.add(this.westWallCenterDisable);
        westWallCenterPopup.add(westWallCenterPowerOn);
        westWallCenterPopup.add(westWallCenterPowerOff);

        this.westWallCenterHamburger = makeContextMenu(westWallCenterPopup, icon);
        add(this.westWallCenterHamburger);

        //

        this.last1Enable = makeMenuItem(ENABLE, LAST1_ENABLE);
        this.last1Disable = makeMenuItem(DISABLE, LAST1_DISABLE);
        final JMenuItem last1PowerOn = makeMenuItem(POWER_ON, LAST1_POWER_ON);
        final JMenuItem last1PowerOff = makeMenuItem(POWER_OFF, LAST1_POWER_OFF);

        final JPopupMenu last1Popup = new JPopupMenu();
        last1Popup.add(this.last1Enable);
        last1Popup.add(this.last1Disable);
        last1Popup.add(last1PowerOn);
        last1Popup.add(last1PowerOff);

        this.last1Hamburger = makeContextMenu(last1Popup, icon);
        add(this.last1Hamburger);

        //
        //

        this.last2Enable = makeMenuItem(ENABLE, LAST2_ENABLE);
        this.last2Disable = makeMenuItem(DISABLE, LAST2_DISABLE);
        final JMenuItem last2PowerOn = makeMenuItem(POWER_ON, LAST2_POWER_ON);
        final JMenuItem last2PowerOff = makeMenuItem(POWER_OFF, LAST2_POWER_OFF);

        final JPopupMenu last2Popup = new JPopupMenu();
        last2Popup.add(this.last2Enable);
        last2Popup.add(this.last2Disable);
        last2Popup.add(last2PowerOn);
        last2Popup.add(last2PowerOff);

        this.last2Hamburger = makeContextMenu(last2Popup, icon);
        add(this.last2Hamburger);

        addComponentListener(this);
    }

    /**
     * Creates a menu item for a popup context menu.
     *
     * @param label the menu item label
     * @param cmd   the action command
     * @return the menu item
     */
    private JMenuItem makeMenuItem(final String label, final String cmd) {

        final JMenuItem item = new JMenuItem(label);
        item.setActionCommand(cmd);
        item.addActionListener(this);

        return item;
    }

    /**
     * Constructs a context menu.
     *
     * @param popup            the popup menu that will appear when the button is pressed
     * @param icon             the button icon
     * @return the context menu button
     */
    private static JButton makeContextMenu(final JPopupMenu popup, final ImageIcon icon) {

        final JButton result = icon == null ? new JButton(CoreConstants.SPC) : new JButton(icon);

        result.setBackground(Skin.WHITE);
        result.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(final MouseEvent e) {

                popup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        return result;
    }

    /**
     * Paints the panel.
     */
    @Override
    public void paintComponent(final Graphics g) {

        synchronized (this.clients) {
            final int count = this.clients.size();

            // Before we call super, update status of "ON/OFF" buttons, so they get drawn properly.
            final Map<String, Boolean> clientStatus = new HashMap<>(count);

            for (final RawClientPc pc : this.clients) {
                final Integer status = pc.currentStatus;
                final boolean on = RawClientPc.STATUS_FORCE_SUBMIT.equals(status)
                        || RawClientPc.STATUS_AWAIT_STUDENT.equals(status)
                        || RawClientPc.STATUS_TAKING_EXAM.equals(status)
                        || RawClientPc.STATUS_EXAM_RESULTS.equals(status)
                        || RawClientPc.STATUS_LOCKED.equals(status);
                clientStatus.put(pc.stationNbr, Boolean.valueOf(on));
            }

            // East wall
            int eastWallOn = 0;
            for (final String test : E_WALL_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++eastWallOn;
                }
            }
            this.eastWallEnable.setEnabled(eastWallOn < E_WALL_STATIONS.length);
            this.eastWallDisable.setEnabled(eastWallOn > 0);

            // East main 1
            int eastMain1On = 0;
            for (final String test : E_MAIN1_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++eastMain1On;
                }
            }
            this.eastMain1Enable.setEnabled(eastMain1On < E_MAIN1_STATIONS.length);
            this.eastMain1Disable.setEnabled(eastMain1On > 0);

            // East main 2
            int eastMain2On = 0;
            for (final String test : E_MAIN2_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++eastMain2On;
                }
            }
            this.eastMain2Enable.setEnabled(eastMain2On < E_MAIN2_STATIONS.length);
            this.eastMain2Disable.setEnabled(eastMain2On > 0);

            // East aisle
            int eastAisleOn = 0;
            for (final String test : E_AISLE_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++eastAisleOn;
                }
            }
            this.eastAisleEnable.setEnabled(eastAisleOn < E_AISLE_STATIONS.length);
            this.eastAisleDisable.setEnabled(eastAisleOn > 0);

            // West aisle
            int westAisleOn = 0;
            for (final String test : W_AISLE_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++westAisleOn;
                }
            }
            this.westAisleEnable.setEnabled(westAisleOn < W_AISLE_STATIONS.length);
            this.westAisleDisable.setEnabled(westAisleOn > 0);

            // West aisle
            int westMainOn = 0;
            for (final String test : W_MAIN_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++westMainOn;
                }
            }
            this.westMainEnable.setEnabled(westMainOn < W_MAIN_STATIONS.length);
            this.westMainDisable.setEnabled(westMainOn > 0);

            // West wall
            int westWallOn = 0;
            for (final String test : W_WALL_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++westWallOn;
                }
            }
            this.westWallEnable.setEnabled(westWallOn < W_WALL_STATIONS.length);
            this.westWallDisable.setEnabled(westWallOn > 0);

            // West wall center
            int westWallCenterOn = 0;
            for (final String test : W_WALL_CENTER_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++westWallCenterOn;
                }
            }
            this.westWallCenterEnable.setEnabled(westWallCenterOn < W_WALL_CENTER_STATIONS.length);
            this.westWallCenterDisable.setEnabled(westWallCenterOn > 0);

            // Last 1
            int last1On = 0;
            for (final String test : LAST1_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++last1On;
                }
            }
            this.last1Enable.setEnabled(last1On < LAST1_STATIONS.length);
            this.last1Disable.setEnabled(last1On > 0);

            // Last 2
            int last2On = 0;
            for (final String test : LAST2_STATIONS) {
                if (Boolean.TRUE.equals(clientStatus.get(test))) {
                    ++last2On;
                }
            }
            this.last2Enable.setEnabled(last2On < LAST2_STATIONS.length);
            this.last2Disable.setEnabled(last2On > 0);

            super.paintComponent(g);

            if (g instanceof final Graphics2D g2d) {
                drawMap(g2d, this.clients);
            }
        }
    }

    /**
     * Refreshes the panel.
     */
    void refresh() {

        repaint();
    }

    /**
     * Draws the map.
     *
     * @param g2d        the {@code Graphics} to which to draw
     * @param clientList the list of client_pc records
     */
    private void drawMap(final Graphics2D g2d, final Iterable<RawClientPc> clientList) {

        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int w = getWidth();
        final int h = getHeight();

        final double xScale = (double) w / 1040.0;
        final double yScale = (double) h / 670.0;
        final double dscl = Math.min(xScale, yScale);
        final float fscl = (float)dscl;

        final int cx = w / 2;
        final int cy = h / 2;
        final int x = cx - Math.round(fscl * 340.0f);
        final int y = cy - Math.round(fscl * 290.0f);

        final Path2D path = new Path2D.Float();
        final Rectangle2D rect = new Rectangle2D.Float();
        final Ellipse2D oval = new Ellipse2D.Float();
        final RoundRectangle2D rrect = new RoundRectangle2D.Float();

        final Stroke origStroke = g2d.getStroke();
        final Stroke dashed = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND,
                0.0f, new float[]{10.0f, 10.0f}, 0.0f);
        final Stroke thick = new BasicStroke(2.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 0.0f);

        // Draw the quiet testing room outline

        path.moveTo((double) x - dscl * 150.0, (double) y);
        path.lineTo((double) x - dscl * 70.0, (double) y);
        path.lineTo((double) x - dscl * 70.0, (double) y + dscl * 100.0);
        path.lineTo((double) x - dscl * 150.0, (double) y + dscl * 100.0);
        path.closePath();

        g2d.setColor(this.mapFill);
        g2d.fill(path);
        g2d.setColor(this.mapOutline);
        g2d.draw(path);
        path.reset();

        // Draw the main room outline

        path.moveTo((double) x + dscl * 28.0, (double) y);
        path.lineTo((double) x + dscl * 312.0, (double) y);
        path.lineTo((double) x + dscl * 312.0, (double) y + dscl * 4.0);
        path.lineTo((double) x + dscl * 336.0, (double) y + dscl * 4.0);
        path.lineTo((double) x + dscl * 336.0, (double) y);
        path.lineTo((double) x + dscl * 642.0, (double) y);
        path.lineTo((double) x + dscl * 642.0, (double) y + dscl * 528.0);
        path.lineTo((double) x + dscl * 222.0, (double) y + dscl * 528.0);
        path.lineTo((double) x + dscl * 222.0, (double) y + dscl * 612.0);
        path.lineTo((double) x, (double) y + dscl * 612.0);
        path.lineTo((double) x, (double) y + dscl * 28.0);
        path.lineTo((double) x + dscl * 28.0, (double) y + dscl * 28.0);
        path.closePath();

        g2d.setColor(this.mapFill);
        g2d.fill(path);
        g2d.setColor(this.mapOutline);
        g2d.draw(path);
        path.reset();

        // Draw columns
        rect.setRect((double) x + dscl * 312.0, (double) y + dscl * 186.0, dscl * 20.0, dscl * 20.0);
        g2d.draw(rect);
        rect.setRect((double) x + dscl * 312.0, (double) y + dscl * 392.0, dscl * 20.0, dscl * 20.0);
        g2d.draw(rect);

        // Draw doors
        rect.setRect((double) x - dscl * 115.0, (double) y, dscl * 40.0, dscl * 3.0);
        g2d.fill(rect);

        rect.setRect((double) x + dscl * 470.0, (double) y + dscl * 526.0, dscl * 40.0, dscl * 5.0);
        g2d.fill(rect);
        rect.setRect((double) x + dscl * 220.0, (double) y + dscl * 538.0, dscl * 5.0, dscl * 40.0);
        g2d.fill(rect);
        rect.setRect((double) x + dscl * 340.0, (double) y, dscl * 40.0, dscl * 3.0);
        g2d.fill(rect);

        // Draw outlined regions to indicate zones that can be turned on or off at will.

        g2d.setStroke(dashed);

        // East wall (7 - 18)
        rrect.setRoundRect((double) x + dscl * 18.0, (double) y + dscl * 25.0, dscl * 40.0, dscl * 497.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // East main 2 (19 - 32)
        rrect.setRoundRect((double) x + dscl * 92.0, (double) y + dscl * 42.0, dscl * 83.0, dscl * 412.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // East main 1 (37 - 50)
        rrect.setRoundRect((double) x + dscl * 194.0, (double) y + dscl * 42.0, dscl * 83.0, dscl * 412.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // East aisle (51 - 58)
        rrect.setRoundRect((double) x + dscl * 305.0, (double) y + dscl * 32.0, dscl * 40.0, dscl * 362.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // West aisle (59 - 68)
        rrect.setRoundRect((double) x + dscl * 372.0, (double) y + dscl * 47.0, dscl * 40.0, dscl * 408.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // West main (69 - 82)
        rrect.setRoundRect((double) x + dscl * 446.0, (double) y + dscl * 42.0, dscl * 83.0, dscl * 413.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // West wall (85 - 88, 90 - 91, 94 - 95, 97 - 100)
        path.moveTo((double) x + dscl * 549.0, (double) y + dscl * 42.0);
        path.lineTo((double) x + dscl * 624.0, (double) y + dscl * 42.0);
        path.quadTo((double) x + dscl * 629.0, (double) y + dscl * 42.0, (double) x + dscl * 629.0, (double) y + dscl * 47.0); // NE
        path.lineTo((double) x + dscl * 629.0, (double) y + dscl * 105.0);
        path.quadTo((double) x + dscl * 629.0, (double) y + dscl * 110.0, (double) x + dscl * 624.0, (double) y + dscl * 110.0); // SE
        path.lineTo((double) x + dscl * 589.0, (double) y + dscl * 110.0);
        path.quadTo((double) x + dscl * 584.0, (double) y + dscl * 110.0, (double) x + dscl * 584.0, (double) y + dscl * 115.0); // NW
        path.lineTo((double) x + dscl * 584.0, (double) y + dscl * 415.0);
        path.quadTo((double) x + dscl * 584.0, (double) y + dscl * 420.0, (double) x + dscl * 589.0, (double) y + dscl * 420.0); // SW
        path.lineTo((double) x + dscl * 624.0, (double) y + dscl * 420.0);
        path.quadTo((double) x + dscl * 629.0, (double) y + dscl * 420.0, (double) x + dscl * 629.0, (double) y + dscl * 425.0); // NE
        path.lineTo((double) x + dscl * 629.0, (double) y + dscl * 481.0);
        path.quadTo((double) x + dscl * 629.0, (double) y + dscl * 486.0, (double) x + dscl * 624.0, (double) y + dscl * 486.0); // SE
        path.lineTo((double) x + dscl * 549.0, (double) y + dscl * 486.0);
        path.quadTo((double) x + dscl * 544.0, (double) y + dscl * 486.0, (double) x + dscl * 544.0, (double) y + dscl * 481.0); // SW
        path.lineTo((double) x + dscl * 544.0, (double) y + dscl * 49.0);
        path.quadTo((double) x + dscl * 544.0, (double) y + dscl * 42.0, (double) x + dscl * 549.0, (double) y + dscl * 42.0); // NW
        path.closePath();
        g2d.setColor(this.zoneFill);
        g2d.fill(path);
        g2d.setColor(this.zoneOutline);
        g2d.draw(path);
        path.reset();

        // West wall center (89, 92 - 93, 96)
        rrect.setRoundRect((double) x + dscl * 588.0, (double) y + dscl * 168.0, dscl * 40.0, dscl * 194.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // Last 1 (33 - 36, 83 - 84)
        rrect.setRoundRect((double) x + dscl * 92.0, (double) y + dscl * 454.0, dscl * 436.0, dscl * 32.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // Last 2 (3 - 4)
        rrect.setRoundRect((double) x + dscl * 81.0, (double) y + dscl * 575.0, dscl * 83.0, dscl * 40.0, dscl * 10.0, dscl * 10.0);
        g2d.setColor(this.zoneFill);
        g2d.fill(rrect);
        g2d.setColor(this.zoneOutline);
        g2d.draw(rrect);

        // Draw tables (first is in quiet testing)

        g2d.setStroke(origStroke);

        rrect.setRoundRect((double) x - dscl * 147.0, (double) y + dscl * 3.0, dscl * 28.0, dscl * 94.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 30.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 104.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 186.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 272.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 356.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 447.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 24.0, (double) y + dscl * 521.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 98.0, (double) y + dscl * 48.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 98.0, (double) y + dscl * 174.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 98.0, (double) y + dscl * 300.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 98.0, (double) y + dscl * 426.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 86.0, (double) y + dscl * 580.0, dscl * 72.0, dscl * 28.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 200.0, (double) y + dscl * 48.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 200.0, (double) y + dscl * 174.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 200.0, (double) y + dscl * 300.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 200.0, (double) y + dscl * 426.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 312.0, (double) y + dscl * 38.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 312.0, (double) y + dscl * 112.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 312.0, (double) y + dscl * 208.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 312.0, (double) y + dscl * 318.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 378.0, (double) y + dscl * 52.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 378.0, (double) y + dscl * 133.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 378.0, (double) y + dscl * 219.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 378.0, (double) y + dscl * 302.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 378.0, (double) y + dscl * 384.0, dscl * 28.0, dscl * 72.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 452.0, (double) y + dscl * 48.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 452.0, (double) y + dscl * 174.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 452.0, (double) y + dscl * 300.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 452.0, (double) y + dscl * 426.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        rrect.setRoundRect((double) x + dscl * 550.0, (double) y + dscl * 48.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 550.0, (double) y + dscl * 174.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 550.0, (double) y + dscl * 300.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);
        rrect.setRoundRect((double) x + dscl * 550.0, (double) y + dscl * 426.0, dscl * 72.0, dscl * 56.0, dscl * 4.0, dscl * 4.0);
        g2d.setColor(this.tableColor);
        g2d.fill(rrect);
        g2d.setColor(this.tableOutline);
        g2d.draw(rrect);

        g2d.setStroke(origStroke);

        // Callouts with pads that will hold buttons to turn zones on/off.

        final Dimension btnSize = this.eastWallHamburger.getPreferredSize();

        // East wall
        int xx = x - btnSize.width + 6;
        int yy = (int)Math.round((double) y + dscl * 225.0);

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.curveTo((double) (xx + btnSize.width / 2), (double) yy + (double) btnSize.height + 10.0,
                (double) xx + (double) btnSize.width / 2.0 + dscl * 16.0 - 10.0, (double) yy + (double) btnSize.height + 15.0,
                (double) xx + (double) btnSize.width / 2.0 + dscl * 16.0, (double) yy + (double) btnSize.height + 15.0);
        path.lineTo((double) x + dscl * 17.0, (double) yy + (double) btnSize.height + 15.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // East main 1

        xx = (int)Math.round((double) x + dscl * 233.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 42.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // East main 2

        xx = (int)Math.round((double) x + dscl * 132.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 42.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // East aisle

        xx = (int)Math.round((double) x + dscl * 324.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 31.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // West aisle

        xx = (int)Math.round((double) x + dscl * 390.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 42.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // West main

        xx = (int)Math.round((double) x + dscl * 487.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 42.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // West wall

        xx = (int)Math.round((double) x + dscl * 582.0 - (double) btnSize.width / 2.0);
        yy = y - 6;

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 42.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // West wall center

        xx = (int)Math.round((double) x + dscl * 644.0);
        yy = (int)Math.round((double) y + dscl * 225.0);

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) (yy + btnSize.height));
        path.curveTo((double) xx + (double) btnSize.width / 2.0, (double) yy + (double) btnSize.height + 10.0,
                (double) xx + (double) btnSize.width / 2.0 - dscl * 16.0 + 10.0, (double) yy + (double) btnSize.height + 15.0,
                (double) xx + (double) btnSize.width / 2.0 - dscl * 16.0, (double) yy + (double) btnSize.height + 15.0);
        path.lineTo((double) x + dscl * 628.0, (double) yy + (double) btnSize.height + 15.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // Last1

        xx = (int)Math.round((double) x + dscl * 340.0 - (double) btnSize.width / 2.0);
        yy = (int)Math.round((double) y + dscl * 524.0);

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width / 2.0, (double) yy - 5.0);
        path.lineTo((double) xx + (double) btnSize.width / 2.0, (double) y + dscl * 486.0);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // Last2

        xx = x - btnSize.width;
        yy = (int)Math.round((double) y + dscl * 596.0);

        g2d.setStroke(thick);
        path.moveTo((double) xx + (double) btnSize.width + 6.0, (double) yy);
        path.lineTo((double) x + dscl * 80.0, (double) yy);
        g2d.draw(path);
        g2d.setStroke(origStroke);

        // Draw all client PCs with numbers

        g2d.setFont(this.pcFont.deriveFont(fscl * 15.0f));

        final FontMetrics fm = g2d.getFontMetrics();

        int numInUse = 0;
        int numAvailable = 0;
        final int numOff = 0;

        for (final RawClientPc client : clientList) {

            if (client.iconX != null && client.iconY != null && client.currentStatus != null) {

                final Integer status = client.currentStatus;

                if (RawClientPc.STATUS_FORCE_SUBMIT.equals(status)) {
                    g2d.setColor(this.pcWarningColor);
                    ++numInUse;
                } else if (RawClientPc.STATUS_LOCKED.equals(status)) {
                    g2d.setColor(this.pcLockedColor);
                    ++numAvailable;
                } else if (RawClientPc.STATUS_PAPER_ONLY.equals(status)) {
                    g2d.setColor(this.pcPaperColor);
                } else if (RawClientPc.STATUS_AWAIT_STUDENT.equals(status)) {
                    g2d.setColor(this.pcAwaitColor);
                    ++numInUse;
                } else if (RawClientPc.STATUS_TAKING_EXAM.equals(status)
                        || RawClientPc.STATUS_EXAM_RESULTS.equals(status)) {
                    g2d.setColor(this.pcInExamColor);
                    ++numInUse;
                } else {
                    g2d.setColor(this.pcErrorColor);
                }

                rect.setRect((double) x + dscl * (double) (client.iconX.intValue() - 2),
                        (double) y + dscl * (double) (client.iconY.intValue() - 1), dscl * 28.0, dscl * 22.0);
                g2d.fill(rect);
                g2d.setColor(this.pcOutline);
                g2d.draw(rect);

                g2d.setColor(this.pcNumber);
                final int strw = fm.stringWidth(client.stationNbr);

                final int midx = (int)Math.round((double) x + dscl * ((double) client.iconX.intValue() + 12.0));
                final int midy = (int)Math.round((double) y + dscl * ((double) client.iconY.intValue() + 9.5));

                g2d.drawString(client.stationNbr, midx - strw / 2, midy + fm.getAscent() / 2 - 1);

                // Draw the power status badge
                switch (client.powerStatus) {
                    case RawClientPc.POWER_REPORTING_ON -> g2d.setColor(this.onColor);
                    case RawClientPc.POWER_OFF -> g2d.setColor(this.offColor);
                    case RawClientPc.POWER_TURNING_ON -> g2d.setColor(this.poweringColor);
                    case null, default -> g2d.setColor(this.poweringColor);
                }

                oval.setFrame((double) x + dscl * ((double) client.iconX.intValue() - 4.0),
                        (double) y + dscl * ((double) client.iconY.intValue() - 3.0), dscl * 8.0, dscl * 8.0);
                g2d.fill(oval);
                g2d.setColor(this.pcOutline);
                g2d.draw(oval);
            }
        }

        // Draw the legend
        final Font newLegendFont = this.legendFont.deriveFont(fscl * 18.0f);
        g2d.setFont(newLegendFont);

        g2d.setColor(Color.BLACK);
        g2d.drawString("LEGEND", (int) ((double) x + dscl * 680.0), (int) ((double) y + dscl * 20.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 32.0, dscl * 20.0, dscl * 20.0);
        g2d.setColor(this.pcErrorColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Station Down", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 50.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 62.0, dscl * 20.0, dscl * 2.0);
        g2d.setColor(this.pcWarningColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Self-Configuring", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 80.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 92.0, dscl * 20.0, dscl * 20.0);
        g2d.setColor(this.pcLockedColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Station Locked", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 110.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 122.0, dscl * 20.0, dscl * 20.0);
        g2d.setColor(this.pcPaperColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Paper Exams Only", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 140.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 152.0, dscl * 20.0, dscl * 20.0);
        g2d.setColor(this.pcAwaitColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Student Login", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 170.0));

        rect.setRect((double) x + dscl * 680.0, (double) y + dscl * 182.0, dscl * 20.0, dscl * 20.0);
        g2d.setColor(this.pcInExamColor);
        g2d.fill(rect);
        g2d.setColor(this.pcOutline);
        g2d.draw(rect);
        g2d.drawString("Exam In Progress", (int) ((double) x + dscl * 706.0), (int) ((double) y + dscl * 200.0));

        g2d.setColor(this.pcOutline);
        g2d.drawString("Stations in use: " + numInUse, (int) ((double) x + dscl * 660.0), (int) ((double) h - dscl * 90.0));
        g2d.drawString("Stations available: " + numAvailable, (int) ((double) x + dscl * 660.0), (int) ((double) h - dscl * 60.0));
        g2d.drawString("Stations not enabled: " + numOff, (int) ((double) x + dscl * 660.0), (int) ((double) h - dscl * 30.0));
    }

    /**
     * Invoked when the component's size changes.
     */
    @Override
    public void componentResized(final ComponentEvent e) {

        final int w = getWidth();
        final int h = getHeight();

        final float xScale = (float) w / 1040.0f;
        final float yScale = (float) h / 670.0f;
        final float scl = Math.min(xScale, yScale);

        final int cx = w / 2;
        final int cy = h / 2;
        final int x = cx - Math.round(scl * 340.0F);
        final int y = cy - Math.round(scl * 290.0F);

        final Dimension btSize = this.eastWallHamburger.getPreferredSize();

        // East wall controls

        int xx = x - btSize.width + 6;
        int yy = Math.round((float) y + scl * 225.0F);

        this.eastWallHamburger.setSize(btSize);
        this.eastWallHamburger.setLocation(xx, yy);

        // East main 1 controls

        xx = Math.round((float) x + scl * 233.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.eastMain1Hamburger.setSize(btSize);
        this.eastMain1Hamburger.setLocation(xx, yy);

        // East main 2 controls

        xx = Math.round((float) x + scl * 132.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.eastMain2Hamburger.setSize(btSize);
        this.eastMain2Hamburger.setLocation(xx, yy);

        // East aisle controls

        xx = Math.round((float) x + scl * 324.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.eastAisleHamburger.setSize(btSize);
        this.eastAisleHamburger.setLocation(xx, yy);

        // West aisle controls

        xx = Math.round((float) x + scl * 390.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.westAisleHamburger.setSize(btSize);
        this.westAisleHamburger.setLocation(xx, yy);

        // West main controls

        xx = Math.round((float) x + scl * 487.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.westMainHamburger.setSize(btSize);
        this.westMainHamburger.setLocation(xx, yy);

        // West wall controls

        xx = Math.round((float) x + scl * 582.0F - (float) (btSize.width / 2));
        yy = y - 6;

        this.westWallHamburger.setSize(btSize);
        this.westWallHamburger.setLocation(xx, yy);

        // West wall center controls

        xx = Math.round((float) x + scl * 644.0F);
        yy = Math.round((float) y + scl * 225.0F);

        this.westWallCenterHamburger.setSize(btSize);
        this.westWallCenterHamburger.setLocation(xx, yy);

        // Last1 controls

        xx = Math.round((float) x + scl * 340.0F - (float) (btSize.width / 2));
        yy = Math.round((float) y + scl * 524.0F);

        this.last1Hamburger.setSize(btSize);
        this.last1Hamburger.setLocation(xx, yy);

        // Last2 controls

        xx = x - btSize.width + 6;
        yy = Math.round((float) y + scl * 596.0F - (float) (btSize.height / 2));

        this.last2Hamburger.setSize(btSize);
        this.last2Hamburger.setLocation(xx, yy);

        repaint();
    }

    /**
     * Invoked when the component's position changes.
     */
    @Override
    public void componentMoved(final ComponentEvent e) {

        // No action
    }

    /**
     * Invoked when the component has been made visible.
     */
    @Override
    public void componentShown(final ComponentEvent e) {

        // No action
    }

    /**
     * Invoked when the component has been made invisible.
     */
    @Override
    public void componentHidden(final ComponentEvent e) {

        // No action
    }

    /**
     * Called when an ON or OFF button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (E_WALL_DISABLE.equals(cmd)) {
            this.owner.disableStations(E_WALL_STATIONS);
        } else if (E_WALL_ENABLE.equals(cmd)) {
            this.owner.enableStations(E_WALL_STATIONS);
        } else if (E_WALL_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(E_WALL_STATIONS);
        } else if (E_WALL_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(E_WALL_STATIONS);

        } else if (E_MAIN1_DISABLE.equals(cmd)) {
            this.owner.disableStations(E_MAIN1_STATIONS);
        } else if (E_MAIN1_ENABLE.equals(cmd)) {
            this.owner.enableStations(E_MAIN1_STATIONS);
        } else if (E_MAIN1_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(E_MAIN1_STATIONS);
        } else if (E_MAIN1_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(E_MAIN1_STATIONS);

        } else if (E_MAIN2_DISABLE.equals(cmd)) {
            this.owner.disableStations(E_MAIN2_STATIONS);
        } else if (E_MAIN2_ENABLE.equals(cmd)) {
            this.owner.enableStations(E_MAIN2_STATIONS);
        } else if (E_MAIN2_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(E_MAIN2_STATIONS);
        } else if (E_MAIN2_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(E_MAIN2_STATIONS);

        } else if (E_AISLE_DISABLE.equals(cmd)) {
            this.owner.disableStations(E_AISLE_STATIONS);
        } else if (E_AISLE_ENABLE.equals(cmd)) {
            this.owner.enableStations(E_AISLE_STATIONS);
        } else if (E_AISLE_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(E_AISLE_STATIONS);
        } else if (E_AISLE_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(E_AISLE_STATIONS);

        } else if (W_AISLE_DISABLE.equals(cmd)) {
            this.owner.disableStations(W_AISLE_STATIONS);
        } else if (W_AISLE_ENABLE.equals(cmd)) {
            this.owner.enableStations(W_AISLE_STATIONS);
        } else if (W_AISLE_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(W_AISLE_STATIONS);
        } else if (W_AISLE_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(W_AISLE_STATIONS);

        } else if (W_MAIN_DISABLE.equals(cmd)) {
            this.owner.disableStations(W_MAIN_STATIONS);
        } else if (W_MAIN_ENABLE.equals(cmd)) {
            this.owner.enableStations(W_MAIN_STATIONS);
        } else if (W_MAIN_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(W_MAIN_STATIONS);
        } else if (W_MAIN_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(W_MAIN_STATIONS);

        } else if (W_WALL_DISABLE.equals(cmd)) {
            this.owner.disableStations(W_WALL_STATIONS);
        } else if (W_WALL_ENABLE.equals(cmd)) {
            this.owner.enableStations(W_WALL_STATIONS);
        } else if (W_WALL_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(W_WALL_STATIONS);
        } else if (W_WALL_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(W_WALL_STATIONS);

        } else if (W_WALL_CENTER_DISABLE.equals(cmd)) {
            this.owner.disableStations(W_WALL_CENTER_STATIONS);
        } else if (W_WALL_CENTER_ENABLE.equals(cmd)) {
            this.owner.enableStations(W_WALL_CENTER_STATIONS);
        } else if (W_WALL_CENTER_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(W_WALL_CENTER_STATIONS);
        } else if (W_WALL_CENTER_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(W_WALL_CENTER_STATIONS);

        } else if (LAST1_DISABLE.equals(cmd)) {
            this.owner.disableStations(LAST1_STATIONS);
        } else if (LAST1_ENABLE.equals(cmd)) {
            this.owner.enableStations(LAST1_STATIONS);
        } else if (LAST1_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(LAST1_STATIONS);
        } else if (LAST1_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(LAST1_STATIONS);

        } else if (LAST2_DISABLE.equals(cmd)) {
            this.owner.disableStations(LAST2_STATIONS);
        } else if (LAST2_ENABLE.equals(cmd)) {
            this.owner.enableStations(LAST2_STATIONS);
        } else if (LAST2_POWER_ON.equals(cmd)) {
            this.owner.powerOnStations(LAST2_STATIONS);
        } else if (LAST2_POWER_OFF.equals(cmd)) {
            this.owner.powerOffStations(LAST2_STATIONS);
        }
    }
}
