package com.example.dissap

import android.content.Context
import androidx.room.*

/* Construct the Hunt Database using the Hunt
 * entity, and their respective DAO */
@Database(entities = [Hunt::class], version = 1)
abstract class HuntDB : RoomDatabase() {
    abstract fun huntDao(): HuntDao
    companion object {
        private var INSTANCE: HuntDB? = null
        fun getDatabase(context: Context): HuntDB? {
            if (INSTANCE == null) {
                synchronized(HuntDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        HuntDB::class.java, "dissap.hunt.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}