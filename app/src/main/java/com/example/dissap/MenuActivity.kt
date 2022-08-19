package com.example.dissap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View

//Handle button clicks by launching the appropriate form
class MenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)
    }

    fun buttonJoinHunt(view: View) {
        val joinIntent : Intent = Intent(this, ShowActivity::class.java)
        startActivity(joinIntent)
    }

    fun buttonCreateHunt(view: View) {
        val createIntent : Intent = Intent(this, CreateActivity::class.java)
        startActivity(createIntent)
    }

}
