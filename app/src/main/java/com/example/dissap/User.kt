package com.example.dissap

import androidx.room.Entity
import androidx.room.PrimaryKey

//Data class for User objects
@Entity
data class User(
    @PrimaryKey
    val username: String,
    val password: String,
    val email: String,
    val studentNo: String,
    val telNo: String
)

