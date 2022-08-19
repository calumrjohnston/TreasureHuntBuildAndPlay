package com.example.dissap

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

//Dao used to handle operations on the comment database
@Dao
interface CommentDao {
    @Insert
    fun insert(c: Comment)
    @Query("SELECT * FROM comment WHERE huntId = :hId AND imgId = :cId")
    fun searchByComment(hId: String, cId: String): MutableList<Comment>
}

