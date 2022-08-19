package com.example.dissap

import android.content.Context
import androidx.room.*

/* Construct the Room Database using the User and Online Game
 * entities, and their respective DAOs. */
@Database(entities = [Comment::class], version = 1)
abstract class CommentDB : RoomDatabase() {
    abstract fun commentDao(): CommentDao
    companion object {
        private var INSTANCE: CommentDB? = null
        fun getDatabase(context: Context): CommentDB? {
            if (INSTANCE == null) {
                synchronized(CommentDB::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        CommentDB::class.java, "dissap.comment.db"
                    ).build()
                }
            }
            return INSTANCE
        }
    }
}