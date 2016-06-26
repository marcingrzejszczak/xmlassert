package com.toomuchcoding.xmlassert;

import com.jayway.jsonpath.DocumentContext;

import java.util.LinkedList;

import org.w3c.dom.Document;

class FieldAssertion extends XmlAsserter {
    protected FieldAssertion(Document parsedJson, LinkedList<String> jsonPathBuffer,
                             Object value, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(parsedJson, jsonPathBuffer, value, xmlAsserterConfiguration);
    }
}