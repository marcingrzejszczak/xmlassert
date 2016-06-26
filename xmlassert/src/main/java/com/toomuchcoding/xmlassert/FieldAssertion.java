package com.toomuchcoding.xmlassert;

import java.util.LinkedList;

class FieldAssertion extends XmlAsserter {
    protected FieldAssertion(XmlCachedObjects cachedObjects, LinkedList<String> xPathBuffer,
                             Object value, XmlAsserterConfiguration xmlAsserterConfiguration) {
        super(cachedObjects, xPathBuffer, value, xmlAsserterConfiguration);
    }

    protected FieldAssertion(XmlAsserter asserter) {
        super(asserter);
    }
}