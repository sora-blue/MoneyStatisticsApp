package com.zzz.moneystatistics.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.runtime.structuralEqualityPolicy
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
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
class YearAdapter(var list: ArrayList<ArrayList<Entry>>, var fragment: EntryStatisticsFragment? = null) : RecyclerView.Adapter<YearAdapter.YearViewHolder>() {
    private lateinit var context: Context //?
    var manager: YearLayoutManager? = null
    var sumUp = MutableLiveData(0.0)
    var sumInitialized = false
    inner class YearViewHolder(view: View) : RecyclerView.ViewHolder(view){
        var monthTitle: TextView = view.findViewById(R.id.monthTitleTv)
        var monthRecyclerView : RecyclerView = view.findViewById(R.id.monthBasedRv)
        var monthSum : TextView = view.findViewById(R.id.monthSumTv)
    }
    inner class YearLayoutManager(context: Context) : LinearLayoutManager(context), Cloneable{
        public override fun clone() : YearLayoutManager
        {
            return super.clone() as YearLayoutManager
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): YearViewHolder {
        manager = YearLayoutManager(parent.context)
        val view = LayoutInflater.from(parent.context).inflate(R.layout.year, parent, false)
        return YearViewHolder(view)
    }

    override fun onBindViewHolder(holder: YearViewHolder, position: Int) {
        //val manager = LinearLayoutManager(context) //?
        var adapter = MonthAdapter(list[position], fragment)
        val month = BaseActivity.getMonth(list[position][0].date)
        val showStr = "V $month 月"
        val hideStr = "> $month 月"
        var hideIt = false
        Log.d(LOG_TAG, "setting adapter at $position, manager: $manager")
        holder.monthRecyclerView.layoutManager = manager
        holder.monthRecyclerView.adapter = adapter
        holder.monthTitle.text = showStr
        holder.itemView.setOnClickListener {
            if (!hideIt)
            {
                hideIt = true
                adapter = holder.monthRecyclerView.adapter as MonthAdapter //?
                holder.monthRecyclerView.adapter = MonthAdapter(ArrayList()) // it is ok to be empty?
                holder.monthTitle.text = hideStr
            }
            else
            {
                hideIt = false
                holder.monthRecyclerView.adapter = adapter
                holder.monthTitle.text = showStr
            }
        }

        val tmpSum = adapter.sumUp()
        var tmpStr = tmpSum.toString()
        if (tmpStr[0].isDigit()) tmpStr = "+$tmpStr"
        if(!sumInitialized)
        {
            // this is to solve the problem occurred when ViewHolder is re-binded
            sumUp.value = sumUp.value?.plus(tmpSum)
        }
        holder.monthSum.text = BaseActivity.trunkToTwo(tmpStr)
    }

    override fun getItemCount() = list.size

    companion object{
        const val LOG_TAG = "YearAdapter"
    }
}