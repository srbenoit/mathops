package jwabbit.gui.options;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The General" tab.
 */
final class SkinTab extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -3623393074816566184L;

    /**
     * Constructs a new {@code SkinTab}.
     */
    SkinTab() {

        super(new BorderLayout(4, 4));
        setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

        final JPanel skinOpt = new JPanel(new BorderLayout(0, 0));
        skinOpt.setBorder(BorderFactory.createTitledBorder("Skin options"));
        add(skinOpt, BorderLayout.PAGE_START);

        final JPanel skinOpt1 = new JPanel(new GridLayout(2, 2));
        skinOpt.add(skinOpt1, BorderLayout.PAGE_START);

        final JCheckBox cutoutSkin = new JCheckBox("Use cutout skin");
        final JCheckBox screenTexture = new JCheckBox("Use screen texture");
        final JCheckBox customSkin = new JCheckBox("Use custom skin");

        skinOpt1.add(cutoutSkin);
        skinOpt1.add(screenTexture);
        skinOpt1.add(customSkin);

        final JPanel skinOpt2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 4));
        skinOpt.add(skinOpt2, BorderLayout.PAGE_END);
        skinOpt2.add(new JLabel("Faceplate color:"));

        final ColorChoicesPane colorChoices = new ColorChoicesPane();
        skinOpt2.add(colorChoices);

        final JPanel nest1 = new JPanel(new BorderLayout(4, 4));
        add(nest1, BorderLayout.CENTER);

        final JPanel customOpt = new JPanel(new BorderLayout(4, 4));
        customOpt.setBorder(BorderFactory.createTitledBorder("Custom skin options"));
        nest1.add(customOpt, BorderLayout.PAGE_START);

        final JPanel imgPath = new JPanel(new BorderLayout());
        customOpt.add(imgPath, BorderLayout.PAGE_START);

        final JPanel flow1 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow1.add(new JLabel("Image file:"));
        imgPath.add(flow1, BorderLayout.PAGE_START);

        final JPanel flow2 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        final JTextField imagePath = new JTextField(20);
        flow2.add(imagePath, BorderLayout.CENTER);
        final JButton browseImg = new JButton("Browse");
        browseImg.setActionCommand("image");
        browseImg.addActionListener(this);
        flow2.add(browseImg);
        imgPath.add(flow2, BorderLayout.PAGE_END);

        final JPanel keyPath = new JPanel(new BorderLayout());
        customOpt.add(keyPath, BorderLayout.PAGE_END);

        final JPanel flow3 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        flow3.add(new JLabel("Image file:"));
        keyPath.add(flow3, BorderLayout.PAGE_START);

        final JPanel flow4 = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
        final JTextField keymapPath = new JTextField(20);
        flow4.add(keymapPath, BorderLayout.CENTER);
        final JButton browseKey = new JButton("Browse");
        browseKey.setActionCommand("keymap");
        browseKey.addActionListener(this);
        flow4.add(browseKey);
        keyPath.add(flow4, BorderLayout.PAGE_END);
    }

    /**
     * Handles click on the browse buttons.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        // No action
    }
}
