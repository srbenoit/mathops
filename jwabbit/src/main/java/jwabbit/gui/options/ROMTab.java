package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * The General tab.
 */
final class ROMTab extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -8596243154454024432L;

    /**
     * Constructs a new {@code ROMTab}.
     */
    ROMTab() {

        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JPanel currentRom = new JPanel(new GridLayout(4, 0));
        currentRom.setBorder(BorderFactory.createTitledBorder("Current ROM attributes"));

        final JLabel filename = new JLabel("-");
        final JLabel version = new JLabel("-");
        final JLabel model = new JLabel("-");
        final JLabel flashsize = new JLabel("-");

        final JPanel[] flows = {new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 1)),
                new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 1)),
                new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 1)),
                new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 1))};

        final JLabel[] lbls = {new JLabel("Filename:"), new JLabel("Version:"), new JLabel("Model:"),
                new JLabel("Flash size:")};
        int width = 0;
        for (final JLabel lbl : lbls) {
            width = Math.max(width, lbl.getPreferredSize().width);
        }
        final int numLbls = lbls.length;
        for (int i = 0; i < numLbls; ++i) {
            lbls[i].setPreferredSize(new Dimension(width, lbls[i].getPreferredSize().height));
            flows[i].add(lbls[i]);
            currentRom.add(flows[i]);
        }

        final Dimension pref = new Dimension(width * 3, filename.getPreferredSize().height);

        filename.setPreferredSize(pref);
        version.setPreferredSize(pref);
        model.setPreferredSize(pref);
        flashsize.setPreferredSize(pref);

        flows[0].add(filename);
        flows[1].add(version);
        flows[2].add(model);
        flows[3].add(flashsize);

        add(currentRom, BorderLayout.PAGE_START);

        final JPanel nest1 = new JPanel(new BorderLayout());
        add(nest1, BorderLayout.CENTER);

        final JPanel hardware = new JPanel(new GridLayout(4, 0));
        hardware.setBorder(BorderFactory.createTitledBorder("Hardware"));

        final JCheckBox emulateMissingRam = new JCheckBox("Emulate missing RAM pages");
        final JCheckBox emulate2025 = new JCheckBox("Emulate 20 MHz and 25 MHz calculators");
        final JCheckBox emulateOrig83 = new JCheckBox("Emulate original 83+ hardware");

        hardware.add(emulateMissingRam);
        hardware.add(emulate2025);
        hardware.add(emulateOrig83);

        final JPanel lcdFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        lcdFlow.add(new JLabel("LCD Delay (6 MHz):"));
        final JTextField lcdDelay = new JTextField(4);
        lcdFlow.add(lcdDelay);
        hardware.add(lcdFlow);

        nest1.add(hardware, BorderLayout.PAGE_START);
    }
}
