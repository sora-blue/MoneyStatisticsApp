package com.zzz.moneystatistics.database

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.zzz.moneystatistics.activity.BaseActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// not necessary indeed
class EntryViewModel(application: Application): AndroidViewModel(application) {
    val allEntry : List<Entry>
    val allEntryLive : LiveData<List<Entry>>
    private val repository: EntryRepository

    init {
        val dao = EntryDatabase.getDatabase(application).entryDao()
        repository = EntryRepository(dao)
        allEntry = repository.allEntries
        allEntryLive = repository.liveAllEntries
    }
    fun addEntry(entry: Entry)
    {
        viewModelScope.launch(Dispatchers.IO) {
            entry.cost = BaseActivity.trunkToTwo(entry.cost)
            if(entry.cost[0].isDigit()) entry.cost = "-" + entry.cost
            repository.addEntry(entry)
        }
    }
    fun updateEntry(entry: Entry)
    {
        viewModelScope.launch(Dispatchers.IO) {
            entry.cost = BaseActivity.trunkToTwo(entry.cost)
            repository.updateEntry(entry)
        }
    }
    fun deleteEntry(entry: Entry)
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteEntry(entry)
        }
    }
    fun clearAll()
    {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearAll()
        }
    }
    fun retrieveCountedStatistics() = repository.retrieveCountedStatistics()

}