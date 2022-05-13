package com.zzz.moneystatistics.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.databinding.ActivityInstallmentBinding
import com.zzz.moneystatistics.fragment.EntryListShowFragment

class InstallmentActivity : BaseActivity() {
    private lateinit var binding: ActivityInstallmentBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityInstallmentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.installationDateEt.setText(BaseActivity.getDateToday())

        binding.installationPhaseTv.setOnClickListener {
            if (binding.installationPhaseTv.text == YEAR_INS)
                binding.installationPhaseTv.text = MONTH_INS
            else
                binding.installationPhaseTv.text = YEAR_INS
        }

        binding.installationAddBt.setOnClickListener {
            try {
                var date = binding.installationDateEt.text.toString()
                if (!BaseActivity.checkDateStringLegality(date))
                    date = BaseActivity.getDateToday()
                val totalCost = binding.installationTotalEt.text.toString().toDouble()
                val interest = if(binding.installationInterestRateEt.text.isNotEmpty())
                    binding.installationInterestRateEt.text.toString().toDouble() / 100.0 * totalCost
                else
                    0.0
                val phase = binding.installationPhaseEt.text.toString().toInt()
                val isYear = binding.installationPhaseTv.text == YEAR_INS
                var count = 1
                while(count <= phase)
                {
                    val newEntry = Entry()
                    var preDate = date
                    newEntry.cost = (totalCost / phase + interest).toString()
                    newEntry.otherInfo = "分期付款$totalCost 元(${count}/$phase)\n 利息 $interest 元" + binding.installationOtherInfoEt.text.toString()
                    newEntry.date = date
                    newEntry.event = binding.installationEventEt.text.toString()
                    newEntry.category = binding.installationCategoryEt.text.toString()
                    EntryListShowFragment.getViewModel(application).apply {
                        addEntry(newEntry)
                    }
                    Log.d(LOG_TAG, "$date $preDate")
                    // may there a better way to deal with it
                    // but i'm lazy
                    if (isYear)
                    {
                        var year = BaseActivity.getYear(preDate).toInt()
                        var tmp = 0
                        while (preDate[tmp] != '-')
                            tmp++
                        year++

                        date = year.toString() + preDate.substring(tmp)
                    }
                    else
                    {
                        while (BaseActivity.getMonth(preDate) == BaseActivity.getMonth(date))
                        {
                            date = BaseActivity.getTomorrowOf(date)
                        }
                        val day = BaseActivity.getDay(preDate)
                        preDate = date
                        while(BaseActivity.getMonth(preDate) == BaseActivity.getMonth(date) &&
                            BaseActivity.getDay(date) != day)
                        {
                            date = BaseActivity.getTomorrowOf(date)
                        }
                        if (BaseActivity.getMonth(preDate) != BaseActivity.getMonth(date))
                            date = BaseActivity.getYesterdayOf(date)
                    }
                    // end fetching legal next date
                    count++
                }
            }catch (e: Exception){
                Toast.makeText(this, "未知错误", Toast.LENGTH_SHORT).show()
                Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
            }
            finish()
        }
    }
    companion object{
        const val YEAR_INS = "年分期数"
        const val MONTH_INS = "月分期数"
        const val LOG_TAG = "InstallmentActivity"
    }
}