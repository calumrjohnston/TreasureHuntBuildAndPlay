package com.example.dissap

import androidx.room.Entity
import androidx.room.PrimaryKey

//Data class object for leaderboard entries
@Entity
data class Leaderboard(
    var username: String,
    var time:Double,
    var huntId: String,
    @PrimaryKey
    var leaderboardId: String
    )
