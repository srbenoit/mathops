package dev.mathops.app.adm.resource;

import dev.mathops.app.adm.AdminMainWindow;
import dev.mathops.app.adm.FixedData;
import dev.mathops.app.adm.Skin;
import dev.mathops.core.CoreConstants;
import dev.mathops.db.old.Cache;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;

/**
 * The "Resource" pane.
 */
public class ResourceTabPane extends JPanel implements ActionListener {

    /** A button action command. */
    private static final String LEND_CMD = "LEND";

    /** A button action command. */
    private static final String RETURN_CMD = "RETURN";

    /** A button action command. */
    private static final String HISTORY_CMD = "HISTORY";

    /** A button action command. */
    private static final String OUTSTANDING_CMD = "OUTSTANDING";

    /** A button action command. */
    private static final String ACTIVITY_CMD = "ACTIVITY";

    /** A button action command. */
    private static final String INVENTORY_CMD = "INVENTORY";

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 1647727447094579478L;

    /** The center panel. */
    private final JPanel cardPane;

    /** The card layout. */
    private final CardLayout cards;

    /** The "loan" card. */
    private final LendCard loanCard;

    /** The "return" card. */
    private final ReturnCard returnCard;

    /** The "student loan history" card. */
    private final StudentLoanHistoryCard stuLoanCard;

    /** The "outstanding loans" card. */
    private final OutstandingResourceCard outstandingCard;

    /** The "today's activity" card. */
    private final RecentActivityCard activityCard;

    /** The "inventory" card. */
    private final InventoryCard inventoryCard;

    /** The key of the currently showing card layout. */
    private String showing;

    /**
     * Constructs a new {@code ResourceTabPane}.
     *
     * @param theCache         the data cache
     * @param fixed            the fixed data
     */
    public ResourceTabPane(final Cache theCache, final Object theRenderingHint,
                           final FixedData fixed) {

        // Functions:
        // [ Loan Item ]
        // [ Return Item ]
        // [ Check Student Status ]
        // [ View Outstanding Items ]
        // [ Recent Activity ]
        // [ Inventory ]

        super(new BorderLayout(5, 5));
        setPreferredSize(AdminMainWindow.PREF_SIZE);

        setBackground(Skin.OFF_WHITE_GRAY);
        setBorder(BorderFactory.createCompoundBorder( //
                BorderFactory.createEtchedBorder(), //
                BorderFactory.createEmptyBorder(5, 5, 5, 5)));

        final JPanel menu = new JPanel();
        menu.setBackground(Skin.OFF_WHITE_GRAY);
        menu.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));
        add(menu, BorderLayout.NORTH);

        if (fixed.getClearanceLevel("RES_LOAN") != null) {
            menu.add(makeTopButton("Lend Item", LEND_CMD, Skin.LT_GREEN));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("RES_RETRN") != null) {
            menu.add(makeTopButton("Return Item", RETURN_CMD, Skin.LT_RED));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("RES_STU") != null) {
            menu.add(makeTopButton("Check Student's Loans", HISTORY_CMD, Skin.LT_CYAN
            ));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("RES_OUTST") != null) {
            menu.add(makeTopButton("View Outstanding Items", OUTSTANDING_CMD, Skin.LT_MAGENTA
            ));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("RES_TODAY") != null) {
            menu.add(
                    makeTopButton("Today's Activity", ACTIVITY_CMD, Skin.LT_YELLOW));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        if (fixed.getClearanceLevel("RES_IVENT") != null) {
            menu.add(makeTopButton("Inventory", INVENTORY_CMD, Skin.LT_BLUE));
            menu.add(new JLabel(CoreConstants.SPC));
        }

        this.cards = new CardLayout();
        this.cardPane = new JPanel(this.cards);
        this.cardPane.setBackground(Color.WHITE);
        add(this.cardPane, BorderLayout.CENTER);

        // Card 1: Loan Item

        if (fixed.getClearanceLevel("RES_LOAN") != null) {
            this.loanCard = new LendCard(theCache, fixed);
            this.cardPane.add(this.loanCard, LEND_CMD);
        } else {
            this.loanCard = null;
        }

        // Card 2: Return Item

        if (fixed.getClearanceLevel("RES_RETRN") != null) {
            this.returnCard = new ReturnCard(theCache);
            this.cardPane.add(this.returnCard, RETURN_CMD);
        } else {
            this.returnCard = null;
        }

        // Card 3: Check Student Loans

        if (fixed.getClearanceLevel("RES_STU") != null) {
            this.stuLoanCard = new StudentLoanHistoryCard(theCache, theRenderingHint);
            this.cardPane.add(this.stuLoanCard, HISTORY_CMD);
        } else {
            this.stuLoanCard = null;
        }

        // Card 4: View Outstanding Items

        if (fixed.getClearanceLevel("RES_OUTST") != null) {
            this.outstandingCard = new OutstandingResourceCard(theCache.conn);
            this.cardPane.add(this.outstandingCard, OUTSTANDING_CMD);
        } else {
            this.outstandingCard = null;
        }

        // Card 5: Today's Activity

        if (fixed.getClearanceLevel("RES_STU") != null) {
            this.activityCard = new RecentActivityCard(theCache);
            this.cardPane.add(this.activityCard, ACTIVITY_CMD);
        } else {
            this.activityCard = null;
        }

        // Card 6: Inventory

        if (fixed.getClearanceLevel("RES_IVENT") != null) {
            this.inventoryCard = new InventoryCard();
            this.cardPane.add(this.inventoryCard, INVENTORY_CMD);
        } else {
            this.inventoryCard = null;
        }

        this.cards.show(this.cardPane, LEND_CMD);
        this.showing = LEND_CMD;
    }

    /**
     * Creates a button for the top of the pane.
     *
     * @param title            the button title
     * @param command          the action command
     * @param background       the background color
     * @return the button panel
     */
    private JPanel makeTopButton(final String title, final String command, final Color background) {

        final JButton btn = new JButton(title);
        btn.setActionCommand(command);
        btn.addActionListener(this);
        btn.setFont(Skin.BIG_BUTTON_16_FONT);

        final JPanel menuBox = new JPanel(new BorderLayout());
        menuBox.setBackground(background);
        menuBox.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLoweredBevelBorder(), BorderFactory.createEmptyBorder(2, 2, 2, 2)));
        menuBox.add(btn, BorderLayout.CENTER);

        return menuBox;
    }

    /**
     * Sets the focus when this panel is activated.
     */
    public void focus() {

        if (LEND_CMD.equals(this.showing)) {
            if (this.loanCard != null) {
                this.loanCard.focus();
            }
        } else if (RETURN_CMD.equals(this.showing)) {
            if (this.returnCard != null) {
                this.returnCard.focus();
            }
        } else if (HISTORY_CMD.equals(this.showing)) {
            if (this.stuLoanCard != null) {
                this.stuLoanCard.focus();
            }
        } else if (OUTSTANDING_CMD.equals(this.showing)) {
            if (this.outstandingCard != null) {
                this.outstandingCard.focus();
            }
        } else if (ACTIVITY_CMD.equals(this.showing)) {
            if (this.activityCard != null) {
                this.activityCard.focus();
            }
        } else if (INVENTORY_CMD.equals(this.showing)) {
            if (this.inventoryCard != null) {
                this.inventoryCard.focus();
            }
        }
    }

    /**
     * Called when a button is pressed.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (LEND_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, LEND_CMD);
            this.showing = LEND_CMD;
            this.loanCard.reset();
        } else if (RETURN_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, RETURN_CMD);
            this.showing = RETURN_CMD;
            this.returnCard.reset();
        } else if (HISTORY_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, HISTORY_CMD);
            this.showing = HISTORY_CMD;
            this.stuLoanCard.reset();
        } else if (OUTSTANDING_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, OUTSTANDING_CMD);
            this.showing = OUTSTANDING_CMD;
            this.outstandingCard.reset();
        } else if (ACTIVITY_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, ACTIVITY_CMD);
            this.showing = ACTIVITY_CMD;
            this.activityCard.reset();
        } else if (INVENTORY_CMD.equals(cmd)) {
            this.cards.show(this.cardPane, INVENTORY_CMD);
            this.showing = INVENTORY_CMD;
            this.inventoryCard.reset();
        }
    }
}
