package com.zzz.moneystatistics.adapter

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.fragment.EntryListShowFragment
import com.zzz.moneystatistics.fragment.EntryStatisticsFragment

// todo: clarify how data is conveyed
class MonthAdapter(var list: ArrayList<Entry>, var fragment: EntryStatisticsFragment? = null) : RecyclerView.Adapter<MonthAdapter.MonthViewHolder>() {
    inner class MonthViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var indexTv: TextView = view.findViewById(R.id.entryIndexTv)
        var categoryTv: TextView = view.findViewById(R.id.entryCategoryTv)
        var eventTv: TextView = view.findViewById(R.id.entryEventTv)
        var costTv: TextView = view.findViewById(R.id.entryCostTv)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MonthViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.entry, parent, false)
        return MonthViewHolder(view)
    }

    override fun onBindViewHolder(holder: MonthViewHolder, position: Int) {
        val curEntry = list[position]
        holder.categoryTv.text = curEntry.category
        var tmp = curEntry.cost
        if(tmp[0].isDigit()) tmp = "+$tmp"
        holder.costTv.text = tmp
        holder.eventTv.text = ""
        holder.indexTv.text = ""
        Log.d("MonthAdapter", "fragment: $fragment")
        holder.itemView.setOnClickListener {
            fragment?.apply {
                val bundle = Bundle()
                bundle.putSerializable(EntryListShowFragment.LOOKUP_KEY, EntryS(curEntry))
                findNavController().navigate(R.id.action_entryStatisticsFragment_to_entryListShowFragment, bundle)
            }
        }

    }

    public fun sumUp() : Double {
        var returnVal = 0.0
        for (entry in list)
            returnVal += entry.cost.toDouble()
        return returnVal
    }

    override fun getItemCount() = list.size

}