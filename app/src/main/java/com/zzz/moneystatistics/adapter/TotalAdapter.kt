package com.zzz.moneystatistics.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.fragment.EntryStatisticsFragment

/*
* @date: 20211214
*
* Most questions came from 'match_parent' in itemView
* And it only load itemView when it's scrolled to
* */
// todo: clarify how data is conveyed
class TotalAdapter(var list: ArrayList<ArrayList<ArrayList<Entry>>>, var lifecycleOwner: LifecycleOwner, var fragment: EntryStatisticsFragment? = null) : RecyclerView.Adapter<TotalAdapter.TotalViewHolder>() {
    private lateinit var context: Context//?
    inner class TotalViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var yearSum: TextView = view.findViewById(R.id.yearSumTv)
        var yearTitle: TextView = view.findViewById(R.id.yearTitleTv)
        var yearRecyclerView : RecyclerView = view.findViewById(R.id.yearBasedRv)
    }
    inner class YearLayoutManager(context: Context) : LinearLayoutManager(context), Cloneable{
        public override fun clone() : YearLayoutManager
        {
            return super.clone() as YearLayoutManager
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context).inflate(R.layout.total, parent, false)
        return TotalViewHolder(view)
    }

    override fun onBindViewHolder(holder: TotalViewHolder, position: Int) {
        var adapter = YearAdapter(list[position], fragment)
        var manager = YearLayoutManager(context)
        val year = BaseActivity.getYear(list[position][0][0].date)
        val showStr = "V $year 年"
        val hideStr = "> $year 年"
        var hideIt = false
        Log.d(LOG_TAG, "setting adapter at $position, manager: $manager")

        holder.yearRecyclerView.layoutManager = manager
        holder.yearRecyclerView.adapter = adapter

        // if it is invoked, at least there's one element
        holder.yearTitle.text = showStr

        holder.itemView.setOnClickListener {
            if (!hideIt)
            {
                hideIt = true

                holder.yearTitle.text = hideStr
                adapter = holder.yearRecyclerView.adapter as YearAdapter // ?
                adapter.apply { sumInitialized = true }
                holder.yearRecyclerView.adapter = YearAdapter(ArrayList()) // it is ok to be empty?
            }
            else
            {
                hideIt = false
                holder.yearTitle.text = showStr
                holder.yearRecyclerView.adapter = adapter
            }
        }

        // this is to solve the problem that the sub-recyclerView hasn't binded view when sumUp is required
        adapter.sumUp.observe(lifecycleOwner){
            var tmp = it.toString()
            if (tmp[0].isDigit()) tmp = "+$tmp"
            tmp = BaseActivity.trunkToTwo(tmp)
            holder.yearSum.text = tmp
        }
    }

    override fun getItemCount() = list.size

    companion object{
        const val LOG_TAG = "YearAdapter"
    }
}