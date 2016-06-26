package com.toomuchcoding.xmlassert;

import org.assertj.core.api.BDDAssertions;

import com.jayway.jsonpath.DocumentContext;

/**
 * Entry point for {@link DocumentContext} {@link BDDAssertions}
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.2.0
 */
public class BDDXmlAssertions extends BDDAssertions {

    public static XPathAssert then(DocumentContext actual) {
        return new XPathAssert(actual);
    }

    public static XPathAssert then(XmlVerifiable actual) {
        return new XPathAssert(actual);
    }

}
