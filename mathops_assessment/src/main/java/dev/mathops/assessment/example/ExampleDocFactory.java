package dev.mathops.assessment.example;

import dev.mathops.commons.log.Log;
import dev.mathops.text.parser.CharSpan;
import dev.mathops.text.parser.ICharSpan;
import dev.mathops.text.parser.ParsingException;
import dev.mathops.text.parser.xml.CData;
import dev.mathops.text.parser.xml.IElement;
import dev.mathops.text.parser.xml.INode;
import dev.mathops.text.parser.xml.NonemptyElement;
import dev.mathops.text.parser.xml.XmlContent;
import dev.mathops.text.parser.xml.XmlContentError;

import java.util.Objects;

/**
 * A factory class that can load Example documents from an XML source file.
 */
public enum ExampleDocFactory {
    ;

    /**
     * Loads the problem from an {@code XmlSource}.
     *
     * @param content the {@code XmlContent} from which to load the problem
     * @return the loaded {@code ExampleDoc} object on success, {@code null} on failure
     */
    public static ExampleDoc load(final XmlContent content) {

        return createFromSource(content);
    }

    /**
     * Generate a problem from a {@code String} containing XML source.
     *
     * @param xml the {@code String} containing the problem source XML
     * @return the loaded {@code ExampleDoc} object on success, {@code null} on failure
     */
    public static ExampleDoc load(final String xml) {

        ExampleDoc example = null;

        try {
            final XmlContent source = new XmlContent(xml, false, true);
            example = createFromSource(source);
            for (final XmlContentError err : source.getAllErrors()) {
                Log.warning("    ", err);
            }

        } catch (final ParsingException ex) {
            Log.warning(ex);
        }

        return example;
    }

    /**
     * Generates an {@code ExampleDoc} object from the source XML. Any errors encountered are logged in the
     * {@code XmlSource} object.
     *
     * @param content the {@code XmlContent} containing the source XML
     * @return the loaded {@code ExampleDoc} object on success, {@code null} on failure
     */
    private static ExampleDoc createFromSource(final XmlContent content) {

        ExampleDoc example = null;

        final IElement top = content.getTopLevel();

        if (top instanceof final NonemptyElement nonempty) {
            final String tagName = top.getTagName();

            if ("example".equals(tagName)) {
                final ExampleDoc doc = new ExampleDoc();
                if (parseFromExampleElement(doc, nonempty)) {
                    example = doc;
                }
            } else {
                content.logError(top, "Unrecognized top-level element: " + tagName);
            }
        } else {
            final ICharSpan source = Objects.requireNonNullElseGet(top, () -> new CharSpan(0, 0, 1, 1));
            content.logError(source, "Example must be defined in a nonempty top-level 'example' element.");
        }

        return example;
    }

    /**
     * Populates a {@code ExampleDoc} object from the top-level non-empty "example" element.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean parseFromExampleElement(final ExampleDoc example, final NonemptyElement elem) {

        return findTitle(example, elem)
               && findObjectives(example, elem)
               && findPrompt(example, elem)
               && findHints(example, elem)
               && findWalkthrough(example, elem)
               && findSolution(example, elem);
    }

    /**
     * Searches for the &lt;title&gt; element within the top-level example element, and if found, stores the title text
     * in an example document object.  If the title element is not found or does not contain simple text, or if there
     * are multiple title elements found; parsing fails.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findTitle(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("title".equals(tag)) {
                if (example.title == null) {
                    if (inner instanceof final NonemptyElement titleElem && titleElem.getNumChildren() == 1) {
                        final INode child = titleElem.getChild(0);
                        if (child instanceof final CData titleData) {
                            example.title = titleData.content;
                            success = true;
                        } else {
                            Log.warning("Title element must be nonempty and contain text title.");
                        }
                    } else {
                        Log.warning("Title element must be nonempty and contain text title.");
                    }
                } else {
                    Log.warning("Multiple <title> elements found.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for &lt;objective&gt; elements within the top-level example element, and stores the text of each in the
     * objectives list of an example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findObjectives(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("objective".equals(tag)) {
                if (inner instanceof final NonemptyElement titleElem && titleElem.getNumChildren() == 1) {
                    final INode child = titleElem.getChild(0);
                    if (child instanceof final CData objData) {
                        example.objectives.add(objData.content);
                        success = true;
                    } else {
                        Log.warning("Objective element must be nonempty and contain objective title.");
                    }
                } else {
                    Log.warning("Objective element must be nonempty and contain objective title.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;prompt&gt; element within the top-level example element and parses its content, storing the
     * prompt in an example document object if successful.  If there is no prompt element (or if it can't be parsed)
     * this method fails.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findPrompt(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        boolean found = false;
        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("prompt".equals(tag)) {
                if (found) {
                    Log.warning("Multiple 'prompt' elements found.");
                    success = false;
                    break;
                }

                found = true;
                if (inner instanceof final NonemptyElement nonemptyInner) {
                    success = parsePrompt(example, nonemptyInner);
                } else {
                    Log.warning("'prompt' element must be nonempty and contain 'heading' and 'content' elements.");
                }
            }
        }

        return success;
    }

    /**
     * Populates the prompt portion of an {@code ExampleDoc} object from the top-level non-empty "prompt" element.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean parsePrompt(final ExampleDoc example, final NonemptyElement elem) {

        return findPromptHeading(example, elem)
               && findPromptContent(example, elem);
    }

    /**
     * Searches for the &lt;heading&gt; element within a "prompt" element, and if found, stores the prompt text in an
     * example document object.  If the heading element is not found or does not contain simple text, or if there are
     * multiple heading elements found; parsing fails.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findPromptHeading(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("heading".equals(tag)) {
                if (example.promptHeading == null) {
                    if (inner instanceof final NonemptyElement headingElem && headingElem.getNumChildren() == 1) {
                        final INode child = headingElem.getChild(0);
                        if (child instanceof final CData headingData) {
                            example.promptHeading = headingData.content;
                            success = true;
                        } else {
                            Log.warning("Prompt heading element must be nonempty and contain text title.");
                        }
                    } else {
                        Log.warning("Prompt heading element must be nonempty and contain text title.");
                    }
                } else {
                    Log.warning("Multiple <heading> elements found in prompt.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;content&gt; element within a "prompt" element, and if found, extracts the content document
     * column and stores in a provided example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findPromptContent(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("content".equals(tag)) {
                if (example.promptContent == null) {
                    // TODO:
                    //  DocFactory.createNoVariableDocument()...
                } else {
                    Log.warning("Multiple <content> elements found in prompt.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for all &lt;hint&gt; elements within the top-level example element and parses each one into a hint
     * stored in an example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findHints(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("hint".equals(tag)) {
                if (inner instanceof final NonemptyElement nonemptyInner) {
                    success = parseHint(example, nonemptyInner);
                } else {
                    Log.warning("'hint' element must be nonempty and contain 'button' and 'content' elements.");
                }
            }
        }

        return success;
    }

    /**
     * Populates a hint within an {@code ExampleDoc} object from the top-level non-empty "hint" element.
     *
     * @param example the example document object to populate
     * @param elem    the "hint" element
     * @return true on success, false on failure
     */
    private static boolean parseHint(final ExampleDoc example, final NonemptyElement elem) {

        return findHintButton(example, elem)
               && findHintContent(example, elem);
    }

    /**
     * Searches for the &lt;button&gt; element within a "hint" element, and if found, stores the button text in an
     * example document object.  If the button element is not found or does not contain simple text, or if there are
     * multiple button elements found; parsing fails.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findHintButton(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        boolean found = false;
        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("button".equals(tag)) {
                if (found) {
                    Log.warning("Multiple <button> elements found in hint.");
                    success = false;
                    break;
                }
                found = true;

                if (inner instanceof final NonemptyElement headingElem && headingElem.getNumChildren() == 1) {
                    final INode child = headingElem.getChild(0);
                    if (child instanceof final CData headingData) {
                        example.hintButtons.add(headingData.content);
                        success = true;
                    } else {
                        Log.warning("Hint button element must be nonempty and contain button text.");
                        break;
                    }
                } else {
                    Log.warning("Hint button element must be nonempty and contain button text.");
                    break;
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;content&gt; element within a "prompt" element, and if found, extracts the content document
     * column and stores in a provided example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findHintContent(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        boolean found = false;
        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("content".equals(tag)) {
                if (found) {
                    Log.warning("Multiple <content> elements found in hint.");
                    success = false;
                    break;
                }

                found = true;
                // TODO:
                //  DocFactory.createNoVariableDocument()...
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;walkthrough&gt; element within the top-level example element and parses its content, storing
     * the walkthrough in an example document object if successful.  If there is no walkthrough element (or if it can't
     * be parsed) this method fails.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findWalkthrough(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        boolean found = false;
        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("walkthrough".equals(tag)) {
                if (found) {
                    Log.warning("Multiple 'walkthrough' elements found.");
                    success = false;
                    break;
                }

                found = true;
                if (inner instanceof final NonemptyElement nonemptyInner) {
                    success = parseWalkthrough(example, nonemptyInner);
                } else {
                    Log.warning("'walkthrough' element must be nonempty and contain 'heading' and 'content' elements.");
                }
            }
        }

        return success;
    }

    /**
     * Populates the walkthrough portion of an {@code ExampleDoc} object from the top-level non-empty "walkthrough"
     * element.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean parseWalkthrough(final ExampleDoc example, final NonemptyElement elem) {

        return findWalkthroughHeading(example, elem)
               && findWalkthroughContent(example, elem);
    }

    /**
     * Searches for the &lt;heading&gt; element within a "walkthrough" element, and if found, stores the prompt text in
     * an example document object.  If the heading element is not found or does not contain simple text, or if there are
     * multiple heading elements found; parsing fails.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findWalkthroughHeading(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("heading".equals(tag)) {
                if (example.walkthroughHeading == null) {
                    if (inner instanceof final NonemptyElement headingElem && headingElem.getNumChildren() == 1) {
                        final INode child = headingElem.getChild(0);
                        if (child instanceof final CData headingData) {
                            example.walkthroughHeading = headingData.content;
                            success = true;
                        } else {
                            Log.warning("Walkthrough heading element must be nonempty and contain text title.");
                        }
                    } else {
                        Log.warning("Walkthrough heading element must be nonempty and contain text title.");
                    }
                } else {
                    Log.warning("Multiple <heading> elements found in walkthrough.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;content&gt; element within a "walkthrough" element, and if found, extracts the content
     * document column and stores in a provided example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean findWalkthroughContent(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("content".equals(tag)) {
                if (example.walkthroughContent == null) {
                    // TODO:
                    //  DocFactory.createNoVariableDocument()...
                } else {
                    Log.warning("Multiple <content> elements found in walkthrough.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;solution&gt; element within the top-level example element and parses its content, storing
     * the solution in an example document object if successful.  If there is no solution element (or if it can't be
     * parsed) this method fails.
     *
     * @param example the example document object to populate
     * @param elem    the "example" element
     * @return true on success, false on failure
     */
    private static boolean findSolution(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        boolean found = false;
        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("solution".equals(tag)) {
                if (found) {
                    Log.warning("Multiple 'solution' elements found.");
                    success = false;
                    break;
                }

                found = true;
                if (inner instanceof final NonemptyElement nonemptyInner) {
                    success = parseSolution(example, nonemptyInner);
                } else {
                    Log.warning("'solution' element must be nonempty and contain 'heading' and 'content' elements.");
                }
            }
        }

        return success;
    }

    /**
     * Populates the solution portion of an {@code ExampleDoc} object from the top-level non-empty "solution" element.
     *
     * @param example the example document object to populate
     * @param elem    the "prompt" element
     * @return true on success, false on failure
     */
    private static boolean parseSolution(final ExampleDoc example, final NonemptyElement elem) {

        return findSolutionHeading(example, elem)
               && findSolutionContent(example, elem);
    }

    /**
     * Searches for the &lt;heading&gt; element within a "solution" element, and if found, stores the solution heading
     * in an example document object.  If the heading element is not found or does not contain simple text, or if there
     * are multiple heading elements found; parsing fails.
     *
     * @param example the example document object to populate
     * @param elem    the "solution" element
     * @return true on success, false on failure
     */
    private static boolean findSolutionHeading(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("heading".equals(tag)) {
                if (example.solutionHeading == null) {
                    if (inner instanceof final NonemptyElement headingElem && headingElem.getNumChildren() == 1) {
                        final INode child = headingElem.getChild(0);
                        if (child instanceof final CData headingData) {
                            example.solutionHeading = headingData.content;
                            success = true;
                        } else {
                            Log.warning("Solution heading element must be nonempty and contain text title.");
                        }
                    } else {
                        Log.warning("Solution heading element must be nonempty and contain text title.");
                    }
                } else {
                    Log.warning("Multiple <heading> elements found in solution.");
                }
            }
        }

        return success;
    }

    /**
     * Searches for the &lt;content&gt; element within a "solution" element, and if found, extracts the content document
     * column and stores in a provided example document object.
     *
     * @param example the example document object to populate
     * @param elem    the "solution" element
     * @return true on success, false on failure
     */
    private static boolean findSolutionContent(final ExampleDoc example, final NonemptyElement elem) {

        boolean success = false;

        for (final IElement inner : elem.getElementChildrenAsList()) {
            final String tag = inner.getTagName();
            if ("content".equals(tag)) {
                if (example.solutionContent == null) {
                    // TODO:
                    //  DocFactory.createNoVariableDocument()...
                } else {
                    Log.warning("Multiple <content> elements found in solution.");
                }
            }
        }

        return success;
    }
}
