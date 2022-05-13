package com.zzz.moneystatistics.database

import androidx.lifecycle.LiveData

class EntryRepository(private val entryDao: EntryDao) {
    var allEntries : List<Entry> = entryDao.fetchAll()
    var liveAllEntries: LiveData<List<Entry>> = entryDao.fetchAllLive()
    fun addEntry(entry: Entry)
    {
        entryDao.addEntry(entry)
    }
    fun updateEntry(entry: Entry)
    {
        entryDao.addEntry(entry)
    }
    fun deleteEntry(entry: Entry)
    {
        entryDao.deleteEntry(entry)
    }
    fun clearAll()
    {
        entryDao.clearAll()
    }
    fun retrieveCountedStatistics() : ArrayList<ArrayList<ArrayList<Entry>>>
    {
        // todo: to be implemented in viewModel
        return ArrayList<ArrayList<ArrayList<Entry>>>()
    }
}