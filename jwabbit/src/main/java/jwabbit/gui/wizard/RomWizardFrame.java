package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.gui.Gui;
import jwabbit.log.ObjLogger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.Objects;

/**
 * SOURCE: gui/wizard/romwizard.h, "RomWizard" class.
 */
final class RomWizardFrame extends JFrame implements WindowListener, ActionListener {

    /** S log to which to write diagnostic messages. */
    private static final ObjLogger LOG;

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2655086539928891468L;

    /** the wizard runner. */
    private final IWizardRunner runner;

    /** The start page. */
    private final WizardStartPage startPage;

    /** The calculator type page. */
    private final WizardCalcTypePage calcTypePage;

    /** The OS page. */
    private final WizardOSPage osPage;

    /** The current page. */
    private WizardPage currentPage;

    /** The card panel. */
    private final JPanel cardPanel;

    /** The layout. */
    private final CardLayout layout;

    /** The next button. */
    private final JButton nextBtn;

    static {
        LOG = new ObjLogger();
    }

    /**
     * Creates the main frame for the ROM wizard.
     *
     * <p>
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::RomWizard" constructor.
     *
     * @param theRunner the wizard runner to notify when the wizard is complete.
     */
    RomWizardFrame(final IWizardRunner theRunner) {

        super("JWabbitemu Setup");

        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(this);

        if (!SwingUtilities.isEventDispatchThread()) {
            LOG.warning("RomWizard constructed from outside the AWT event thread");
        }

        this.runner = theRunner;

        final JPanel content = new JPanel(new BorderLayout());
        this.setContentPane(content);

        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 3, 3));
        content.add(flow, BorderLayout.PAGE_START);

        final BufferedImage icon = Gui.loadImage("wabbitemu_28.png");
        if (icon != null) {
            flow.add(new JLabel(new ImageIcon(icon)));
        }

        // Title
        final JLabel lbl1 = new JLabel("  Wabbitemu Setup");
        lbl1.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        final Font fnt = lbl1.getFont();
        lbl1.setFont(fnt.deriveFont(Font.BOLD));
        flow.add(lbl1);

        // Next and cancel buttons
        final JPanel buttonBar = new JPanel(new FlowLayout(FlowLayout.TRAILING, 10, 6));
        this.nextBtn = new JButton("Next >");
        this.nextBtn.addActionListener(this);
        buttonBar.add(this.nextBtn);
        final JButton cancel = new JButton("Cancel");
        cancel.addActionListener(this);
        buttonBar.add(cancel);

        content.add(buttonBar, BorderLayout.PAGE_END);

        this.layout = new CardLayout();
        this.cardPanel = new JPanel(this.layout);
        this.cardPanel.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        content.add(this.cardPanel, BorderLayout.CENTER);

        this.startPage = new WizardStartPage(this, null);
        this.calcTypePage = new WizardCalcTypePage(this, null);
        this.osPage = new WizardOSPage(this, null);

        this.startPage.setNextPage(this.calcTypePage);
        this.calcTypePage.setNextPage(this.osPage);
        this.osPage.setNextPage(this.calcTypePage);

        this.cardPanel.add(this.startPage, this.startPage.getPageName());
        this.cardPanel.add(this.calcTypePage, this.calcTypePage.getPageName());
        this.cardPanel.add(this.osPage, this.osPage.getPageName());
        this.currentPage = this.startPage;
        this.currentPage.requestFocusInWindow();
    }

    /**
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::Begin" method.
     */
    void begin() {

        this.nextBtn.setText("Finish");
        this.nextBtn.setEnabled(false);

        pack();

        final Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        final Dimension size = getSize();
        setLocation((screen.width - size.width) / 2, (screen.height - size.height) / 2);
        setVisible(true);
    }

    /**
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::OnPageChanged" method.
     */
    private void onPageChanged() {

        if (this.calcTypePage == this.currentPage) {
            this.calcTypePage.enableRadios(this.startPage.getCopyRadioState());
        } else if (this.osPage == this.currentPage) {
            this.osPage.setCreatingRom(this.startPage.getCreateRadioState());
            switch (Objects.requireNonNull(this.calcTypePage.getModel())) {
                case TI_73:
                    this.osPage.setChoices("OS 1.91");
                    break;
                case TI_83P:
                case TI_83PSE:
                    this.osPage.setChoices("OS 1.19");
                    break;
                case TI_84P:
                case TI_84PSE:
                    this.osPage.setChoices("OS 2.43", "OS 2.55 MP");
                    break;

                case INVALID_MODEL:
                case TI_81:
                case TI_82:
                case TI_83:
                case TI_84PCSE:
                case TI_85:
                case TI_86:
                default:
                    this.osPage.setChoices((String[]) null);
                    break;
            }

            if (this.osPage.isCreatingRom()) {
                setNextLabel("Finish");
            } else {
                setNextLabel("Next >");
            }
        }
    }

    /**
     * Called when the user presses the "Finish" button on the wizard.
     *
     * <p>
     * SOURCE: gui/wizard/romwizard.cpp, "RomWizard::OnFinish" method.
     */
    private void onFinish() {

        this.runner.setBrowsedForRom(this.startPage.getBrowseRadioState());
        this.runner.setBrowsePath(this.startPage.getFilePath());
        this.runner.setCreatedRom(this.startPage.getCreateRadioState());
        this.runner.setModel(this.calcTypePage.getModel());
        this.runner.setDownloadOs(this.osPage.getDownloadOsState());

        setVisible(false);
        dispose();

        this.runner.complete(true);
    }

    /**
     * Sets the label for the "next" button.
     *
     * @param theLabel the label
     */
    void setNextLabel(final String theLabel) {

        this.nextBtn.setText(theLabel);
    }

    /**
     * Enables or disables the "next" button.
     *
     * @param enabled true to enable the "next" button
     */
    void enableNext(final boolean enabled) {

        this.nextBtn.setEnabled(enabled);
    }

    /**
     * Handles actions generated by the "Next" and "Cancel" buttons.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (e.getSource() == this.nextBtn) {
            if ("Finish".equals(this.nextBtn.getText())) {
                onFinish();
            } else {
                this.currentPage = this.currentPage.getNextPage();
                this.layout.show(this.cardPanel, this.currentPage.getPageName());
                this.currentPage.requestFocusInWindow();
                onPageChanged();
            }
        } else {
            // Assume "Cancel"
            setVisible(false);
            dispose();
            this.runner.complete(false);
        }
    }

    /**
     * Handles window open events.
     *
     * @param e the window event
     */
    @Override
    public void windowOpened(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window closing events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosing(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window closed events.
     *
     * @param e the window event
     */
    @Override
    public void windowClosed(final WindowEvent e) {

        this.runner.complete(false);
    }

    /**
     * Handles window iconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowIconified(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window deiconified events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeiconified(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window activated events.
     *
     * @param e the window event
     */
    @Override
    public void windowActivated(final WindowEvent e) {

        // No action
    }

    /**
     * Handles window deactivated events.
     *
     * @param e the window event
     */
    @Override
    public void windowDeactivated(final WindowEvent e) {

        // No action
    }
}
