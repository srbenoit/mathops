package dev.mathops.assessment.expression.editview;

import dev.mathops.assessment.expression.editmodel.Expr;
import dev.mathops.assessment.expression.editmodel.ExprLeafDecimalPoint;
import dev.mathops.assessment.expression.editmodel.ExprLeafDigit;
import dev.mathops.assessment.expression.editmodel.ExprLeafEngineeringE;
import dev.mathops.assessment.expression.editmodel.ExprLeafOperator;
import dev.mathops.assessment.expression.editmodel.ExprLeafSymbolicConstant;
import dev.mathops.assessment.expression.editmodel.ExprObject;
import dev.mathops.commons.log.Log;

import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A laid out expression.
 */
public class ExprBox extends AbstractObjectBox {

    /** The source expression object. */
    private final Expr source;

    /** The list of child boxes. */
    private final List<AbstractObjectBox> childBoxes;

    /**
     * Constructs a new {@code ExprBox}.
     *
     * @param theSource the source expression
     * @param currentFontSize the current font size
     * @param minFontSize the minimum font size
     */
    public ExprBox(final Expr theSource, final float currentFontSize, final float minFontSize) {

        super();

        this.source = theSource;

        final int numChildren = this.source.size();
        this.childBoxes = new ArrayList<>(numChildren);

        final Font font = Fonts.sans.deriveFont(currentFontSize);
        setAxisCenter(font);

        int maxTop = getTypoCenter() * 2;
        int minBottom = 0;
        int x = 0;

        for (int i = 0; i < numChildren; ++i) {
            final ExprObject obj = this.source.get(i);
            AbstractObjectBox box = null;

            switch (obj) {
                case final ExprLeafDigit leafDigit -> box = new LeafDigitBox(leafDigit, currentFontSize);
                case final ExprLeafOperator leafOperator -> box = new LeafOperatorBox(leafOperator, currentFontSize);
                case final ExprLeafDecimalPoint leafDecimalPoint ->
                        box = new LeafDecimalPointBox(leafDecimalPoint, currentFontSize);
                case final ExprLeafEngineeringE leafEngineeringE ->
                        box = new LeafEngineeringEBox(leafEngineeringE, currentFontSize);
                case final ExprLeafSymbolicConstant leafSymbolicConstant ->
                        box = new LeafSymbolicConstantBox(leafSymbolicConstant, currentFontSize);
                case null, default ->
                        Log.warning("Encountered ", obj.getClass().getSimpleName(), ", but unable to lay out.");
            }

            if (box != null) {
                this.childBoxes.add(box);
                maxTop = Math.max(maxTop, box.getTop());
                minBottom = Math.min(minBottom, box.getBottom());
                box.setX(x);
                x += box.getWidth();
            }
        }

        setTopBottom(maxTop, minBottom);
        setY(maxTop);

        for (final AbstractObjectBox box : this.childBoxes) {
            final int origY = box.getY();
            box.setY(origY + maxTop);
        }
    }

    /**
     * Paints the contents of the box
     *
     * @param g2d the {@code Graphics2D} to which to draw
     */
    @Override
    public void paint(final Graphics2D g2d) {

        for (final AbstractObjectBox childBox : this.childBoxes) {

            childBox.paint(g2d);
        }
    }
}
