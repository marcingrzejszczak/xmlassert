package com.toomuchcoding.xmlassert;

import com.jayway.jsonpath.DocumentContext;
import org.assertj.core.api.Assertions;

/**
 * Entry point for {@link DocumentContext} {@link Assertions}
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.2.0
 */
public class XmlAssertions extends Assertions {

    public static XPathAssert assertThat(DocumentContext actual) {
        return new XPathAssert(actual);
    }

    public static XPathAssert assertThat(XmlVerifiable actual) {
        return new XPathAssert(actual);
    }

}
