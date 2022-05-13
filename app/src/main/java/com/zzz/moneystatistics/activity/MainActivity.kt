package com.zzz.moneystatistics.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.graphics.BitmapCompat
import androidx.core.view.GravityCompat
import com.zzz.moneystatistics.R
import com.zzz.moneystatistics.database.Entry
import com.zzz.moneystatistics.databinding.ActivityMainBinding
import com.zzz.moneystatistics.fragment.EntryListShowFragment
import java.io.*

/*
* 记账app 大二软设一作业
*
* 20211213 完成展示和修改界面的设计
*
* todo-list:
*  1.完成统计界面的设计 completed
*  2.部署数据库 completed
*  3.完善业务逻辑 :
*       日期设置问题,
*       全局错误处理,
*       etc.
*
* @author: 周政
* */
class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        prepareBackgroundWrapped(IMAGE_MAIN_CODE)
        prepareBackgroundWrapped(IMAGE_MENU_CODE)

        //val entry = Entry(EntryListShowFragment.nextIdOfEntries, "启动测试", "测试", trunkToTwo(Random.nextDouble().toString()), getDateToday())
        //EntryListShowFragment.getViewModel(application).apply {
        //    addEntry(entry)
        //}
        binding.drawerLayout.close()
        binding.navView.setNavigationItemSelectedListener {
            binding.drawerLayout.closeDrawer(GravityCompat.START)
            //may these turned to fragments
            when(it.itemId)
            {
                R.id.navAboutBt -> {
                    val intent = Intent(this, AboutActivity::class.java)
                    startActivity(intent)
                }
                R.id.navInstallmentBt -> {
                    val intent = Intent(this, InstallmentActivity::class.java)
                    startActivity(intent)
                }
                R.id.navExitBt -> {
                    android.os.Process.killProcess(android.os.Process.myPid())
                }
                R.id.navExportCurrentBt -> {
                    try{
                        val text = EntryListShowFragment.cacheSelectedList.joinToString{ e -> "${e}\n" }
                        Log.d(LOG_TAG, "got: $text")
                        writeIntoClipboard(text)
                        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
                    }catch (e : Exception)
                    {
                        Toast.makeText(this, "复制失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
                R.id.navExportAllBt -> {
                    try{
                        val text = EntryListShowFragment.cacheList.joinToString{ e -> "${e}\n" }
                        Log.d(LOG_TAG, "got: $text")
                        writeIntoClipboard(text)
                        Toast.makeText(this, "复制成功", Toast.LENGTH_SHORT).show()
                    }catch (e : Exception)
                    {
                        Toast.makeText(this, "复制失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
                R.id.navImportStandardBt -> {
                    try{
                        val content = readFromClipboard().get()
                        Log.d(LOG_TAG, "read from clipboard: $content")
                        val parsed = parseEntryFromStandard(content)
                        importEntry(parsed)
                        Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){
                        Toast.makeText(this, "导入失败, 请检查格式", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }

                }
                R.id.navImportCasualBt -> {
                    try{
                        val content = readFromClipboard().get()
                        Log.d(LOG_TAG, "read from clipboard: $content")
                        val parsed = parseEntryFromCasual(content)
                        importEntry(parsed)
                        Toast.makeText(this, "导入成功", Toast.LENGTH_SHORT).show()
                    }catch (e: Exception){
                        Toast.makeText(this, "导入失败, 请检查格式", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
                R.id.navSetMainPageBackgroundBt -> {
                    setBackgroundWrapped(IMAGE_MAIN_CODE)
                }
                R.id.navSetMenuBackgroundBt -> {
                    setBackgroundWrapped(IMAGE_MENU_CODE)
                }
                R.id.navExportAllStdBt -> {
                    try{
                        val text = EntryListShowFragment.cacheList.joinToString{ e -> "${e}\n" }
                        Log.d(LOG_TAG, "got: $text")
                        writeIntoFile(text)
                    }catch (e : Exception)
                    {
                        Toast.makeText(this, "保存失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
                R.id.navImportAllStdBt -> {
                    try{
                        readFromFile()
                    }catch (e : Exception)
                    {
                        Toast.makeText(this, "读取失败", Toast.LENGTH_SHORT).show()
                        Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_LONG).show()
                    }
                }
            }
            true
        }
    }
    private fun prepareBackgroundWrapped(code: Int){
        try{
            val o1 = openFileInput(code2str(code))
            o1.close()
            setBackground(code)
        }catch (ignored: Exception){}
    }
    private fun setBackgroundWrapped(code: Int)
    {
        try {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            startActivityForResult(intent, code)
        }catch (e: Exception){
            Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show()
        }
    }

    /**
    * "Casual" form is as follows:
    *   yyyy-MM<-dd>
    *   event cost
     *   event cost
     *   event cost
     *   ...
     *
     *   Each line counts as an independent entry with the same date.
    * */
    private fun parseEntryFromCasual(content: String) : ArrayList<Entry>
    {
        val returnVal = ArrayList<Entry>()
        val unParsed = content.split('\n').toMutableList()
        if(unParsed[0].length == 7)
        {
            unParsed[0] += "-01"
        }
        if(checkDateStringLegality(unParsed[0]))
        {
            val commonDate = unParsed[0]
            for (i in 1 until unParsed.size)
            {
                try{
                    val tmp = unParsed[i].split(' ')
                    val event = tmp[0]
                    var cost = tmp[1]
                    cost.toDouble() //check
                    if (cost[0].isDigit()) cost = "-$cost" // default behavior

                    val newEntry = Entry()
                    newEntry.date = commonDate
                    newEntry.event = event
                    newEntry.cost = cost
                    returnVal.add(newEntry)
                }catch (e: Exception){
                    Toast.makeText(this, "error: ${unParsed[i]}", Toast.LENGTH_LONG).show()
                    throw Exception("Error when importing")
                }

            }
        }
        return returnVal
    }

    private fun saveImageToFile(pos: Int, data: Intent?){
        data?.data?.apply{
            var image : Bitmap?
            if(Build.VERSION.SDK_INT >= 28)
            {
                val source = ImageDecoder.createSource(contentResolver, this)
                image = ImageDecoder.decodeBitmap(source)
            }
            else
            {
                image = MediaStore.Images.Media.getBitmap(contentResolver, this)
            }
            image?.apply {
                val output = openFileOutput(code2str(pos), Context.MODE_PRIVATE)
                compress(Bitmap.CompressFormat.PNG, 100, output)
                output.close()
            }
        }
    }

    private fun setBackground(code: Int)
    {
        //todo: settle the picture-deforming/stretching problem
        val file = getFileStreamPath(code2str(code))
        val options =  BitmapFactory.Options().apply {
            this.inScaled = true
        }
        val bitmap = BitmapFactory.decodeFile(file.path, options)
        val drawable = BitmapDrawable(resources, bitmap)
        if (code == IMAGE_MAIN_CODE){
            drawable.alpha = (255 * 0.5).toInt()
            binding.drawerLayout.background = drawable
        }else if(code == IMAGE_MENU_CODE){
            drawable.alpha = (255 * 0.3).toInt()
            binding.navViewBackground.background = drawable
        }
    }


    // on api 28 or higher
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode)
        {
            IMAGE_MAIN_CODE, IMAGE_MENU_CODE -> {
                try{
                    saveImageToFile(requestCode, data)
                    setBackground(requestCode)
                }catch (e: Exception){
                    Toast.makeText(this, "设置失败", Toast.LENGTH_SHORT).show()
                    Toast.makeText(this, "${e.stackTraceToString()}", Toast.LENGTH_SHORT).show()
                }

            }

        }
    }
    companion object{
        const val IMAGE_MAIN_CODE = 1
        const val IMAGE_MENU_CODE = 2
        private const val IMAGE_MAIN_STR = "background_main"
        private const val IMAGE_MENU_STR = "background_menu"
        const val LOG_TAG = "MainActivity"
        private fun code2str(code:Int) =  if(code == IMAGE_MAIN_CODE) IMAGE_MAIN_STR else IMAGE_MENU_STR

    }

}