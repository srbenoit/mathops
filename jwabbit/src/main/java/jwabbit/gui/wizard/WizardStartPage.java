package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.utilities.TIFILE;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.Serial;

/**
 * SOURCE: gui/wizard/wizardstart.h, "WizardStartPage" class.
 */
final class WizardStartPage extends WizardPage implements ActionListener, ChangeListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8765200812617072731L;

    /** The radio button to browse for a local ROM file. */
    private final JRadioButton browseRadio;

    /** The last known state of the browse radio button. */
    private boolean browseState;

    /** The browse location. */
    private final JTextField browseLoc;

    /** The browse button. */
    private final JButton browseBtn;

    /** The radio button to create a ROM by download. */
    private final JRadioButton createRadio;

    /** The radio button to copy ROM from a physical calculator. */
    private final JRadioButton copyRadio;

    /** The file chooser. */
    private final JFileChooser filePicker1;

    /**
     * Constructs a new {@code WizardStartPage}.
     *
     * <p>
     * SOURCE: gui/wizard/wizardos.cpp, "WizardStartPage::WizardStartPage" constructor.
     *
     * @param theParent the parent wizard frame
     * @param theIcon   the icon to show next to the page title
     */
    WizardStartPage(final RomWizardFrame theParent, final BufferedImage theIcon) {

        super(theParent, "start", theIcon, "JWabbitemu ROM Selection");

        final JPanel sub1 = new JPanel(new BorderLayout(0, 15));
        sub1.setBackground(getBgColor());
        add(sub1, BorderLayout.CENTER);
        final JLabel lbl2 = new JLabel("This wizard will guide you through running JWabbitemu for the first time.");
        final Font font = lbl2.getFont().deriveFont(Font.PLAIN);
        lbl2.setFont(font);
        sub1.add(lbl2, BorderLayout.PAGE_START);

        final JPanel sub2 = new JPanel(new BorderLayout(0, 15));
        sub2.setBackground(getBgColor());
        sub1.add(sub2, BorderLayout.CENTER);
        final JPanel grid = new JPanel(new GridLayout(2, 1));
        grid.setBackground(getBgColor());
        final JLabel lbl3 = new JLabel("A ROM image is required to emulate TI calculators. How do you want to");
        lbl3.setFont(font);
        grid.add(lbl3);
        final JLabel lbl4 = new JLabel("get a ROM image for JWabbitemu?");
        lbl4.setFont(font);
        grid.add(lbl4);
        sub2.add(grid, BorderLayout.PAGE_START);

        final JPanel sub3 = new JPanel(new BorderLayout());
        sub3.setBackground(getBgColor());
        sub2.add(sub3, BorderLayout.CENTER);
        final JPanel options = new JPanel(new GridLayout(4, 1));
        options.setBackground(getBgColor());
        sub3.add(options, BorderLayout.PAGE_START);

        final ButtonGroup group = new ButtonGroup();

        this.browseRadio = new JRadioButton("Browse for a ROM image on my computer");
        this.browseRadio.setBackground(getBgColor());
        this.browseRadio.setFont(font);
        group.add(this.browseRadio);
        options.add(this.browseRadio);

        final JPanel flow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow.setBackground(getBgColor());
        flow.setFocusable(true);
        flow.add(new JLabel("   "));
        this.browseLoc = new JTextField(20);
        flow.add(this.browseLoc);
        this.browseBtn = new JButton("Browse...");
        this.browseBtn.setFont(font);
        this.browseBtn.addActionListener(this);
        flow.add(this.browseBtn);
        options.add(flow);

        this.copyRadio = new JRadioButton("Copy a ROM image from a real calculator");
        this.copyRadio.setBackground(getBgColor());
        this.copyRadio.setFont(font);
        group.add(this.copyRadio);
        options.add(this.copyRadio);

        this.createRadio = new JRadioButton("Create a ROM image using open source software");
        this.createRadio.setBackground(getBgColor());
        this.createRadio.setFont(font);
        group.add(this.createRadio);
        options.add(this.createRadio);

        this.browseRadio.setSelected(true);

        this.browseRadio.addChangeListener(this);
        this.copyRadio.addChangeListener(this);
        this.createRadio.addChangeListener(this);

        this.browseState = true;

        if (theParent != null) {
            theParent.setNextLabel("Finish");
        }

        this.filePicker1 = new JFileChooser();
        this.filePicker1.setDialogTitle("Browse for a ROM image");
        this.filePicker1.addChoosableFileFilter(new FileNameExtensionFilter("ROM images", "rom"));
        this.filePicker1.addChoosableFileFilter(new FileNameExtensionFilter("Savestate images", "sav"));
        this.filePicker1.setFileSelectionMode(JFileChooser.FILES_ONLY);
        this.filePicker1.setMultiSelectionEnabled(false);
    }

    /**
     * Handles action events generated by the browse button.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        if (this.filePicker1.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            this.browseLoc.setText(getFilePath());
            onFileChanged();
        }
    }

    /**
     * SOURCE: gui/wizard/wizardos.cpp, "WizardStartPage::OnFileChanged" constructor.
     */
    private void onFileChanged() {

        final String path = getFilePath();
        if (path != null) {
            final TIFILE tifile = TIFILE.importvar(path, true);
            getWizard().enableNext(tifile != null);
            if (tifile != null) {
                tifile.freeTiFile();
            }
        }
    }

    /**
     * Called when a radio button selection changes.
     *
     * <p>
     * SOURCE: gui/wizard/wizardos.cpp, "WizardStartPage::OnRadioSelected" constructor.
     *
     * @param e the change event
     */
    @Override
    public void stateChanged(final ChangeEvent e) {

        if (this.browseRadio.isSelected()) {
            if (!this.browseState) {
                getWizard().setNextLabel("Finish");
                final String path = getFilePath();
                if (path == null) {
                    getWizard().enableNext(false);
                } else {
                    final TIFILE tifile = TIFILE.importvar(path, true);
                    if (tifile == null) {
                        getWizard().enableNext(false);
                    } else {
                        getWizard().enableNext(true);
                        tifile.freeTiFile();
                    }
                }
                this.browseBtn.setEnabled(true);
                this.browseLoc.setEnabled(true);
                this.browseState = true;
            }
        } else {
            getWizard().setNextLabel("Next >");
            getWizard().enableNext(true);

            this.browseBtn.setEnabled(false);
            this.browseLoc.setEnabled(false);
            this.browseState = false;
        }
    }

    /**
     * Tests whether the copy radio button is selected.
     *
     * @return true if selected
     */
    boolean getCopyRadioState() {

        return this.copyRadio.isSelected();
    }

    /**
     * Tests whether the create radio button is selected.
     *
     * @return true if selected
     */
    boolean getCreateRadioState() {

        return this.createRadio.isSelected();
    }

    /**
     * Tests whether the create radio button is selected.
     *
     * @return true if selected
     */
    boolean getBrowseRadioState() {

        return this.browseRadio.isSelected();
    }

    /**
     * Gets the path of the selected file.
     *
     * @return the file path
     */
    String getFilePath() {

        final File file = this.filePicker1.getSelectedFile();

        return file == null ? null : file.getAbsolutePath();
    }
}
