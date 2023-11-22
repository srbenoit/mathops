package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.utilities.TIFILE;

import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * SOURCE: gui/wizard/wizardos.h, "WizardOSPage" class.
 */
final class WizardOSPage extends WizardPage implements ActionListener, ChangeListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -5033236654375756725L;

    /** Browse OS. */
    private final JRadioButton browseOS;

    /** Browse location. */
    private final JTextField browseLoc;

    /** Browse button. */
    private final JButton browseBtn;

    /** Creating rom. */
    private boolean creatingROM;

    /** The data model for the OS choice. */
    private final DefaultComboBoxModel<String> choiceModel;

    /** Choice box. */
    private final JComboBox<String> choice1;

    /** File picker 2. */
    private final JFileChooser filePicker2;

    /** Download OS button. */
    private final JRadioButton downloadOS;

    /** The next bak. */
    private WizardPage nextBak;

    /**
     * Constructs a new {@code WizardOSPage}.
     *
     * <p>
     * SOURCE: gui/wizard/wizardos.cpp, "WizardOSPage::WizardOSPage" constructor.
     *
     * @param theParent the parent wizard frame
     * @param theIcon   the icon to show next to the page title
     */
    WizardOSPage(final RomWizardFrame theParent, final BufferedImage theIcon) {

        super(theParent, "os", theIcon, "OS Selection");

        final JPanel sub1 = new JPanel(new BorderLayout(0, 15));
        add(sub1, BorderLayout.CENTER);

        final JPanel grid1 = new JPanel(new GridLayout(2, 1));
        grid1.setBackground(getBgColor());
        sub1.add(grid1, BorderLayout.PAGE_START);
        grid1.add(new JLabel(
                "A calculator OS file is required in addition to a ROM image to emulate TI"));
        grid1.add(new JLabel("calculators. How do you want to obtain an OS for Wabbitemu?"));

        final JPanel sub2 = new JPanel(new BorderLayout(0, 15));
        sub2.setBackground(getBgColor());
        sub1.add(sub2, BorderLayout.CENTER);

        final JPanel choices = new JPanel(new GridLayout(2, 1));
        choices.setBackground(getBgColor());
        sub2.add(choices, BorderLayout.PAGE_START);

        final ButtonGroup group = new ButtonGroup();

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 0));
        flow1.setBackground(getBgColor());
        choices.add(flow1);
        final JPanel theChoice1 = new JPanel(new GridLayout(2, 1));
        theChoice1.setBackground(getBgColor());
        this.downloadOS = new JRadioButton("Download OS files from TI's website");
        group.add(this.downloadOS);
        this.downloadOS.addChangeListener(this);
        theChoice1.add(this.downloadOS);
        final JPanel choice1Flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        choice1Flow.setBackground(getBgColor());
        theChoice1.add(choice1Flow);
        choice1Flow.add(new JLabel("   TI-OS Version:"));
        this.choiceModel = new DefaultComboBoxModel<>();
        this.choice1 = new JComboBox<>(this.choiceModel);
        choice1Flow.add(this.choice1);

        final JPanel terms = new JPanel(new GridLayout(2, 1));
        terms.setBackground(getBgColor());
        terms.add(new JLabel("By downloading you"));
        terms.add(new JLabel("agree to TI's terms."));
        theChoice1.add(terms);

        final JButton termBtn = new JButton("Terms");
        termBtn.addActionListener(this);
        flow1.add(termBtn);

        final JPanel choice2 = new JPanel(new GridLayout(2, 1));
        choice2.setBackground(getBgColor());
        choices.add(choice2);

        this.browseOS = new JRadioButton("Browse for OS files on my computer");
        choice2.add(this.browseOS);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow2.setBackground(getBgColor());
        flow2.add(new JLabel("   "));
        this.browseLoc = new JTextField(20);
        flow2.add(this.browseLoc);
        this.browseBtn = new JButton("Browse...");
        this.browseBtn.addActionListener(this);
        flow2.add(this.browseBtn);
        choice2.add(flow2);

        this.downloadOS.setSelected(true);

        this.filePicker2 = new JFileChooser();
        this.filePicker2.setDialogTitle("Select a file");
        this.filePicker2.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.filePicker2.setMultiSelectionEnabled(false);
    }

    /**
     * Called when a radio button state changes.
     *
     * <p>
     * SOURCE: gui/wizard/wizardos.cpp, "WizardOSPage::OnRadioSelected" method.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(final ChangeEvent e) {

        if (this.creatingROM) {
            this.nextBak = getNextPage();
            getWizard().setNextLabel("Finish");
            setNextPage(null);
        } else {
            if (getNextPage() == null) {
                setNextPage(this.nextBak);
            }
            getWizard().setNextLabel("Next >");
            this.nextBak = null;
        }

        if (this.browseOS.isSelected()) {
            final String path = this.filePicker2.getSelectedFile().getAbsolutePath();
            final TIFILE tifile = TIFILE.importvar(path, true);
            if (tifile == null || tifile.getType() != TIFILE.FLASH_TYPE) {
                getWizard().enableNext(false);
            }
            if (tifile != null) {
                tifile.freeTiFile();
            }
            return;
        }

        getWizard().enableNext(true);
    }

    /**
     * Handles actions generated by clicking the "Browse..." or "Terms" button.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (e.getSource() == this.browseBtn) {
            if (this.filePicker2.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
                this.browseLoc.setText(this.filePicker2.getSelectedFile().getAbsolutePath());
            }
        } else {
            try {
                final URI uri = new URI("https://education.ti.com/educationportal/"
                        + "downloadcenter/Eula.do?website=US&tabId=1&appId=317");
                Desktop.getDesktop().browse(uri);
            } catch (final URISyntaxException | IOException ex) {
                // No action
            }
        }
    }

    /**
     * Tests whether a ROM should be created.
     *
     * @return true if creating ROM
     */
    boolean isCreatingRom() {

        return this.creatingROM;
    }

    /**
     * Sets flag that indicates whether a ROM should be created.
     *
     * @param creating true if creating ROM
     */
    void setCreatingRom(final boolean creating) {

        this.creatingROM = creating;
    }

    /**
     * Sets the list of choices, makes the first choice (if any) selected, and enables (if there are choices) or
     * disables (if there are none) the list.
     *
     * @param choices the choices
     */
    public void setChoices(final String... choices) {

        if (choices == null) {
            this.choice1.setEnabled(false);
        } else {
            this.choiceModel.removeAllElements();
            for (final String choice : choices) {
                this.choiceModel.addElement(choice);
            }
            this.choice1.setEnabled(true);
            this.choice1.setSelectedIndex(0);
        }
    }

    /**
     * Gets the selected file.
     *
     * @return the selected file
     */
    public File getFile() {

        return this.filePicker2.getSelectedFile();
    }

    /**
     * Tests whether the download OS radio button is selected.
     *
     * @return true if selected
     */
    boolean getDownloadOsState() {

        return this.downloadOS.isSelected();
    }

    /**
     * Tests whether the download OS radio button is selected.
     *
     * @return the selected choice
     */
    public String getChoice() {

        return (String) this.choice1.getSelectedItem();
    }
}
