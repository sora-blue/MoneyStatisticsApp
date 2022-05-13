package com.zzz.moneystatistics.adapter

import com.zzz.moneystatistics.database.Entry
import java.io.Serializable

class EntryS(public val entry: Entry) : Serializable {
    public var advancedSearch = false
    public var lookupGt = false
}