package com.toomuchcoding.xmlassert;

import org.assertj.core.api.AbstractAssert;

import com.jayway.jsonpath.DocumentContext;

/**
 * A AssertJ version of JSON Assert.
 *
 * The methods used by JSON Assert are available as assertions of either
 * {@link DocumentContext} or {@link XmlVerifiable}.
 *
 * Remember that the order of execution matters since it's building the JSON Path
 * in the provided sequence.
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.2.0
 */
public class XPathAssert extends AbstractAssert<XPathAssert, XmlVerifiable> {

    public XPathAssert(DocumentContext actual) {
        super(XmlAssertion.assertThatXml(actual), XPathAssert.class);
    }

    public XPathAssert(XmlVerifiable actual) {
        super(actual, XPathAssert.class);
    }

    /**
     * @see XmlVerifiable#contains(Object)
     */
    public XPathAssert contains(Object value) {
        isNotNull();
        return new XPathAssert(actual.contains(value));
    }

    /**
     * @see XmlVerifiable#node(String)
     */
    public XPathAssert field(Object value) {
        isNotNull();
        return new XPathAssert(actual.node(value));
    }

    /**
     * @see XmlVerifiable#node(String...)
     */
    public XPathAssert field(String... value) {
        isNotNull();
        return new XPathAssert(actual.node(value));
    }

    /**
     * @see XmlVerifiable#array()} (Object)
     */
    public XPathAssert array(Object value) {
        isNotNull();
        return new XPathAssert(actual.array(value));
    }

    /**
     * @see XmlVerifiable#arrayField()
     */
    public XPathAssert arrayField() {
        isNotNull();
        return new XPathAssert(actual.arrayField());
    }

    /**
     * @see XmlVerifiable#array()
     */
    public XPathAssert array() {
        isNotNull();
        return new XPathAssert(actual.array());
    }

    /**
     * @see XmlVerifiable#isEqualTo(String)
     */
    public XPathAssert isEqualTo(String value) {
        isNotNull();
        XmlVerifiable xmlVerifiable = null;
        try {
            xmlVerifiable = actual.isEqualTo(value);
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
        return new XPathAssert(xmlVerifiable);
    }

    /**
     * @see XmlVerifiable#isEqualTo(Number)
     */
    public XPathAssert isEqualTo(Number value) {
        isNotNull();
        XmlVerifiable xmlVerifiable = null;
        try {
            xmlVerifiable = actual.isEqualTo(value);
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
        return new XPathAssert(xmlVerifiable);
    }

    /**
     * @see XmlVerifiable#matches(String)
     */
    public XPathAssert matches(String value) {
        isNotNull();
        XmlVerifiable xmlVerifiable = null;
        try {
            xmlVerifiable = actual.matches(value);
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
        return new XPathAssert(xmlVerifiable);
    }

    /**
     * @see XmlVerifiable#isEqualTo(Boolean)
     */
    public XPathAssert isEqualTo(Boolean value) {
        isNotNull();
        XmlVerifiable xmlVerifiable = null;
        try {
            xmlVerifiable = actual.isEqualTo(value);
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
        return new XPathAssert(xmlVerifiable);
    }

    /**
     * @see XmlVerifiable#value()
     */
    public XPathAssert value() {
        isNotNull();
        XmlVerifiable xmlVerifiable = null;
        try {
            xmlVerifiable = actual.value();
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
        return new XPathAssert(xmlVerifiable);
    }

    /**
     * @see XmlVerifiable#isNull()
     */
    @Override
    public void isNull() {
        isNotNull();
        try {
            actual.isNull();
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", actual.xPath());
        }
    }

    /**
     * @see XmlVerifiable#matchesXPath(String)
     */
    public XPathAssert matchesJsonPath(String jsonPath) {
        isNotNull();
        try {
            actual.matchesXPath(jsonPath);
        } catch (IllegalStateException e) {
            failWithMessage("Expected JSON to match JSON Path <%s> but it didn't", jsonPath);
        }
        return this;
    }

}