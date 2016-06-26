package com.toomuchcoding.jsonassert

import spock.lang.Specification

/**
 * @author Marcin Grzejszczak
 */
class JsonPathSpec extends Specification {

    def "should generate proper JSON paths"() {
        expect:
            jsonPath == expectedJsonPath
        where:
            jsonPath                                                                                                    || expectedJsonPath
            XPath.builder().field("some").field("nested").field("anothervalue").isEqualTo(4).xPath()                    || '''$.some.nested[?(@.anothervalue == 4)]'''
            XPath.builder().field("some").field("nested").field("anothervalue").isEqualTo(4).xPath()                    || '''$.some.nested[?(@.anothervalue == 4)]'''
            XPath.builder().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1").xPath() || '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
            XPath.builder().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2").xPath() || '''$.some.nested.withlist[*][?(@.name == 'name2')]'''
            XPath.builder().field("some").field("nested").field("json").isEqualTo("with \"val'ue").xPath()              || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
            XPath.builder().field("some", "nested", "json").isEqualTo("with \"val'ue").xPath()                          || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
    }

}
