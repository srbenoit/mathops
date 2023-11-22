package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The General tab.
 */
final class KeysTab extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1771803118467068669L;

    /** Key categories. */
    private static final String[] CATEGORIES = {"File", "View", "Calculator", "Debug", "Help", "Emulator"};

    /**
     * Constructs a new {@code KeysTab}.
     */
    KeysTab() {

        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JPanel categoryFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 5));
        categoryFlow.add(new JLabel("Category:"));
        final JComboBox<String> combo = new JComboBox<>(CATEGORIES);
        combo.setSelectedIndex(0);
        categoryFlow.add(combo);
        combo.addActionListener(this);
        add(categoryFlow, BorderLayout.PAGE_START);

        final JList<String> commands = new JList<>();
        commands.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        commands.setVisibleRowCount(14);
        commands.setPreferredSize(new Dimension(combo.getPreferredSize().width << 1,
                commands.getPreferredScrollableViewportSize().height));
        add(commands, BorderLayout.CENTER);

        final JPanel east = new JPanel(new BorderLayout(4, 4));
        add(east, BorderLayout.LINE_END);

        final JList<String> shortcuts = new JList<>();
        shortcuts.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        shortcuts.setVisibleRowCount(4);
        shortcuts.setPreferredSize(new Dimension(combo.getPreferredSize().width << 1,
                shortcuts.getPreferredScrollableViewportSize().height));
        east.add(shortcuts, BorderLayout.PAGE_START);

        final JPanel resetFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 0, 0));
        final JButton btn = new JButton("Reset all");
        btn.setActionCommand("reset");
        btn.addActionListener(this);
        resetFlow.add(btn);
        east.add(resetFlow, BorderLayout.PAGE_END);

        final JPanel center = new JPanel(new BorderLayout(4, 4));
        east.add(center, BorderLayout.CENTER);

        center.add(new JLabel("Press new shortcut"), BorderLayout.PAGE_START);

        final JPanel center2 = new JPanel(new BorderLayout(4, 4));
        center.add(center2, BorderLayout.CENTER);

        final JPanel grid = new JPanel(new GridLayout(2, 1, 4, 4));
        center2.add(grid, BorderLayout.PAGE_START);

        final JTextField shortcut = new JTextField(8);
        grid.add(shortcut);

        final JPanel buttonFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 4, 0));
        final JButton btn1 = new JButton("Assign");
        btn1.setActionCommand("assign");
        final JButton btn2 = new JButton("Remove");
        btn2.setActionCommand("remove");
        buttonFlow.add(btn1);
        buttonFlow.add(btn2);
        grid.add(buttonFlow);
    }

    /**
     * Handles selections of category from the combo box.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        // No action
    }
}
