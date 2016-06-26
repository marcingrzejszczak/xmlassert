package com.toomuchcoding.xmlassert;

/**
 * Contract to match an array in a parsed XML via XPath
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.0.1
 */
public interface XmlArrayVerifiable extends XmlVerifiable {

    /**
     * When you want to assert a node with a name in an array.
     */
    XmlArrayVerifiable contains(String nodeName);

}
