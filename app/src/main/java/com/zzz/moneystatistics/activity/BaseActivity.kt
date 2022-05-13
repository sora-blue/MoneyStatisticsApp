package com.zzz.moneystatistics.activity

import android.app.Application
import android.content.*
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.fragment.EntryListShowFragment
import java.io.BufferedInputStream
import java.io.File
import java.io.FileOutputStream
import java.security.cert.PKIXRevocationChecker
import java.text.SimpleDateFormat
import java.util.*

open class BaseActivity : AppCompatActivity() {
    private var buf : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(javaClass.name, " created.")
        sharedApplication = application
        Log.d(javaClass.name, "sharedApplication is evaluated to $sharedApplication.")
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode){
            CREATE_FILE -> {
                if(resultCode == RESULT_OK)
                {
                    try {
                        data?.data?.also { uri ->
                            contentResolver.openOutputStream(uri)?.use {
                                it.write(buf.toByteArray())
                                it.close()
                            }
                        }
                        Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){
                        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            OPEN_FILE -> {
                if(resultCode == RESULT_OK)
                {
                    try{
                        data?.data?.also { uri ->
                            contentResolver.openInputStream(uri)?.use {
                                val buf = BufferedInputStream(it)
                                val tmp = String(buf.readBytes())
                                val parsed = parseEntryFromStandard(tmp)
                                Log.d("BaseActivity", "got: $tmp")
                                importEntry(parsed)
                                buf.close()
                            }
                        }
                        Toast.makeText(this, "读取成功", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){
                        Toast.makeText(this, "读取失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    protected fun importEntry(list: ArrayList<Entry>)
    {
        try {
            list.forEach {
                Log.d(MainActivity.LOG_TAG, "parsed: $it")
                EntryListShowFragment.getViewModel(application).addEntry(it)
            }
            Toast.makeText(this, "成功导入${list.size} 项", Toast.LENGTH_SHORT).show()
        }catch (e: Exception){
            Toast.makeText(this, "导入失败，请检查格式", Toast.LENGTH_SHORT).show()
        }
    }
    public fun writeIntoFile(text: String)
    {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_TITLE, "entries_" + getDateToday() + ".txt")
        }
        buf = text
        startActivityForResult(intent, CREATE_FILE)
    }
    public fun readFromFile()
    {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
        }
        startActivityForResult(intent, OPEN_FILE)
    }
    public fun writeIntoClipboard(text: String)
    {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clipData = ClipData.newPlainText("zzzData", text)
        clipboard.setPrimaryClip(clipData)
    }
    public fun readFromClipboard() : Optional<String>
    {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.primaryClip?.getItemAt(0)?.text?.apply {
            return Optional.of(this.toString())
        }
        return Optional.empty()
    }
    /**
     * "Standard" form is what you get when exporting data to clipboard.
     */
    protected fun parseEntryFromStandard(content: String) : ArrayList<Entry>
    {
        var i = 0
        val checkList = listOf("Entry", "=", "id", "event", "category", "cost", "date", "refundMark", "otherInfo")
        val returnVal = ArrayList<Entry>()
        while(i < content.length)
        {
            if(content[i] == checkList[0][0]
                    && content.subSequence(i, i + 5) == checkList[0])
            {
                i += 6
                var newEntry = Entry()
                for (j in 2 until checkList.size)
                {
                    val tmpStr = StringBuffer()
                    i += checkList[j].length + checkList[1].length
                    while (i < content.length && content[i] != ',' && content[i] != ')')
                    {
                        tmpStr.append(content[i++])
                    }
                    when(j)
                    {
                        2 -> newEntry.id = tmpStr.toString().toInt()
                        3 -> newEntry.event = tmpStr.toString()
                        4 -> newEntry.category = tmpStr.toString()
                        5 -> {
                            tmpStr.toString().toDouble() //check
                            newEntry.cost = tmpStr.toString()
                        }
                        6 -> newEntry.date = tmpStr.toString()
                        7 -> newEntry.refundMark = tmpStr.toString().toInt()
                        8 -> newEntry.otherInfo = tmpStr.toString()
                    }
                    i += 2
                } //end one entry
                returnVal.add(newEntry)
            } //end one entry
            i++
        } // end whole content
        return returnVal
    }
    companion object{
        var sharedApplication : Application? = null
        const val CREATE_FILE = 4
        const val OPEN_FILE = 5


        public fun getDateToday(): String{
            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val rawDate = Date(System.currentTimeMillis())
            return dateFormat.format(rawDate)
        }
        public fun trunkToTwo(str: String) : String
        {
            if(str.isEmpty())
                return "-0.00"
            if (str.length == 1 && !str[0].isDigit())
                return "${str}0.00"
            var i = 0
            while(i < str.length)
            {
                if(str[i] == '.')
                {
                    if(i + 2 < str.length) // i i+1 i+2
                        return str.substring(0, Integer.min(i + 3, str.length))
                    else if(i + 1 < str.length)
                        return "${str}0"
                    else
                        return "${str}00"
                }

                i++
            }
            return "$str.00"
        }
        /**
        * @param: String with format "yyyy-MM-dd"
        * @return: String that belongs to "yyyy"
        * */
        public fun checkDateStringLegality(date: String) : Boolean{
            if(date.length != 10) return false;
            val checkRange = setOf<Int>(0,1,2,3,5,6,8,9) // bucket would be fine :)
            for (i in 0..9) // 0 <= i <= 9
            {
                if(i in checkRange && !date[i].isDigit())
                    return false
                else if(i !in checkRange && date[i] != '-')
                    return false
                // it does not check error like 8102-13-32
            }
            return true
        }
        public fun getYear(formatDate: String) : String
        {
            return formatDate.substring(0, 4)
        }
        /**
         * @param: String with format "yyyy-MM-dd"
         * @return: String that belongs to "MM"
         * */
        public fun getMonth(formatDate: String) : String
        {
            return formatDate.substring(5, 7)
        }
        /**
         * @param: String with format "yyyy-MM-dd"
         * @return: String that belongs to "dd"
         * */
        public fun getDay(formatDate: String) : String
        {
            return formatDate.substring(8, 10)
        }
        public fun getYesterdayOf(currentDateStr: String) = getAdjacentDayOf(currentDateStr, -1)
        public fun getTomorrowOf(currentDateStr: String) = getAdjacentDayOf(currentDateStr, 1)
        private fun getAdjacentDayOf(currentDateStr: String, offset: Int) : String
        {

            val dateFormat = SimpleDateFormat("yyyy-MM-dd")
            val calendar = Calendar.getInstance()
            try{
                dateFormat.parse(currentDateStr)?.apply{
                    calendar.time = this
                }
            }catch (e : Exception){
                Log.d("BaseActivity", "wrong currentDate input")
                return currentDateStr
            }
            calendar.add(Calendar.DATE, offset)
            return dateFormat.format(calendar.time)
        }
    }
}