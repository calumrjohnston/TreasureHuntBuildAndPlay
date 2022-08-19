package com.example.dissap

import android.content.Context
import androidx.room.*
import androidx.room.RoomDatabase
import androidx.room.Database



/* Construct the Img Database using the Img
 * entity, and their respective DAO */
@Database(entities = [Img::class], version = 1)
abstract class ImgDB : RoomDatabase() {
    abstract fun imgDao(): ImgDao
    companion object {
        private var INSTANCE: ImgDB? = null
        fun getDatabase(context: Context): ImgDB? {
            if (INSTANCE == null) {
                synchronized(ImgDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        ImgDB::class.java, "dissap.img.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}
