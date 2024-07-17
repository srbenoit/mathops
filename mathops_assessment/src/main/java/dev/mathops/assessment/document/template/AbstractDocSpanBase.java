package dev.mathops.assessment.document.template;

import dev.mathops.assessment.document.EVAlign;

import java.io.Serial;
import java.util.Set;

/**
 * The base class for span-type objects, which contain a collection of other objects.
 */
public abstract class AbstractDocSpanBase extends AbstractDocContainer {

    /** Version number for serialization. */
    @Serial
    private static final long serialVersionUID = -2238627054020330715L;

    /**
     * Construct a new {@code AbstractDocSpanBase} object.
     */
    AbstractDocSpanBase() {

        super();
    }

    /**
     * Copy information from a source {@code SpanBase} object, including all underlying {@code DocObject} information.
     *
     * @param source the {@code AbstractJDocSpanBase} from which to copy data
     */
    final void copySpanFrom(final AbstractDocSpanBase source) {

        clearChildren();

        for (final AbstractDocObjectTemplate child : source.getChildren()) {
            add(child.deepCopy());
        }

        copyObjectFromContainer(source);
    }

    /**
     * Add any parameter names referenced by the object or its children to a set of names.
     *
     * @param set the set of parameter names
     */
    @Override
    public final void accumulateParameterNames(final Set<String> set) {

        accumulateChildrenParameterNames(set);
    }
}
