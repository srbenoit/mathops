/**
 * Document components.
 *
 * <pre>
 * AbstractDocObject {Realizable}
 *   DocImage [BufferedImage image, URL source]
 *   DocText [String text, boolean isStixText, boolean isStixMath]
 *   DocWhitespace
 *   DocVSpace
 *   DocHSpace
 *   AbstractDocContainer [String tag, List&lt;AbstractDocObject&gt; children] - mouse/key events
 *     DocColumn [Integer id, int width, List&lt;AbstractDocInput&gt; inputs]
 *     DocFraction [DocNonwrappingSpan numerator, DocNonwrappingSpan denominator, int lineY]
 *     DocRadical [AbstractDocObject base, AbstractDocObject root]
 *     DocRelativeOffset [AbstractDocObject base, AbstractDocObject superscript,
 *                        AbstractDocObject subscript, AbstractDocObject over,
 *                        AbstractDocObject under]
 *     DocTable [AbstractDocObject[][] objectData, Insets cellInsets, int spacing,
 *               int justification, int[] rowY, int[] rowBase, int[] colX, int boxWidth,
 *               int hLineWidth, int vLineWidth, String backgroundColorName]
 *     AbstractDocSpanBase DocFence [int leftAlign, int type, int outlines, DocText openFence,
 *                         DocText closeFence]
 *       DocMathSpan [int outlines]
 *       DocNonwrappingSpan [int outlines, String backgroundColorName]
 *       DocParagraph [int justification]
 *       DocParameterReference [String paramName]
 *       DocSimpleSpan
 *       DocWrappingSpan
 *     AbstractDocPrimitiveContainer [List&lt;AbstractDocPrimitive&gt; primitives, String name,
 *                                    EvalContext context, BufferedImage offscreen]
 *       DocDrawing
 *       DocGraphXY [NumberFormat format, Rectangle2D window, String backgroundColorName,
 *                   int borderWidth, String borderColorName, int gridWidth, String gridColorName,
 *                   int tickWidth, String tickColorName, int tickSize, double xTickInterval,
 *                   double yTickInterval, int axisWidth, String axisColorName, int axisLabelSize,
 *                   int tickLabelSize, String xAxisLabel, String yAxisLabel]
 *     AbstractDocInput [Formula enabledFormula, List&lt;InputChangeListener&gt; listeners, String name,
 *                       boolean selected, EvalContext context]
 *       DocInputRadioButton [int value]
 *       AbstractDocInputField [Object clipboard, String text, int[] charPositions, int caret,
 *                              int selectStart, int selectEnd, int ownsDrag, List&lt;String&gt; history,
 *                              int historyPos]
 *         DocInputDoubleField [Double value, Double defaultValue, Double minusAs]
 *         DocInputLongField [Long value, Long defaultValue, Long minusAs]
 * </pre>
 */
package dev.mathops.assessment.document;
