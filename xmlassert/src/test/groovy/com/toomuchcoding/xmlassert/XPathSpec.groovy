package com.toomuchcoding.xmlassert

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class XPathSpec extends Specification {

    def "should generate proper JSON paths"() {
        expect:
            jsonPath == expectedJsonPath
        where:
            jsonPath                                                                                                         || expectedJsonPath
            XPathBuilder.builder().node("some").node("nested").node("anothervalue").isEqualTo(4).xPath()                     || '''$.some.nested[?(@.anothervalue == 4)]'''
            XPathBuilder.builder().node("some").node("nested").node("anothervalue").isEqualTo(4).xPath()                     || '''$.some.nested[?(@.anothervalue == 4)]'''
            XPathBuilder.builder().node("some").node("nested").array("withlist").contains("name").isEqualTo("name1").xPath() || '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
            XPathBuilder.builder().node("some").node("nested").array("withlist").contains("name").isEqualTo("name2").xPath() || '''$.some.nested.withlist[*][?(@.name == 'name2')]'''
            XPathBuilder.builder().node("some").node("nested").node("json").isEqualTo("with \"val'ue").xPath()               || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
            XPathBuilder.builder().node("some", "nested", "json").isEqualTo("with \"val'ue").xPath()                         || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
    }

}
