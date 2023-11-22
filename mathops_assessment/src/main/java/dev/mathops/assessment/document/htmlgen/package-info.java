/**
 * Utility classes to generate HTML from instances of document objects.
 *
 * <p>
 * When a DocColumnInstance or DocParagraphInst is emitted, it is assumed that the containing HTML provides a block
 * formatting context.
 *
 * <p>
 * Documents that are the "question" part of a problem will be emitted within an HTML form with the necessary "action"
 * to submit updates.
 */
package dev.mathops.assessment.document.htmlgen;