package com.toomuchcoding.jsonassert

import com.jayway.jsonpath.DocumentContext
import com.jayway.jsonpath.JsonPath
import spock.lang.Issue
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import static XmlAssertion.assertThat
import static XmlAssertion.assertThatXml
import static groovy.json.JsonOutput.toJson
/**
 * @author Marcin Grzejszczak
 */
public class XmlAssertionSpec extends Specification {

    @Shared String xml1 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <some>
        <nested>
            <json>with &quot;val'ue</json>
            <anothervalue>4</anothervalue>
            <withlist>
                <name>name1</name>
            </withlist>
            <withlist>
                <name>name2</name>
            </withlist>
        </nested>
    </some>'''

    @Unroll
    def 'should convert a json with a map as root to a map of path to value '() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                                                           || expectedJsonPath
            assertThat(xml1).field("some").field("nested").field("anothervalue").isEqualTo(4)                    || '''$.some.nested[?(@.anothervalue == 4)]'''
            assertThat(xml1).field("some").field("nested").field("anothervalue")                                 || '''$.some.nested.anothervalue'''
            assertThatXml(xml1).field("some").field("nested").field("anothervalue").isEqualTo(4)                 || '''$.some.nested[?(@.anothervalue == 4)]'''
            assertThat(xml1).field("some").field("nested").array("withlist").contains("name").isEqualTo("name1") || '''$.some.nested.withlist[*][?(@.name == 'name1')]'''
            assertThat(xml1).field("some").field("nested").array("withlist").contains("name").isEqualTo("name2") || '''$.some.nested.withlist[*][?(@.name == 'name2')]'''
            assertThat(xml1).field("some").field("nested").field("json").isEqualTo("with \"val'ue")              || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
            assertThat(xml1).field("some", "nested", "json").isEqualTo("with \"val'ue")                          || '''$.some.nested[?(@.json == 'with "val\\'ue')]'''
    }

    @Shared String xml2 =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <property1>a</property1>
    <property2>b</property2>
'''

    @Unroll
    def "should generate assertions for simple response body"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                         || expectedJsonPath
            assertThat(xml2).field("property1").isEqualTo("a") || '''$[?(@.property1 == 'a')]'''
            assertThat(xml2).field("property2").isEqualTo("b") || '''$[?(@.property2 == 'b')]'''
    }

    @Shared String xml3 =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <property1>true</property1>
    <property2 />
    <property3>false</property3>
'''

    @Unroll
    def "should generate assertions for null and boolean values"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                            || expectedJsonPath
            assertThat(xml3).field("property1").isEqualTo("true") || '''$[?(@.property1 == 'true')]'''
            assertThat(xml3).field("property2").isNull()          || '''$[?(@.property2 == null)]'''
            assertThat(xml3).field("property3").isEqualTo(false)  || '''$[?(@.property3 == false)]'''
    }

    @Shared Map xml4 =  [
            property1: 'a',
            property2: [
                    [a: 'sth'],
                    [b: 'sthElse']
            ]
    ]

    @Unroll
    def "should generate assertions for simple response body constructed from map with a list"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                                     || expectedJsonPath
            assertThat(toJson(xml4)).field("property1").isEqualTo("a")                     || '''$[?(@.property1 == 'a')]'''
            assertThat(toJson(xml4)).array("property2").contains("a").isEqualTo("sth")     || '''$.property2[*][?(@.a == 'sth')]'''
            assertThat(toJson(xml4)).array("property2").contains("b").isEqualTo("sthElse") || '''$.property2[*][?(@.b == 'sthElse')]'''
    }

    @Shared Map xml5 =  [
            property: [
                    14: 0.0,
                    7 : 0.0
            ]
    ]

    @Unroll
    def "should generate assertions for a response body containing map with integers as keys"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                          || expectedJsonPath
            assertThat(toJson(xml5)).field("property").field(7).isEqualTo(0.0)  || '''$.property[?(@.7 == 0.0)]'''
            assertThat(toJson(xml5)).field("property").field(14).isEqualTo(0.0) || '''$.property[?(@.14 == 0.0)]'''
    }

    @Shared String xml6 =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>
        <property1>a</property1>
    </0>
    <1>
        <property2>b</property2>
    </1>
'''

    @Unroll
    def "should generate assertions for array in response body"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                    || expectedJsonPath
            assertThat(xml6).array().contains("property1").isEqualTo("a") || '''$[*][?(@.property1 == 'a')]'''
            assertThat(xml6).array().contains("property2").isEqualTo("b") || '''$[*][?(@.property2 == 'b')]'''
    }

    @Shared String xml7 =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <property1>
        <property2>test1</property2>
    </property1>
    <property1>
        <property3>test2</property3>
    </property1>
'''

    @Unroll
    def "should generate assertions for array inside response body element"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                                   || expectedJsonPath
            assertThat(xml7).array("property1").contains("property2").isEqualTo("test1") || '''$.property1[*][?(@.property2 == 'test1')]'''
            assertThat(xml7).array("property1").contains("property3").isEqualTo("test2") || '''$.property1[*][?(@.property3 == 'test2')]'''
    }

    @Shared String xml8 =  """<?xml version="1.0" encoding="UTF-8" ?>
    <property1>a</property1>
    <property2>
        <property3>b</property3>
    </property2>
"""

    def "should generate assertions for nested objects in response body"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                            || expectedJsonPath
            assertThat(xml8).field("property2").field("property3").isEqualTo("b") || '''$.property2[?(@.property3 == 'b')]'''
            assertThat(xml8).field("property1").isEqualTo("a")                    || '''$[?(@.property1 == 'a')]'''
    }

    @Shared Map xml9 =  [
            property1: "a",
            property2: 123
    ]

    @Unroll
    def "should generate regex assertions for map objects in response body"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                      || expectedJsonPath
            assertThat(toJson(xml9)).field("property2").matches("[0-9]{3}") || '''$[?(@.property2 =~ /[0-9]{3}/)]'''
            assertThat(toJson(xml9)).field("property1").isEqualTo("a")      || '''$[?(@.property1 == 'a')]'''
    }

    def "should generate escaped regex assertions for string objects in response body"() {
        given:
        Map json =  [
                property2: 123123
        ]
        expect:
            def verifiable = assertThat(toJson(json)).field("property2").matches("\\d+")
            verifiable.xPath() == '''$[?(@.property2 =~ /\\d+/)]'''
    }

    @Shared Map xml10 =  [
            errors: [
                    [property: "bank_account_number",
                     message: "incorrect_format"]
            ]
    ]

    @Unroll
    def "should work with more complex stuff and jsonpaths"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                                                      || expectedJsonPath
            assertThat(toJson(xml10)).array("errors").contains("property").isEqualTo("bank_account_number") || '''$.errors[*][?(@.property == 'bank_account_number')]'''
            assertThat(toJson(xml10)).array("errors").contains("message").isEqualTo("incorrect_format")     || '''$.errors[*][?(@.message == 'incorrect_format')]'''
    }

    @Shared String xml11 = '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>
        <place>
            <bounding_box>
                <coordinates>-77.119759</coordinates>
                <coordinates>38.995548</coordinates>
                <coordinates>-76.909393</coordinates>
                <coordinates>38.791645</coordinates>
            </bounding_box>
        </place>
    </0>
'''

    @Unroll
    def "should manage to parse a double array"() {
        expect:
            verifiable.xPath() == expectedJsonPath
        where:
            verifiable                                                                                                                            || expectedJsonPath
            assertThat(xml11).array().field("place").field("bounding_box").array("coordinates").array().arrayField().contains(38.995548).value()  || '''$[*].place.bounding_box.coordinates[*][*][?(@ == 38.995548)]'''
            assertThat(xml11).array().field("place").field("bounding_box").array("coordinates").array().arrayField().contains(-77.119759).value() || '''$[*].place.bounding_box.coordinates[*][*][?(@ == -77.119759)]'''
            assertThat(xml11).array().field("place").field("bounding_box").array("coordinates").array().arrayField().contains(-76.909393).value() || '''$[*].place.bounding_box.coordinates[*][*][?(@ == -76.909393)]'''
            assertThat(xml11).array().field("place").field("bounding_box").array("coordinates").array().arrayField().contains(38.791645).value()  || '''$[*].place.bounding_box.coordinates[*][*][?(@ == 38.791645)]'''

    }

    @Unroll
    def 'should convert a json with list as root to a map of path to value'() {
        expect:
            assertThat(xml).array().field("some").field("nested").field("json").isEqualTo("with value").xPath() == '''$[*].some.nested[?(@.json == 'with value')]'''
            assertThat(xml).array().field("some").field("nested").field("anothervalue").isEqualTo(4).xPath() == '''$[*].some.nested[?(@.anothervalue == 4)]'''
            assertThat(xml).array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name1").xPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name1')]'''
            assertThat(xml).array().field("some").field("nested").array("withlist").contains("name").isEqualTo("name2").xPath() == '''$[*].some.nested.withlist[*][?(@.name == 'name2')]'''
            assertThat(xml).array().field("some").field("nested").array("withlist").field("anothernested").field("name").isEqualTo("name3").xPath() == '''$[*].some.nested.withlist[*].anothernested[?(@.name == 'name3')]'''
        where:
        xml << [
                '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>
        <some>
            <nested>
                <json>with value</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name1</name>
                </withlist>
                <withlist>
                    <name>name2</name>
                </withlist>
                <withlist>
                    <anothernested>
                        <name>name3</name>
                    </anothernested>
                </withlist>
            </nested>
        </some>
    </0>
    <1>
        <someother>
            <nested>
                <json>with value</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name1</name>
                </withlist>
                <withlist>
                    <name>name2</name>
                </withlist>
            </nested>
        </someother>
    </1>

    ''',
    '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>
        <someother>
            <nested>
                <json>with value</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name1</name>
                </withlist>
                <withlist>
                    <name>name2</name>
                </withlist>
            </nested>
        </someother>
    </0>
    <1>
        <some>
            <nested>
                <json>with value</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name2</name>
                </withlist>
                <withlist>
                    <anothernested>
                        <name>name3</name>
                    </anothernested>
                </withlist>
                <withlist>
                    <name>name1</name>
                </withlist>
            </nested>
        </some>
    </1>
''']
    }

    def "should run json path when provided manually"() {
        given:
            String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <property1>a</property1>
    <property2>
        <property3>b</property3>
    </property2>
"""
        and:
            String jsonPath = '''$[?(@.property1 == 'a')]'''
        expect:
            assertThat(xml).matchesJsonPath(jsonPath)
    }

    def "should throw exception when json path is not matched"() {
        given:
            String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <property1>a</property1>
    <property2>
        <property3>b</property3>
    </property2>

"""
        and:
            String jsonPath = '''$[?(@.property1 == 'c')]'''
        when:
            assertThat(xml).matchesJsonPath(jsonPath)
        then:
            IllegalStateException illegalStateException = thrown(IllegalStateException)
            illegalStateException.message.contains("Parsed JSON")
            illegalStateException.message.contains("doesn't match")
    }

    def "should not throw exception when json path is not matched and system prop overrides the check"() {
        given:
            String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <property1>a</property1>
    <property2>
        <property3>b</property3>
    </property2>
    """
        and:
            String jsonPath = '''$[?(@.property1 == 'c')]'''
        when:
            assertThat(xml).withoutThrowingException().matchesJsonPath(jsonPath)
        then:
            noExceptionThrown()
    }

    def "should generate escaped regex assertions for boolean objects in response body"() {
        given:
        Map xml =  [
                property2: true
        ]
        expect:
        def verifiable = assertThat(toJson(xml)).field("property2").matches('true|false')
        verifiable.xPath() == '''$[?(@.property2 =~ /true|false/)]'''
    }

    def "should generate escaped regex assertions for numbers objects in response body"() {
        given:
        Map xml =  [
                property2: 50
        ]
        expect:
        def verifiable = assertThat(toJson(xml)).field("property2").matches('[0-9]{2}')
        verifiable.xPath() == '''$[?(@.property2 =~ /[0-9]{2}/)]'''
    }

    def "should escape regular expression properly"() {
        given:
        String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <path>/api/12</path>
    <correlationId>123456</correlationId>"""
        expect:
        DocumentContext parsedJson = JsonPath.parse(xml)
        def verifiable = assertThatXml(parsedJson).field("path").matches("^/api/[0-9]{2}\$")
        verifiable.xPath() == '''$[?(@.path =~ /^\\/api\\/[0-9]{2}$/)]'''
    }

    def "should escape single quotes in a quoted string"() {
        given:
        String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <text>text with 'quotes' inside</text>

        """
        expect:
        DocumentContext parsedJson = JsonPath.parse(xml)
        def verifiable = assertThatXml(parsedJson).field("text").isEqualTo("text with 'quotes' inside")
        verifiable.xPath() == '''$[?(@.text == 'text with \\'quotes\\' inside')]'''
    }

    def "should escape brackets in a string"() {
        given:
        String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <id>&lt;escape me&gt;</id>
        """
        expect:
        DocumentContext parsedJson = JsonPath.parse(xml)
        def verifiable = assertThatXml(parsedJson).field("text").isEqualTo("text with 'quotes' inside")
        verifiable.xPath() == '''$[?(@.text == 'text with \\'quotes\\' inside')]'''
    }

    def "should escape double quotes in a quoted string"() {
        given:
            String xml = """<?xml version="1.0" encoding="UTF-8" ?>
    <text>text with &quot;quotes&quot; inside</text>

            """
        expect:
            DocumentContext parsedJson = JsonPath.parse(xml)
            def verifiable = assertThatXml(parsedJson).field("text").isEqualTo('''text with "quotes" inside''')
            verifiable.xPath() == '''$[?(@.text == 'text with "quotes" inside')]'''
    }

    def 'should resolve the value of XML via XPath'() {
        given:
            String xml =
                    '''
                            <?xml version="1.0" encoding="UTF-8" ?>
    <0>
        <some>
            <nested>
                <json>with value</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name1</name>
                </withlist>
                <withlist>
                    <name>name2</name>
                </withlist>
                <withlist>
                    <anothernested>
                        <name>name3</name>
                    </anothernested>
                </withlist>
            </nested>
        </some>
    </0>
    <1>
        <someother>
            <nested>
                <json>true</json>
                <anothervalue>4</anothervalue>
                <withlist>
                    <name>name1</name>
                </withlist>
                <withlist>
                    <name>name2</name>
                </withlist>
                <withlist2>a</withlist2>
                <withlist2>b</withlist2>
            </nested>
        </someother>
    </1>

        '''
        expect:
            XPath.builder(xml).array().field("some").field("nested").field("json").read(String) == 'with value'
            XPath.builder(xml).array().field("some").field("nested").field("anothervalue").read(Integer) == 4
            assertThat(xml).array().field("some").field("nested").array("withlist").field("name").read(List) == ['name1', 'name2']
            assertThat(xml).array().field("someother").field("nested").array("withlist2").read(List) == ['a', 'b']
            assertThat(xml).array().field("someother").field("nested").field("json").read(Boolean) == true
    }

    def 'should assert xml with only top list elements'() {
        given:
            String xml = '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>Java</0>
    <1>Java8</1>
    <2>Spring</2>
    <3>SpringBoot</3>
    <4>Stream</4>
    '''
        expect:
            assertThatXml(xml).arrayField().contains("Java8").value()
            assertThatXml(xml).arrayField().contains("Spring").value()
            assertThatXml(xml).arrayField().contains("Java").value()
            assertThatXml(xml).arrayField().contains("Stream").value()
            assertThatXml(xml).arrayField().contains("SpringBoot").value()
    }

    def 'should match array containing an array of primitives'() {
        given:
            String xml =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <first_name>existing</first_name>
    <partners>
        <role>AGENT</role>
        <payment_methods>BANK</payment_methods>
        <payment_methods>CASH</payment_methods>
    </partners>
'''
        expect:
            def verifiable = assertThatXml(xml).array("partners").array("payment_methods").arrayField().isEqualTo("BANK").value()
            verifiable.xPath() == '''$.partners[*].payment_methods[?(@ == 'BANK')]'''
    }

    def 'should match pattern in array'() {
        given:
            String json =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <authorities>ROLE_ADMIN</authorities>'''

        expect:
            def verifiable = assertThatXml(json).array("authorities").arrayField().matches("^[a-zA-Z0-9_\\- ]+\$").value()
            verifiable.xPath() == '''$.authorities[?(@ =~ /^[a-zA-Z0-9_\\- ]+$/)]'''
    }

    @Issue("#10")
    def 'should manage to parse array with string values'() {
        given:
            String json =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <some_list>name1</some_list>
    <some_list>name2</some_list>
    '''

        expect:
            def v1 = assertThat(JsonPath.parse(json)).array("some_list").arrayField().isEqualTo("name1")
            def v2 = assertThat(JsonPath.parse(json)).array("some_list").arrayField().isEqualTo("name2")
        and:
            v1.xPath() == '''$.some_list[?(@ == 'name1')]'''
            v2.xPath() == '''$.some_list[?(@ == 'name2')]'''
    }

    def 'should parse an array of arrays that are root elements'() {
        given:
            String xml =  '''<?xml version="1.0" encoding="UTF-8" ?>
    <0>Programming</0>
    <0>Java</0>
    <1>Programming</1>
    <1>Java</1>
    <1>Spring</1>
    <1>Boot</1>
    '''

        expect:
            def v1 = assertThatXml(JsonPath.parse(xml)).array().arrayField().isEqualTo("Java").value()
        and:
            v1.xPath() == '''$[*][?(@ == 'Java')]'''
    }

}