package com.noomtech.csedi.message.parsers.msgschema;

import com.noomtech.csedi.message.parsers.msgdata.MsgNode


/**
 * Represents a node in the schema that is used to validate CSEDI data and to parse the data into a [MsgNode] structure
 * @param name This must be unique within the schema
 * @param optional True if this node does not need to be present
 * @param type The [SchemaNodeValueType]
 *
 */
data class SchemaNode(
    val name : String, val optional : Boolean, val type : SchemaNodeValueType) {

    //The path to this node in relation to the root e.g. if this node is C in A->B->C then its path is [A,B,C]
    var path = ArrayList<String>();
    var children = HashMap<String,SchemaNode>()

    init {
        path.add(name);
        addChildren(children.values);
    }

    fun addChildren(children : Collection<SchemaNode>) {
        for(child in children) {
            if(this.children[child.name] != null) {
                throw IllegalArgumentException(child.name + " already exists in parent " + this.name)
            }
            this.children[child.name] = child
            child.onAddedToParent(path)
        }
    }

    //Update the path when this node is added as a child to another node
    fun onAddedToParent(parentName : ArrayList<String>) {
        path = (parentName + path) as ArrayList<String>
    }

    /**
     * Validate a message node against this node
     */
    fun validate(value : MsgNode) : Boolean {
       return type.validate(value)
    }

    /**
     * Returns the child denoted by the given path e.g. if this node is B in A->B->C->D then a path of [C,D] will
     * return node D
     */
    fun getChildFromPath(path : MutableList<String>) : SchemaNode? {
        val child = children[path[0]]
        if(path.size > 1) {
            return child?.getChildFromPath(path.subList(1, path.size))
        }
        return child
    }
}
