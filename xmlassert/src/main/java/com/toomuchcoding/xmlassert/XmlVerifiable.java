package com.toomuchcoding.xmlassert;

/**
 * Contract to match a parsed XML via XPath
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.0.1
 */
public interface XmlVerifiable extends IteratingOverArray, XmlReader {

    /**
     * Field assertion. Adds a XPath entry for a single node.
     */
    XmlVerifiable node(String nodeName);

    /**
     * Field assertions. Traverses through the list of nodes and
     * adds a XPath entry for each one.
     */
    XmlVerifiable node(String... nodeNames);

    /**
     * When you want to assert values in a array with a given name, e.g.
     *
     * </p>
     *
     * {@code
            <list>
            <element>foo</element>
            <element>bar</element>
            <complexElement>
                <param>baz</param>
            </complexElement>
            </list>
     * }
     *
     * </p>
     * The code to check it would look like this:
     * </p>
     *
     * {@code array("list").contains("element").isEqualTo("foo")}
     * {@code array("list").contains("complexElement").node("param").isEqualTo("baz")}
     *
     * </p>
     * The generated XPaths would be
     * </p>
     *
     * {@code /list/element[text()='foo']}
     * {@code /list/complexElement[param='baz']}
     */
    XmlArrayVerifiable array(String value);

    /**
     * Equality comparison with String
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable isEqualTo(String value) throws IllegalStateException;

    /**
     * Equality comparison with any object
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable isEqualTo(Object value) throws IllegalStateException;

    /**
     * Equality comparison with a Number
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable isEqualTo(Number value) throws IllegalStateException;

    /**
     * Equality comparison to null
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable isNull() throws IllegalStateException;

    /**
     * Regex matching for strings
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable matches(String value) throws IllegalStateException;

    /**
     * Equality comparison with a Boolean
     *
     * @throws IllegalStateException - if XPath is not matched for the parsed XML
     */
    XmlVerifiable isEqualTo(Boolean value) throws IllegalStateException;

    /**
     * Calling this method will setup the fluent interface to ignore any XPath verification
     */
    XmlVerifiable withoutThrowingException();

    /**
     * Returns current XPath expression
     */
    String xPath();

    /**
     * Checks if the parsed document matches given XPath
     */
    void matchesXPath(String xPath);

}
