package com.example.dissap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.content.Intent
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    //Initialize Database.
    private var appDB: AppDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //Set database
        appDB = AppDB.getDatabase(this)!!
    }

    //Launch register menu
    fun buttonRegister(view: View) {
        val registerIntent :Intent = Intent(this, RegisterActivity::class.java).apply {}
        startActivity(registerIntent)
    }

    fun buttonLogin(view: View) {
        //Only attempt login if the user has inputted data
        if (!editUserName.text.toString().isEmpty() && !editPassword.text.toString().isEmpty()) {

            Log.i(R.string.log_app.toString(), R.string.log_login_attempt.toString()) //Log the attempt.

            //Run the SQL login query with the inputted username and password.
            RequestLogin(
                this,
                editUserName.text.toString(),
                editPassword.text.toString()
            ).execute()

            //Procees to main menu once the user variable has been set
            if(Global.loggedUser != null) {
                val mainMenuIntent :Intent = Intent(this, MenuActivity::class.java).apply {}
                startActivity(mainMenuIntent)
            }
        }else {
            //Tell the user they are missing the required fields
            Toast.makeText(this, R.string.toast_missing_fields , Toast.LENGTH_LONG).show() }
    }

    //Use assync threads to allow multiple users to attempt to login
    private class RequestLogin(var context: MainActivity, var username :String, var password :String) :
        AsyncTask<Void, Void, List<User>>() {

        //Dao function checks for matching username and password and returns this user as a singular list
        override fun doInBackground(vararg params: Void?): List<User> {
            return context.appDB!!.userDao().attemptLogin(username, password)
        }

        //Set the logged user as the first item in the list as the list is just 1 item and let the
        //user know the log in was succesful or not successful
        override fun onPostExecute(userList: List<User>?) {
            if(userList!!.isNotEmpty()) {
                Global.loggedUser = userList[0]
                println(Global.loggedUser.toString())
                Toast.makeText(context, R.string.toast_login_successful, Toast.LENGTH_LONG).show()
            }else{
                Toast.makeText(context, R.string.toast_login_unsuccessful, Toast.LENGTH_LONG).show()
            }
        }
    }
}