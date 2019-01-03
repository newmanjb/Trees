package com.noomtech.csedi.message.parsers

import com.noomtech.csedi.message.parsers.msgdata.MsgNode
import com.noomtech.csedi.message.parsers.msgdata.MsgNodeValue
import com.noomtech.csedi.message.parsers.msgschema.SchemaNode
import org.xml.sax.Attributes
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource
import java.io.Reader
import java.lang.IllegalArgumentException


/**
 * Parses XML into [MsgNode] objects using a [SchemaNode]
 * @author Joshua Newman
 */
//@todo - add repreating groups
//@todo - log exceptions
object XMLMsgParser {


    //@todo - if these are thread safe then some of these can be statics
    /**
     * @return A pair containing the error message (if applicable) and the root message node.  Only one of these is ever
     * non-null
     */
    fun toMessage (reader : Reader, schema : SchemaNode) : Pair<String?,MsgNode?> {
        val saxParser = SAXParserFactory.newInstance().newSAXParser()
        val handler = Parser(schema)
        val inputSource = InputSource(reader)
        try {
            saxParser.parse(inputSource, handler)
        }
        catch(e : Exception ) {
            return Pair(e.message, null)
        }

        return Pair(null, handler.rootMsgNode)
    }

    private class Parser(val schemaNode : SchemaNode) : DefaultHandler() {


        //Keeps track of the path we are on in the schema and msg node
        private val pathStack = ArrayList<String>()
        //Keeps track of which schema node is the equivalent of the XML element we are currently parsing
        private val schemaNodeStack = ArrayList<SchemaNode>()
        //Keeps track of which msg node in the structure that is being built is the equivalent of the XML element we are
        //currently parsing
        private val msgNodeStack : ArrayList<MsgNode> = ArrayList()

        //The resulting msg node.  Only non-null if parsing was successful
        var rootMsgNode : MsgNode? = null


        override fun startElement(uri : String, localName : String, qName : String, attributes : Attributes) {
            val isRoot =  schemaNodeStack.isEmpty()
            val newMsgNode = MsgNode(qName)
            var newSchemaNode : SchemaNode?
            pathStack.add(qName)
            if(!isRoot) {
                newSchemaNode = schemaNode.getChildFromPath(pathStack.subList(1,pathStack.size))
                msgNodeStack.get(msgNodeStack.size - 1).addChildren(listOf(newMsgNode))
            }
            else {
                newSchemaNode = if(schemaNode.name.equals(qName)) schemaNode else null
            }

            if (newSchemaNode == null) {
                throw IllegalArgumentException("Element " + pathStack + " not in schema")
            }

            schemaNodeStack.add(newSchemaNode)
            msgNodeStack.add(newMsgNode)
        }

        override fun endElement(uri : String, localName : String, qName : String) {
            val finishedMsgNode = msgNodeStack.removeAt(msgNodeStack.size - 1)
            val finishedSchemaNode = schemaNodeStack.removeAt(schemaNodeStack.size - 1)

            //Validate the direct children of the element that has just been completed
            for(childSchemaNode in finishedSchemaNode.children.values) {
                val childMsgNode = finishedMsgNode.getChildFromPath(mutableListOf(childSchemaNode.name))
                if(childMsgNode == null && !childSchemaNode.optional) {
                    throw IllegalArgumentException("Missing mandatory node: " + childSchemaNode.path)
                }
                if(childMsgNode != null && !childSchemaNode.validate(childMsgNode)) {
                    throw IllegalArgumentException("Invalid value for " + childSchemaNode.path + ": '" + childMsgNode.value + "'")
                }

            }

            pathStack.removeAt(pathStack.size - 1)

            if(finishedMsgNode.name.equals(schemaNode.name)) {
                //If we've just finished the root then validate any data within this (its children will have been validated above)
                if(!schemaNode.validate(finishedMsgNode)) {
                    throw IllegalArgumentException("Invalid value for " + schemaNode.path + ": '" + finishedMsgNode.value + "'")
                }
                rootMsgNode = finishedMsgNode
            }
        }

        override fun characters(chars : CharArray, start : Int, length : Int) {
            val value = String(chars, start, length)
            val msgNode = msgNodeStack[msgNodeStack.size - 1]
            val existing = msgNode.value
            if(existing != MsgNodeValue.EMPTY) {
                throw IllegalArgumentException("Element " + pathStack + " already has a value:" + existing)
            }
            val newValue = MsgNodeValue(value)
            msgNode.value = newValue
        }
    }
}