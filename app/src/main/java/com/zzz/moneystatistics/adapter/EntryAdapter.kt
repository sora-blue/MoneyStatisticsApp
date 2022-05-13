package com.zzz.moneystatistics.adapter

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.TextView
import androidx.navigation.NavController
import androidx.recyclerview.widget.RecyclerView
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.fragment.EntryListShowFragment

class EntryAdapter(var list: ArrayList<Entry>, private var controller: NavController, private val multiMode: Int = 0) : RecyclerView.Adapter<EntryAdapter.EntryViewHolder>() {
    public var chosenItems: ArrayList<Entry> = ArrayList() // store IDs of chosen items
    private var holders: ArrayList<EntryViewHolder> = ArrayList()
    private var allCheck = false

    inner class EntryViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var idTv: TextView = view.findViewById(if(multiMode == 0) R.id.entryIndexTv else R.id.multiEntryIndexTv)
        var categoryTv: TextView = view.findViewById(if(multiMode == 0) R.id.entryCategoryTv else R.id.multiEntryCategoryTv)
        var eventTv: TextView = view.findViewById(if(multiMode == 0) R.id.entryEventTv else R.id.multiEntryEventTv)
        var costTv: TextView = view.findViewById(if(multiMode == 0) R.id.entryCostTv else R.id.multiEntryCostTv)
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EntryViewHolder {
        val view = if(multiMode == 0)
            LayoutInflater.from(parent.context).inflate(R.layout.entry, parent, false)
        else
            LayoutInflater.from(parent.context).inflate(R.layout.multi_entry, parent, false)
        return EntryViewHolder(view)
    }
    //SQLite

    override fun onBindViewHolder(holder: EntryViewHolder, position: Int) {
        val curEntry = list[position]
        val tvList = ArrayList<TextView>()
        tvList.add(holder.categoryTv)
        tvList.add(holder.eventTv)
        tvList.add(holder.costTv)
        tvList.add(holder.idTv)

        holder.categoryTv.text = curEntry.category
        holder.eventTv.text = curEntry.event
        holder.costTv.text = curEntry.cost
        holder.idTv.text = curEntry.id.toString()

        if(curEntry.refundMark == -1)
        {
            tvList.forEach{it.setTextColor(Color.parseColor("#ff0000"))} //red
        }
        else if(curEntry.refundMark != 0)
        {
            tvList.forEach{it.setTextColor(Color.parseColor("#cc00ff"))} //purple
            holder.eventTv.text = curEntry.event + curEntry.otherInfo
        }


        if(multiMode == 0)
        {
            holder.itemView.setOnClickListener {
                val bundle = Bundle()
                bundle.putInt(EntryListShowFragment.ID_KEY, curEntry.id)
                bundle.putInt(EntryListShowFragment.INDEX_KEY, position)
                bundle.putString(EntryListShowFragment.EVENT_KEY, curEntry.event)
                bundle.putString(EntryListShowFragment.CATEGORY_KEY, curEntry.category)
                bundle.putString(EntryListShowFragment.COST_KEY, curEntry.cost)
                bundle.putString(EntryListShowFragment.DATE_KEY, curEntry.date)
                bundle.putInt(EntryListShowFragment.REFUND_KEY, curEntry.refundMark)
                bundle.putString(EntryListShowFragment.OTHER_INFO_KEY, curEntry.otherInfo)

                Log.d(EntryListShowFragment.LOG_TAG, "put into bundle for EntryModFragment: $position, ${curEntry.event}, ${curEntry.category}, ${curEntry.cost}")

                controller.navigate(R.id.action_entryListShowFragment_to_entryModFragment, bundle)
            }
        }
        else
        {
            val checkBox = holder.itemView.findViewById<CheckBox>(R.id.multiEntryCb)
            holder.itemView.setOnClickListener {
                val state = checkBox.isChecked;
                checkBox.isChecked = !state;
            }
            checkBox.setOnCheckedChangeListener{ _, b ->
                //val info = curEntry.id.toString() + " " + curEntry.event + "(" + curEntry.cost + ")"
                if(b)
                {
                    chosenItems.add(curEntry)
                }
                else
                {
                    chosenItems.remove(curEntry)
                }
            }
        }
        holders.add(holder)
    }
    public fun allCheck()
    {
        holders.forEach {
            it.itemView.findViewById<CheckBox>(R.id.multiEntryCb)?.isChecked = !allCheck
        }
        allCheck = !allCheck
    }

    override fun getItemCount() = list.size
}