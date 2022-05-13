package com.zzz.moneystatistics.fragment

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.adapter.EntryS
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.database.signEntry
import com.zzz.moneystatistics.database.signOfEntry
import com.zzz.moneystatistics.databinding.FragmentEntryModBinding

class EntryModFragment : BaseFragment() {

    private lateinit var binding: FragmentEntryModBinding

    private fun initializeEt(editText: EditText, key: String)
    {
        arguments?.getString(key)?.apply {
            editText.setText(this)
            Log.d(LOG_TAG, "read from bundle: $this")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEntryModBinding.inflate(inflater)
        try {
            initializeEt(binding.eventEditEt, EntryListShowFragment.EVENT_KEY)
            initializeEt(binding.categoryEditEt, EntryListShowFragment.CATEGORY_KEY)
            initializeEt(binding.costEditEt, EntryListShowFragment.COST_KEY)
            initializeEt(binding.otherInfoEt, EntryListShowFragment.OTHER_INFO_KEY)
            initializeEt(binding.dateEditEt, EntryListShowFragment.DATE_KEY)

            val isAdvancedSearching = arguments?.getBoolean(EntryDateSwitchingFragment.ADVANCED_SEARCH_KEY)
            binding.costOrIncomeTv.text = if (isAdvancedSearching == true) GT_STR else COST_STR

            binding.costOrIncomeTv.setOnClickListener {
                val text = binding.costOrIncomeTv.text
                if (isAdvancedSearching == true)
                {
                    binding.costOrIncomeTv.text = if (text == GT_STR) LT_STR else GT_STR
                }
                else
                {
                    binding.costOrIncomeTv.text = if (text == COST_STR) INCOME_STR else COST_STR
                }

            }

            binding.entryModBt.setOnClickListener {
                // bundling info into A new Entry -> leave it to ViewModel
                var id = EntryListShowFragment.nextIdOfEntries
                if(arguments != null)
                {
                    id = arguments?.getInt(EntryListShowFragment.ID_KEY)!!
                }
                var category = binding.categoryEditEt.text.toString()
                var event = binding.eventEditEt.text.toString()
                var cost = binding.costEditEt.text.toString()
                val date = binding.dateEditEt.text.toString()
                val otherInfo = binding.otherInfoEt.text.toString()
                val refundMark = arguments?.getInt(EntryListShowFragment.REFUND_KEY, 0)!!

                var newEntry = Entry(id, event, category, cost, date)
                newEntry.otherInfo = otherInfo
                newEntry.refundMark = refundMark

                if (isAdvancedSearching == true)
                {
                    val data = EntryS(newEntry)
                    val bundle = Bundle()
                    val idText = binding.otherInfoEt.text
                    data.advancedSearch = true
                    if (idText.isNotEmpty())
                    {
                        data.entry.id = idText.toString().toInt()
                    }
                    else
                    {
                        data.entry.id = -1
                    }
                    data.lookupGt = binding.costOrIncomeTv.text.toString() == GT_STR
                    bundle.putSerializable(EntryListShowFragment.LOOKUP_KEY, data)
                    findNavController().navigate(R.id.action_entryModFragment_to_entryListShowFragment, bundle)
                }else
                {
                    newEntry = signEntry(newEntry, if (binding.costOrIncomeTv.text.toString() == COST_STR) '-' else  '+')
                    BaseActivity.sharedApplication?.let { application ->
                        //add
                        EntryListShowFragment.getViewModel(application).apply {
                            if(id == EntryListShowFragment.nextIdOfEntries)
                                addEntry(newEntry)
                            else
                                updateEntry(newEntry)
                        }
                    }
                    backToList(date)
                }

            }
            binding.entryDeleteBt.setOnClickListener {
                // honor to Android programmers
                // i'm just child matching toys

                //check if it's here,  or...
                AlertDialog.Builder(context).apply {
                    setTitle("警告")
                    setMessage("确认要删除该条吗？数据将无法复原！")
                    setPositiveButton("是的"){ _, _ ->
                        val id = arguments?.getInt(EntryListShowFragment.ID_KEY, 0)!!
                        val refundMark = arguments?.getInt(EntryListShowFragment.REFUND_KEY, 0)!!
                        BaseActivity.sharedApplication?.let { application ->
                            EntryListShowFragment.getViewModel(application).apply {
                                // if withdrawing refund, withdraw refund
                                if (refundMark == -1)
                                {
                                    val list = EntryListShowFragment.cacheList
                                    for (item in list)
                                    {
                                        if (item.refundMark == id){
                                            Log.d(LOG_TAG, "clear refundMark of $item")
                                            item.refundMark = 0
                                            updateEntry(item)
                                        }
                                    }
                                }
                                deleteEntry(Entry(id, "", "", ""))
                            }
                        }
                        Log.d(LOG_TAG, "item at $id is deleted")
                        Toast.makeText(context, "删除成功", Toast.LENGTH_SHORT).show()

                        backToList()
                    }
                    setNegativeButton("取消"){ _, _ ->
                    }
                    setCancelable(true)
                }.show()
            }
            if(arguments != null)
            {
                // warning: the same names
                val id = arguments?.getInt(EntryListShowFragment.ID_KEY)!!
                binding.entryIdTv.text = resources.getText(R.string.id).toString() + " "+ id.toString()
            }

            //----- search mode
            if(isAdvancedSearching == true)
            {
                binding.entryModBt.text = "查询"
                binding.entryDeleteBt.isClickable = false
                binding.entryDeleteBt.visibility = View.INVISIBLE

                binding.advancedIdTv.text = "查询id"
                binding.otherInfoEt.inputType = EditorInfo.TYPE_CLASS_NUMBER
            }
            else {
                if (binding.costEditEt.text.isNotEmpty())
                {
                    val tmp = binding.costEditEt.text.toString()
                    if(tmp[0].isDigit()){
                        binding.costOrIncomeTv.text = COST_STR
                    }else{
                        val sign = signOfEntry(tmp)
                        binding.costOrIncomeTv.text = if(sign == '+') INCOME_STR else COST_STR
                        binding.costEditEt.setText(tmp.substring(1, tmp.length))
                    }
                }
                else
                {
                    binding.costEditEt.setText("0.00")
                }

                //----- normal mode
                if(arguments == null || arguments?.isEmpty == true)
                {
                    if(arguments?.getString(EntryListShowFragment.DATE_KEY) == null)
                    {
                        binding.dateEditEt.setText(BaseActivity.getDateToday())
                    }
                    if (binding.eventEditEt.text.isEmpty())
                    {
                        binding.eventEditEt.setText("默认事件")
                    }
                    if (binding.categoryEditEt.text.isEmpty())
                    {
                        binding.categoryEditEt.setText("默认")
                    }
                    // change labels according to whether $cost is minus or positive
                    // when $cost is not signed, it is MINUS by default

                    binding.entryModBt.text = "新建账目"
                    binding.entryDeleteBt.isClickable = false
                    binding.entryDeleteBt.visibility = View.INVISIBLE
                }
                else{
                    // edit mode

                }

            }
        }catch (e: Exception){
            Toast.makeText(context, "未知错误", Toast.LENGTH_SHORT).show()
            Toast.makeText(context, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
        }

        return binding.root
    }
    private fun backToList(date: String? = null)
    {
        if(date == null)
        {
            findNavController().navigate(R.id.action_entryModFragment_to_entryListShowFragment)
        }
        else
        {
            val bundle = Bundle()
            bundle.apply {
                putString(EntryListShowFragment.DATE_KEY, date)
            }
            findNavController().navigate(R.id.action_entryModFragment_to_entryListShowFragment, bundle)
        }
    }

    companion object{
        const val LOG_TAG = "EntryModFragment"
        const val COST_STR = "支出"
        const val INCOME_STR = "收入"
        const val GT_STR = "大于金额"
        const val LT_STR = "小于金额"
    }

}