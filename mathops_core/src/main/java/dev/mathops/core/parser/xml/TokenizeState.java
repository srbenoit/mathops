package dev.mathops.core.parser.xml;

import dev.mathops.core.parser.FilePosition;

import java.util.ArrayList;
import java.util.List;

/**
 * The state of an XML tokenization operation.
 */
final class TokenizeState {

    /** Estimated number of tokens, for list allocation. */
    private static final int EST_NUM_TOKENS = 250;

    /** The XML content. */
    private final XmlContent content;

    /** The list of tokens being assembled. */
    private final List<IXmlToken> tokens;

    /** The current parse state. */
    private EXmlParseState state;

    /** The start position of the current token. */
    private final FilePosition start;

    /** The current nesting level. */
    private int nesting;

    /**
     * Constructs a new {@code TokenizeState}.
     *
     * @param theContent the XML content
     */
    TokenizeState(final XmlContent theContent) {

        this.content = theContent;
        this.tokens = new ArrayList<>(EST_NUM_TOKENS);
        this.state = EXmlParseState.CHARS;
        this.start = new FilePosition();
        this.nesting = 0;
    }

    /**
     * Gets the XML content.
     *
     * @return the XML content
     */
    public XmlContent getContent() {

        return this.content;
    }

    /**
     * Gets the XML content.
     *
     * @return the content
     */
    public List<IXmlToken> getTokens() {

        return this.tokens;
    }

    /**
     * Adds a token to the list of tokens.
     *
     * @param token the token to add
     */
    void addToken(final IXmlToken token) {

        this.tokens.add(token);
    }

    /**
     * Gets the current parse state.
     *
     * @return the current parse state
     */
    public EXmlParseState getState() {

        return this.state;
    }

    /**
     * Sets the current parse state.
     *
     * @param newState the current parse state
     */
    public void setState(final EXmlParseState newState) {

        this.state = newState;
    }

    /**
     * Get the current level of nesting.
     *
     * @return the nesting level
     */
    public int getNesting() {

        return this.nesting;
    }

    /**
     * Increments the current level of nesting.
     */
    void incrementNesting() {

        ++this.nesting;
    }

    /**
     * Decrements the current level of nesting.
     */
    void decrementNesting() {

        --this.nesting;
    }

    /**
     * Gets the position of the start of the current token.
     *
     * @return the start position
     */
    public FilePosition getStart() {

        return this.start;
    }

    /**
     * Sets the position of the start of the current token.
     *
     * @param newStart the new start position
     */
    public void FilePosition(final FilePosition newStart) {

        this.start.copyFrom(newStart);
    }
}
