package com.noomtech.csedi.message.msg_node

import com.noomtech.csedi.message.parsers.Constants
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
        assert(textNode.validate(MsgNode("text", MsgNodeValue("joshua"))))
        assert(textNode.validate(MsgNode("text", MsgNodeValue(""))))

        assert(!textNode.validate(MsgNode("text", MsgNodeValue("joshua" + Constants.NEWLINE))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue("joshua" + "\t"))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue(" "))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue("   "))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue("\t " + Constants.NEWLINE))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue("jos\tu " + Constants.NEWLINE + "a"))))
        assert(!textNode.validate(MsgNode("text", MsgNodeValue("joshu" + Constants.NEWLINE + "a"))))

    }

    @Test
    fun testNumberType() {
        val numberNode = SchemaNode("number", false, SchemaNodeValueType.NUMBER)
        assert(numberNode.validate(MsgNode("number", MsgNodeValue("1234"))))
        assert(!numberNode.validate(MsgNode("number", MsgNodeValue(""))))
        assert(!numberNode.validate(MsgNode("number", MsgNodeValue("1234a"))))
    }

    @Test
    fun testDecimalType() {
        val decimalNode = SchemaNode("decimal", false, SchemaNodeValueType.DECIMAL)
        assert(decimalNode.validate(MsgNode("decimal",MsgNodeValue("1234"))))
        assert(decimalNode.validate(MsgNode("decimal",MsgNodeValue("1234.6789"))))
        assert(!decimalNode.validate(MsgNode("decimal",MsgNodeValue(""))))
        assert(!decimalNode.validate(MsgNode("decimal",MsgNodeValue("1234a"))))
        assert(!decimalNode.validate(MsgNode("decimal",MsgNodeValue("1234.546456a"))))
    }
}
