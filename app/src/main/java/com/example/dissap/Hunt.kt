package com.example.dissap

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

//Data class object for hunts and images
@Entity(tableName = "hunt")
data class Hunt(
    @PrimaryKey
    var hId:String,
    var title: String,
    var victory: String,
    var names: String,
    var hints: String,
    var infos: String,
    var lats: String,
    var longs: String
) {
    constructor() : this("", "", "", "", "", "", "", "")
}

    @Parcelize
    @Entity(tableName = "img")
    data class Img(
        val parentId: String,
        val imageStorage: ByteArray,
        @PrimaryKey
        val imgId: String
    ):Parcelable{
        constructor() : this("", byteArrayOf(), "")
    }
