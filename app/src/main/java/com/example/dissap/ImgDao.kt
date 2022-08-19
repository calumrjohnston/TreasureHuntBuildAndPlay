package com.example.dissap

import androidx.room.*

//Dao used to handle operations on the Image database
@Dao
interface ImgDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(i: Img)
    @Query("SELECT * FROM img WHERE parentId = :hId ")
    fun searchByHuntId(hId: String): MutableList<Img>
    @Query("SELECT * FROM img ORDER BY parentId ASC")
    fun getAll() : List<Img>
}

