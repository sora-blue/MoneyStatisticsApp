package com.zzz.moneystatistics.database

import androidx.lifecycle.LiveData
import androidx.room.*

// Data Access Object
@Dao
interface EntryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addEntry(entry: Entry)
    @Update
    fun updateEntry(entry: Entry)
    @Delete
    fun deleteEntry(entry: Entry)
    @Query("select * from statistics order by id")
    fun fetchAll(): List<Entry>
    @Query("select * from statistics order by id")
    fun fetchAllLive() : LiveData<List<Entry>>
    @Query("delete from statistics")
    fun clearAll()
}