package com.example.dissap

//Class used to store global variables
class Global {
    companion object Variables {
        var commentDB: CommentDB? = null
        var huntDB: HuntDB? = null
        var huntsList: MutableList<Hunt>? = null
        var imgDB: ImgDB? = null
        var loggedUser: User? = null
        var huntId:String = ""
    }
}