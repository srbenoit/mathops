/**
 * Objects that represent instances of document objects.  An instance contains no formulas that require runtime
 * evaluation, but may have attributes that scripts can use to control visibility or to enable or disable controls.
 * Document object instances are immutable - constructed from templates in their fixed state.
 *
 * <p>
 * Instances contain the data needed to display the document, much like an HTML element, but it does not store the
 * run-time variable layout information such as the position and bounding box of each object, or the user's responses
 * from each input.  It is a fixed object that a delivery system acts upon.  This allows banks of instances to be
 * generated from templates once, stored persistently, and then accessed at need.
 *
 * <pre>
 * AbstractDocObjectInst [parent,style]
 *  |   |
 *  |   +--DocHSpaceInst [spaceWidth]
 *  |   +--DocImageInst [source, width, height, baseline]
 *  |
 *  +--AbstractDocContainerInst [children list]
 *  |   |
 *  |   +--DocColumnInst [tag]
 *  |   +--DocFenceInst [type, baseline]
 *  |   +--DocFractionInst
 *  |
 *  +--AbstractPrimitiveContainerInst [width, height, primitive list]
 *  |   |
 *  |   +--DocDrawingInst
 *  |   +--DocGraphXYInst [window, background, border, grid, x-axis, y-axis]
 *  |
 *  |--AbstractDocInputInst [name, enabled-var-name, enabled-var-value]
 *
 * AbstractPrimitiveInst
 *
 *
 *
 *
 * </pre>
 */
package dev.mathops.assessment.document.inst;