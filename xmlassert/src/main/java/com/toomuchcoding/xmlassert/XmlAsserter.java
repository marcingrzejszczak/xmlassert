package com.toomuchcoding.xmlassert;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.wst.xml.xpath2.api.ResultSequence;
import org.eclipse.wst.xml.xpath2.api.XPath2Expression;
import org.eclipse.wst.xml.xpath2.processor.Engine;
import org.eclipse.wst.xml.xpath2.processor.internal.types.ElementType;
import org.eclipse.wst.xml.xpath2.processor.util.DynamicContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class XmlAsserter implements XmlVerifiable {

    private static final Logger log = LoggerFactory.getLogger(XmlAsserter.class);

    private final static Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+*?^$\\\\|]");

    protected final XmlCachedObjects cachedObjects;
    protected final LinkedList<String> xPathBuffer;
    // for things like count(...)
    protected final LinkedList<String> specialCaseXPathBuffer;
    protected final Object fieldName;
    protected final XmlAsserterConfiguration xmlAsserterConfiguration;

    protected XmlAsserter(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer, LinkedList<String> specialCaseXPathBuffer,
                          Object fieldName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        this.cachedObjects = cachedObjects;
        this.xPathBuffer = new LinkedList<String>(xPathBuffer);
        this.specialCaseXPathBuffer = new LinkedList<String>(specialCaseXPathBuffer);
        this.fieldName = fieldName;
        this.xmlAsserterConfiguration = xmlAsserterConfiguration;
    }

    protected XmlAsserter(XmlAsserter asserter) {
        this.cachedObjects = asserter.cachedObjects;
        this.xPathBuffer = new LinkedList<String>(asserter.xPathBuffer);
        this.specialCaseXPathBuffer = new LinkedList<String>(asserter.specialCaseXPathBuffer);
        this.fieldName = asserter.fieldName;
        this.xmlAsserterConfiguration = asserter.xmlAsserterConfiguration;
    }

    @Override
    public FieldAssertion node(final String value) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer, specialCaseXPathBuffer, value,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer(String.valueOf(value));
        asserter.xPathBuffer.offer("/");
        return asserter;
    }

    @Override
    public XmlVerifiable withAttribute(String attribute, String attributeValue) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer,
                specialCaseXPathBuffer, fieldName,
                xmlAsserterConfiguration);
        if (asserter.xPathBuffer.peekLast().equals("/")) {
            asserter.xPathBuffer.removeLast();
        }
        if (isReadyToCheck()) {
            asserter.xPathBuffer.offer("/" + fieldName);
        }
        asserter.xPathBuffer.offer("[@" + String.valueOf(attribute) + "=" + escapeText(attributeValue) + "]");
        updateCurrentBuffer(asserter);
        asserter.checkBufferedXPathString();
        return asserter;
    }

    @Override
    public XmlVerifiable withAttribute(String attribute) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer,
                specialCaseXPathBuffer, fieldName,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer("@" + String.valueOf(attribute));
        updateCurrentBuffer(asserter);
        asserter.checkBufferedXPathString();
        return asserter;
    }

    @Override
    public XmlVerifiable text() {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer,
                specialCaseXPathBuffer, fieldName,
                xmlAsserterConfiguration);
        asserter.xPathBuffer.offer("text()");
        return asserter;
    }

    @Override
    public XmlVerifiable index(int index) {
        FieldAssertion asserter = new FieldAssertion(cachedObjects, xPathBuffer,
                specialCaseXPathBuffer, fieldName,
                xmlAsserterConfiguration);
        if (asserter.xPathBuffer.peekLast().equals("/")) {
            asserter.xPathBuffer.removeLast();
        }
        asserter.xPathBuffer.offer("[" + index + "]");
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
        ArrayValueAssertion asserter = new ArrayValueAssertion(cachedObjects, xPathBuffer, specialCaseXPathBuffer,
                value, xmlAsserterConfiguration);
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
        readyToCheck.xPathBuffer.offer("[" + fieldName + "=" + escapeText(value) + "]");
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
        readyToCheck.xPathBuffer.offer("not(boolean(" + xpath + "/text()[1]))");
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
        removeLastFieldElement(readyToCheck);
        readyToCheck.xPathBuffer.offer("[matches(" + fieldName + ", " +
                escapeText(escapeRegex(value)) + ")]");
        updateCurrentBuffer(readyToCheck);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable isEqualTo(Boolean value) {
        if (value == null) {
            return isNull();
        }
        return isEqualTo(String.valueOf(value));
    }

    @Override
    public XmlVerifiable withoutThrowingException() {
        xmlAsserterConfiguration.ignoreXPathException = true;
        return this;
    }

    protected void check(String xPathString) {
        if (xmlAsserterConfiguration.ignoreXPathException) {
            log.trace("WARNING!!! Overriding verification of the XPath. Your tests may pass even though they shouldn't");
            return;
        }
        ResultSequence expr = resultSequence(xPathString);
        boolean xpathMatched = !expr.empty();
        if (!xpathMatched) {
            throw new IllegalStateException("Parsed XML [" + cachedObjects.xmlAsString + "] doesn't match the XPath <" + xPathString + ">");
        }
    }

    protected ResultSequence resultSequence(String xPathString) {
        return xPathExpression(xPathString);
    }

    private ResultSequence xPathExpression(String xPathString) {
        try {
            XPath2Expression expr = new Engine().parseExpression(xPathString, cachedObjects.xpathBuilder);
            return expr.evaluate(new DynamicContextBuilder(cachedObjects.xpathBuilder),
                    new Object[] { cachedObjects.document });
        } catch (Exception e) {
            throw new XmlAsserterXpathException(xPath(), cachedObjects.xmlAsString, e);
        }
    }

    protected void checkBufferedXPathString() {
        check(createXPathString());
    }

    protected String createXPathString() {
        return createXPathString(xPathBuffer);
    }

    protected String createSpecialCaseXPathString() {
        return createXPathString(specialCaseXPathBuffer);
    }

    protected String createXPathString(LinkedList<String> buffer) {
        LinkedList<String> queue = new LinkedList<String>(buffer);
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
        if (!specialCaseXPathBuffer.isEmpty()) {
            return createSpecialCaseXPathString();
        }
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

    protected static String escapeText(Object object) {
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

    protected static String escapeRegex(Object object) {
        return String.valueOf(object);
    }

    private static String escapeSpecialRegexChars(String str) {
        return SPECIAL_REGEX_CHARS.matcher(str).replaceAll("\\\\$0");
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
        ResultSequence expr = resultSequence(xpath);
        if (expr.empty()) {
            throw new XmlAsserterXpathException(xPath(), cachedObjects.xmlAsString);
        }
        if (expr instanceof ElementType) {
           return ((ElementType) expr).getStringValue();
        }
        throw new UnsupportedOperationException("Can't return values of complex types");
    }

    protected boolean isReadyToCheck() {
        return false;
    }

    private static class XmlAsserterXpathException extends RuntimeException {
        XmlAsserterXpathException(String xPath, String xmlAsString) {
            super("Exception occurred while trying to evaluate " +
                    "XPath [" + xPath + "] from XML [" + xmlAsString + "]");
        }

        XmlAsserterXpathException(String xPath, String xmlAsString, Exception e) {
            super("Exception occurred while trying to evaluate " +
                    "XPath [" + xPath + "] from XML [" + xmlAsString + "]", e);
        }
    }
}