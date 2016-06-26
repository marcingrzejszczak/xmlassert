package com.toomuchcoding.xmlassert;

import java.util.LinkedList;

class ReadyToCheckAsserter extends XmlAsserter {

    public ReadyToCheckAsserter(XmlCachedObjects cachedObjects, LinkedList<String> jsonPathBuffer,
                                Object fieldName, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(cachedObjects, jsonPathBuffer, fieldName, xmlAsserterConfiguration);
    }
}