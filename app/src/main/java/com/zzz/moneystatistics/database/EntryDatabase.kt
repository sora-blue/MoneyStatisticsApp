package com.zzz.moneystatistics.database

import android.content.ContentValues
import android.content.Context
import androidx.room.Database
import androidx.room.OnConflictStrategy
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.withContext

@Database(entities = [Entry::class], version = 2, exportSchema = false)
abstract class EntryDatabase: RoomDatabase() {
    abstract fun entryDao(): EntryDao
    companion object{
        @Volatile
        private var instance: EntryDatabase? = null
        fun getDatabase(context: Context): EntryDatabase {
            var tempInstance = instance
            if(tempInstance != null)
                return tempInstance as EntryDatabase //?
            synchronized(this){
                tempInstance = Room.databaseBuilder(
                    context.applicationContext,
                    EntryDatabase::class.java,
                    "statistics"
                ).apply {
                    // 迁 移 数 据 库 大 法 坏
                    // migrate version 1 to version 2
                    val migration = object : Migration(1, 2){
                        override fun migrate(database: SupportSQLiteDatabase) {
                            database.execSQL("alter table statistics add refundMark int not null default 0")
                            database.execSQL("alter table statistics add otherInfo text not null default \"\"")
                        }
                    }
                    addMigrations(migration)
                }.build()
                instance = tempInstance


                return instance as EntryDatabase //?


            }
        }
    }

}

