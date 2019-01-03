package com.noomtech.csedi.message.parsers

import com.noomtech.csedi.message.parsers.msgschema.SchemaNode
import com.noomtech.csedi.message.parsers.msgschema.SchemaNodeValueType
import java.io.StringReader

fun main(args : Array<String>) {
    XMLMsgParser.toMessage(StringReader(
        "<root>" +
                "Hello" +
                "<A>" +
                    "hello" +
                    "<B></B>" +
                "</A>" +
            "</root>"), SchemaNode("j", false, SchemaNodeValueType.DECIMAL))

}