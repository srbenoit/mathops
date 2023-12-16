package dev.mathops.app.db.ui.configuration;

import dev.mathops.app.AppFileLoader;
import dev.mathops.app.db.config.MutableDatabaseConfig;
import dev.mathops.app.db.ui.MainWindow;
import dev.mathops.core.builder.SimpleBuilder;
import dev.mathops.core.ui.layout.StackedBorderLayout;
import dev.mathops.db.config.DatabaseConfig;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

/**
 * A panel that manages database configuration on the local system, as stored in the "db_config.xml" file.
 */
public final class MainConfigurationPanel extends JPanel {

    /** The initial width of the window's client area. */
    private static final int WIN_WIDTH = 1200;

    /** The initial height of the window's client area. */
    private static final int WIN_HEIGHT = 800;

    /** The gap between icon and text in buttons. */
    private static final int ICON_TEXT_GAP = 20;

    /** The configuration controller. */
    private final Controller controller;

    /** The active database configuration. */
    private DatabaseConfig activeConfig;

    /** The mutable database configuration based on the currently loaded configuration. */
    private MutableDatabaseConfig mutableConfig;

    /** The button to save configuration and apply any changes. */
    private final JButton applyButton;

    /** The button to revert to saved config. */
    private final JButton revertButton;

    /**
     * Constructs a new {@code MainConfigurationPanel}.
     */
    public MainConfigurationPanel() {

        super(new StackedBorderLayout());

        this.controller = new Controller();

        setPreferredSize(new Dimension(WIN_WIDTH, WIN_HEIGHT));

        final Color bg = getBackground();
        final int shade = bg.getRed() + bg.getGreen() + bg.getBlue();
        final Color highlight = shade < 384 ? bg.brighter() : bg.darker();

        final JPanel west = new JPanel(new StackedBorderLayout(10, 10));
        final Border westPadding = BorderFactory.createEmptyBorder(15, 10, 15, 15);
        final Border westOutline = BorderFactory.createMatteBorder(0, 0, 0, 2, highlight);
        final Border westBorder = BorderFactory.createCompoundBorder(westOutline, westPadding);
        west.setBorder(westBorder);
        add(west, StackedBorderLayout.WEST);

        final String serversLbl = Res.get(Res.SERVERS_BTN_LABEL);
        final JButton serversButton = buildMainButton(serversLbl, "servers48.png", 10, this.controller,
                Controller.SERVERS_CMD);

        final String profilesLbl = Res.get(Res.PROFILES_BTN_LABEL);
        final JButton profilesButton = buildMainButton(profilesLbl, "profiles48.png", 10, this.controller,
                Controller.PROFILES_CMD);

        final String codeLbl = Res.get(Res.CODE_BTN_LABEL);
        final JButton codeContextsButton = buildMainButton(codeLbl, "codecontexts48.png", 10, this.controller,
                Controller.CODE_CTX_CMD);

        final String webLbl = Res.get(Res.WEB_BTN_LABEL);
        final JButton webContextsButton = buildMainButton(webLbl, "webcontexts48.png", 10, this.controller,
                Controller.WEB_CTX_CMD);

        final Dimension serversPref = serversButton.getPreferredSize();
        final Dimension profilesPref = profilesButton.getPreferredSize();
        final Dimension codePref = codeContextsButton.getPreferredSize();
        final Dimension webPref = webContextsButton.getPreferredSize();
        final int max1 = Math.max(serversPref.width, profilesPref.width);
        final int max2 = Math.max(codePref.width, webPref.width);
        final int maxW = Math.max(max1, max2);
        serversButton.setPreferredSize(new Dimension(maxW, serversPref.height));
        profilesButton.setPreferredSize(new Dimension(maxW, profilesPref.height));
        codeContextsButton.setPreferredSize(new Dimension(maxW, codePref.height));
        webContextsButton.setPreferredSize(new Dimension(maxW, webPref.height));
        serversButton.setHorizontalAlignment(SwingConstants.LEADING);
        profilesButton.setHorizontalAlignment(SwingConstants.LEADING);
        codeContextsButton.setHorizontalAlignment(SwingConstants.LEADING);
        webContextsButton.setHorizontalAlignment(SwingConstants.LEADING);

        final JPanel serversFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        serversFlow.add(serversButton);
        west.add(serversFlow, StackedBorderLayout.NORTH);

        final JPanel profilesFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        profilesFlow.add(profilesButton);
        west.add(profilesFlow, StackedBorderLayout.NORTH);

        final JPanel codeFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        codeFlow.add(codeContextsButton);
        west.add(codeFlow, StackedBorderLayout.NORTH);

        final JPanel webFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        webFlow.add(webContextsButton);
        west.add(webFlow, StackedBorderLayout.NORTH);

        final JPanel south = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 10));
        final Border southBorder = BorderFactory.createMatteBorder(2, 0, 0, 0, highlight);
        south.setBorder(southBorder);
        add(south, StackedBorderLayout.SOUTH);

        this.applyButton = buildMainButton("Save and Apply Changes", "save_apply32.png", 7,
                this.controller, Controller.APPLY_CMD);

        this.revertButton = buildMainButton("Discard Changes and Revert to Saved", "revert32.png", 7,
                this.controller, Controller.REVERT_CMD);

        south.add(this.applyButton);
        south.add(this.revertButton);
    }

    /**
     * Constructs a "main" button for the interface.
     *
     * @param label          the button label
     * @param iconFilename   the icon filename (an image file in the Resources folder under the same path as the
     *                       "MainWindow" class)
     * @param pad            the amount of top/bottom padding to apply
     * @param actionListener the action listener
     * @param actionCommand  the button's action command
     * @return the generated button
     */
    private static JButton buildMainButton(final String label, final String iconFilename, final int pad,
                                           final ActionListener actionListener, final String actionCommand) {

        final JButton btn = new JButton(label);

        btn.setIconTextGap(ICON_TEXT_GAP);
        final Font origFont = btn.getFont();
        final float origFontSize = origFont.getSize2D();
        final Font largerFont = origFont.deriveFont(origFontSize * 1.5f);
        btn.setFont(largerFont);

        btn.setActionCommand(actionCommand);
        btn.addActionListener(actionListener);

        final Border origBorder = btn.getBorder();
        final Border padding = BorderFactory.createEmptyBorder(pad, 10, pad, 10);
        final Border newBorder = BorderFactory.createCompoundBorder(origBorder, padding);
        btn.setBorder(newBorder);

        final BufferedImage serverImg = AppFileLoader.loadFileAsImage(MainWindow.class, iconFilename, true);
        if (serverImg != null) {
            btn.setIcon(new ImageIcon(serverImg));
        }

        return btn;
    }

    /**
     * Updates configuration, including the active database configuration.  This reconstructs the mutable configuration
     * object this panel administers and updates the UI to match.
     *
     * @param theMainWindow the owning {@code MainWindow}
     */
    public void updateConfig(final MainWindow theMainWindow) {

        this.activeConfig = theMainWindow.getDbConfig();
        this.mutableConfig = new MutableDatabaseConfig(this.activeConfig);
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        return SimpleBuilder.concat("MainConfigurationPanel{}");
    }
}
