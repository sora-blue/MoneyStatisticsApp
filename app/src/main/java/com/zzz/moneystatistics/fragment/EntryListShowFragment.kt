package com.zzz.moneystatistics.fragment

import android.app.AlertDialog
import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.databinding.FragmentEntryListShowBinding
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.adapter.EntryAdapter
import com.zzz.moneystatistics.adapter.EntryS
import com.zzz.moneystatistics.database.EntryViewModel
import com.zzz.moneystatistics.database.offSignEntry
import kotlin.Exception
import kotlin.concurrent.thread
import kotlin.coroutines.coroutineContext


class EntryListShowFragment : BaseFragment() {

    // Fragment instance is re-created when $navigate() is called

    private lateinit var binding: FragmentEntryListShowBinding
    private var list: ArrayList<Entry>? = null
    private var curMultiMode = 0
    private var query : EntryS? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEntryListShowBinding.inflate(inflater)

        try {
            var date = arguments?.get(DATE_KEY)
            if(date == null)
                date = BaseActivity.getDateToday()
            binding.entryShowDateTv.text = date.toString()

            // !!! handle data from EntryStatistics only once
            query = arguments?.get(LOOKUP_KEY) as EntryS?
            query?.let {
                if (query?.advancedSearch!!)
                {
                    binding.entryShowDateTv.text = "特殊查询"
                }
                else
                {
                    binding.entryShowDateTv.text = query?.entry?.date!!.substring(0, 7)+ " " +query?.entry?.category!!
                }
            }

            BaseActivity.sharedApplication?.let { application ->
                Log.d(LOG_TAG, "Trying to register an observer")
                getViewModel(application).allEntryLive.observe(viewLifecycleOwner){
                    Log.d(LOG_TAG, "Observer is triggered.")
                    // 更新状态
                    sizeOfEntries = it.size
                    nextIdOfEntries = if (it.isNotEmpty()) it[it.size - 1].id + 1 else 0 // ()? a : b
                    cacheList = it
                    updateEntryList(it)
                }
            }
            binding.entryShowDateTv.setOnClickListener {
                val bundle = Bundle()
                bundle.putString(DATE_KEY, binding.entryShowDateTv.text.toString())
                findNavController().navigate(R.id.action_entryListShowFragment_to_entryDateSwitchingFragment, bundle)
            }
            binding.showStatisticsBt.setOnClickListener {
                if (curMultiMode == 1)
                {
                    try {
                        val adapter = binding.entryListRv.adapter as EntryAdapter
                        adapter.allCheck()
                    }catch (e: Exception)
                    {
                        AlertDialog.Builder(context).apply {
                            setMessage("未知错误")
                        }.show()
                    }
                }else
                {
                    findNavController().navigate(R.id.action_entryListShowFragment_to_entryStatisticsFragment)
                }
            }
            binding.addEntryBt.setOnClickListener {
                if(curMultiMode == 1)
                {
                    val chosen = binding.entryListRv.adapter as EntryAdapter
                    var bundle = Bundle()
                    bundle.putSerializable(MULTI_KEY, chosen.chosenItems);
                    findNavController().navigate(R.id.action_entryListShowFragment_to_entryMultiEditFragment, bundle)
                }
                else
                {
                    findNavController().navigate(R.id.action_entryListShowFragment_to_entryModFragment)
                }
            }
            binding.previousDayBt.setOnClickListener {
                binding.entryShowDateTv.text = BaseActivity.getYesterdayOf(binding.entryShowDateTv.text.toString())
                updateEntryList(cacheList)
            }
            binding.nextDayBt.setOnClickListener {
                binding.entryShowDateTv.text = BaseActivity.getTomorrowOf(binding.entryShowDateTv.text.toString())
                updateEntryList(cacheList)
            }
            binding.multiEntryBt.setOnClickListener {
                curMultiMode = 1 - curMultiMode
                updateOperationIcon()
                updateEntryList(cacheList)
            }
        }catch (e: Exception){
            Toast.makeText(context, "未知错误", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        updateOperationIcon()
    }
    private fun updateOperationIcon()
    {
        if(curMultiMode == 1)
        {
            binding.addEntryBt.setImageResource(R.drawable.ic_baseline_bookmarks_24)
            binding.showStatisticsBt.setImageResource(R.drawable.ic_baseline_beenhere_24)
        }
        else
        {
            binding.addEntryBt.setImageResource(R.drawable.ic_baseline_add_24)
            binding.showStatisticsBt.setImageResource(R.drawable.ic_baseline_calendar_today_24)
        }
    }


    private fun updateEntryList(it: List<Entry>, multiMode : Int = curMultiMode)
    {
        try {
            Log.d(LOG_TAG, "List updated.")
            // sizeOfEntries = it.size
            // nextIdOfEntries = if (it.isNotEmpty()) it[it.size - 1].id + 1 else 0 // ()? a : b
            val arrayList = ArrayList<Entry>()

            // !!! handle data from EntryStatistics only once

            for(item in it)
            {
                if(curSerialized == null)
                    curSerialized = ""
                if (query != null && !BaseActivity.checkDateStringLegality(binding.entryShowDateTv.text.toString()))
                {
                    val thisEntry = query?.entry!!
                    if (query?.advancedSearch!!)
                    {
                        // advance search mode
                        Log.d(LOG_TAG, "$item")
                        if (thisEntry.id != -1 && item.id != thisEntry.id) continue
                        Log.d(LOG_TAG, "as: passed id")
                        if (thisEntry.category.isNotEmpty() && item.category != thisEntry.category) continue
                        Log.d(LOG_TAG, "as: passed category")
                        if (thisEntry.date.isNotEmpty())
                        {
                            if (thisEntry.date.length < 10)
                            {
                                val sub = thisEntry.date.substring(0, thisEntry.date.length)
                                if (item.date.substring(0, thisEntry.date.length) != sub) continue
                            }
                            else
                            {
                                val sub = thisEntry.date.substring(0, 10)
                                if (item.date != sub) continue
                            }
                        }
                        Log.d(LOG_TAG, "as: passed date")
                        if (thisEntry.event.isNotEmpty() && item.event != thisEntry.event) continue
                        Log.d(LOG_TAG, "as: passed event")

                        if (thisEntry.cost.isNotEmpty()){
                            val tmp = item.cost
                            val o1 = offSignEntry(thisEntry).cost.toDouble()
                            val o2 = offSignEntry(item).cost.toDouble()
                            Log.d(LOG_TAG, "as: $o1 $o2")
                            val result = query?.lookupGt!!.xor(o2 < o1) || o1 == o2
                            if (!result) continue
                            item.cost = tmp
                        }
                        Log.d(LOG_TAG, "as: passed cost")
                    }
                    else
                    {
                        if (item.date.substring(0, 8) != thisEntry.date.substring(0, 8)) continue
                        if (item.category != thisEntry.category) continue
                    }

                    arrayList.add(item)
                    Log.d(LOG_TAG, "(filtered) found qualified item : $item")
                }
                else
                {
                    if(item.date == binding.entryShowDateTv.text.toString())
                    {
                        curSerialized?.apply { curSerialized = this + "\n" + item }
                        arrayList.add(item)
                        Log.d(LOG_TAG, "found qualified item: $item")
                    }
                }
            }
            if (query != null && !BaseActivity.checkDateStringLegality(binding.entryShowDateTv.text.toString()))
            {
                Toast.makeText(context, "查询成功，共查询到${arrayList.size} 项", Toast.LENGTH_SHORT).show()
            }
            cacheSelectedList = arrayList
            setUpEntryListRv(arrayList, multiMode)
        }catch (e: Exception){
            Toast.makeText(context, "列表更新失败", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
        }
    }
    private fun setUpEntryListRv(newList: ArrayList<Entry>, multiMode : Int)
    {
        list = newList
        binding.entryListRv.layoutManager = LinearLayoutManager(context)
        list?.apply { binding.entryListRv.adapter = EntryAdapter(this, findNavController(), multiMode) }
    }

    companion object
    {
        const val ID_KEY = "id"
        const val INDEX_KEY = "index"
        const val EVENT_KEY = "event"
        const val CATEGORY_KEY = "category"
        const val COST_KEY = "cost"
        const val DATE_KEY = "date"
        const val REFUND_KEY = "refund"
        const val OTHER_INFO_KEY = "otherInfo"

        const val MULTI_KEY = "multi"

        const val LOOKUP_KEY = "lookup"

        const val LOG_TAG = "EntryListShowFragment"

        var entryViewModel: EntryViewModel? = null
        var sizeOfEntries = 0 // note that it is not thread safe
        var nextIdOfEntries = 0 // note that it is not thread safe

        // share data collected from database to improve efficiency
        var cacheList : List<Entry> = List<Entry>(0){Entry()}
        var cacheSelectedList = ArrayList<Entry>()
        var curSerialized : String? = null


        fun getViewModel(application: Application): EntryViewModel {
            if(entryViewModel != null)
                return entryViewModel as EntryViewModel
            thread {
                entryViewModel = EntryViewModel(application)
            }
            while(entryViewModel == null){
                //waiting in fact
                //blocking ui
            }

            return entryViewModel as EntryViewModel
        }
    }
}