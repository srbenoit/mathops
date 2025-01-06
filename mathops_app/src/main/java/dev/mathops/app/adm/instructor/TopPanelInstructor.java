package dev.mathops.app.adm.instructor;

import dev.mathops.app.adm.MainWindow;
import dev.mathops.app.adm.Skin;
import dev.mathops.app.adm.UserData;
import dev.mathops.commons.CoreConstants;
import dev.mathops.db.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The "Instructor" pane.
 */
public final class TopPanelInstructor extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String STATUS_CMD = "STATUS";

    /** A button action command. */
    private static final String DEADLINES_CMD = "DEADLINES";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 9054536124109728232L;

    /** The data cache. */
    private final Cache cache;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The card to display status by section. */
    private final CardStatusBySection cardStatusBySection;

    /** The card to display deadlines by section. */
    private final CardDeadlinesBySection cardDeadlinesBySection;

    /** The key of the currently showing card. */
    private String showing;

    /**
     * Constructs a new {@code TestingTabPane}.
     *
     * @param theCache the data cache
     * @param fixed    the fixed data
     */
    public TopPanelInstructor(final Cache theCache, final UserData fixed) {

        // Functions:
        // [ Map ]
        // [ Manage ]
        // [ Issue ]

        super(new BorderLayout(5, 5));
        setPreferredSize(MainWindow.PREF_SIZE);

        this.cache = theCache;

        setBackground(Skin.OFF_WHITE_GRAY);
        final Border padding = BorderFactory.createEmptyBorder(5, 5, 5, 5);
        final Border etching = BorderFactory.createEtchedBorder();
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(etching, padding);
        setBorder(newBorder);

        final JPanel menu = new JPanel();
        menu.setBackground(Skin.OFF_WHITE_GRAY);
        menu.setLayout(new FlowLayout(FlowLayout.LEFT, 0, 5));

        final JPanel mapButton = makeTopButton("Section Status", STATUS_CMD, Skin.LT_GREEN);
        menu.add(mapButton);
        menu.add(new JLabel(CoreConstants.SPC));

        final JPanel manageButton = makeTopButton("Deadlines", DEADLINES_CMD, Skin.LT_RED);
        menu.add(manageButton);
        menu.add(new JLabel(CoreConstants.SPC));

        add(menu, BorderLayout.PAGE_START);

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Skin.OFF_WHITE_GREEN);
        add(this.cardPane, BorderLayout.CENTER);

        this.cards.show(this.cardPane, STATUS_CMD);

        this.cardStatusBySection = new CardStatusBySection(theCache);
        this.cardPane.add(this.cardStatusBySection, STATUS_CMD);

        this.cardDeadlinesBySection = new CardDeadlinesBySection(this, theCache, fixed);
        this.cardPane.add(this.cardDeadlinesBySection, DEADLINES_CMD);

        this.showing = STATUS_CMD;
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title      the button title
     * @param command    the action command
     * @param background the background color
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background) {

        final JButton btn = new JButton(title);
        btn.setActionCommand(command);
        btn.addActionListener(this);
        btn.setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        final Border padding = BorderFactory.createEmptyBorder(2, 2, 2, 2);
        final Border bevel = BorderFactory.createLoweredBevelBorder();
        final CompoundBorder newBorder = BorderFactory.createCompoundBorder(bevel, padding);
        menuBox.setBorder(newBorder);
        menuBox.add(btn, BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        // No action
    }

    /**
     * Clears the display - this makes sure any open dialogs are closed so the app can close.
     */
    public void clearDisplay() {

        // No action
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (STATUS_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, STATUS_CMD);
            this.showing = STATUS_CMD;
            this.cardStatusBySection.reset();
        } else if (DEADLINES_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, DEADLINES_CMD);
            this.showing = DEADLINES_CMD;
            this.cardDeadlinesBySection.focus();
        }
    }
}

