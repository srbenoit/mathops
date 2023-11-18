package dev.mathops.core.parser.xml;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.parser.ICharSpan;
import dev.mathops.core.parser.ParsingException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

/**
 * A parser for XML content that supports a limited subset of the XML 1.0 grammar. All state is stored in the
 * {@code XmlContent} object being parsed, allowing a single parser to be used by multiple threads concurrently.
 *
 * <p>
 * The supported subset of the XML 1.0 grammar is included here:
 *
 * <pre>
 * Char          ::= #x9 | #xA | #xD | [#x20-@xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
 * S             ::= (#x20 | #x9 | #xD | #xA)+
 * NameStartChar ::= ":" | [A-Z] | "_" | [a-z] | [#xC0-#xD6] | [#xD8-#xF6] | [#xF8-#x2FF] |
 *                   [#x370-#x37D] | [#x37F-#x1FFF] | [#x200C-#x200D] | [#x2070-#x218F] |
 *                   [#x2C00-#x2FEF] | [#x3001-#xD7FF] | [#xF900-#xFDCF] | [#xFDF0-#xFFFD] |
 *                   [#x10000-#xEFFFF]
 * NameChar      ::= NameStartChar | "-" | "." | [0-9] | #xB7 | [#x0300-#x036F] | [#x203F-#x2040]
 *
 * Name          ::= NameStartChar (NameChar)*
 * document      ::= prolog element (Comment | S)*
 * prolog        ::= XMLDecl? (Comment | S)*
 * XMLDecl       ::= '&lt;?xml' VersionInfo EncodingDecl? SDDecl? S? '?&gt;'
 * VersionInfo   ::= S 'version' S? '=' S? ("'" VersionNum "'" | '"' VersionNum '"')
 * VersionNum    ::= '1.' [0-9]+
 * EncodingDecl  ::= S 'encoding' S? '=' S? ('"' EncName '"' | "'" EncName "'")
 * SDDecl        ::= S 'standalone' S? '=' S? (("'" ('yes' | 'no') "'") | ('"' ('yes' | 'no') '"'))
 * EncName       ::= [A-Za-z] ([A-Za-z0-9._) | '-')*
 * Comment       ::= '&lt;!--' ((Char - '-') | ('-' (Char - '-')))* '--&gt;'
 * doctypedecl   ::= '&lt;!DOCTYPE' (Char - '&gt;')* ('&lt;' (Char - '&gt;')* '&gt;' )*
 *                   (Char - '&gt;')* '&gt;'
 * element       ::= EmptyElemTag | STag content ETag
 * EmptyElemTag  ::= '&lt;' Name (S Name S? '=' S? AttValue)* S? '/&gt;'
 * STag          ::= '&lt;' Name (S Name S? '=' S? AttValue)* S? '&gt;'
 * ETag          ::= '&lt;/ Name S? '&gt;'
 * AttValue      ::= '"' ([^&lt;&"] | Reference)* '"' | "'" ([^&lt;&'] | Reference)* "'"
 * Reference     ::= '&' Name ';' | '&#' [0-9]+ ';' | '&#x' [0-9a-fA-F]+ ';'
 * content       ::= CharData? ((element | Reference | CDSect | Comment) CharData?)*
 * CDSect        ::= '&lt;![CDATA[' (Char* - (Char* ']]&gt;' Char*)) ']]&gt;'
 * CharData      ::= [^&lt;&]* - ([^&lt;&]* ']]&gt;' [^&lt;&]*)
 * </pre>
 */
enum XmlParser {
    ;

    /** The length of the start of a CDATA block, &lt;![CDATA[. */
    private static final int CDATA_START_LEN = 9;

    /** The length of the end of a CDATA block, ]]&gt;. */
    private static final int CDATA_END_LEN = 3;

    /**
     * Attempts to parse some XML content. It is assumed that the caller obtains the write lock on the
     * {@code XmlContent} before making this call.
     *
     * @param content         the content to parse
     * @param elementsOnly    {@code true} to omit any character and whitespace found between elements from the
     *                        constructed parsed structure (simplifies parsing of XML content where only elements are
     *                        significant)
     * @param includeComments true to include comments
     * @throws ParsingException if the content could not be parsed
     */
    static void parse(final XmlContent content, final boolean elementsOnly,
                      final boolean includeComments) throws ParsingException {

        final List<IXmlToken> tokens = XmlTokenizer.tokenize(content);
        for (final IXmlToken token : tokens) {
            token.validate();
        }

        final List<INode> nodes = buildNodes(tokens, elementsOnly, includeComments);

        // We get here only if no parse exceptions, in which case we can store the results
        content.setTokens(tokens);
        content.setNodes(nodes);
    }

    /**
     * Converts a list of tokens into a sequence of nodes.
     *
     * @param tokens          the XML tokens
     * @param elementsOnly    {@code true} to omit any character and whitespace found between elements from the
     *                        constructed parsed structure
     * @param includeComments true to include comments
     * @return the list of nodes
     * @throws ParsingException if the content could not be parsed
     */
    private static List<INode> buildNodes(final Collection<IXmlToken> tokens, final boolean elementsOnly,
                                          final boolean includeComments) throws ParsingException {

        final List<INode> rootNodes = new ArrayList<>(tokens.size());
        final Deque<NonemptyElement> stack = new LinkedList<>();
        List<INode> current = rootNodes;

        for (final IXmlToken tok : tokens) {

            if (includeComments && tok instanceof TokComment) {
                final XmlContent xml = tok.getContent();
                final int start = tok.getStart();
                final int end = tok.getEnd();

                final String content = xml.substring(start + 4, end - 3);
                final INode comment = new Comment(start, end, tok.getLineNumber(), tok.getColumn(), content);
                current.add(comment);
            } else {
                if (!tok.hasNodeStructure()) {
                    continue;
                }

                if (tok.isNonElement()) {
                    final NonemptyElement top = stack.peek();

                    if (top != null) {
                        top.addToken(tok);
                    }

                    if (!elementsOnly) {
                        processNonElement(tok, current, includeComments);
                    }
                } else if (tok instanceof TokEmptyElement) {
                    processEmptyElement(tok, current);
                } else if (tok instanceof TokSTag) {
                    current = processStartTag(tok, current, stack);
                } else if (tok instanceof TokETag) {
                    current = processEndTag(tok, stack, rootNodes);
                }
            }
        }

        return rootNodes;
    }

    /**
     * Processes a single token that represents a non-element.
     *
     * @param tok             the token being processed
     * @param current         the current list of nodes
     * @param includeComments true to include comments
     */
    private static void processNonElement(final IXmlToken tok, final List<INode> current,
                                          final boolean includeComments) {

        IContentNode newData;

        if (tok instanceof TokCData) {
            newData = new CData(tok.getContent(), tok.getStart() + CDATA_START_LEN,
                    tok.getEnd() - CDATA_END_LEN, tok.getLineNumber(), tok.getColumn());
        } else if (tok instanceof TokChars) {
            newData = new CData(tok.getContent(), tok.getStart(), tok.getEnd(), tok.getLineNumber(),
                    tok.getColumn());
        } else if (tok instanceof TokComment && includeComments) {
            newData = new Comment(tok.getContent(), tok.getStart() + CDATA_START_LEN,
                    tok.getEnd() - CDATA_END_LEN, tok.getLineNumber(), tok.getColumn());
        } else if (tok instanceof TokReference) {
            newData = new CData(tok.getContent(), tok.getStart(), tok.getEnd(), tok.getLineNumber(),
                    tok.getColumn(), ((TokReference) tok).getValue());
        } else if (tok instanceof TokWhitespace) {
            newData = new CData(tok.getContent(), tok.getStart(), tok.getEnd(), tok.getLineNumber(),
                    tok.getColumn(), CoreConstants.SPC);
        } else {
            newData = null;
        }

        if (newData != null) {
            final int size = current.size();

            if (size > 0) {
                final INode last = current.get(size - 1);

                // Don't allow two adjacent CData nodes - concatenate
                if (last instanceof final CData prior) {
                    newData = new CData(tok.getContent(), prior.getStart(), newData.getEnd(),
                            tok.getLineNumber(), tok.getColumn(), prior.content + newData.getContent());

                    current.remove(size - 1);
                }
            }

            current.add(newData);
        }
    }

    /**
     * Processes a token that represents an empty element.
     *
     * @param tok     the token being processed
     * @param current the current list of nodes
     */
    private static void processEmptyElement(final ICharSpan tok, final Collection<INode> current) {

        final TokEmptyElement token = (TokEmptyElement) tok;

        final TagSpan span = new TagSpan(tok.getStart(), tok.getEnd(), tok.getLineNumber(), tok.getColumn(), true,
                true, tok.getStart() + 1, tok.getStart() + 1 + token.getName().length());

        final IElement node = new EmptyElement(token.getName(), span);

        for (final Attribute attribute : token.getAttributes().values()) {
            node.putAttribute(attribute);
        }

        current.add(node);
    }

    /**
     * Processes a start tag.
     *
     * @param tok     the token being processed
     * @param current the current list of nodes
     * @param stack   the stack of node lists
     * @return the new list of nodes representing content of the started tag
     */
    private static List<INode> processStartTag(final ICharSpan tok, final Collection<INode> current,
                                               final Deque<NonemptyElement> stack) {

        final TokSTag token = (TokSTag) tok;

        final TagSpan span = new TagSpan(tok.getStart(), tok.getEnd(), tok.getLineNumber(), tok.getColumn(), true,
                true, tok.getStart() + 1, tok.getStart() + 1 + token.getName().length());

        final NonemptyElement node = new NonemptyElement(token.getName(), span);

        for (final Attribute attribute : token.getAttributes().values()) {
            node.putAttribute(attribute);
        }

        current.add(node);
        stack.push(node);

        return node.getChildrenAsList();
    }

    /**
     * Processes an end tag.
     *
     * @param tok       the token being processed
     * @param stack     the stack of node lists
     * @param rootNodes the list of root nodes in the XML structure
     * @return the list of nodes representing the parent container of the ended tag
     * @throws ParsingException if the tag being ended does not match the currently open tag
     */
    private static List<INode> processEndTag(final ICharSpan tok, final Deque<NonemptyElement> stack,
                                             final List<INode> rootNodes) throws ParsingException {

        if (stack.isEmpty()) {
            throw new ParsingException(tok.getStart(), tok.getEnd(), Res.get(Res.NO_OPEN));
        }

        final TokETag token = (TokETag) tok;
        final NonemptyElement opening = stack.pop();

        if (!opening.getTagName().equals(token.getName())) {
            throw new ParsingException(tok.getStart(), tok.getEnd(), Res.fmt(Res.BAD_CLOSE, opening.getTagName()));
        }

        final TagSpan span = new TagSpan(tok.getStart(), tok.getEnd(), tok.getLineNumber(), tok.getColumn(), false,
                true, tok.getStart() + 1, tok.getStart() + 1 + token.getName().length());
        opening.setClosingTagSpan(span);

        return stack.isEmpty() ? rootNodes : stack.peek().getChildrenAsList();
    }
}
