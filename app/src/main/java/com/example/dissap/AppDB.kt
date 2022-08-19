package com.example.dissap

import android.content.Context
import androidx.room.*

/* Construct the Room Database using the User
 * entity, and their respective DAOs */
@Database(entities = arrayOf(User::class), version = 1)
abstract class AppDB : RoomDatabase() {
    abstract fun userDao(): UserDao
    companion object {
        private var INSTANCE: AppDB? = null
        fun getDatabase(context: Context): AppDB? {
            if (INSTANCE == null) {
                synchronized(AppDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDB::class.java, "dissap.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}