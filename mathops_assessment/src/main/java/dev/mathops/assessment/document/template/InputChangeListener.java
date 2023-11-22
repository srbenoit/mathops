package dev.mathops.assessment.document.template;

/**
 * An interface for classes that wish to be notified when the value of an input changes.
 */
@FunctionalInterface
public interface InputChangeListener {

    /**
     * Indication that an input's value has changed.
     *
     * @param source the input whose value has changed
     */
    void inputChanged(AbstractDocInput source);
}
