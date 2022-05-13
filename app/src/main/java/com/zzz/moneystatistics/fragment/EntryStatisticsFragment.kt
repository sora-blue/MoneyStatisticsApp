package com.zzz.moneystatistics.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.databinding.FragmentEntryStatisticsBinding
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.adapter.TotalAdapter
import java.lang.Exception

class EntryStatisticsFragment : Fragment() {
    private lateinit var binding: FragmentEntryStatisticsBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentEntryStatisticsBinding.inflate(inflater)
        binding.totalRv.layoutManager = LinearLayoutManager(context)
        try{
            val data = fetchData3(EntryListShowFragment.cacheList)
            binding.totalRv.adapter = TotalAdapter(data, this, this)
        }catch (e: Exception){
            Toast.makeText(context, "Wrong in fetchData3", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
        }
        return binding.root
    }
    /**
     * @param: data     上一层分类后的数据
     * @param: criteria 筛选关键字的函数
     * @param: nextFunc 下一层的分类函数
     * */
    private fun <T : Any?, C : ArrayList<Entry>> abstractFetchData(data: C, criteria: (Entry)->String,
                                             nextFunc: (C) -> ArrayList<T>) : ArrayList<ArrayList<T>>
    {
        val map = HashMap<String, C>()
        val returnArr = ArrayList<ArrayList<T>>()
        // 1.sorted & bagged
        for (item in data)
        {
            if(!BaseActivity.checkDateStringLegality(item.date))
                continue
            val ct = criteria(item)
            if(map.containsKey(ct))
            {
                map[ct]?.add(item)
            }
            else
            {
                map[ct] = ArrayList<Entry>().apply{add(item)} as C
            }
        }
        // 2.converted & collected
        for(key in map.keys.sorted())
        {
            returnArr.add(nextFunc(map[key]!!))
        }

        return returnArr
    }
    // pattern: a simple version of Chain of Responsibility
    private fun fetchData3(data: List<Entry>) : ArrayList<ArrayList<ArrayList<Entry>>> {
        // year - month - category
        val convert = ArrayList<Entry>()
        data.forEach { it -> convert.add(it) }
        val returnVal = abstractFetchData(convert, {BaseActivity.getYear(it.date)}){
            try {
                fetchData2(it)
            }catch (e: Exception){
                Toast.makeText(context, "Wrong in fetchData2", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                ArrayList()
            }
        }
        Log.d(LOG_TAG, "fetchData3 size: ${returnVal.size}")
        return returnVal
    }
    private fun fetchData2(data: ArrayList<Entry>) : ArrayList<ArrayList<Entry>> {
        // month - category
        val returnVal = abstractFetchData(data, {BaseActivity.getMonth(it.date)}){
            try {
                fetchData1(it)
            }catch (e: Exception){
                Toast.makeText(context, "Wrong in fetchData1", Toast.LENGTH_SHORT).show()
                Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                ArrayList()
            }
        }
        Log.d(LOG_TAG, "fetchData2 size: ${returnVal.size}")
        return returnVal
    }
    private fun fetchData1(data: ArrayList<Entry>) : ArrayList<Entry> {
        // category
        val map = HashMap<String, Entry>()
        val returnArr = ArrayList<Entry>()
        for (item in data)
        {
            if(!BaseActivity.checkDateStringLegality(item.date))
                continue
            val category = item.category
            if(map.containsKey(category))
            {
                var tmp = item.cost.toDouble()
                map[category]?.cost?.toDouble()?.let { tmp += it }
                map[category]?.apply { cost = tmp.toString() }
            }
            else
            {
                map[category] = Entry(0, "", item.category, item.cost, item.date)
            }
        }
        for (value in map.values)
        {
            value.cost = BaseActivity.trunkToTwo(value.cost)
            returnArr.add(value)
        }
        Log.d(LOG_TAG, "fetchData1 size: ${returnArr.size}")
        return returnArr
    }
    companion object{
        const val LOG_TAG = "EntryStatisticsFragment"
    }
}