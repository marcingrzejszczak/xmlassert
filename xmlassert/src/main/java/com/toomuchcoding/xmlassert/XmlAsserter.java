package com.toomuchcoding.xmlassert;

import java.util.LinkedList;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import net.minidev.json.JSONArray;

/**

 XPathFactory xPathfactory = XPathFactory.newInstance();
 XPath xpath = xPathfactory.newXPath();
 XPathExpression expr = xpath.compile(<xpath_expression>);

 XPathExpression expr = xpath.compile("/howto/topic[@name='PowerBuilder']/url");
 NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);

 */
class XmlAsserter implements XmlVerifiable {

    private static final Logger log = LoggerFactory.getLogger(XmlAsserter.class);

    protected final Document parsedXml;
    protected final LinkedList<String> xPathBuffer;
    protected final Object fieldName;
    protected final XmlAsserterConfiguration xmlAsserterConfiguration;

    protected XmlAsserter(Document parsedXml, LinkedList<String> xPathBuffer,
                           Object fieldName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        this.parsedXml = parsedXml;
        this.xPathBuffer = new LinkedList<String>(xPathBuffer);
        this.fieldName = fieldName;
        this.xmlAsserterConfiguration = xmlAsserterConfiguration;
    }

    @Override
    public FieldAssertion node(final String value) {
        FieldAssertion asserter = new FieldAssertion(parsedXml, xPathBuffer, value,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer(String.valueOf(value));
        asserter.xPathBuffer.offer("/");
        return asserter;
    }

    @Override
    public FieldAssertion node(String... nodeNames) {
        FieldAssertion assertion = null;
        for(String field : nodeNames) {
            assertion = assertion == null ? node(field) : assertion.node(field);
        }
        return assertion;
    }

    @Override
    public XmlArrayVerifiable array(final String value) {
        ArrayValueAssertion asserter = new ArrayValueAssertion(parsedXml, xPathBuffer, value,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer(String.valueOf(value));
        asserter.xPathBuffer.offer("/");
        return asserter;
    }

    @Override
    public XmlVerifiable isEqualTo(String value) {
        if (value == null) {
            return isNull();
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[?(@." + String.valueOf(fieldName) + " == " + wrapValueWithSingleQuotes(value) + ")]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    private void updateCurrentBuffer(XmlAsserter readyToCheck) {
        xPathBuffer.clear();
        xPathBuffer.addAll(readyToCheck.xPathBuffer);
    }

    @Override
    public XmlVerifiable isEqualTo(Object value) {
        if (value == null) {
            return isNull();
        }
        if (value instanceof Number) {
            return isEqualTo((Number) value);
        } else if (value instanceof Boolean) {
            return isEqualTo((Boolean) value);
        } else if (value instanceof Pattern) {
            return matches(((Pattern) value).pattern());
        }
        return isEqualTo(value.toString());
    }

    @Override
    public XmlVerifiable isEqualTo(Number value) {
        if (value == null) {
            return isNull();
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[" + fieldName + "=" + String.valueOf(value) + "]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable isNull() {
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[?(@." + String.valueOf(fieldName) + " == null)]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable matches(String value) {
        if (value == null) {
            return isNull();
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[?(@." + String.valueOf(fieldName)
                + " =~ /" + stringWithEscapedSingleQuotesForRegex(value) + "/)]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable isEqualTo(Boolean value) {
        if (value == null) {
            return isNull();
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[?(@." + String.valueOf(fieldName) + " == " + String.valueOf(value) + ")]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable value() {
        ReadyToCheckAsserter readyToCheckAsserter = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheckAsserter.checkBufferedXPathString();
        return readyToCheckAsserter;
    }

    @Override
    public XmlVerifiable withoutThrowingException() {
        xmlAsserterConfiguration.ignoreXPathException = true;
        return this;
    }

    private void check(String jsonPathString) {
        if (xmlAsserterConfiguration.ignoreXPathException) {
            log.trace("WARNING!!! Overriding verification of the XPath. Your tests may pass even though they shouldn't");
            return;
        }
        boolean empty;
        try {
            empty = parsedXml.read(jsonPathString, JSONArray.class).isEmpty();
        } catch (Exception e) {
           log.error("Exception occurred while trying to match XPath [{}]", jsonPathString, e);
           throw new RuntimeException(e);
        }
        if (empty) {
            throw new IllegalStateException("Parsed JSON [" + parsedXml.jsonString() + "] doesn't match the XPath [" + jsonPathString + "]");
        }
    }

    protected void checkBufferedXPathString() {
        check(createJsonPathString());
    }

    private String createJsonPathString() {
        LinkedList<String> queue = new LinkedList<String>(xPathBuffer);
        StringBuilder stringBuffer = new StringBuilder();
        while (!queue.isEmpty()) {
            stringBuffer.append(queue.remove());
        }
        return stringBuffer.toString();
    }

    @Override
    public String xPath() {
        return createJsonPathString();
    }

    @Override
    public void matchesXPath(String xPath) {
        check(xPath);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        XmlAsserter that = (XmlAsserter) o;
        if (xPathBuffer != null ? !xPathBuffer.equals(that.xPathBuffer) : that.xPathBuffer
                != null)
            return false;
        return fieldName != null ? fieldName.equals(that.fieldName) : that.fieldName == null;

    }

    @Override
    public int hashCode() {
        int result = xPathBuffer != null ? xPathBuffer.hashCode() : 0;
        result = 31 * result + (fieldName != null ? fieldName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "\\nAsserter{\n    " + "xPathBuffer=" + String.valueOf(xPathBuffer)
                + "\n}";
    }

    @Override
    public boolean isIteratingOverArray() {
        return false;
    }

    @Override
    public boolean isAssertingAValueInArray() {
        return false;
    }

    protected static String stringWithEscapedSingleQuotes(Object object) {
        String stringValue = object.toString();
        return stringValue.replaceAll("'", "\\\\'");
    }

    protected static String stringWithEscapedSingleQuotesForRegex(Object object) {
        return stringWithEscapedSingleQuotes(object).replace("/", "\\/");
    }

    protected String wrapValueWithSingleQuotes(Object value) {
        return value instanceof String ?
                "'" + stringWithEscapedSingleQuotes(value) + "'" :
                value.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T read(Class<T> clazz) {
        Object readObject = parsedXml.read(xPath());
        if (readObject instanceof JSONArray) {
            JSONArray array = parsedXml.read(xPath());
            if (array.size() == 1) {
                return (T) array.get(0);
            }
            return (T) array;
        }
        return (T) readObject;
    }
}