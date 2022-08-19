package com.example.dissap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.os.AsyncTask
import android.text.TextUtils
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    //Initialisation of db
    private var appDB: AppDB? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        appDB = AppDB.getDatabase(this)!!
    }

    //Return user object so it can be inserted later
    fun createUser(): User {
        return User(
            editTextName.text.toString(),
            editTextPassword.text.toString(),
            editTextEmail.text.toString(),
            editTextStudNo.text.toString(),
            editTextPhone.text.toString()
        )
    }

    //Validated input by checking length and if something is input for fields, validate password by
    //checking it matches the confirm field for verification, display error messages to let user know
    //what went wrong
    fun buttonRegister(view: View) {
        if(editTextPassword.text.toString().equals(editTextPasswordConfirm.text.toString()) && !editTextPassword.text.toString().isEmpty()) {
            if(TextUtils.isEmpty(editTextName.text.toString()) || editTextName.text.toString().length < 5)
            {
                editTextName.error = "You must have a minimum of 5 characters in username."
                return
            }

            if(TextUtils.isEmpty(editTextPassword.text.toString()) || editTextPassword.text.toString().length < 5)
            {
                editTextPassword.error = "You must have a minimum of 5 characters in password."
                    return
            }

            if(TextUtils.isEmpty(editTextEmail.text.toString()) || editTextEmail.text.toString().length < 8)
            {
                editTextEmail.error = "You must have a minimum of 8 characters in email."
                return
            }

            if(TextUtils.isEmpty(editTextStudNo.text.toString()) || editTextStudNo.text.toString().length < 6)
            {
                editTextStudNo.error = "You must have a minimum of 6 characters in student number."
                return
            }

            if(TextUtils.isEmpty(editTextPhone.text.toString()) || editTextPhone.text.toString().length < 11)
            {
                editTextPhone.error = "You must have a minimum of 11 characters in telephone number."
                return
            }
            InsertUser(this, createUser()).execute()

        }
        else{
            editTextPasswordConfirm.error = "Your password and password confirm field must matc."
        }
    }

    //Create an async thread so multiple users manipulate the thread without collisions
    private class InsertUser(var context: RegisterActivity, var user: User) :
        AsyncTask<Void, Void, Boolean>() {

        //Insert user and post a toast message to let the user know it was successful
        override fun doInBackground(vararg params: Void?): Boolean {
            println(user.toString())
            context.appDB!!.userDao().insert(user)
            return true
        }

        override fun onPostExecute(bool: Boolean?) {
            if(bool!!) {
                Toast.makeText(context, context.resources.getString(R.string.user_created)  , Toast.LENGTH_LONG).show()
            }
        }
    }
}

