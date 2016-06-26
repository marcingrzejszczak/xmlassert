package com.toomuchcoding.xmlassert;

import java.util.LinkedList;

import org.w3c.dom.Document;

class ReadyToCheckAsserter extends XmlAsserter {

    public ReadyToCheckAsserter(Document parsedXml, LinkedList<String> jsonPathBuffer,
                                Object fieldName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(parsedXml, jsonPathBuffer, fieldName, xmlAsserterConfiguration);
    }
}