package jwabbit.debugger;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.ICalcStateListener;
import jwabbit.core.BankState;
import jwabbit.core.MemoryContext;
import jwabbit.gui.fonts.Fonts;
import jwabbit.iface.Calc;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * A panel to show or update memory map.
 */
final class DebugMemoryMapPane extends CollapsePane implements ICalcStateListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 6035380345983586370L;

    /** Register name color. */
    private static final Color NAME_COLOR = new Color(0x55, 0xAA, 0x55);

    /** Register value color. */
    private static final Color VAL_COLOR = Color.BLACK;

    /** Label to show which banks are active. */
    private final JLabel which;

    /** The RAM radios. */
    private final JRadioButton[] ram;

    /** The flash radios. */
    private final JRadioButton[] flash;

    /** The read-only checkboxes. */
    private final JCheckBox[] readonly;

    /** The text fields for page. */
    private final JTextField[] pages;

    /**
     * Constructs a new {@code DebugMemoryMapPane}.
     */
    DebugMemoryMapPane() {

        super("Memory Map", new JPanel(new GridLayout(5, 1)));

        final Font sans = Fonts.getSans().deriveFont(Font.PLAIN, 11.0f);
        final Font mono = Fonts.getMono().deriveFont(Font.PLAIN, 11.0f);

        final JPanel center = getCenter();

        final String[] titles = {"-", "Bank 0", "Bank 1", "Bank 2", "Bank 3"};
        final int numTitles = titles.length;

        final JLabel[] lbls = new JLabel[numTitles];
        int maxWidth = 0;
        int maxHeight = 0;
        final JPanel[] flows = new JPanel[numTitles];
        for (int i = 0; i < numTitles; ++i) {
            lbls[i] = new JLabel(" " + titles[i]);
            lbls[i].setForeground(NAME_COLOR);
            lbls[i].setFont(sans);
            maxWidth = Math.max(maxWidth, lbls[i].getPreferredSize().width);
            maxHeight = Math.max(maxHeight, lbls[i].getPreferredSize().height);
        }
        for (int i = 0; i < numTitles; ++i) {
            lbls[i].setPreferredSize(new Dimension(maxWidth, maxHeight));

            flows[i] = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            flows[i].setBackground(getBackground());
            flows[i].add(lbls[i]);
        }
        this.which = lbls[0];

        final ButtonGroup[] groups = new ButtonGroup[4];
        this.ram = new JRadioButton[4];
        this.flash = new JRadioButton[4];
        this.readonly = new JCheckBox[4];
        this.pages = new JTextField[4];

        for (int i = 0; i < 4; ++i) {
            groups[i] = new ButtonGroup();
            this.ram[i] = new JRadioButton();
            this.ram[i].setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            this.ram[i].setBackground(center.getBackground());
            this.flash[i] = new JRadioButton();
            this.flash[i].setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            this.flash[i].setBackground(center.getBackground());
            this.readonly[i] = new JCheckBox();
            this.readonly[i].setBackground(center.getBackground());
            this.readonly[i].setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            this.pages[i] = new JTextField(4);
            this.pages[i].setForeground(VAL_COLOR);
            this.pages[i].setBackground(getBackground());
            this.pages[i].setBorder(null);
            this.pages[i].setFont(mono);
        }

        final JLabel rLabel = new JLabel("R");
        rLabel.setFont(sans);
        rLabel.setHorizontalAlignment(SwingConstants.CENTER);
        rLabel.setPreferredSize(
                new Dimension(this.ram[0].getPreferredSize().width, rLabel.getPreferredSize().height));
        flows[0].add(rLabel);
        for (int i = 0; i < 4; ++i) {
            flows[i + 1].add(this.ram[i]);
        }

        final JLabel fLabel = new JLabel("F");
        fLabel.setFont(sans);
        fLabel.setHorizontalAlignment(SwingConstants.CENTER);
        fLabel.setPreferredSize(new Dimension(this.flash[0].getPreferredSize().width,
                fLabel.getPreferredSize().height));
        flows[0].add(fLabel);
        for (int i = 0; i < 4; ++i) {
            flows[i + 1].add(this.flash[i]);
        }

        final JLabel roLabel = new JLabel("RO");
        roLabel.setFont(sans);
        roLabel.setHorizontalAlignment(SwingConstants.CENTER);
        roLabel.setPreferredSize(new Dimension(this.readonly[0].getPreferredSize().width,
                roLabel.getPreferredSize().height));
        flows[0].add(roLabel);
        for (int i = 0; i < 4; ++i) {
            flows[i + 1].add(this.readonly[i]);
        }

        final JLabel pageLabel = new JLabel("Page");
        pageLabel.setFont(sans);
        flows[0].add(pageLabel);
        for (int i = 0; i < 4; ++i) {
            flows[i + 1].add(this.pages[i]);
        }

        for (final JPanel flow : flows) {
            center.add(flow);
        }
    }

    /**
     * Called from the calculator thread to allow a client to retrieve data values from a running or stopped calculator
     * without fear of thread conflicts. The receiver should try to minimize time in the function, but will have
     * exclusive access to the calculator data while this method executes.
     *
     * @param theCalc the calculator
     */
    @Override
    public void calcState(final Calc theCalc) {

        // Called from the AWT event thread while the calculator thread is suspended

        final MemoryContext memc = theCalc.getCPU().getMemoryContext();

        final BankState[] normal = memc.getNormalBanks();
        final BankState[] bootmap = memc.getBootmapBanks();
        final BankState[] banks = memc.getBanks();

        if (banks == normal) {
            this.which.setText("Norm");
        } else if (banks == bootmap) {
            this.which.setText("BMap");
        } else {
            this.which.setText("?");
        }

        for (int i = 0; i < 4; ++i) {
            this.ram[i].setSelected(banks[i].isRam());
            this.flash[i].setSelected(!banks[i].isRam());
            this.readonly[i].setSelected(banks[i].isReadOnly());
            this.pages[i].setText(Debugger.toHex2(banks[i].getPage()));
        }
    }

    /**
     * Enables or disables panels.
     *
     * @param enable true to enable; false to disable
     */
    @Override
    public void enableControls(final boolean enable) {

        // Called from the AWT event thread while the calculator thread is suspended

        this.which.setEnabled(enable);

        for (int i = 0; i < 4; ++i) {
            this.ram[i].setEnabled(enable);
            this.flash[i].setEnabled(enable);
            this.readonly[i].setEnabled(enable);
            this.pages[i].setEnabled(enable);
        }
    }
}
