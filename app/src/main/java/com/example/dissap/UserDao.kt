package com.example.dissap

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

//Dao used to handle operations on the User database
@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insert(u: User)
    @Query("DELETE FROM user")
    fun deleteAll()
    @Query("SELECT username FROM user WHERE username = :uname ")
    fun searchByUsername(uname: String): List<String>
    @Query("SELECT * FROM user ORDER BY username ASC")
    fun getAll() : List<User>
    @Query("SELECT * FROM  User WHERE username = :uname AND password = :pword")
    fun attemptLogin(uname: String, pword: String): List<User>
}

