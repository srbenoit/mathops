package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.io.Serial;

/**
 * The "General" tab.
 */
final class GeneralTab extends JPanel {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1942458697633276582L;

    /**
     * Constructs a new {@code GeneralTab}.
     */
    GeneralTab() {

        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JPanel startup = new JPanel(new GridLayout(4, 0));
        startup.setBorder(BorderFactory.createTitledBorder("Startup"));

        final JCheckBox loadFiles = new JCheckBox("Load files passed in arguments in a new calculator");
        final JCheckBox autoTurnOn = new JCheckBox("Turn calculator on automatically");
        final JCheckBox updateCheck = new JCheckBox("Automatically check for updates");
        final JCheckBox whatsNew = new JCheckBox("Show what's new on upgrade");

        startup.add(loadFiles);
        startup.add(autoTurnOn);
        startup.add(updateCheck);
        startup.add(whatsNew);

        add(startup, BorderLayout.PAGE_START);

        final JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        add(nest1, BorderLayout.CENTER);

        final JPanel debugger = new JPanel(new GridLayout(4, 0));
        debugger.setBorder(BorderFactory.createTitledBorder("Startup"));

        final JCheckBox reverseTime = new JCheckBox("Enable code rewinding");
        reverseTime.setEnabled(false);
        final JCheckBox debugExe = new JCheckBox("Open debugger on execution violations");
        final JCheckBox debugFlash = new JCheckBox("Open debugger on invalid flash commands");
        final JCheckBox disableTios = new JCheckBox("Disable TIOS features in disassembly");

        debugger.add(reverseTime);
        debugger.add(debugExe);
        debugger.add(debugFlash);
        debugger.add(disableTios);

        nest1.add(debugger, BorderLayout.PAGE_START);

        final JPanel nest2 = new JPanel(new BorderLayout(4, 4));
        nest1.add(nest2, BorderLayout.CENTER);

        final JPanel general = new JPanel(new GridLayout(3, 0));
        general.setBorder(BorderFactory.createTitledBorder("Startup"));

        final JCheckBox autoSave = new JCheckBox("Automatically save and restore state");
        final JCheckBox alwaysOnTop = new JCheckBox("Always show JWabbitemu on top of other windows");
        final JCheckBox portableMode = new JCheckBox("Portable mode");
        portableMode.setEnabled(false);
        portableMode.setSelected(true);

        general.add(autoSave);
        general.add(alwaysOnTop);
        general.add(portableMode);

        nest2.add(general, BorderLayout.PAGE_START);

        // set initial state of checkboxes from properties
    }
}
