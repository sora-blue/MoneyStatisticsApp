package com.zzz.moneystatistics.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.get
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.findFragment
import androidx.navigation.NavHost
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.databinding.FragmentEntryMultiEditBinding

class EntryMultiEditFragment : Fragment() {
    private lateinit var binding: FragmentEntryMultiEditBinding
    private var chosenItems : ArrayList<Entry>? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentEntryMultiEditBinding.inflate(inflater)
        arguments?.getSerializable(EntryListShowFragment.MULTI_KEY)?.apply {
            chosenItems = this as ArrayList<Entry>
            var str = String()
            for (item in chosenItems!!)
            {
                str = str + "\n" + item.id + " " + item.event + " " + item.date + " " + item.cost
                Log.d(LOG_TAG, "find item from EntryListShowFragment: ${item.id}, ${item.event}")
            }
            binding.multiEntryShowTv.text = str
        }

        binding.multiDeleteBt.setOnClickListener {
            AlertDialog.Builder(context).apply {
                setTitle("确认删除这些条目吗？")
                setMessage("所有数据删除后无法恢复！")
                setPositiveButton("确认"){ _, _ ->
                    BaseActivity.sharedApplication?.let { application ->
                        val viewModel = EntryListShowFragment.getViewModel(application)
                        chosenItems?.forEach {
                            viewModel.deleteEntry(it)
                        }
                    }
                    Toast.makeText(context, "成功删除${chosenItems?.size}项", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_entryMultiEditFragment_to_entryListShowFragment)
                }
                setNegativeButton("取消"){ _, _ ->
                }
            }.show()
        }
        binding.multiRefundBt.setOnClickListener {
            // todo: more info save to $otherInfo
            // farther conditions need to be considered

            val refundEvent = binding.multiRefundEventEt.text.toString()
            val refundCost = "+" + binding.multiRefundCostEt.text
            val refundId = EntryListShowFragment.nextIdOfEntries
            val refundEntry = Entry(refundId, refundEvent, "退款", refundCost, BaseActivity.getDateToday(), -1, binding.multiEntryShowTv.text.toString())
            BaseActivity.sharedApplication?.let { application ->
                val viewModel = EntryListShowFragment.getViewModel(application)
                viewModel.addEntry(refundEntry)
                chosenItems?.forEach {
                    it.refundMark = refundId
                    it.event = it.event
                    it.otherInfo = "{已退款，退款id: $refundId}"
                    viewModel.updateEntry(it)
                }
            }
            findNavController().navigate(R.id.action_entryMultiEditFragment_to_entryListShowFragment)
        }
        return binding.root
    }

    companion object{
        const val LOG_TAG = "EntryMultiEditFragment"
    }

}