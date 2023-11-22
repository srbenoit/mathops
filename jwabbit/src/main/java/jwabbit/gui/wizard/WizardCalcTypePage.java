package jwabbit.gui.wizard;

/*
 * This software was derived from the Wabbitemu software, as it existed in October 2015, by Steve Benoit. This software
 * is licensed under the GNU General Public License version 2 (GPLv2). See the disclaimers or warranty and liability
 * included in the terms of that license.
 */

import jwabbit.core.EnumCalcModel;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.Serial;

/**
 * SOURCE: gui/wizard/wizardcalctype.h, "WizardCalcTypePage" class.
 */
final class WizardCalcTypePage extends WizardPage {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -6291335388620629138L;

    /** Radio button. */
    private final JRadioButton calc73;

    /** Radio button. */
    private final JRadioButton calc82;

    /** Radio button. */
    private final JRadioButton calc83;

    /** Radio button. */
    private final JRadioButton calc83p;

    /** Radio button. */
    private final JRadioButton calc83pse;

    /** Radio button. */
    private final JRadioButton calc84p;

    /** Radio button. */
    private final JRadioButton calc84pse;

    /** Radio button. */
    private final JRadioButton calc85;

    /** Radio button. */
    private final JRadioButton calc86;

    /**
     * Constructs a new {@code WizardCalcTypePage}.
     *
     * <p>
     * SOURCE: gui/wizard/wizardcalctype.cpp, "WizardCalcTypePage::WizardCalcTypePage" constructor.
     *
     * @param theParent the parent wizard frame
     * @param theIcon   the icon to show next to the page title
     */
    WizardCalcTypePage(final RomWizardFrame theParent, final BufferedImage theIcon) {

        super(theParent, "type", theIcon, "Calculator Type");

        final JPanel sub1 = new JPanel(new BorderLayout(0, 15));
        add(sub1, BorderLayout.CENTER);
        final JLabel lbl2 = new JLabel("What type of calculator would you like to emulate?");
        sub1.add(lbl2, BorderLayout.PAGE_START);

        final JPanel sub2 = new JPanel(new BorderLayout());
        sub1.add(sub2, BorderLayout.CENTER);

        final JPanel choices = new JPanel(new GridLayout(3, 3));
        choices.setBorder(BorderFactory.createTitledBorder("Model"));
        sub2.add(choices, BorderLayout.PAGE_START);

        final ButtonGroup group = new ButtonGroup();

        this.calc73 = new JRadioButton("TI-73");
        group.add(this.calc73);
        this.calc82 = new JRadioButton("TI-82");
        group.add(this.calc82);
        this.calc83 = new JRadioButton("TI-83");
        group.add(this.calc83);

        this.calc83p = new JRadioButton("TI-83 Plus");
        group.add(this.calc83p);
        this.calc83pse = new JRadioButton("TI-83 Plus SE");
        group.add(this.calc83pse);
        this.calc84p = new JRadioButton("TI-84 Plus");
        group.add(this.calc84p);

        this.calc84pse = new JRadioButton("TI-84 Plus SE");
        group.add(this.calc84pse);
        this.calc85 = new JRadioButton("TI-85");
        group.add(this.calc85);
        this.calc86 = new JRadioButton("TI-86");
        group.add(this.calc86);

        choices.add(this.calc73);
        choices.add(this.calc83p);
        choices.add(this.calc84pse);

        choices.add(this.calc82);
        choices.add(this.calc83pse);
        choices.add(this.calc85);

        choices.add(this.calc83);
        choices.add(this.calc84p);
        choices.add(this.calc86);
    }

    /**
     * SOURCE: gui/wizard/wizardcalctype.cpp, "WizardCalcTypePage::GetModel" method.
     *
     * @return the selected model
     */
    public EnumCalcModel getModel() {

        if (this.calc73.isSelected()) {
            return EnumCalcModel.TI_73;
        } else if (this.calc82.isSelected()) {
            return EnumCalcModel.TI_82;
        } else if (this.calc83p.isSelected()) {
            return EnumCalcModel.TI_83;
        } else if (this.calc83pse.isSelected()) {
            return EnumCalcModel.TI_83PSE;
        } else if (this.calc84p.isSelected()) {
            return EnumCalcModel.TI_84P;
        } else if (this.calc84pse.isSelected()) {
            return EnumCalcModel.TI_84PSE;
        } else if (this.calc85.isSelected()) {
            return EnumCalcModel.TI_85;
        } else if (this.calc86.isSelected()) {
            return EnumCalcModel.TI_86;
        }
        return null;
    }

    /**
     * Enables or disables radio buttons.
     *
     * <p>
     * SOURCE: gui/wizard/wizardcalctype.cpp, "WizardCalcTypePage::EnableRadios" method.
     *
     * @param enableRadios true to enable; false to disable
     */
    void enableRadios(final boolean enableRadios) {

        this.calc82.setEnabled(enableRadios);
        this.calc83.setEnabled(enableRadios);
        this.calc85.setEnabled(enableRadios);
        this.calc86.setEnabled(enableRadios);
    }
}
