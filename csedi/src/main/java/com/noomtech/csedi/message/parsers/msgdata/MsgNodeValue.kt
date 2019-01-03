package com.noomtech.csedi.message.parsers.msgdata


/**
 * Represents the value contained within a [MsgNode]
 */
class MsgNodeValue(val rawValue : String) {


    companion object {
        val EMPTY = MsgNodeValue("")
    }

    override fun toString() : String {
        return rawValue
    }
}