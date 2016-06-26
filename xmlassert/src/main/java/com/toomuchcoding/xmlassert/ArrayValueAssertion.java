package com.toomuchcoding.xmlassert;

import java.util.LinkedList;

class ArrayValueAssertion extends FieldAssertion implements XmlArrayVerifiable {

    final boolean checkingPrimitiveType;

    protected ArrayValueAssertion(XmlCachedObjects cachedObjects, LinkedList<String> jsonPathBuffer,
                                  Object arrayName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(cachedObjects, jsonPathBuffer, arrayName, xmlAsserterConfiguration);
        this.checkingPrimitiveType = true;
    }

    protected ArrayValueAssertion(XmlCachedObjects cachedObjects, LinkedList<String> jsonPathBuffer,
                                  Object arrayName, XmlAsserterConfiguration xmlAsserterConfiguration,
                                  boolean checkingPrimitiveType) {
        super(cachedObjects, jsonPathBuffer, arrayName, xmlAsserterConfiguration);
        this.checkingPrimitiveType = checkingPrimitiveType;
    }

    protected ArrayValueAssertion(XmlCachedObjects cachedObjects, LinkedList<String> jsonPathBuffer,
                                  XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(cachedObjects, jsonPathBuffer, null, xmlAsserterConfiguration);
        this.checkingPrimitiveType = true;
    }

    protected ArrayValueAssertion(XmlAsserter asserter, boolean checkingPrimitiveType) {
        super(asserter);
        this.checkingPrimitiveType = checkingPrimitiveType;
    }

    @Override
    public XmlArrayVerifiable contains(String value) {
        return new ArrayValueAssertion(cachedObjects, xPathBuffer, value,
                xmlAsserterConfiguration, false);
    }

    @Override
    public FieldAssertion node(String value) {
        FieldAssertion assertion = super.node(value);
        return new ArrayValueAssertion(assertion, false);
    }

    @Override
    public FieldAssertion node(String... nodeNames) {
        FieldAssertion assertion = super.node(nodeNames);
        return new ArrayValueAssertion(assertion, false);
    }

    @Override
    protected void removeLastFieldElement(XmlAsserter readyToCheck) {
        readyToCheck.xPathBuffer.removeLast();
    }

    @Override
    public XmlVerifiable isEqualTo(String value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        return equalityOnAPrimitive("[text()=" + escapeTextForXPath(value) + "]");
    }

    @Override
    public XmlVerifiable isEqualTo(Number value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        return equalityOnAPrimitive("[number()=" + String.valueOf(value) + "]");
    }

    private XmlVerifiable equalityOnAPrimitive(String xPath) {
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(cachedObjects,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.removeLast();
        readyToCheck.xPathBuffer.offer(xPath);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable matches(String value) {
        if (!checkingPrimitiveType) {
            return super.matches(value);
        }
        return equalityOnAPrimitive(
                "[matches(text(), " +
						escapeTextForXPath(value) + ")]");
    }

    @Override
    public XmlVerifiable isEqualTo(Boolean value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        return isEqualTo(String.valueOf(value));
    }

    @Override
    public boolean isAssertingAValueInArray() {
        return true;
    }

}