package com.noomtech.csedi.message.parsers.xml

import com.noomtech.csedi.message.parsers.XMLMsgParser
import com.noomtech.csedi.message.parsers.msgschema.SchemaNode
import com.noomtech.csedi.message.parsers.msgschema.SchemaNodeValueType
import org.junit.jupiter.api.Test
import java.io.StringReader


/**
 * Test cases that ensure that everything works as expected when valid XML is parsed i.e. no error, correct message
 * structure etc..
 */
class XMLMsgValidMsgTests {


    @Test
    fun testWhenDataIsEmpty() {
        //Test for text - should OK
        checkisOK(VERSION + "<A><B>1234</B><C><D>898932.444</D><E>4.4242</E></C></A>")
        //Test for number - should not be OK
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B></B><C><D>898932.444</D><E>4.4242</E></C></A>")
        //Test for decimal - should not be OK
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D></D><E>4.4242</E></C></A>")
    }

    @Test
    fun testWhenOneNodeInSchema() {
        val schemaNode = SchemaNode("root", false, SchemaNodeValueType.TEXT)
        val xml = "<root>hello</root>"
        checkisOK(xml, schemaNode)
    }

    @Test
    fun testAllOptionalsAndNonePresent() {
        //Have a root whos children are all optional and have none of them present
        var schemaNode = SchemaNode("root", false, SchemaNodeValueType.CHILDREN_ONLY)
        var o1 = SchemaNode("o1", true, SchemaNodeValueType.TEXT)
        var o2 = SchemaNode("o2", true, SchemaNodeValueType.TEXT)
        schemaNode.addChildren(listOf(o1, o2))

        var xml = "<root></root>"
        checkisOK(xml, schemaNode)

        //Same as above but with the parent element of the optionals being one level down from the root
        schemaNode = SchemaNode("root", false, SchemaNodeValueType.CHILDREN_ONLY)
        val m1 = SchemaNode("m1", false, SchemaNodeValueType.CHILDREN_ONLY)
        o1 = SchemaNode("o1", true, SchemaNodeValueType.TEXT)
        o2 = SchemaNode("o2", true, SchemaNodeValueType.TEXT)
        schemaNode.addChildren(listOf(m1))
        m1.addChildren(listOf(o1, o2))

        xml = "<root><m1></m1></root>"
        checkisOK(xml, schemaNode)
    }

    //A-B|-text
    //   |- C-Number
    //C is optional.  Build XML where C is not present and make sure it's OK
    @Test
    fun testOptionalElementAndDataWithOptionalNotPresent() {
        val schemaNode = SchemaNode("root", false, SchemaNodeValueType.CHILDREN_ONLY)
        val B = SchemaNode("B", false, SchemaNodeValueType.TEXT)
        val C = SchemaNode("C", true, SchemaNodeValueType.NUMBER)

        schemaNode.addChildren(listOf(B))
        B.addChildren(listOf(C))

        val xml = "<root><B>knackers</B></root>"

        checkisOK(xml, schemaNode)
    }

    //Simple one to make sure it's valid when all data types are correct, all optionals are present and all mandatory
    //elements are present
    @Test
    fun testWhenAllOptionalsPresentAndEverythingCorrect() {
        checkisOK(VERSION + "<A>knackers<B>123</B><C><D>2.89</D><E>3.89</E></C></A>")
    }

    //Use the "complex" schema (see below) and test that xml still passes validation when the optionals are not present
    @Test
    fun testWhenOptionalsNotPresent() {
        //C not present
        checkisOK(VERSION + "<A>knackers<B>1234</B></A>")
        //E not present
        checkisOK(VERSION + "<A>knackers<B>1234</B><C><D>898932.444</D></C></A>")
    }


    private fun checkForErrorAndNoMsgObject(xml : String, schemaNode: SchemaNode = buildMoreComplexSchema()) {
        val reader = StringReader(xml)
        val result = XMLMsgParser.toMessage(reader, schemaNode)

        assert(result.first != null)
        assert(result.second == null)
    }

    private fun checkisOK(xml : String, schemaNode: SchemaNode = buildMoreComplexSchema()) {
        val reader = StringReader(xml)
        val result = XMLMsgParser.toMessage(reader, schemaNode)

        assert(result.first == null)
        assert(result.second != null)

        //@todo - check resulting msg node structure
    }
    
    companion object {
        val VERSION = """<?xml version="1.0"?>"""
    }

    // Builds the below:
    //
    // m = mandatory
    // o = optional
    //co = children only
    //
    //        |---B(m)---number
    //        |
    //        |          |-D(m)---decimal
    //  A(m)--|--C(o,co)-|
    //        |          |-E(o)---decimal
    //        |
    //        |---text
    //
    private fun buildMoreComplexSchema(): SchemaNode {

        val A = SchemaNode("A", false, SchemaNodeValueType.TEXT)
        val B = SchemaNode("B", false, SchemaNodeValueType.NUMBER)
        val C = SchemaNode("C", true, SchemaNodeValueType.CHILDREN_ONLY)
        val D = SchemaNode("D", false, SchemaNodeValueType.DECIMAL)
        val E = SchemaNode("E", true, SchemaNodeValueType.DECIMAL)
        A.addChildren(mutableListOf(B, C))
        C.addChildren(mutableListOf(D, E))
        return A
    }
}