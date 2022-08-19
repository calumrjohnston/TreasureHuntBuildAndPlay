package com.example.dissap

import android.content.Context
import androidx.room.*

/* Construct the Room Database using the User and Online Game
 * entities, and their respective DAOs. */
@Database(entities = [Leaderboard::class], version = 1)
abstract class LeaderboardDB : RoomDatabase() {
    abstract fun leaderboardDao(): LeaderboardDao
    companion object {
        private var INSTANCE: LeaderboardDB? = null
        fun getDatabase(context: Context): LeaderboardDB? {
            if (INSTANCE == null) {
                synchronized(LeaderboardDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        LeaderboardDB::class.java, "dissap.leaderboard.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}