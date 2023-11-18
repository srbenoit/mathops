package dev.mathops.core.parser.xml;

/**
 * The set of parse states.
 */
enum EXmlParseState {

    /** Accumulating non-whitespace character data. */
    CHARS,

    /** Accumulating whitespace. */
    WHITESPACE,

    /** Accumulating a character or entity reference. */
    REFERENCE,

    /** Accumulating a tag. */
    TAG,

    /** Accumulating a tag starting with <!. */
    BANGTAG,

    /** Accumulating an XMLDecl tag. */
    XMLDECL,

    /** Accumulating an DOCTYPE tag. */
    DOCTYPE,

    /** Accumulating a comment. */
    COMMENT,

    /** Accumulating a CDATA block. */
    CDATA
}
