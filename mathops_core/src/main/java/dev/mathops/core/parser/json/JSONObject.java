package dev.mathops.core.parser.json;

import dev.mathops.core.CoreConstants;
import dev.mathops.core.builder.HtmlBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * A parsed JSON object, which is a collection of named properties, each of which having a value that is one of the
 * following.
 *
 * <ul>
 * <li>a {@code JSONObject}
 * <li>an array of values
 * <li>a {@code String}
 * <li>a {@code Double}
 * <li>a {@code Boolean}
 * <li>{@code null}
 * </ul>
 */
public class JSONObject {

    /** Map from property name to value. */
    private final Map<String, Object> properties;

    /**
     * Constructs a new {@code JSONObject}.
     */
    public JSONObject() {

        this.properties = new HashMap<>(10);
    }

    /**
     * Sets the value of a property.
     *
     * @param name  the property name
     * @param value the value
     */
    public void setProperty(final String name, final Object value) {

        this.properties.put(name, value);
    }

    /**
     * Gets the value of a property.
     *
     * @param name the property name
     * @return the value
     */
    public Object getProperty(final String name) {

        return this.properties.get(name);
    }

    /**
     * Gets the string value of a property.
     *
     * @param name the property name
     * @return the value, if the value was present and is a string; null if not
     */
    public String getStringProperty(final String name) {

        final Object value = this.properties.get(name);

        return value instanceof String ? (String) value : null;
    }

    /**
     * Gets the number value of a property.
     *
     * @param name the property name
     * @return the value, if the value was present and is a double; null if not
     */
    public Double getNumberProperty(final String name) {

        final Object value = this.properties.get(name);

        return value instanceof Double ? (Double) value : null;
    }

    /**
     * Escapes a string, so it can be emitted in a JSON formatted file.
     *
     * @param source the source string to escape
     * @param target the {@code HtmlBuilder} to which to append the escaped string (surrounding quotes are not emitted)
     */
    private static void escapeJSONString(final String source, final HtmlBuilder target) {

        for (final char ch : source.toCharArray()) {

            if (ch == '\b') {
                target.add("\\b");
            } else if (ch == '\f') {
                target.add("\\f");
            } else if (ch == '\n') {
                target.add("\\n");
            } else if (ch == '\r') {
                target.add("\\r");
            } else if (ch == '\t') {
                target.add("\\t");
            } else if (ch == '"') {
                target.add("\\\"");
            } else if (ch == '\\') {
                target.add("\\\\");
            } else {
                target.add(ch);
            }
        }
    }

    /**
     * Generates the JSON representation of the object.
     *
     * @return the JSON representation
     */
    public String toJSONCompact() {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add('{');
        boolean comma = false;
        for (final Map.Entry<String, Object> entry : this.properties.entrySet()) {
            if (comma) {
                htm.add(CoreConstants.COMMA_CHAR);
            }
            htm.add('"');
            escapeJSONString(entry.getKey(), htm);
            htm.add('"').add(':');
            emitValueCompact(entry.getValue(), htm);
            comma = true;
        }
        htm.add('}');

        return htm.toString();
    }

    /**
     * Emits a single JSON object.
     *
     * @param value the value to emit
     * @param htm   the {@code HtmlBuilder} to which to emit
     */
    private void emitValueCompact(final Object value, final HtmlBuilder htm) {

        if (value instanceof final JSONObject obj) {
            htm.add(obj.toJSONCompact());
        } else if (value instanceof final Object[] array) {
            htm.add('[');
            boolean comma = false;
            for (final Object o : array) {
                if (comma) {
                    htm.add(CoreConstants.COMMA_CHAR);
                }
                emitValueCompact(o, htm);
                comma = true;
            }
            htm.add(']');
        } else if (value instanceof final String str) {
            htm.add('"');
            escapeJSONString(str, htm);
            htm.add('"');
        } else if (value instanceof Double) {
            htm.add(value.toString());
        } else if (value instanceof Boolean) {
            htm.add(value.toString());
        } else {
            htm.add("null");
        }
    }

    /**
     * Generates the JSON representation of the object.
     *
     * @param indent the indentation level (the leading '{' is not indented - but all subsequent lines are indented
     *               "indent + 1" steps)
     * @return the JSON representation
     */
    public String toJSONFriendly(final int indent) {

        final HtmlBuilder htm = new HtmlBuilder(100);

        htm.add("{ ");
        boolean comma = false;
        for (final Map.Entry<String, Object> entry : this.properties.entrySet()) {
            if (comma) {
                htm.addln(CoreConstants.COMMA_CHAR);
                htm.indent(indent + 2);
            }
            htm.add('"');
            escapeJSONString(entry.getKey(), htm);
            htm.add("\": ");
            emitValueFriendly(entry.getValue(), htm, indent + 2);
            comma = true;
        }
        htm.add('}');

        return htm.toString();
    }

    /**
     * Emits a single JSON object.
     *
     * @param value  the value to emit
     * @param htm    the {@code HtmlBuilder} to which to emit
     * @param indent the indentation level (the value itself is not indented - but array contents and object contents
     *               are indented "indent + 1" steps)
     */
    private void emitValueFriendly(final Object value, final HtmlBuilder htm, final int indent) {

        if (value instanceof final JSONObject obj) {
            htm.add(obj.toJSONFriendly(indent));
        } else if (value instanceof final Object[] array) {
            htm.addln('[');
            htm.indent(indent + 2);

            boolean comma = false;
            for (final Object o : array) {
                if (comma) {
                    htm.addln(CoreConstants.COMMA_CHAR);
                    htm.indent(indent + 2);
                }
                emitValueFriendly(o, htm, indent + 2);
                comma = true;
            }
            htm.add(']');
        } else if (value instanceof final String str) {
            htm.add('"');
            escapeJSONString(str, htm);
            htm.add('"');
        } else if (value instanceof Double) {
            htm.add(value.toString());
        } else if (value instanceof Boolean) {
            htm.add(value.toString());
        } else {
            htm.add("null");
        }
    }
}
