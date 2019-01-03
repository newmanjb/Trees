package com.noomtech.csedi.message.msg_node

import com.noomtech.csedi.message.parsers.msgdata.MsgNode
import com.noomtech.csedi.message.parsers.msgdata.MsgNodeValue
import com.noomtech.csedi.message.parsers.msgschema.SchemaNode
import com.noomtech.csedi.message.parsers.msgschema.SchemaNodeValueType
import org.junit.jupiter.api.Test


/**
 * Test cases to ensure that schema nodes can validate their equivalent message node
 */
class DataValidationTests {


    @Test
    fun testTextType() {
        val textNode = SchemaNode("text", false, SchemaNodeValueType.TEXT)
        assert(textNode.validate(MsgNode("joshua")))
        assert(textNode.validate(MsgNode("")))
    }

    @Test
    fun testNumberType() {
        val textNode = SchemaNode("number", false, SchemaNodeValueType.NUMBER)
        assert(textNode.validate(MsgNode("number", MsgNodeValue("1234"))))
        assert(!textNode.validate(MsgNode("number", MsgNodeValue(""))))
        assert(!textNode.validate(MsgNode("number", MsgNodeValue("1234a"))))
    }

    @Test
    fun testDecimalType() {
        val textNode = SchemaNode("decimal", false, SchemaNodeValueType.DECIMAL)
        assert(textNode.validate(MsgNode("decimal",MsgNodeValue("1234"))))
        assert(textNode.validate(MsgNode("decimal",MsgNodeValue("1234.6789"))))
        assert(!textNode.validate(MsgNode("decimal",MsgNodeValue(""))))
        assert(!textNode.validate(MsgNode("decimal",MsgNodeValue("1234a"))))
        assert(!textNode.validate(MsgNode("decimal",MsgNodeValue("1234.546456a"))))
    }
}