package com.noomtech.csedi.message.parsers.msgschema


import com.noomtech.csedi.message.parsers.msgdata.MsgNode
import com.noomtech.csedi.message.parsers.msgdata.MsgNodeValue
import com.noomtech.csedi.message.parsers.Constants
import java.math.BigDecimal


/**
 * Represents the type of value a [SchemaNode] can have
 */
enum class SchemaNodeValueType(val validator : (MsgNode) -> Boolean) {

    TEXT({s -> !(s.value.rawValue.contains(Constants.NEWLINE) || s.value.rawValue.contains("\t") || (s.value.rawValue.indexOf(" ") != -1 &&
            s.value.rawValue.replace(" ", "").length == 0))}),
    NUMBER({n ->
        var returnVal = false
        try {
            val i : Int = Integer.parseInt(n.value.rawValue)
            returnVal = true
        }
        catch(nfe : java.lang.NumberFormatException){}
        returnVal
    }),
    DECIMAL({bds ->
        var returnVal = false
        try {
            val i : BigDecimal = BigDecimal(bds.value.rawValue)
            returnVal = true;
        }
        catch(nfe : java.lang.NumberFormatException){}
        returnVal
    }),
    //Means that this type of node can only contain children and no data
    CHILDREN_ONLY({cos ->
        cos.value == MsgNodeValue.EMPTY
    });


    /**
     * Validates the data in the node against this type
     */
    fun validate(value : MsgNode) : Boolean {
        return validator(value)
    }
}