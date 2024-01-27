package dev.mathops.app.assessment.problemauthor;

import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.List;

/**
 * A panel that shows all variable values, allowing the user to sort in definition order or alphabetically.
 */
final class VariableValuesPanel extends JPanel implements ActionListener {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = 4880801178951759300L;

    /** CardLayout key for the "sorted in definition order" card. */
    private static final String DEFINITION_KEY = "DEFINITION";

    /** CardLayout key for the "sorted in alphabetical order" card. */
    private static final String ALPHABETICAL_KEY = "ALPHABETICAL";

    /** The layout. */
    private final CardLayout cardLayout;

    /** The evaluation context whose variables this pane will render. */
    private EvalContext context;

    /** The table model for values in declaration order. */
    private final DefaultTableModel declOrderModel;

    /** A table of variable values in declaration order. */
    private final JTable declOrderTable;

    /** Scroll pane for declaration order table. */
    private final JScrollPane declScroller;

    /** The table model for values in alphabetical order. */
    private final DefaultTableModel alphOrderModel;

    /** A table of variable values in alphabetical order. */
    private final JTable alphOrderTable;

    /** Scroll pane for alphabetical order table. */
    private final JScrollPane alphScroller;

    /**
     * Constructs a new {@code VariableValuesPanel}.
     *
     * @param bg the background color
     */
    VariableValuesPanel(final Color bg) {

        super();

        this.cardLayout = new CardLayout();
        setLayout(this.cardLayout);

        setBackground(bg);

        final Font font = getFont();
        final Font bigger = font.deriveFont(font.getSize2D() * 1.3f);

        final String[] columns = {"Variable", "Value"};

        this.declOrderModel = new DefaultTableModel(columns, 20);

        this.declOrderTable = new JTable(this.declOrderModel);

        this.declScroller =
                new JScrollPane(this.declOrderTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.declScroller.setPreferredSize(new Dimension(195, 1));

        this.alphOrderModel = new DefaultTableModel(columns, 20);

        this.alphOrderTable = new JTable(this.alphOrderModel);

        this.alphScroller =
                new JScrollPane(this.alphOrderTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
                        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        this.alphScroller.setPreferredSize(new Dimension(195, 1));

        final JPanel sortedByDeclaration = makeSortByDeclarationPanel(bg, bigger);
        add(sortedByDeclaration, DEFINITION_KEY);

        final JPanel sortedByAlphabetical = makeSortedAlphabeticallyPanel(bg, bigger);
        add(sortedByAlphabetical, ALPHABETICAL_KEY);
    }

    /**
     * Creates the panel that shows variables in declaration order.
     *
     * @param bg     the background color
     * @param bigger the header font
     * @return the panel
     */
    private JPanel makeSortByDeclarationPanel(final Color bg, final Font bigger) {

        final JPanel panel = new JPanel(new StackedBorderLayout());
        panel.setBackground(bg);

        final JPanel flowTop = makeHeader(bg, bigger);
        panel.add(flowTop, StackedBorderLayout.NORTH);

        final JPanel flowDef = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        flowDef.setBackground(bg);
        final JButton sortByDefDef = new JButton("Declaration Order");
        sortByDefDef.setEnabled(false);
        flowDef.add(sortByDefDef);
        final JButton sortByAlphDef = new JButton("Alphabetical Order");
        sortByAlphDef.setActionCommand(ALPHABETICAL_KEY);
        sortByAlphDef.addActionListener(this);
        flowDef.add(sortByAlphDef);
        panel.add(flowDef, StackedBorderLayout.NORTH);

        panel.add(this.declScroller, StackedBorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the panel that shows variables in alphabetical order.
     *
     * @param bg     the background color
     * @param bigger the header font
     * @return the panel
     */
    private JPanel makeSortedAlphabeticallyPanel(final Color bg, final Font bigger) {

        final JPanel panel = new JPanel(new StackedBorderLayout());
        add(panel, ALPHABETICAL_KEY);
        panel.setBackground(bg);

        final JPanel flowTop = makeHeader(bg, bigger);
        panel.add(flowTop, StackedBorderLayout.NORTH);

        final JPanel flowAlph = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 6));
        flowAlph.setBackground(bg);
        final JButton sortByDefAlph = new JButton("Declaration Order");
        sortByDefAlph.setActionCommand(DEFINITION_KEY);
        sortByDefAlph.addActionListener(this);
        flowAlph.add(sortByDefAlph);
        final JButton sortByAlphAlph = new JButton("Alphabetical Order");
        sortByAlphAlph.setEnabled(false);
        flowAlph.add(sortByAlphAlph);
        panel.add(flowAlph, StackedBorderLayout.NORTH);

        panel.add(this.alphScroller, StackedBorderLayout.CENTER);

        return panel;
    }

    /**
     * Creates the top header panel for either layout.
     *
     * @param bg     the background color
     * @param bigger the header font
     * @return the panel
     */
    private static JPanel makeHeader(final Color bg, final Font bigger) {

        final JPanel header = new JPanel(new FlowLayout(FlowLayout.LEADING, 10, 3));
        header.setBackground(bg);
        final JLabel varLabelsDef = new JLabel("Variables");
        varLabelsDef.setFont(bigger);
        header.add(varLabelsDef);

        return header;
    }

    /**
     * Sets the problem this panel will display.
     *
     * @param theContext the evaluation context
     */
    public void setEvalContext(final EvalContext theContext) {

        this.context = theContext;
    }

    /**
     * Clears all variable values.
     */
    void clearVariableValues() {

        this.declOrderModel.setRowCount(0);
        this.alphOrderModel.setRowCount(0);

        revalidate();
        repaint();
    }

    /**
     * Updates variable values.
     */
    void updateVariableValues() {

        if (this.context != null) {
            this.declOrderModel.setRowCount(0);
            this.alphOrderModel.setRowCount(0);

            final String[] row = new String[2];

            final List<String> names = this.context.getVariableNames();

            for (final String name : names) {
                final AbstractVariable var = this.context.getVariable(name);
                final Object value = var.getValue();

                row[0] = name;
                row[1] = value == null ? "(null)" : value.toString();

                this.declOrderModel.addRow(row);
            }

            names.sort(null);

            for (final String name : names) {
                final AbstractVariable var = this.context.getVariable(name);
                final Object value = var.getValue();

                row[0] = name;
                row[1] = value == null ? "(null)" : value.toString();

                this.alphOrderModel.addRow(row);
            }

            this.declOrderTable.revalidate();
            this.alphOrderTable.revalidate();
            repaint();
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

        if (DEFINITION_KEY.equals(cmd) || ALPHABETICAL_KEY.equals(cmd)) {
            this.cardLayout.show(this, cmd);
        }
    }
}
