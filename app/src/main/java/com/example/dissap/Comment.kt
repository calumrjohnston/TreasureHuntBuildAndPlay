package com.example.dissap

import androidx.room.Entity
import androidx.room.PrimaryKey

//Data class object for comments
@Entity
data class Comment(
    var username: String,
    var imgId: String,
    var huntId: String,
    var comment: String,
    @PrimaryKey
    var commentId: String
    )
