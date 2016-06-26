package com.toomuchcoding.xmlassert;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * Entry point for assertions. Use the static factory method and you're ready to go!
 *
 * @author Marcin Grzejszczak
 * @since 0.0.1
 *
 * @see XmlVerifiable
 */
public class XmlAssertion {
    private final Document parsedXml;
    private final LinkedList<String> xPathBuffer = new LinkedList<String>();
    private final XmlAsserterConfiguration xmlAsserterConfiguration = new XmlAsserterConfiguration();
    private static final Map<String, Document> CACHE = new ConcurrentHashMap<String, Document>();

    private XmlAssertion(Document parsedXml) {
        this.parsedXml = parsedXml;
    }

    private XmlAssertion(String body) {
        Document document = CACHE.get(body);
        if (document == null && !empty(body)) {
            try {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                InputSource inputXml = new InputSource(new StringReader(body));
                document = builder.parse(inputXml);
            } catch (Exception e) {
              throw new IllegalStateException("Exception occurred while trying to parse the XML", e);  
            }
            CACHE.put(body, document);
        }
        this.parsedXml = document;
    }

    private boolean empty(String text) {
        return text == null || text.length() == 0 || text.matches("^\\s*$");
    }

    /**
     * Starts assertions for the XML provided as {@code String}
     */
    public static XmlVerifiable assertThat(String body) {
        return new XmlAssertion(body).root();
    }

    /**
     * Starts assertions for the XML provided as {@code Document}
     */
    public static XmlVerifiable assertThat(Document parsedXml) {
        return new XmlAssertion(parsedXml).root();
    }

    /**
     * Helper method so that there are no clashes with other static methods of that name
     *
     * @see XmlAssertion#assertThat(String)
     */
    public static XmlVerifiable assertThatXml(String body) {
        return assertThat(body);
    }

    /**
     * Helper method so that there are no clashes with other static methods of that name
     *
     * @see XmlAssertion#assertThat(Document)
     */
    public static XmlVerifiable assertThatXml(Document parsedXml) {
        return assertThat(parsedXml);
    }

    private XmlVerifiable root() {
        return new XmlAsserter(parsedXml, xPathBuffer, "/", xmlAsserterConfiguration);
    }

}
