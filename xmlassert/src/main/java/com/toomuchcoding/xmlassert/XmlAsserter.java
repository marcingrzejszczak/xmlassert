package com.toomuchcoding.xmlassert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

class XmlAsserter implements XmlVerifiable {

    private static final Logger log = LoggerFactory.getLogger(XmlAsserter.class);

    protected final XmlCachedObjects cachedObjects;
    protected final LinkedList<String> xPathBuffer;
    protected final Object fieldName;
    protected final XmlAsserterConfiguration xmlAsserterConfiguration;

    protected XmlAsserter(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer,
                          Object fieldName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        this.cachedObjects = cachedObjects;
        this.xPathBuffer = new LinkedList<String>(xPathBuffer);
        this.fieldName = fieldName;
        this.xmlAsserterConfiguration = xmlAsserterConfiguration;
    }

    protected XmlAsserter(XmlAsserter asserter) {
        this.cachedObjects = asserter.cachedObjects;
        this.xPathBuffer = new LinkedList<String>(asserter.xPathBuffer);
        this.fieldName = asserter.fieldName;
        this.xmlAsserterConfiguration = asserter.xmlAsserterConfiguration;
    }

    @Override
    public FieldAssertion node(final String value) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer, value,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer(String.valueOf(value));
        asserter.xPathBuffer.offer("/");
        return asserter;
    }

    @Override
    public XmlVerifiable withAttribute(String attribute, String attributeValue) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer, fieldName,
                xmlAsserterConfiguration);
        if (asserter.xPathBuffer.peekLast().equals("/")) {
            asserter.xPathBuffer.removeLast();
        }
        if (isReadyToCheck()) {
            asserter.xPathBuffer.offer("/" + fieldName);
        }
        asserter.xPathBuffer.offer("[@" + String.valueOf(attribute) + "=" + escapeTextForXPath(attributeValue) + "]");
        updateCurrentBuffer(asserter);
        asserter.checkBufferedXPathString();
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
        ArrayValueAssertion asserter = new ArrayValueAssertion(cachedObjects, xPathBuffer, value,
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
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(cachedObjects,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        removeLastFieldElement(readyToCheck);
        readyToCheck.xPathBuffer.offer("[" + fieldName + "=" + escapeTextForXPath(value) + "]");
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
        return xmlVerifiableFromObject(value);
    }

    private XmlVerifiable xmlVerifiableFromObject(Object value) {
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(cachedObjects,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        removeLastFieldElement(readyToCheck);
        readyToCheck.xPathBuffer.offer("[" + fieldName + "=" + String.valueOf(value) + "]");
        // and finally '/foo/bar[baz='sth']
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    protected void removeLastFieldElement(XmlAsserter readyToCheck) {
        // assuming /foo/bar/baz/
        // remove '/'
        readyToCheck.xPathBuffer.removeLast();
        // remove field name ('baz')
        readyToCheck.xPathBuffer.removeLast();
        // remove '/'
        readyToCheck.xPathBuffer.removeLast();
        // and then we get '/foo/bar'
    }

    @Override
    public XmlVerifiable isNull() {
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(cachedObjects,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        String xpath = createXPathString();
        readyToCheck.xPathBuffer.clear();
        readyToCheck.xPathBuffer.offer("boolean(" + xpath + "/text()[1])");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable matches(String value) {
        if (value == null) {
            return isNull();
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(cachedObjects,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer("[matches(" + fieldName + ", " +
                wrapValueWithSingleQuotes(escapeTextForXPath(value)) + ")]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable isEqualTo(Boolean value) {
        if (value == null) {
            return isNull();
        }
        return xmlVerifiableFromObject(value);
    }

    @Override
    public XmlVerifiable withoutThrowingException() {
        xmlAsserterConfiguration.ignoreXPathException = true;
        return this;
    }

    private void check(String xPathString) {
        if (xmlAsserterConfiguration.ignoreXPathException) {
            log.trace("WARNING!!! Overriding verification of the XPath. Your tests may pass even though they shouldn't");
            return;
        }
        boolean empty;
        try {
            XPathExpression expr = xPathExpression(xPathString);
            NodeList nl = (NodeList) expr.evaluate(cachedObjects.document, XPathConstants.NODESET);
            empty = nl.getLength() == 0;
        } catch (Exception e) {
           log.error("Exception occurred while trying to match XPath <{}>", xPathString, e);
           throw new RuntimeException(e);
        }
        if (empty) {
            throw new IllegalStateException("Parsed XML [" + cachedObjects.xmlAsString + "] doesn't match the XPath <" + xPathString + ">");
        }
    }

    private XPathExpression xPathExpression(String xPathString) {
        XPath xpath = cachedObjects.factory.newXPath();
        try {
            return xpath.compile(xPathString);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("For XPath <" + xPathString + "> " +
                    "XPath compile exception occurred", e);
        }
    }

    protected void checkBufferedXPathString() {
        check(createXPathString());
    }

    private String createXPathString() {
        LinkedList<String> queue = new LinkedList<String>(xPathBuffer);
        StringBuilder stringBuffer = new StringBuilder();
        while (!queue.isEmpty()) {
            String value = queue.remove();
            if (!(queue.isEmpty() && value.equals("/"))) {
                stringBuffer.append(value);
            }
        }
        return stringBuffer.toString();
    }

    @Override
    public String xPath() {
        return createXPathString();
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
        if (!xPathBuffer.equals(that.xPathBuffer))
            return false;
        return fieldName != null ? fieldName.equals(that.fieldName) : that.fieldName == null;

    }

    @Override
    public int hashCode() {
        int result = xPathBuffer.hashCode();
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

    protected static String escapeTextForXPath(Object object) {
        String string = String.valueOf(object);
        if (!string.contains("'")) {
            return wrapValueWithSingleQuotes(string);
        }
        String[] split = string.split("'");
        LinkedList<String> list = new LinkedList<String>();
        list.add("concat(");
        for (String splitString : split) {
            list.add("'" + splitString + "'");
            list.add(",");
            list.add("\"'\"");
            list.add(",");
        }
        // will remove the last ,', entries
        // removing last colon
        list.removeLast();
        // removing last escaped apostrophe
        list.removeLast();
        // removing last colon
        list.removeLast();
        list.add(")");
        return buildStringFromList(list);
    }

    private static String buildStringFromList(List<String> list) {
        StringBuilder builder = new StringBuilder();
        for (String string : list) {
            builder.append(string);
        }
        return builder.toString();
    }

    protected static String wrapValueWithSingleQuotes(Object value) {
        return value instanceof String ?
                "'" + value + "'" :
                value.toString();
    }

    @Override
    public String read() {
        String xpath = xPath();
        XPathExpression expr = xPathExpression(xpath);
        try {
            return expr.evaluate(cachedObjects.document);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("Exception occurred while trying to evaluate " +
                    "XPath [" + xPath() + "] from XML [" + cachedObjects.xmlAsString + "]", e);
        }
    }

    protected boolean isReadyToCheck() {
        return false;
    }
}