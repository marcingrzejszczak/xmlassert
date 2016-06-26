package com.toomuchcoding.xmlassert;

import java.util.LinkedList;

import org.w3c.dom.Document;

class ArrayValueAssertion extends FieldAssertion implements XmlArrayVerifiable {

    boolean checkingPrimitiveType = true;

    protected ArrayValueAssertion(Document parsedXml, LinkedList<String> jsonPathBuffer,
                                  Object arrayName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(parsedXml, jsonPathBuffer, arrayName, xmlAsserterConfiguration);
    }

    protected ArrayValueAssertion(Document parsedXml, LinkedList<String> jsonPathBuffer,
                                  XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(parsedXml, jsonPathBuffer, null, xmlAsserterConfiguration);
    }

    @Override
    public XmlArrayVerifiable contains(String value) {
        return new ArrayValueAssertion(parsedXml, xPathBuffer, value,
                xmlAsserterConfiguration);
    }

    @Override public FieldAssertion node(String value) {
        checkingPrimitiveType = false;
        return super.node(value);
    }

    @Override public FieldAssertion node(String... nodeNames) {
        checkingPrimitiveType = false;
        return super.node(nodeNames);
    }

    @Override
    public XmlVerifiable isEqualTo(String value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        return equalityOnAPrimitive(parsedXml, xPathBuffer, fieldName, xmlAsserterConfiguration,
                "[text()=" + wrapValueWithSingleQuotes(value) + "]");
    }

    @Override
    public XmlVerifiable isEqualTo(Number value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        return equalityOnAPrimitive(parsedXml, xPathBuffer, fieldName,
                xmlAsserterConfiguration, "[text()=" + String.valueOf(value) + "]");
    }

    private XmlVerifiable equalityOnAPrimitive(Document parsedXml,
            LinkedList<String> xPathBuffer, Object fieldName,
            XmlAsserterConfiguration xmlAsserterConfiguration, String e) {
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.offer(e);
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public XmlVerifiable matches(String value) {
        if (!checkingPrimitiveType) {
            return super.matches(value);
        }
        return equalityOnAPrimitive(parsedXml, xPathBuffer, fieldName, xmlAsserterConfiguration,
                "[matches(text(), " +
						wrapValueWithSingleQuotes(stringWithEscapedSingleQuotesForRegex(value)) + ")]");
    }

    @Override
    public XmlVerifiable isEqualTo(Boolean value) {
        if (!checkingPrimitiveType) {
            return super.isEqualTo(value);
        }
        ReadyToCheckAsserter readyToCheck = new ReadyToCheckAsserter(parsedXml,
                xPathBuffer, fieldName, xmlAsserterConfiguration);
        readyToCheck.xPathBuffer.offer("[text()=" + String.valueOf(value) + "]");
        readyToCheck.checkBufferedXPathString();
        return readyToCheck;
    }

    @Override
    public boolean isAssertingAValueInArray() {
        return true;
    }

}