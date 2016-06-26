package com.toomuchcoding.jsonassert;

/**
 * Builder of JSON Paths.
 *
 * @author Marcin Grzejszczak
 * @since 0.3.0
 *
 * @see JsonVerifiable
 * @see XmlAssertion
 */
public class XPath {

    /**
     * Returns a builder of {@link JsonVerifiable} with which you can build your
     * JSON Path. Once finished just call {@link JsonVerifiable#xPath()} to get
     * JSON Path as String.
     */
    public static JsonVerifiable builder() {
        return XmlAssertion.assertThat("").withoutThrowingException();
    }

    /**
     * Using a JSON Path builder for the given JSON you can read its value.
     */
    public static JsonVerifiable builder(String json) {
        return XmlAssertion.assertThat(json).withoutThrowingException();
    }

}
