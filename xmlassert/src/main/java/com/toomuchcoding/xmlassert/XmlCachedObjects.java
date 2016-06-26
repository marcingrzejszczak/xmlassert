package com.toomuchcoding.xmlassert;

import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathFactory;
import java.io.StringWriter;

/**
 * Contains cached objects that are memory consuming
 *
 * @author Marcin Grzejszczak
 *
 * @since 0.0.1
 */
class XmlCachedObjects {
    final Document document;
    final XPathFactory factory;
    final String xmlAsString;

    XmlCachedObjects(Document document, XPathFactory factory) {
        this.document = document;
        this.factory = factory;
        this.xmlAsString = xmlAsString();
    }

    XmlCachedObjects(Document document, XPathFactory factory, String xmlAsString) {
        this.document = document;
        this.factory = factory;
        this.xmlAsString = xmlAsString;
    }

    private String xmlAsString() {
        try {
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString().replaceAll("\n|\r", "");
        } catch (TransformerException e) {
            throw new RuntimeException("Exception occured while trying to convert XML Document to String", e);
        }
    }
}
