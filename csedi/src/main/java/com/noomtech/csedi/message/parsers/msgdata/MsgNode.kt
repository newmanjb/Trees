package com.noomtech.csedi.message.parsers.msgdata


/**
 * Represents a node in the internal message structure of CSEDI.
 * @param name This must be unique within the schema
 * @param value The value that this node holds (defaults to [MsgNodeValue.EMPTY])
 */
data class MsgNode(val name : String, var value : MsgNodeValue = MsgNodeValue.EMPTY) {


    //The path to this node in relation to the root e.g. if this node is C in A->B->C then its path is [A,B,C]
    private var path = ArrayList<String>();
    private val children = HashMap<String,MsgNode>()

    init {
        path.add(name);
        addChildren(children.values);
    }


    fun addChildren(children : Collection<MsgNode>) {
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
     * Returns the child denoted by the given path e.g. if this node is B in A->B->C->D then a path of [C,D] will
     * return node D
     */
    fun getChildFromPath(path : MutableList<String>) : MsgNode? {
        val child = children[path[0]]
        if(path.size > 1) {
            return child?.getChildFromPath(path.subList(1, path.size))
        }
        return child
    }
}