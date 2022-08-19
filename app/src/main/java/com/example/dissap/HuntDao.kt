package com.example.dissap

import androidx.room.*

//Dao used to handle operations on the Hunt database
@Dao
interface HuntDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(h: Hunt)
    @Query("DELETE FROM hunt WHERE hId = :huntId")
    fun deleteHunt(huntId: String)
    @Query("SELECT * FROM hunt")
    fun getAll() : MutableList<Hunt>
}

