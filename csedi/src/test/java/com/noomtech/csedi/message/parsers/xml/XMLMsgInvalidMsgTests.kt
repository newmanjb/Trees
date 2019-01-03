package com.noomtech.csedi.message.parsers.xml

import com.noomtech.csedi.message.parsers.XMLMsgParser
import com.noomtech.csedi.message.parsers.msgschema.SchemaNode
import com.noomtech.csedi.message.parsers.msgschema.SchemaNodeValueType
import org.junit.jupiter.api.Test
import java.io.StringReader


/**
 * Test cases that ensure that an error is produced when invalid XML is parsed
 */
class XMLMsgInvalidMsgTests {

    @Test
    fun testEmptyString() {
        checkForErrorAndNoMsgObject("")
    }

    @Test
    fun testInvalidXML() {
        checkForErrorAndNoMsgObject(VERSION + "<A_wrong>knackers<B>123<B><C><D>2.89</D><E>3.89</E></C></A_wrong>")
    }

    @Test
    fun testDifferentNamedRoot() {
        checkForErrorAndNoMsgObject(VERSION + "<A_wrong>knackers<B>123</B><C><D>2.89</D><E>3.89</E></C></A_wrong>")
    }

    @Test
    fun testInvalidData() {

        //root is invalid (need to build different schema for this)
        val rootSchemaNode = SchemaNode("A", false, SchemaNodeValueType.DECIMAL)
        val child = SchemaNode("B", false, SchemaNodeValueType.TEXT)
        rootSchemaNode.addChildren(listOf(child))
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>text</B></A>", rootSchemaNode)

        //B is invalid
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>not a number</B><C><D>34.3425</D><E>0.000041</E></C></A>")

        //D is invalid
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>not a decimal</D><E>0.34895</E></C></A>")

        //E is invalid
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>898932.444</D><E>not a decimal</E></C></A>")
    }

    @Test
    fun testWhenMandatorysAreMissing() {
        //B is missing
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<C><D>2.89</D><E>3.89</E></C></A>")
        //D is missing
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>123</B><C><E>3.89</E></C></A>")
        //B and D are missing
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<C><E>3.89</E></C></A>")
    }

    @Test
    fun testWhenChildOnlyNodeHasVal() {
        //Add a value to C, which is defined as child only
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C>I shouldn't be here<D>898932.444</D><E>4.4242</E></C></A>")
    }

    @Test
    fun testWhenDataInRootIsInvalid() {

    }

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
    fun testElementsInXMLNotInSchema() {
        //Extra element in A
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<Extra>hello</Extra><B>1234</B><C><D>898932.444</D><E>4.4242</E></C></A>")
        //Extra B element in A
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>8888</B><B>1234</B><C><D>898932.444</D><E>4.4242</E></C></A>")

        //Element in B in addition to the data
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234<OI>NO</IO></B><C><D>898932.444</D><E>4.4242</E></C></A>")

        //Extra element in C
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>898932.444</D><Hello>Hi</Hello><E>4.4242</E></C></A>")
        //Extra element in C that is another D
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>898932.444</D><D>Hi</D><E>4.4242</E></C></A>")

        //Element in D in addition to the data
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>898932.444<Huh>huh</Huh></D><E>4.4242</E></C></A>")

        //Element in E in addition to the data
        checkForErrorAndNoMsgObject(VERSION + "<A>knackers<B>1234</B><C><D>898932.444</D><E><Huh>huh</Huh4.4242</E></C></A>")

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
        //@todo - add custom exception with error codes and check them
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