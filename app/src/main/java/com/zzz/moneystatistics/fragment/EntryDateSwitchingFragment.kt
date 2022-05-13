package com.zzz.moneystatistics.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.activity.BaseActivity
import com.zzz.moneystatistics.databinding.FragmentEntryDateSwitchingBinding
import java.lang.StringBuilder

class EntryDateSwitchingFragment : Fragment() {

    private lateinit var binding: FragmentEntryDateSwitchingBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentEntryDateSwitchingBinding.inflate(inflater)

        arguments?.getString(EntryListShowFragment.DATE_KEY)?.apply {
            if (BaseActivity.checkDateStringLegality(this))
            {
                val year = BaseActivity.getYear(this)
                val month = BaseActivity.getMonth(this)
                val day = BaseActivity.getDay(this)
                binding.dateSwitchingYearEt.setText(year)
                binding.dateSwitchingMonthEt.setText(month)
                binding.dateSwitchingDayEt.setText(day)
            }else
            {
                val today = BaseActivity.getDateToday()
                val year = BaseActivity.getYear(today)
                val month = BaseActivity.getMonth(today)
                val day = BaseActivity.getDay(today)
                binding.dateSwitchingYearEt.setText(year)
                binding.dateSwitchingMonthEt.setText(month)
                binding.dateSwitchingDayEt.setText(day)
            }
        }


        binding.dateSwitchingBt.setOnClickListener {
            var date = collectDate()
            Log.d("dateSwitching", "$date")
            val bundle = Bundle()
            bundle.putString(EntryListShowFragment.DATE_KEY, date)
            findNavController().navigate(R.id.action_entryDateSwitchingFragment_to_entryListShowFragment, bundle)
        }

        binding.advancedSearchBt.setOnClickListener {
            val bundle = Bundle()
            val date = collectDate()
            //bundle.putString(EntryListShowFragment.DATE_KEY, date)
            bundle.putBoolean(ADVANCED_SEARCH_KEY, true)
            findNavController().navigate(R.id.action_entryDateSwitchingFragment_to_entryModFragment, bundle)
        }

        return binding.root
    }
    private fun collectDate() = StringBuffer().apply {
        var year = binding.dateSwitchingYearEt.text.toString()
        var month = binding.dateSwitchingMonthEt.text.toString()
        var day = binding.dateSwitchingDayEt.text.toString()
        if (month.length == 1) month = "0$month"
        if (day.length == 1) day = "0$day"
        append(year)
        append('-')
        append(month)
        append('-')
        append(day)
    }.toString()
    companion object{
        const val ADVANCED_SEARCH_KEY = "advanced_search"
    }
}