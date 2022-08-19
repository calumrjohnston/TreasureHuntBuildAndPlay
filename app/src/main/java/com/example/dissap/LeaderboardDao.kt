package com.example.dissap

import androidx.room.*

//Dao used to handle operations on the Leaderboard database
@Dao
interface LeaderboardDao {
    @Insert (onConflict = OnConflictStrategy.REPLACE)
    fun insert(l: Leaderboard)
    @Query("SELECT * FROM leaderboard WHERE huntId = :hId ORDER BY time DESC")
    fun searchByHuntId(hId: String): MutableList<Leaderboard>
}

