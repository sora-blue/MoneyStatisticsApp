package com.zzz.moneystatistics.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable
import java.lang.Exception
import kotlin.math.cos

@Entity(tableName = "statistics")
data class Entry(
        @PrimaryKey(autoGenerate = true)
        var id:Int = 0,
        var event: String = "",
        var category: String = "",
        var cost: String = "",
        var date: String = "",
        var refundMark: Int = 0,
        var otherInfo: String = ""
):Serializable, Cloneable
public fun signEntry(entry: Entry, sign: Char) : Entry{
        var cost = entry.cost
        if(cost.isEmpty()) cost = "0.00"
        cost = if (cost[0].isDigit()) "$sign$cost" else "$sign${cost.substring(1, cost.length)}"
        entry.cost = cost
        return entry
}
public fun offSignEntry(entry: Entry): Entry{
        var cost = entry.cost
        if (cost.isEmpty()) cost = "0.00"
        cost = if (!cost[0].isDigit()) cost.substring(1) else cost
        entry.cost = cost
        return entry
}
public fun signOfEntry(entry: Entry) = if(entry.cost[0].isDigit()) '+' else entry.cost[0] // > 0
public fun signOfEntry(entryCost: String) = if(entryCost[0].isDigit()) '+' else entryCost[0] // > 0