package dev.mathops.app.course.presenter;

import dev.mathops.commons.model.AttrKey;
import dev.mathops.commons.model.ModelTreeNode;
import dev.mathops.commons.model.codec.IntegerCodec;
import dev.mathops.commons.model.codec.StringCodec;
import dev.mathops.text.model.AllowedAttributes;
import dev.mathops.text.model.IModelTreeNodeFactory;
import dev.mathops.text.model.TagNotAllowedException;
import dev.mathops.text.model.XmlTreeWriter;

/**
 * A container for the structure of a Presentation model tree,  This class is also a node factory for nodes in the such
 * a tree.
 */
class PresentationFormat implements IModelTreeNodeFactory {

    /** An element tag name. */
    private static final String PRESENTATION_TAG = "presentation";

    /** An element tag name. */
    private static final String SLIDE_TAG = "slide";

    /** An element tag name. */
    private static final String ANIMATION_TAG = "animation";

    /** An attribute key. */
    private static final AttrKey<Integer> WIDTH_ATTR = new AttrKey<>("width", IntegerCodec.INST);

    /** An attribute key. */
    private static final AttrKey<Integer> HEIGHT_ATTR = new AttrKey<>("height", IntegerCodec.INST);

    /** An attribute key. */
    private static final AttrKey<String> ID_ATTR = new AttrKey<>("id", StringCodec.INST);

    /**
     * Constructs a new {@code PresentationFormat}.
     */
    PresentationFormat() {

        // No action
    }

    /**
     * Constructs a new {@code ModelTreeNode} based on an XML tag and a parent node.  This method does not need to set
     * the parent of the new node to {@code parent} or add the new node to the parent's list of child nodes, nor does it
     * need to store the XML tag as a DATA value.  It simply performs construction of the new tree node.
     *
     * @param xmlTag   the XML tag (null to attempt to create a data node)
     * @param parent   the parent node (which should have all attributes set); null to create the root node
     * @param attrKeys an object to which this method will add all supported attribute keys for the new node (if no keys
     *                 are added to this map, all attributes will be allowed and will be stored as String type)
     * @return the newly constructed node (typically a subclass of {@code ModelTreeNode})
     * @throws TagNotAllowedException if the XML tag is not allowed within the provided parent
     */
    @Override
    public ModelTreeNode construct(final String xmlTag, final ModelTreeNode parent,
                                   final AllowedAttributes attrKeys) throws TagNotAllowedException {

        ModelTreeNode node = null;

        if (parent == null) {
            node = buildRootNode(xmlTag, attrKeys);
        } else {
            final String parentTag = parent.map().getString(XmlTreeWriter.TAG);

            if (PRESENTATION_TAG.equals(parentTag)) {
                node = buildPresentationChild(xmlTag, attrKeys);
            } else if (SLIDE_TAG.equals(parentTag)) {
                node = buildSlideChild(xmlTag, attrKeys);
            }
        }

        return node;
    }

    /**
     * Builds the root node of a presentation model tree.
     *
     * @param xmlTag   the XML tag
     * @param attrKeys the set of attribute keys
     * @return the newly constructed node (typically a subclass of {@code ModelTreeNode})
     * @throws TagNotAllowedException if the XML tag is not allowed as the root node
     */
    private static ModelTreeNode buildRootNode(final String xmlTag, final AllowedAttributes attrKeys)
            throws TagNotAllowedException {

        if (PRESENTATION_TAG.equals(xmlTag)) {
            attrKeys.add(WIDTH_ATTR, HEIGHT_ATTR);
            return new ModelTreeNode();
        } else {
            throw new TagNotAllowedException("Top-level element must be 'presentation'.");
        }
    }

    /**
     * Builds a node that is a child of a 'presentation' node.
     *
     * @param xmlTag   the XML tag
     * @param attrKeys the set of attribute keys
     * @return the newly constructed node (typically a subclass of {@code ModelTreeNode})
     * @throws TagNotAllowedException if the XML tag is not allowed as the root node
     */
    private static ModelTreeNode buildPresentationChild(final String xmlTag, final AllowedAttributes attrKeys)
            throws TagNotAllowedException {

        if (SLIDE_TAG.equals(xmlTag)) {
            attrKeys.add(ID_ATTR);
            return new ModelTreeNode();
        } else {
            throw new TagNotAllowedException("'" + xmlTag + "' not allowed in 'presentation' element.");
        }
    }

    /**
     * Builds a node that is a child of a 'slide' node.
     *
     * @param xmlTag   the XML tag
     * @param attrKeys the set of attribute keys
     * @return the newly constructed node (typically a subclass of {@code ModelTreeNode})
     * @throws TagNotAllowedException if the XML tag is not allowed as the root node
     */
    private static ModelTreeNode buildSlideChild(final String xmlTag, final AllowedAttributes attrKeys)
            throws TagNotAllowedException {

        if (ANIMATION_TAG.equals(xmlTag)) {
            attrKeys.add(ID_ATTR);
            return new ModelTreeNode();
        } else {
            throw new TagNotAllowedException("'" + xmlTag + "' not allowed in 'slide' element.");
        }
    }
}
