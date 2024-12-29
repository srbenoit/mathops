package dev.mathops.assessment.expression.editmodel;

import dev.mathops.commons.CoreConstants;
import dev.mathops.text.builder.HtmlBuilder;

/**
 * An expression object that represents a matrix with subexpressions for each entry.
 */
public final class ExprBranchMatrix extends ExprObjectBranch {

    /** The type of brackets. */
    private EVectorMatrixBrackets brackets;

    /** The justification. */
    private EEntryJustification justification;

    /** The number of rows. */
    private final int numRows;

    /** The number of columns. */
    private final int numCols;

    /** The components (row-major order, index = row * numCols + col). */
    public final Expr[] components;

    /**
     * Constructs a new {@code ExprBranchMatrix}.
     *
     * @param theBrackets the type of brackets
     * @param theNumRows the number of rows
     * @param theNumCols the number of columns
     */
    public ExprBranchMatrix(final EVectorMatrixBrackets theBrackets, final int theNumRows, final int theNumCols) {

        super();

        this.brackets = theBrackets;
        this.numRows = theNumRows;
        this.numCols = theNumCols;
        final int numComponents = theNumRows * theNumCols;

        this.components = new Expr[numComponents];

        for (int i = 0; i < numComponents; ++i) {
            final Expr expr = new Expr();
            this.components[i] = expr;
        }
    }

    /**
     * Gets the type of brackets.
     *
     * @return the type of brackets
     */
    public EVectorMatrixBrackets getBrackets() {

        return this.brackets;
    }

    /**
     * Sets the type of brackets.
     *
     * @param theBrackets the new type of brackets
     */
    public void setBrackets(final EVectorMatrixBrackets theBrackets) {

        if (theBrackets == null) {
            throw new IllegalArgumentException("Bracket type may not be null");
        }

        this.brackets = theBrackets;
    }

    /**
     * Gets the number of rows in the matrix.
     *
     * @return the number of rows
     */
    public int getNumRows() {

        return this.numRows;
    }

    /**
     * Gets the number of columns in the matrix.
     *
     * @return the number of columns
     */
    public int getNumCols() {

        return this.numCols;
    }

    /**
     * Gets the expression at a specified row and column.
     *
     * @param row the row (0-based)
     * @param col the column (0-based)
     * @return the entry expression
     */
    public Expr getComponent(final int row, final int col) {

        return this.components[row * this.numCols + col];
    }

    /**
     * Generates a diagnostic string representation of the object.
     *
     * @return the string representation
     */
    @Override
    public String toString() {

        final HtmlBuilder htm = new HtmlBuilder(20 + this.numRows * this.numCols * 20);

        htm.add("ExprBranchMatrix{brackets=", this.brackets, ",entries=[");

        int index = 0;
        if (this.numRows > 0 && this.numCols > 0) {
            for (int row = 0; row < this.numRows; ++row) {
                final ExprObject child0 = this.components[index];
                htm.add(child0);
                ++index;

                for (int i = 1; i < this.numCols; ++i) {
                    htm.add(CoreConstants.COMMA);
                    final ExprObject child = this.components[index];
                    htm.add(child);
                    ++index;
                }
            }
        }

        htm.add("]}");

        return htm.toString();
    }
}
