package dev.mathops.assessment.variable.edit;

import dev.mathops.assessment.EParserMode;
import dev.mathops.assessment.EType;
import dev.mathops.assessment.NumberOrFormula;
import dev.mathops.assessment.document.template.DocFactory;
import dev.mathops.assessment.document.template.DocSimpleSpan;
import dev.mathops.assessment.formula.AbstractFormulaObject;
import dev.mathops.assessment.formula.Formula;
import dev.mathops.assessment.formula.IntegerFormulaVector;
import dev.mathops.assessment.formula.edit.FEFormula;
import dev.mathops.assessment.formula.edit.FormulaEditorPanel;
import dev.mathops.assessment.formula.edit.IFormulaEditorListener;
import dev.mathops.assessment.variable.AbstractFormattableVariable;
import dev.mathops.assessment.variable.AbstractVariable;
import dev.mathops.assessment.variable.EvalContext;
import dev.mathops.assessment.variable.VariableBoolean;
import dev.mathops.assessment.variable.VariableDerived;
import dev.mathops.assessment.variable.VariableInteger;
import dev.mathops.assessment.variable.VariableRandomBoolean;
import dev.mathops.assessment.variable.VariableRandomChoice;
import dev.mathops.assessment.variable.VariableRandomInteger;
import dev.mathops.assessment.variable.VariableRandomPermutation;
import dev.mathops.assessment.variable.VariableRandomReal;
import dev.mathops.assessment.variable.VariableRandomSimpleAngle;
import dev.mathops.assessment.variable.VariableReal;
import dev.mathops.assessment.variable.VariableSpan;
import dev.mathops.commons.CoreConstants;
import dev.mathops.commons.file.FileLoader;
import dev.mathops.commons.log.Log;
import dev.mathops.commons.parser.ParsingException;
import dev.mathops.commons.parser.xml.IElement;
import dev.mathops.commons.parser.xml.NonemptyElement;
import dev.mathops.commons.parser.xml.XmlContent;
import dev.mathops.commons.ui.layout.StackedBorderLayout;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.Serial;
import java.text.DecimalFormat;
import java.util.Objects;

/**
 * A panel that allows the user to edit a variable.
 */
public final class VariableEditorPanel extends JPanel
        implements ActionListener, DocumentListener, IFormulaEditorListener {

    /** Version for serialization. */
    @Serial
    private static final long serialVersionUID = -3472753167500461557L;

    /** The font size for formula editors. */
    private static final int FORMULA_FONT_SIZE = 14;

    /** The minimum width of a derived variable formula. */
    private static final int MIN_FORMULA_WIDTH = 300;

    /** A type string. */
    public static final String BOOLEAN = "Boolean";

    /** A type string. */
    public static final String INTEGER = "Integer";

    /** A type string. */
    public static final String REAL = "Real";

    /** A type string. */
    public static final String SPAN = "Span";

    /** A type string. */
    public static final String RANDOM_BOOLEAN = "Random Boolean";

    /** A type string. */
    public static final String RANDOM_INTEGER = "Random Integer";

    /** A type string. */
    public static final String RANDOM_REAL = "Random Real";

    /** A type string. */
    public static final String RANDOM_PERMUTATION = "Random Permutation";

    /** A type string. */
    public static final String RANDOM_CHOICE_INTEGER = "Random Choice (Integer)";

    /** A type string. */
    public static final String RANDOM_CHOICE_REAL = "Random Choice (Real)";

    /** A type string. */
    public static final String RANDOM_CHOICE_SPAN = "Random Choice (Span)";

    /** A type string. */
    public static final String RANDOM_SIMPLE_ANGLE = "Random Simple Angle (Integer)";

    /** A type string. */
    public static final String DERIVED_BOOLEAN = "Derived (Boolean)";

    /** A type string. */
    public static final String DERIVED_INTEGER = "Derived (Integer)";

    /** A type string. */
    public static final String DERIVED_REAL = "Derived (Real)";

    /** A type string. */
    public static final String DERIVED_SPAN = "Derived (Span)";

    /** An action command. */
    private static final String DELETE_CMD = "DELETE";

    /** An action command. */
    private static final String CHANGE_CMD = "CHANGE";

    /** An action command. */
    private static final String ADD_CHOICE_CMD = "ADD_CHOICE";

    /** An action command. */
    private static final String APPLY_CMD = "APPLY";

    /** The possible types of variable. */
    private static final String[] TYPES = {BOOLEAN, INTEGER, REAL, SPAN, RANDOM_BOOLEAN,
            RANDOM_INTEGER, RANDOM_REAL, RANDOM_CHOICE_INTEGER, RANDOM_CHOICE_REAL, RANDOM_CHOICE_SPAN,
            DERIVED_BOOLEAN, DERIVED_INTEGER, DERIVED_REAL, DERIVED_SPAN};

    /** The evaluation context in which the variable exists. */
    private final EvalContext evalContext;

    /** The container that holds this component. */
    private final JPanel container;

    /** The variable object this panel is presenting; {@code null} to create a new variable. */
    private AbstractVariable var;

    /** The variable type (a String from TYPES). */
    private String varType;

    /** A color. */
    private final Color unchangedColor;

    /** A color. */
    private final Color changedValidColor;

    /** A color. */
    private final Color changedInvalidColor;

    /** The variable name field. */
    private final JTextField nameField;

    /** The variable type choice. */
    private final JComboBox<String> typeChoice;

    /** A panel for "formattable" characteristics. */
    private final JPanel formattables;

    /** The format pattern field. */
    private final JTextField patternField;

    /** A panel for "Boolean" variables. */
    private final JPanel booleanProperties;

    /** The boolean value choice. */
    private final JComboBox<Boolean> booleanValue;

    /** A panel for "Integer" variables. */
    private final JPanel integerProperties;

    /** The integer value. */
    private final JTextField integerValue;

    /** A panel for "Real" variables. */
    private final JPanel realProperties;

    /** The real value. */
    private final JTextField realValue;

    /** A panel for "Span" variables. */
    private final JPanel spanProperties;

    /** The default height for the span properties panel. */
    private final int spanPropertiesHeight;

    /** The span value. */
    private final JTextField spanValue;

    /** A panel for "Min/Max" properties. */
    private final JPanel minMaxProperties;

    /** The derived min formula. */
    private final FormulaEditorPanel minFormula;

    /** The derived max formula. */
    private final FormulaEditorPanel maxFormula;

    /** The exclude formula (a number or a vector of numbers). */
    private final FormulaEditorPanel excludeFormula;

    /** A panel for "Derived" variables. */
    private final JPanel derivedProperties;

    /** The derived formula. */
    private final FormulaEditorPanel derivedFormula;

    /** A panel for "Choices". */
    private final JPanel choices;

    /** The default height for the choices panel. */
    private final int choicesHeight;

    /** The "Apply" button. */
    private final JButton applyButton;

    /**
     * Constructs a new {@code VariableEditorPanel} without an associated variable object, that can create a variable.
     *
     * @param theEvalContext the evaluation context in which the variable exists
     * @param theContainer   the container that holds this component
     * @param theVar         the variable
     */
    public VariableEditorPanel(final EvalContext theEvalContext, final JPanel theContainer,
                               final AbstractVariable theVar) {

        super(new StackedBorderLayout());

        this.evalContext = theEvalContext;
        this.container = theContainer;
        this.var = theVar;

        setBackground(Color.WHITE);

        final JPanel topFlow = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
        topFlow.setBackground(Color.WHITE);

        JButton deleteBtn;
        final byte[] icon = FileLoader.loadFileAsBytes(getClass(), "delete_icon.png", true);
        if (icon == null) {
            deleteBtn = new JButton("Delete");
        } else {
            try {
                final BufferedImage img = ImageIO.read(new ByteArrayInputStream(icon));
                deleteBtn = new JButton(new ImageIcon(img));
            } catch (final IOException ex) {
                Log.warning(ex);
                deleteBtn = new JButton("Delete");
            }
        }
        deleteBtn.setActionCommand(DELETE_CMD);
        deleteBtn.addActionListener(this);
        deleteBtn.setBorder(null);
        topFlow.add(deleteBtn);

        this.nameField = new JTextField(10);
        topFlow.add(this.nameField);
        final Border border = this.nameField.getBorder();

        final JLabel spc = new JLabel(CoreConstants.SPC);
        topFlow.add(spc);

        final Dimension spacerSize = new Dimension(deleteBtn.getPreferredSize().width
                + this.nameField.getPreferredSize().width + spc.getPreferredSize().width + 6,
                spc.getPreferredSize().height);

        this.unchangedColor = this.nameField.getBackground();
        final int r = this.unchangedColor.getRed();
        final int g = this.unchangedColor.getGreen();
        final int b = this.unchangedColor.getBlue();
        final int sum = r + g + b;

        if (sum > 380) {
            // "Light mode" - create new colors by darkening
            this.changedValidColor = new Color(r * 4 / 5, g, b * 4 / 5);
            this.changedInvalidColor = new Color(r, g * 4 / 5, b * 4 / 5);
        } else {
            // "Dark mode" - create new colors by lightening
            this.changedValidColor = new Color(r, Math.min(255, g * 5 / 4), b);
            this.changedInvalidColor = new Color(Math.min(255, r * 5 / 4), g, b);
        }

        this.typeChoice = new JComboBox<>(TYPES);
        this.typeChoice.setActionCommand(CHANGE_CMD);
        topFlow.add(this.typeChoice);

        this.formattables = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        this.formattables.setBackground(Color.WHITE);

        this.formattables.add(new JLabel("  Format:"));
        this.patternField = new JTextField(4);
        this.formattables.add(this.patternField);
        this.formattables.setVisible(false);
        topFlow.add(this.formattables);

        this.booleanProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        this.booleanProperties.setBackground(Color.WHITE);
        this.booleanProperties.add(new JLabel("  Value:"));
        final Boolean[] booleanValueChoices = {null, Boolean.TRUE, Boolean.FALSE};
        this.booleanValue = new JComboBox<>(booleanValueChoices);
        this.booleanValue.setActionCommand(CHANGE_CMD);
        this.booleanProperties.add(this.booleanValue);
        this.booleanProperties.setVisible(false);
        topFlow.add(this.booleanProperties);

        this.integerProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        this.integerProperties.setBackground(Color.WHITE);
        this.integerProperties.add(new JLabel("  Value:"));
        this.integerValue = new JTextField(8);
        this.integerProperties.add(this.integerValue);
        this.integerProperties.setVisible(false);
        topFlow.add(this.integerProperties);

        this.realProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        this.realProperties.setBackground(Color.WHITE);
        this.realProperties.add(new JLabel("  Value:"));
        this.realValue = new JTextField(8);
        this.realProperties.add(this.realValue);
        this.realProperties.setVisible(false);
        topFlow.add(this.realProperties);

        this.spanProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
        this.spanProperties.setBackground(Color.WHITE);

        final JLabel spanSpacer = new JLabel(CoreConstants.SPC);
        spanSpacer.setPreferredSize(spacerSize);
        this.spanProperties.add(spanSpacer);

        this.spanProperties.add(new JLabel("Content:"));
        this.spanValue = new JTextField(30);
        this.spanProperties.add(this.spanValue);
        final Dimension oldSpanPref = this.spanProperties.getPreferredSize();
        if (oldSpanPref.width < MIN_FORMULA_WIDTH) {
            oldSpanPref.width = MIN_FORMULA_WIDTH;
        }
        this.spanPropertiesHeight = oldSpanPref.height;
        this.spanProperties.setPreferredSize(new Dimension(oldSpanPref.width, 0));
        this.spanProperties.setVisible(false);

        final JPanel randomChoiceProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        randomChoiceProperties.setBackground(Color.WHITE);
        randomChoiceProperties.add(new JLabel("  Choices:"));
        // TODO: Add choices
        randomChoiceProperties.setVisible(false);
        topFlow.add(randomChoiceProperties);

        this.minMaxProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        this.minMaxProperties.setBackground(Color.WHITE);

        this.minMaxProperties.add(new JLabel("  Min:"));
        this.minFormula = new FormulaEditorPanel(FORMULA_FONT_SIZE, new Insets(4, 6, 4, 6));
        this.minFormula.setBorder(border);
        this.minMaxProperties.add(this.minFormula);

        this.minMaxProperties.add(new JLabel("  Max:"));
        this.maxFormula = new FormulaEditorPanel(FORMULA_FONT_SIZE, new Insets(4, 6, 4, 6));
        this.maxFormula.setBorder(border);
        this.minMaxProperties.add(this.maxFormula);

        this.minMaxProperties.setVisible(false);
        topFlow.add(this.minMaxProperties);

        final JPanel excludeProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 0));
        excludeProperties.setBackground(Color.WHITE);

        excludeProperties.add(new JLabel("  Exclude:"));
        this.excludeFormula = new FormulaEditorPanel(FORMULA_FONT_SIZE, new Insets(4, 6, 4, 6));
        this.excludeFormula.setBorder(border);
        excludeProperties.add(this.excludeFormula);

        excludeProperties.setVisible(false);
        topFlow.add(excludeProperties);

        this.derivedProperties = new JPanel(new FlowLayout(FlowLayout.LEADING, 2, 2));
        this.derivedProperties.setBackground(Color.WHITE);

        final JLabel derivedSpacer = new JLabel(CoreConstants.SPC);
        derivedSpacer.setPreferredSize(spacerSize);
        this.derivedProperties.add(derivedSpacer);

        this.derivedProperties.add(new JLabel("Formula:"));
        this.derivedFormula = new FormulaEditorPanel(FORMULA_FONT_SIZE, new Insets(4, 6, 4, 6));
        this.derivedFormula.setMinWidth(MIN_FORMULA_WIDTH);
        this.derivedFormula.setBorder(border);
        this.derivedProperties.add(this.derivedFormula);
        final Dimension derivedPref = this.derivedProperties.getPreferredSize();
        derivedPref.height = 0;
        this.derivedProperties.setPreferredSize(derivedPref);
        this.derivedProperties.setVisible(false);

        //

        this.choices = new JPanel(new FlowLayout(FlowLayout.LEADING, 6, 2));
        this.choices.setBackground(Color.WHITE);

        final JLabel choicesSpacer = new JLabel(CoreConstants.SPC);
        choicesSpacer.setPreferredSize(spacerSize);
        this.choices.add(choicesSpacer);

        this.choices.add(new JLabel("Choices:"));
        // TODO: Add list of excludes
        final Dimension oldChoicesPref = this.choices.getPreferredSize();
        if (oldChoicesPref.width < MIN_FORMULA_WIDTH) {
            oldChoicesPref.width = MIN_FORMULA_WIDTH;
        }
        this.choicesHeight = oldChoicesPref.height;
        this.choices.setPreferredSize(new Dimension(oldChoicesPref.width, 0));
        this.choices.setVisible(false);

        final byte[] addicon = FileLoader.loadFileAsBytes(getClass(), "add_icon.png", true);

        JButton addChoiceBtn;
        if (addicon == null) {
            addChoiceBtn = new JButton("Add");
        } else {
            try {
                final BufferedImage img = ImageIO.read(new ByteArrayInputStream(addicon));
                addChoiceBtn = new JButton(new ImageIcon(img));
            } catch (final IOException ex) {
                Log.warning(ex);
                addChoiceBtn = new JButton("Add");
            }
        }
        addChoiceBtn.setActionCommand(ADD_CHOICE_CMD);
        addChoiceBtn.addActionListener(this);
        addChoiceBtn.setBorder(null);
        this.choices.add(addChoiceBtn);

        //

        this.applyButton = new JButton("Apply Changes");
        this.applyButton.setActionCommand(APPLY_CMD);
        this.applyButton.addActionListener(this);
        this.applyButton.setVisible(false);
        topFlow.add(new JLabel("  "));
        topFlow.add(this.applyButton);

        add(topFlow, StackedBorderLayout.NORTH);
        add(this.spanProperties, StackedBorderLayout.NORTH);
        add(this.derivedProperties, StackedBorderLayout.NORTH);

        if (theVar != null) {
            this.nameField.setText(theVar.name);

            String type = null;

            switch (theVar) {
                case VariableBoolean variableBoolean -> type = BOOLEAN;
                case VariableInteger variableInteger -> type = INTEGER;
                case VariableReal variableReal -> type = REAL;
                case VariableSpan variableSpan -> type = SPAN;
                case VariableRandomBoolean variableRandomBoolean -> type = RANDOM_BOOLEAN;
                case VariableRandomInteger variableRandomInteger -> type = RANDOM_INTEGER;
                case VariableRandomReal variableRandomReal -> type = RANDOM_REAL;
                case VariableRandomPermutation variableRandomPermutation -> type = RANDOM_PERMUTATION;
                case VariableRandomChoice variableRandomChoice -> {
                    if (theVar.type == EType.INTEGER) {
                        type = RANDOM_CHOICE_INTEGER;
                    } else if (theVar.type == EType.REAL) {
                        type = RANDOM_CHOICE_REAL;
                    } else if (theVar.type == EType.SPAN) {
                        type = RANDOM_CHOICE_SPAN;
                    } else {
                        Log.warning("Unsupported random choice type: ", theVar.type);
                    }
                }
                case VariableRandomSimpleAngle variableRandomSimpleAngle -> type = RANDOM_SIMPLE_ANGLE;
                case VariableDerived variableDerived -> {
                    if (theVar.type == EType.BOOLEAN) {
                        type = DERIVED_BOOLEAN;
                    } else if (theVar.type == EType.INTEGER) {
                        type = DERIVED_INTEGER;
                    } else if (theVar.type == EType.REAL) {
                        type = DERIVED_REAL;
                    } else if (theVar.type == EType.SPAN) {
                        type = DERIVED_SPAN;
                    } else {
                        Log.warning("Unsupported derived type: ", theVar.type);
                    }
                }
                default -> Log.warning("Unsupported variable type: ", theVar.getClass().getSimpleName());
            }

            this.typeChoice.setSelectedItem(type);
            this.varType = type;

            if (theVar instanceof final AbstractFormattableVariable formattableVar) {
                if (INTEGER.equals(type) || REAL.equals(type) || RANDOM_INTEGER.equals(type)
                        || RANDOM_REAL.equals(type) || RANDOM_CHOICE_INTEGER.equals(type)
                        || RANDOM_CHOICE_REAL.equals(type) || DERIVED_INTEGER.equals(type)
                        || DERIVED_REAL.equals(type)) {
                    this.formattables.setVisible(true);
                    final String pattern = formattableVar.getFormatPattern();
                    this.patternField.setText(pattern == null ? CoreConstants.EMPTY : pattern);
                }
            }

            if (theVar instanceof final VariableBoolean varBoolean) {
                this.booleanProperties.setVisible(true);
                this.booleanValue.setSelectedItem(varBoolean.getValue());
            } else if (theVar instanceof final VariableInteger varInteger) {
                this.integerProperties.setVisible(true);
                final Object value = varInteger.getValue();
                if (value != null) {
                    this.integerValue.setText(value.toString());
                }
            } else if (theVar instanceof final VariableReal varReal) {
                this.realProperties.setVisible(true);
                final Object value = varReal.getValue();
                if (value != null) {
                    this.realValue.setText(value.toString());
                }
            } else if (theVar instanceof final VariableSpan varSpan) {
                this.spanProperties.setPreferredSize(oldSpanPref);
                this.spanProperties.setVisible(true);
                final Object value = varSpan.getValue();
                if (value instanceof final DocSimpleSpan span) {
                    this.spanValue.setText(span.toXml(0));
                }
            } else if (theVar instanceof final VariableRandomInteger varRandomInteger) {
                this.minMaxProperties.setVisible(true);
                this.minFormula.setFormula(varRandomInteger.getMin());
                this.maxFormula.setFormula(varRandomInteger.getMax());

                excludeProperties.setVisible(true);
                final Formula[] excludes = varRandomInteger.getExcludes();
                final IntegerFormulaVector vec = new IntegerFormulaVector();
                if (excludes != null) {
                    for (final Formula exclude : excludes) {
                        vec.addChild(exclude);
                    }
                }
                final Formula vecFormula = new Formula(vec);
                this.excludeFormula.setFormula(new NumberOrFormula(vecFormula));
            } else if (theVar instanceof final VariableRandomReal varRandomReal) {
                this.minMaxProperties.setVisible(true);
                this.minFormula.setFormula(varRandomReal.getMin());
                this.maxFormula.setFormula(varRandomReal.getMax());
            } else if (theVar instanceof final VariableRandomChoice varRandomChoice) {
                randomChoiceProperties.setVisible(true);

                // TODO: Populate choices - this should be typed and editable...

                final int w = this.choices.getPreferredSize().width;
                this.choices.setPreferredSize(new Dimension(w, this.choicesHeight));
                this.choices.setVisible(true);
            } else if (theVar instanceof final VariableDerived derived) {
                this.derivedFormula.setFormula(new NumberOrFormula(derived.getFormula()));

                final Dimension derivedFormulaPref = this.derivedFormula.getPreferredSize();
                if (derivedFormulaPref.width < MIN_FORMULA_WIDTH) {
                    derivedFormulaPref.width = MIN_FORMULA_WIDTH;
                }
                final Dimension derivedPropPref = this.derivedProperties.getPreferredSize();
                if (derivedPropPref.height < derivedFormulaPref.height) {
                    derivedPropPref.height = derivedFormulaPref.height;
                }
                this.derivedFormula.setPreferredSize(derivedFormulaPref);
                this.derivedProperties.setPreferredSize(derivedPropPref);
                this.derivedProperties.setVisible(true);

                if (DERIVED_INTEGER.equals(type) || DERIVED_REAL.equals(type)) {
                    this.minMaxProperties.setVisible(true);
                    this.minFormula.setFormula(derived.getMin());
                    this.maxFormula.setFormula(derived.getMax());
                }

                if (DERIVED_INTEGER.equals(type)) {
                    excludeProperties.setVisible(true);
                    final Formula[] excludes = derived.getExcludes();
                    final IntegerFormulaVector vec = new IntegerFormulaVector();
                    if (excludes != null) {
                        for (final Formula exclude : excludes) {
                            vec.addChild(exclude);
                        }
                    }
                    final Formula vecFormula = new Formula(vec);
                    this.excludeFormula.setFormula(new NumberOrFormula(vecFormula));
                }
            }
        }

        this.nameField.getDocument().addDocumentListener(this);
        this.typeChoice.addActionListener(this);
        this.patternField.getDocument().addDocumentListener(this);
        this.booleanValue.addActionListener(this);
        this.integerValue.getDocument().addDocumentListener(this);
        this.realValue.getDocument().addDocumentListener(this);
        this.spanValue.getDocument().addDocumentListener(this);
        this.derivedFormula.addListener(this);
        this.minFormula.addListener(this);
        this.maxFormula.addListener(this);
        this.excludeFormula.addListener(this);
    }

    /**
     * Called when the "Delete" button is clicked.
     *
     * @param e the action event
     */
    @Override
    public void actionPerformed(final ActionEvent e) {

        final String cmd = e.getActionCommand();

        if (DELETE_CMD.equals(cmd)) {
            Container comp = getParent();
            while (comp.getParent() != null) {
                comp = comp.getParent();
            }

            // TODO: See if the variable is used anywhere... warn if so

            final String msg = "Delete variable '" + this.var.name + "'?";

            if (JOptionPane.showConfirmDialog(comp, msg, "Delete Variable",
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {

                this.evalContext.removeVariable(this.var.name);
                this.container.remove(this);
                this.container.revalidate();
                this.container.repaint();
            }
        } else if (CHANGE_CMD.equals(cmd)) {
            updateFieldsAndApplyButton();
        } else if (ADD_CHOICE_CMD.equals(cmd)) {
            addChoice();
        } else if (APPLY_CMD.equals(cmd)) {
            applyChanges();
        }
    }

    /**
     * Called when a text field's document changes.
     *
     * @param e the document event
     */
    @Override
    public void insertUpdate(final DocumentEvent e) {

        updateFieldsAndApplyButton();
    }

    /**
     * Called when a text field's document changes.
     *
     * @param e the document event
     */
    @Override
    public void removeUpdate(final DocumentEvent e) {

        updateFieldsAndApplyButton();
    }

    /**
     * Called when a text field's document changes.
     *
     * @param e the document event
     */
    @Override
    public void changedUpdate(final DocumentEvent e) {

        updateFieldsAndApplyButton();
    }

    /**
     * Called when a formula is edited.
     *
     * @param formula the edited formula
     */
    @Override
    public void formulaEdited(final FEFormula formula) {

        updateFieldsAndApplyButton();
    }

    /**
     * Updates the fields and the "Apply Changes" button state based on field values.
     *
     * @return true if values are valid; false if not
     */
    private boolean updateFieldsAndApplyButton() {

        boolean changes = false;
        boolean valid = true;

        // See if name is different
        final String newName = this.nameField.getText();
        final AbstractVariable existingWithSameName = this.evalContext.getVariable(newName);
        if (existingWithSameName == null) {
            // This variable name is good, and different
            this.nameField.setBackground(this.changedValidColor);
            changes = true;
        } else if (existingWithSameName == this.var) {
            // The variable name is back to original
            this.nameField.setBackground(this.unchangedColor);
        } else {
            // The variable name is a duplicate!
            this.nameField.setBackground(this.changedInvalidColor);
            changes = true;
            valid = false;
        }

        final Object selectedType = this.typeChoice.getSelectedItem();
        if (Objects.equals(selectedType, this.varType)) {
            this.typeChoice.setBackground(this.unchangedColor);
        } else {
            this.typeChoice.setBackground(this.changedValidColor);
            changes = true;
        }

        boolean isBoolean = false;
        boolean isInteger = false;
        boolean isReal = false;
        boolean isSpan = false;
        boolean isFormattable = false;
        boolean hasMinMax = false;
        boolean isDerived = false;
        final boolean hasExcludes = false;
        boolean hasChoices = false;

        if (BOOLEAN.equals(selectedType)) {
            isBoolean = true;
            if (this.booleanValue.getSelectedItem() == null) {
                this.booleanValue.setBackground(this.changedInvalidColor);
                valid = false;
            } else {
                this.booleanValue.setBackground(this.unchangedColor);
            }
        } else if (INTEGER.equals(selectedType)) {
            isInteger = true;
            isFormattable = true;
            final String integerText = this.integerValue.getText();
            if (integerText == null || integerText.isBlank()) {
                this.integerValue.setBackground(this.changedInvalidColor);
                valid = false;
            } else {
                try {
                    Long.valueOf(integerText);
                    this.integerValue.setBackground(this.unchangedColor);
                } catch (final NumberFormatException ex) {
                    this.integerValue.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }
        } else if (REAL.equals(selectedType)) {
            isReal = true;
            isFormattable = true;
            final String realText = this.realValue.getText();
            if (realText == null || realText.isBlank()) {
                this.realValue.setBackground(this.changedInvalidColor);
                valid = false;
            } else {
                try {
                    Double.valueOf(realText);
                    this.realValue.setBackground(this.unchangedColor);
                } catch (final NumberFormatException ex) {
                    this.realValue.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }
        } else if (SPAN.equals(selectedType)) {
            isSpan = true;
            final String spanText = this.spanValue.getText();
            if (spanText == null || spanText.isBlank()) {
                this.spanValue.setBackground(this.changedInvalidColor);
                valid = false;
            } else {
                final String newText = "<a>" + spanText + "</a>";
                try {
                    final XmlContent spanXmlContent = new XmlContent(newText, false, false);
                    final IElement top = spanXmlContent.getToplevel();
                    if (!(top instanceof final NonemptyElement nonempty) || (DocFactory
                            .parseSpan(this.evalContext, nonempty, EParserMode.NORMAL) == null)) {
                        this.spanValue.setBackground(this.changedInvalidColor);
                        valid = false;
                    } else {
                        this.spanValue.setBackground(this.unchangedColor);
                    }
                } catch (final ParsingException ex) {
                    this.spanValue.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }
        } else if (RANDOM_INTEGER.equals(selectedType)) {
            isFormattable = true;
            hasMinMax = true;

            final String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                this.patternField.setBackground(this.unchangedColor);
            } else {
                try {
                    new DecimalFormat().applyPattern(newPattern);
                    this.patternField.setBackground(this.unchangedColor);
                } catch (final IllegalArgumentException ex) {
                    this.patternField.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }

            NumberOrFormula oldMin = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMin = randInt.getMin();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMin = randReal.getMin();
            } else if (this.var instanceof final VariableRandomSimpleAngle randAngle) {
                oldMin = randAngle.getMin();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMin = derived.getMin();
            }

            final FEFormula newMinRoot = this.minFormula.getRoot();
            if (newMinRoot.getTopLevel() == null) {
                if (oldMin == null) {
                    this.minFormula.setBackground(this.unchangedColor);
                } else {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMin = newMinRoot.generate();

                if (newMin == null) {
                    this.minFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMin.equals(oldMin)) {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.minFormula.setBackground(this.unchangedColor);
                }
            }

            NumberOrFormula oldMax = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMax = randInt.getMax();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMax = randReal.getMax();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMax = derived.getMax();
            }

            final FEFormula newMaxRoot = this.maxFormula.getRoot();
            if (newMaxRoot.getTopLevel() == null) {
                if (oldMax == null) {
                    this.maxFormula.setBackground(this.unchangedColor);
                } else {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMax = newMaxRoot.generate();

                if (newMax == null) {
                    this.maxFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMax.equals(oldMax)) {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.maxFormula.setBackground(this.unchangedColor);
                }
            }

            Formula oldExcludes = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldExcludes = makeExcludeVector(randInt.getExcludes());
            } else if (this.var instanceof final VariableDerived derived) {
                oldExcludes = makeExcludeVector(derived.getExcludes());
            }

            final FEFormula newExcludeRoot = this.excludeFormula.getRoot();
            if (newExcludeRoot.getTopLevel() == null) {
                if (oldExcludes == null) {
                    this.excludeFormula.setBackground(this.unchangedColor);
                } else {
                    this.excludeFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newExclude = newExcludeRoot.generate();

                if (newExclude == null) {
                    this.excludeFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newExclude.equals(oldExcludes)) {
                    this.excludeFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.excludeFormula.setBackground(this.unchangedColor);
                }
            }

        } else if (RANDOM_REAL.equals(selectedType)) {
            isFormattable = true;
            hasMinMax = true;

            final String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                this.spanValue.setBackground(this.unchangedColor);
            } else {
                try {
                    new DecimalFormat().applyPattern(newPattern);
                    this.patternField.setBackground(this.unchangedColor);
                } catch (final IllegalArgumentException ex) {
                    this.nameField.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }

            NumberOrFormula oldMin = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMin = randInt.getMin();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMin = randReal.getMin();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMin = derived.getMin();
            }

            final FEFormula newMinRoot = this.minFormula.getRoot();
            if (newMinRoot.getTopLevel() == null) {
                if (oldMin == null) {
                    this.minFormula.setBackground(this.unchangedColor);
                } else {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMin = newMinRoot.generate();

                if (newMin == null) {
                    this.minFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMin.equals(oldMin)) {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.minFormula.setBackground(this.unchangedColor);
                }
            }

            NumberOrFormula oldMax = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMax = randInt.getMax();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMax = randReal.getMax();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMax = derived.getMax();
            }

            final FEFormula newMaxRoot = this.maxFormula.getRoot();
            if (newMaxRoot.getTopLevel() == null) {
                if (oldMax == null) {
                    this.maxFormula.setBackground(this.unchangedColor);
                } else {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMax = newMaxRoot.generate();

                if (newMax == null) {
                    this.maxFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMax.equals(oldMax)) {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.maxFormula.setBackground(this.unchangedColor);
                }
            }

        } else if (RANDOM_CHOICE_INTEGER.equals(selectedType)
                || RANDOM_CHOICE_REAL.equals(selectedType)) {
            isFormattable = true;
            hasChoices = true;

            final String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                this.spanValue.setBackground(this.unchangedColor);
            } else {
                try {
                    new DecimalFormat().applyPattern(newPattern);
                    this.patternField.setBackground(this.unchangedColor);
                } catch (final IllegalArgumentException ex) {
                    this.nameField.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }

        } else if (RANDOM_CHOICE_SPAN.equals(selectedType)) {
            hasChoices = true;
        } else if (DERIVED_BOOLEAN.equals(selectedType)) {
            isDerived = true;
        } else if (DERIVED_INTEGER.equals(selectedType)) {
            isFormattable = true;
            hasMinMax = true;
            isDerived = true;

            final String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                this.spanValue.setBackground(this.unchangedColor);
            } else {
                try {
                    new DecimalFormat().applyPattern(newPattern);
                    this.patternField.setBackground(this.unchangedColor);
                } catch (final IllegalArgumentException ex) {
                    this.nameField.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }

            NumberOrFormula oldMin = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMin = randInt.getMin();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMin = randReal.getMin();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMin = derived.getMin();
            }

            final FEFormula newMinRoot = this.minFormula.getRoot();
            if (newMinRoot.getTopLevel() == null) {
                if (oldMin == null) {
                    this.minFormula.setBackground(this.unchangedColor);
                } else {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMin = newMinRoot.generate();

                if (newMin == null) {
                    this.minFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMin.equals(oldMin)) {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.minFormula.setBackground(this.unchangedColor);
                }
            }

            NumberOrFormula oldMax = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMax = randInt.getMax();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMax = randReal.getMax();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMax = derived.getMax();
            }

            final FEFormula newMaxRoot = this.maxFormula.getRoot();
            if (newMaxRoot.getTopLevel() == null) {
                if (oldMax == null) {
                    this.maxFormula.setBackground(this.unchangedColor);
                } else {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMax = newMaxRoot.generate();

                if (newMax == null) {
                    this.maxFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMax.equals(oldMax)) {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.maxFormula.setBackground(this.unchangedColor);
                }
            }

            Formula oldExcludes = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldExcludes = makeExcludeVector(randInt.getExcludes());
            } else if (this.var instanceof final VariableDerived derived) {
                oldExcludes = makeExcludeVector(derived.getExcludes());
            }

            final FEFormula newExcludeRoot = this.excludeFormula.getRoot();
            if (newExcludeRoot.getTopLevel() == null) {
                if (oldExcludes == null) {
                    this.excludeFormula.setBackground(this.unchangedColor);
                } else {
                    this.excludeFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newExclude = newExcludeRoot.generate();

                if (newExclude == null) {
                    this.excludeFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newExclude.equals(oldExcludes)) {
                    this.excludeFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.excludeFormula.setBackground(this.unchangedColor);
                }
            }

        } else if (DERIVED_REAL.equals(selectedType)) {
            isFormattable = true;
            isDerived = true;
            hasMinMax = true;

            final String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                this.spanValue.setBackground(this.unchangedColor);
            } else {
                try {
                    new DecimalFormat().applyPattern(newPattern);
                    this.patternField.setBackground(this.unchangedColor);
                } catch (final IllegalArgumentException ex) {
                    this.nameField.setBackground(this.changedInvalidColor);
                    valid = false;
                }
            }

            NumberOrFormula oldMin = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMin = randInt.getMin();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMin = randReal.getMin();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMin = derived.getMin();
            }

            final FEFormula newMinRoot = this.minFormula.getRoot();
            if (newMinRoot.getTopLevel() == null) {
                if (oldMin == null) {
                    this.minFormula.setBackground(this.unchangedColor);
                } else {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMin = newMinRoot.generate();

                if (newMin == null) {
                    this.minFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMin.equals(oldMin)) {
                    this.minFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.minFormula.setBackground(this.unchangedColor);
                }
            }

            NumberOrFormula oldMax = null;
            if (this.var instanceof final VariableRandomInteger randInt) {
                oldMax = randInt.getMax();
            } else if (this.var instanceof final VariableRandomReal randReal) {
                oldMax = randReal.getMax();
            } else if (this.var instanceof final VariableDerived derived) {
                oldMax = derived.getMax();
            }

            final FEFormula newMaxRoot = this.maxFormula.getRoot();
            if (newMaxRoot.getTopLevel() == null) {
                if (oldMax == null) {
                    this.maxFormula.setBackground(this.unchangedColor);
                } else {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                }
            } else {
                final Formula newMax = newMaxRoot.generate();

                if (newMax == null) {
                    this.maxFormula.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else if (!newMax.equals(oldMax)) {
                    this.maxFormula.setBackground(this.changedValidColor);
                    changes = true;
                } else {
                    this.maxFormula.setBackground(this.unchangedColor);
                }
            }

        } else if (DERIVED_SPAN.equals(selectedType)) {
            isDerived = true;
        }

        boolean needsLayout = false;

        if (this.booleanProperties.isVisible() != isBoolean) {
            this.booleanProperties.setVisible(isBoolean);
            needsLayout = true;
        }
        if (this.integerProperties.isVisible() != isInteger) {
            this.integerProperties.setVisible(isInteger);
            needsLayout = true;
        }
        if (this.realProperties.isVisible() != isReal) {
            this.realProperties.setVisible(isReal);
            needsLayout = true;
        }
        if (this.spanProperties.isVisible() != isSpan) {
            final int w = this.spanProperties.getPreferredSize().width;
            final int h = isSpan ? this.spanPropertiesHeight : 0;
            this.spanProperties.setPreferredSize(new Dimension(w, h));
            this.spanProperties.setVisible(isSpan);
            needsLayout = true;
        }
        if (this.formattables.isVisible() != isFormattable) {
            this.formattables.setVisible(isFormattable);
            needsLayout = true;
        }
        if (this.minMaxProperties.isVisible() != hasMinMax) {
            this.minMaxProperties.setVisible(hasMinMax);
            needsLayout = true;
        }
        if (this.derivedProperties.isVisible() != isDerived) {
            final Dimension derivedFormulaPref = this.derivedFormula.getPreferredSize();
            if (derivedFormulaPref.width < MIN_FORMULA_WIDTH) {
                derivedFormulaPref.width = MIN_FORMULA_WIDTH;
            }
            final Dimension derivedPropPref = this.derivedProperties.getPreferredSize();
            if (derivedPropPref.height < derivedFormulaPref.height) {
                derivedPropPref.height = derivedFormulaPref.height;
            }
            this.derivedFormula.setPreferredSize(derivedFormulaPref);
            this.derivedProperties.setPreferredSize(derivedPropPref);
            this.derivedProperties.setVisible(isDerived);
            needsLayout = true;
        }
        if (this.choices.isVisible() != hasChoices) {
            final int w = this.choices.getPreferredSize().width;
            final int h = hasChoices ? this.choicesHeight : 0;
            this.choices.setPreferredSize(new Dimension(w, h));
            this.choices.setVisible(hasChoices);
            needsLayout = true;
        }

        // See if format pattern is different
        if (this.var instanceof final AbstractFormattableVariable formattable) {
            String newPattern = this.patternField.getText();
            if (newPattern.isBlank()) {
                newPattern = null;
            }
            final String oldPattern = formattable.getFormatPattern();

            if (Objects.equals(newPattern, oldPattern)) {
                this.patternField.setBackground(this.unchangedColor);
            } else {
                changes = true;
                if (newPattern == null) {
                    this.patternField.setBackground(this.changedValidColor);
                } else {
                    try {
                        new DecimalFormat().applyPattern(newPattern);
                        this.patternField.setBackground(this.changedValidColor);
                    } catch (final IllegalArgumentException ex) {
                        this.nameField.setBackground(this.changedInvalidColor);
                        valid = false;
                    }
                }
            }
        }

        // See if value is different
        final Object oldValue = this.var.getValue();
        switch (this.var) {
            case VariableBoolean variableBoolean -> {

                final Object newValue = this.booleanValue.getSelectedItem();
                if (Objects.equals(newValue, oldValue)) {
                    this.booleanValue.setBackground(this.unchangedColor);
                } else {
                    this.booleanValue.setBackground(this.changedValidColor);
                    changes = true;
                }
            }
            case VariableInteger variableInteger -> {

                final String newText = this.integerValue.getText();
                try {
                    final Long newValue = Long.valueOf(newText);
                    if (Objects.equals(newValue, oldValue)) {
                        this.integerValue.setBackground(this.unchangedColor);
                    } else {
                        this.integerValue.setBackground(this.changedValidColor);
                        changes = true;
                    }
                } catch (final NumberFormatException ex) {
                    this.integerValue.setBackground(this.changedInvalidColor);
                    valid = false;
                }

            }
            case VariableReal variableReal -> {

                final String newText = this.realValue.getText();
                try {
                    final Double newValue = Double.valueOf(newText);
                    if (Objects.equals(newValue, oldValue)) {
                        this.realValue.setBackground(this.unchangedColor);
                    } else {
                        this.realValue.setBackground(this.changedValidColor);
                        changes = true;
                    }
                } catch (final NumberFormatException ex) {
                    this.realValue.setBackground(this.changedInvalidColor);
                    valid = false;
                }

            }
            case VariableSpan variableSpan -> {

                final String spanText = this.spanValue.getText();
                if (spanText == null || spanText.isBlank()) {
                    // Span should not be empty in a span variable
                    this.spanValue.setBackground(this.changedInvalidColor);
                    changes = true;
                    valid = false;
                } else {
                    final String newText = "<a>" + spanText + "</a>";
                    try {
                        final XmlContent spanXmlContent = new XmlContent(newText, false, false);
                        final IElement top = spanXmlContent.getToplevel();
                        if (top instanceof final NonemptyElement nonempty) {
                            final DocSimpleSpan newValue =
                                    DocFactory.parseSpan(this.evalContext, nonempty, EParserMode.NORMAL);

                            if (newValue == null) {
                                this.spanValue.setBackground(this.changedInvalidColor);
                                changes = true;
                                valid = false;
                            } else if (oldValue == null) {
                                this.spanValue.setBackground(this.changedValidColor);
                                changes = true;
                            } else {
                                final String newValueText = newValue.toString();
                                final String oldValueText = oldValue.toString();

                                if (newValueText.equals(oldValueText)) {
                                    this.spanValue.setBackground(this.unchangedColor);
                                } else {
                                    this.spanValue.setBackground(this.changedValidColor);
                                    changes = true;
                                }
                            }
                        } else {
                            this.spanValue.setBackground(this.changedInvalidColor);
                            valid = false;
                        }
                    } catch (final ParsingException ex) {
                        this.spanValue.setBackground(this.changedInvalidColor);
                        valid = false;
                    }
                }
            }
            case final VariableRandomInteger randInt -> {
                final NumberOrFormula oldMin = randInt.getMin();
                final FEFormula minRoot = this.minFormula.getRoot();

                // TODO: Check that min/max will generate integers

                if (minRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMin == null) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMin = minRoot.generate();

                        if (newMin == null) {
                            this.minFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.minFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMin = minRoot.generate();

                    if (newMin == null) {
                        this.minFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMin, oldMin)) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        this.minFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

                final NumberOrFormula oldMax = randInt.getMax();
                final FEFormula maxRoot = this.maxFormula.getRoot();

                if (maxRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMax == null) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMax = maxRoot.generate();

                        if (newMax == null) {
                            this.maxFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.maxFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMax = maxRoot.generate();

                    if (newMax == null) {
                        this.maxFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMax, oldMax)) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        this.maxFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

            }
            case final VariableRandomReal randReal -> {

                final NumberOrFormula oldMin = randReal.getMin();
                final FEFormula minRoot = this.minFormula.getRoot();

                if (minRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMin == null) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMin = minRoot.generate();

                        if (newMin == null) {
                            this.minFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.minFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMin = minRoot.generate();

                    if (newMin == null) {
                        this.minFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMin, oldMin)) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        this.minFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

                final NumberOrFormula oldMax = randReal.getMax();
                final FEFormula maxRoot = this.maxFormula.getRoot();

                if (maxRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMax == null) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMax = maxRoot.generate();

                        if (newMax == null) {
                            this.maxFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.maxFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMax = maxRoot.generate();

                    if (newMax == null) {
                        this.maxFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMax, oldMax)) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        this.maxFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

            }
            case final VariableDerived derived -> {

                final NumberOrFormula oldMin = derived.getMin();
                final FEFormula minRoot = this.minFormula.getRoot();

                if (minRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMin == null) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMin = minRoot.generate();

                        if (newMin == null) {
                            this.minFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.minFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMin = minRoot.generate();

                    if (newMin == null) {
                        this.minFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMin, oldMin)) {
                        this.minFormula.setBackground(this.unchangedColor);
                    } else {
                        this.minFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

                final NumberOrFormula oldMax = derived.getMax();
                final FEFormula maxRoot = this.maxFormula.getRoot();

                if (maxRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (oldMax == null) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMax = maxRoot.generate();

                        if (newMax == null) {
                            this.maxFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.maxFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMax = maxRoot.generate();

                    if (newMax == null) {
                        this.maxFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMax, oldMax)) {
                        this.maxFormula.setBackground(this.unchangedColor);
                    } else {
                        this.maxFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }

                final Formula olFormula = derived.getFormula();
                final FEFormula formulaRoot = this.derivedFormula.getRoot();

                if (formulaRoot.getTopLevel() == null) {
                    // Formula entry is blank
                    if (olFormula == null) {
                        this.derivedFormula.setBackground(this.unchangedColor);
                    } else {
                        changes = true;
                        final Formula newMax = formulaRoot.generate();

                        if (newMax == null) {
                            this.derivedFormula.setBackground(this.changedInvalidColor);
                            valid = false;
                        } else {
                            this.derivedFormula.setBackground(this.changedValidColor);
                        }
                    }
                } else {
                    final Formula newMax = formulaRoot.generate();

                    if (newMax == null) {
                        this.derivedFormula.setBackground(this.changedInvalidColor);
                        changes = true;
                        valid = false;
                    } else if (Objects.equals(newMax, olFormula)) {
                        this.derivedFormula.setBackground(this.unchangedColor);
                    } else {
                        this.derivedFormula.setBackground(this.changedValidColor);
                        changes = true;
                    }
                }
            }
            default -> {
            }
        }

        // TODO: Indicate changes in Choices list or Excludes list

        if (this.applyButton.isVisible() != changes) {
            this.applyButton.setVisible(changes);
            needsLayout = true;
        }
        this.applyButton.setEnabled(valid);
        this.applyButton.setBackground(valid ? this.changedValidColor : this.changedInvalidColor);

        if (needsLayout) {
            revalidate();
            repaint();
        }

        return valid;
    }

    /**
     * Applies changes to the underlying variable
     */
    private void applyChanges() {

        // Perform validation before applying changes
        if (updateFieldsAndApplyButton()) {

            // If name has changed, update name in variable
            final String newName = this.nameField.getText();
            final AbstractVariable existingWithSameName = this.evalContext.getVariable(newName);
            if (existingWithSameName == null) {
                this.evalContext.removeVariable(this.var.name);
                this.var.name = newName;
                this.evalContext.addVariable(this.var);
            }

            final Object selectedType = this.typeChoice.getSelectedItem();

            if (BOOLEAN.equals(selectedType)) {
                applyChangesBoolean();
            } else if (INTEGER.equals(selectedType)) {
                applyChangesInteger();
            } else if (REAL.equals(selectedType)) {
                applyChangesReal();
            } else if (SPAN.equals(selectedType)) {
                applyChangesSpan();
            } else if (RANDOM_BOOLEAN.equals(selectedType)) {
                applyChangesRandomBoolean();
            } else if (RANDOM_INTEGER.equals(selectedType)) {
                applyChangesRandomInteger();
            } else if (RANDOM_REAL.equals(selectedType)) {
                applyChangesRandomReal();
            } else if (RANDOM_PERMUTATION.equals(selectedType)) {
                applyChangesRandomPermutation();
            } else if (RANDOM_CHOICE_INTEGER.equals(selectedType)) {
                applyChangesRandomChoiceInteger();
            } else if (RANDOM_CHOICE_REAL.equals(selectedType)) {
                applyChangesRandomChoiceReal();
            } else if (RANDOM_CHOICE_SPAN.equals(selectedType)) {
                applyChangesRandomChoiceSpan();
            } else if (RANDOM_SIMPLE_ANGLE.equals(selectedType)) {
                applyChangesRandomSimpleAngle();
            } else if (DERIVED_BOOLEAN.equals(selectedType)) {
                applyChangesDerivedBoolean();
            } else if (DERIVED_INTEGER.equals(selectedType)) {
                applyChangesDerivedInteger();
            } else if (DERIVED_REAL.equals(selectedType)) {
                applyChangesDerivedReal();
            } else if (DERIVED_SPAN.equals(selectedType)) {
                applyChangesDerivedSpan();
            }

            this.nameField.setBackground(this.unchangedColor);
            this.typeChoice.setBackground(this.unchangedColor);
            this.patternField.setBackground(this.unchangedColor);
            this.booleanValue.setBackground(this.unchangedColor);
            this.integerValue.setBackground(this.unchangedColor);
            this.realValue.setBackground(this.unchangedColor);
            this.spanValue.setBackground(this.unchangedColor);
            this.minFormula.setBackground(this.unchangedColor);
            this.maxFormula.setBackground(this.unchangedColor);
            this.derivedFormula.setBackground(this.unchangedColor);
            this.applyButton.setVisible(false);

            revalidate();
            repaint();
        }
    }

    /**
     * Applies changes to the underlying variable when "Boolean" type is selected.
     */
    private void applyChangesBoolean() {

        final Object newBooleanValue = this.booleanValue.getSelectedItem();

        if (this.var instanceof final VariableBoolean varBoolean) {
            varBoolean.setValue(newBooleanValue);
        } else {
            final VariableBoolean varBoolean = new VariableBoolean(this.var.name);
            varBoolean.setValue(newBooleanValue);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varBoolean);
            this.var = varBoolean;
        }
    }

    /**
     * Applies changes to the underlying variable when "Integer" type is selected.
     */
    private void applyChangesInteger() {

        final String newIntegerText = this.integerValue.getText();

        try {
            final Long newInteger = Long.valueOf(newIntegerText);

            if (this.var instanceof final VariableInteger varInteger) {
                varInteger.setValue(newInteger);
            } else {
                final VariableInteger varInteger = new VariableInteger(this.var.name);
                varInteger.setValue(newInteger);
                this.evalContext.removeVariable(this.var.name);
                this.evalContext.addVariable(varInteger);
                this.var = varInteger;
            }
        } catch (final NumberFormatException ex) {
            Log.warning("Unable to parse integer value; should have been caught earlier");
        }
    }

    /**
     * Applies changes to the underlying variable when "Real" type is selected.
     */
    private void applyChangesReal() {

        final String newRealText = this.realValue.getText();

        try {
            final Double newReal = Double.valueOf(newRealText);

            if (this.var instanceof final VariableReal varReal) {
                varReal.setValue(newReal);
            } else {
                final VariableReal varReal = new VariableReal(this.var.name);
                varReal.setValue(newReal);
                this.evalContext.removeVariable(this.var.name);
                this.evalContext.addVariable(varReal);
                this.var = varReal;
            }
        } catch (final NumberFormatException ex) {
            Log.warning("Unable to parse real value; should have been caught earlier");
        }
    }

    /**
     * Applies changes to the underlying variable when "Span" type is selected.
     */
    private void applyChangesSpan() {

        final String newSpanText = this.spanValue.getText();
        final String newText = "<a>" + newSpanText + "</a>";

        try {
            final XmlContent spanXmlContent = new XmlContent(newText, false, false);
            final IElement top = spanXmlContent.getToplevel();
            if (top instanceof final NonemptyElement nonempty) {
                final DocSimpleSpan newSpan =
                        DocFactory.parseSpan(this.evalContext, nonempty, EParserMode.NORMAL);
                if (newSpan == null) {
                    Log.warning("Ivalid span value; should have been caught earlier");
                } else if (this.var instanceof final VariableSpan varSpan) {
                    varSpan.setValue(newSpan);
                } else {
                    final VariableSpan varSpan = new VariableSpan(this.var.name);
                    varSpan.setValue(newSpan);
                    this.evalContext.removeVariable(this.var.name);
                    this.evalContext.addVariable(varSpan);
                    this.var = varSpan;
                }
            } else {
                Log.warning("Ivalid span value; should have been caught earlier");
            }
        } catch (final ParsingException ex) {
            Log.warning("Ivalid span value; should have been caught earlier");
        }
    }

    /**
     * Applies changes to the underlying variable when "Random Boolean" type is selected.
     */
    private void applyChangesRandomBoolean() {

        if (!(this.var instanceof VariableRandomBoolean)) {
            final VariableRandomBoolean varBoolean = new VariableRandomBoolean(this.var.name);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varBoolean);
            this.var = varBoolean;
        }
    }

    /**
     * Applies changes to the underlying variable when "Random Integer" type is selected.
     */
    private void applyChangesRandomInteger() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final Formula[] newExcludeList = extractExcludes(this.excludeFormula.getRoot());

        if (this.var instanceof final VariableRandomInteger varRandInt) {
            varRandInt.setMin(newMin);
            varRandInt.setMax(newMax);
            varRandInt.setExcludes(newExcludeList);
        } else {
            final VariableRandomInteger varRandInt = new VariableRandomInteger(this.var.name);
            varRandInt.setMin(newMin);
            varRandInt.setMax(newMax);
            varRandInt.setExcludes(newExcludeList);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandInt);
            this.var = varRandInt;
        }

        // TODO: Store excludes
    }

    /**
     * Applies changes to the underlying variable when "Random Real" type is selected.
     */
    private void applyChangesRandomReal() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        if (this.var instanceof final VariableRandomReal varRandReal) {
            varRandReal.setMin(newMin);
            varRandReal.setMax(newMax);
        } else {
            final VariableRandomReal varRandReal = new VariableRandomReal(this.var.name);
            varRandReal.setMin(newMin);
            varRandReal.setMax(newMax);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandReal);
            this.var = varRandReal;
        }
    }

    /**
     * Applies changes to the underlying variable when "Random Permutation" type is selected.
     */
    private void applyChangesRandomPermutation() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        if (this.var instanceof final VariableRandomReal varRandReal) {
            varRandReal.setMin(newMin);
            varRandReal.setMax(newMax);
        } else {
            final VariableRandomPermutation varRandPermutation =
                    new VariableRandomPermutation(this.var.name);
            varRandPermutation.setMin(newMin);
            varRandPermutation.setMax(newMax);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandPermutation);
            this.var = varRandPermutation;
        }
    }

    /**
     * Applies changes to the underlying variable when "Random Choice Integer" type is selected.
     */
    private void applyChangesRandomChoiceInteger() {

        if (this.var instanceof final VariableRandomChoice varRandChoice
                && varRandChoice.type == EType.INTEGER) {
            // TODO:
        } else {
            final VariableRandomChoice varRandChoice =
                    new VariableRandomChoice(this.var.name, EType.INTEGER);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandChoice);
            this.var = varRandChoice;
        }

        // TODO: Store choices

        // TODO: Store excludes
    }

    /**
     * Applies changes to the underlying variable when "Random Choice Real" type is selected.
     */
    private void applyChangesRandomChoiceReal() {

        if (this.var instanceof final VariableRandomChoice varRandChoice
                && varRandChoice.type == EType.REAL) {
            // TODO:
        } else {
            final VariableRandomChoice varRandChoice =
                    new VariableRandomChoice(this.var.name, EType.REAL);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandChoice);
            this.var = varRandChoice;
        }

        // TODO: Store choices
    }

    /**
     * Applies changes to the underlying variable when "Random Choice Span" type is selected.
     */
    private void applyChangesRandomChoiceSpan() {

        if (this.var instanceof final VariableRandomChoice varRandChoice
                && varRandChoice.type == EType.SPAN) {
            // TODO:
        } else {
            final VariableRandomChoice varRandChoice =
                    new VariableRandomChoice(this.var.name, EType.SPAN);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandChoice);
            this.var = varRandChoice;
        }

        // TODO: Store choices
    }

    /**
     * Applies changes to the underlying variable when "Random Simple Angle" type is selected.
     */
    private void applyChangesRandomSimpleAngle() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final Formula[] newExcludeList = extractExcludes(this.excludeFormula.getRoot());

        if (this.var instanceof final VariableRandomSimpleAngle varRandAngle) {
            varRandAngle.setMin(newMin);
            varRandAngle.setMax(newMax);
            varRandAngle.setExcludes(newExcludeList);
        } else {
            final VariableRandomSimpleAngle varRandAngle =
                    new VariableRandomSimpleAngle(this.var.name);
            varRandAngle.setMin(newMin);
            varRandAngle.setMax(newMax);
            varRandAngle.setExcludes(newExcludeList);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varRandAngle);
            this.var = varRandAngle;
        }

        // TODO: Store max denominator

        // TODO: Store excludes
    }

    /**
     * Applies changes to the underlying variable when "Derived Boolean" type is selected.
     */
    private void applyChangesDerivedBoolean() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;
        Formula newFormula = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final FEFormula newFormulaRoot = this.derivedFormula.getRoot();
        if (newFormulaRoot.getTopLevel() != null) {
            newFormula = newFormulaRoot.generate();
        }

        if (this.var instanceof final VariableDerived varDerived && varDerived.type == EType.BOOLEAN) {
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
        } else {
            final VariableDerived varDerived = new VariableDerived(this.var.name, EType.BOOLEAN);
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varDerived);
            this.var = varDerived;
        }

        // TODO: Store excludes
    }

    /**
     * Applies changes to the underlying variable when "Derived Integer" type is selected.
     */
    private void applyChangesDerivedInteger() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;
        Formula newFormula = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final Formula[] newExcludeList = extractExcludes(this.excludeFormula.getRoot());

        final FEFormula newFormulaRoot = this.derivedFormula.getRoot();
        if (newFormulaRoot.getTopLevel() != null) {
            newFormula = newFormulaRoot.generate();
        }

        if (this.var instanceof final VariableDerived varDerived && varDerived.type == EType.INTEGER) {
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setExcludes(newExcludeList);
            varDerived.setFormula(newFormula);
        } else {
            final VariableDerived varDerived = new VariableDerived(this.var.name, EType.INTEGER);
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setExcludes(newExcludeList);
            varDerived.setFormula(newFormula);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varDerived);
            this.var = varDerived;
        }

        // TODO: Store excludes
    }

    /**
     * Applies changes to the underlying variable when "Derived Real" type is selected.
     */
    private void applyChangesDerivedReal() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;
        Formula newFormula = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final FEFormula newFormulaRoot = this.derivedFormula.getRoot();
        if (newFormulaRoot.getTopLevel() != null) {
            newFormula = newFormulaRoot.generate();
        }

        if (this.var instanceof final VariableDerived varDerived && varDerived.type == EType.REAL) {
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
        } else {
            final VariableDerived varDerived = new VariableDerived(this.var.name, EType.REAL);
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varDerived);
            this.var = varDerived;
        }
    }

    /**
     * Applies changes to the underlying variable when "Derived Span" type is selected.
     */
    private void applyChangesDerivedSpan() {

        NumberOrFormula newMin = null;
        NumberOrFormula newMax = null;
        Formula newFormula = null;

        final FEFormula newMinRoot = this.minFormula.getRoot();
        if (newMinRoot.getTopLevel() != null) {
            newMin = new NumberOrFormula(newMinRoot.generate());
        }

        final FEFormula newMaxRoot = this.maxFormula.getRoot();
        if (newMaxRoot.getTopLevel() != null) {
            newMax = new NumberOrFormula(newMaxRoot.generate());
        }

        final FEFormula newFormulaRoot = this.derivedFormula.getRoot();
        if (newFormulaRoot.getTopLevel() != null) {
            newFormula = newFormulaRoot.generate();
        }

        if (this.var instanceof final VariableDerived varDerived && varDerived.type == EType.SPAN) {
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
        } else {
            final VariableDerived varDerived = new VariableDerived(this.var.name, EType.SPAN);
            varDerived.setMin(newMin);
            varDerived.setMax(newMax);
            varDerived.setFormula(newFormula);
            this.evalContext.removeVariable(this.var.name);
            this.evalContext.addVariable(varDerived);
            this.var = varDerived;
        }
    }

    /**
     * Adds an "exclude" value.
     *
     * @param excludes the array of exclude formulas
     * @return the vector formula
     */
    private static Formula makeExcludeVector(final Formula[] excludes) {

        final IntegerFormulaVector vec = new IntegerFormulaVector();
        if (excludes != null) {
            for (final Formula exclude : excludes) {
                vec.addChild(exclude);
            }
        }

        return new Formula(vec);
    }

    /**
     * Extracts an array of formulas from a formula edit object that *should* contain an integer vector.
     *
     * @param newExcludeRoot the formula edit object
     * @return the array of formulas; null if unable to extract
     */
    private static Formula[] extractExcludes(final FEFormula newExcludeRoot) {

        Formula[] result = null;

        if (newExcludeRoot.getTopLevel() != null) {
            final Formula newExclude = newExcludeRoot.generate();
            if (newExclude != null && newExclude.getChild(0) instanceof final IntegerFormulaVector vec) {
                final int count = vec.numChildren();
                result = new Formula[count];

                for (int i = 0; i < count; ++i) {
                    final AbstractFormulaObject obj = vec.getChild(i);
                    if (obj instanceof final Formula formula) {
                        result[i] = formula;
                    } else {
                        result[i] = new Formula(obj);
                    }
                }
            }
        }

        return result;
    }

    /**
     * Adds a choice value.
     */
    private static void addChoice() {

        Log.info("Add choice");
    }
}
