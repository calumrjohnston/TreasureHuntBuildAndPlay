package com.example.dissap

import android.graphics.Bitmap

//Class object for waypoints
class Waypoint(image_drawable:Bitmap, name:String, hint:String, info: String, lat: String, long:String) {

    var image_drawable:Bitmap = image_drawable
    var name: String= name
    var hint: String = hint
    var info: String = info
    var lat: String = lat
    var long: String = long

    fun getNames(): String{
        return name.toString()
    }

    fun setNames(string: String){
        this.name = name
    }

    fun getHints(): String{
        return hint.toString()
    }

    fun setHints(string: String){
        this.hint = hint
    }

    fun getInfos(): String{
        return info.toString()
    }

    fun setInfos(string: String){
        this.info = info
    }

    fun getLats(): String{
        return lat.toString()
    }

    fun setLats(string: String){
        this.lat = lat
    }

    fun getLongs(): String{
        return long.toString()
    }

    fun setLongs(string: String){
        this.long = long
    }

    fun getImage_drawables(): Bitmap {
        return image_drawable
    }

    fun setImage_drawables(image_drawable: Bitmap) {
        this.image_drawable = image_drawable
    }

    override fun toString(): String {
        return "Waypoint(image_drawable=$image_drawable, name=$name, hint=$hint, info=$info, lat=$lat, long=$long)"
    }


}
