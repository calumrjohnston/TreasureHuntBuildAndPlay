package com.example.dissap

import android.content.Intent
import android.os.AsyncTask
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlin.random.Random

//Initialisation of variables
var leaderboardDB:LeaderboardDB? = null
var leaderboardListItemsToAdd = mutableListOf<Leaderboard>()
var done = false
var victory = ""
var customAdapter: VictoryActivity.CustomAdapter? = null

class VictoryActivity : AppCompatActivity() {

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_victory)
        //Load the name of the user from intent, insert time and user into the leaderboard database
        //then fetch the leaderboard for the hunt to display to the user (the db is intialised)
        if (done == false) {
            intent.getStringExtra("names")
            val elapsedSeconds = intent.getDoubleExtra("elapsedSeconds", 0.0)
            victory = intent.getStringExtra("victory")

            val lbId = Global.loggedUser.toString() + Global.huntId +
                    Random.nextInt().toString() + (elapsedSeconds + Random.nextInt() / 2).toString()

            leaderboardDB = Room.databaseBuilder(
                this,
                LeaderboardDB::class.java, "dissap.leaderboard.db"
            ).build()
            val lbServices = LeaderboardServices()
            lbServices.insertLeaderboard(
                Leaderboard(
                    Global.loggedUser!!.username,
                    elapsedSeconds,
                    Global.huntId,
                    lbId
                )
            )
            leaderboardListItemsToAdd = lbServices.getAllLeaderboard()
            customAdapter = CustomAdapter(this, leaderboardListItemsToAdd)
            done = true
        }
        //Display leaderboard items in list
        var victoryView = findViewById<TextView>(R.id.textViewVictory)
        victoryView.text = victory
            val listView = findViewById<RecyclerView>(R.id.listviewleaderboard)
            listView.layoutManager = LinearLayoutManager(this)
            listView.adapter = customAdapter
    }

    class CustomAdapter(victoryActivity: VictoryActivity, leaderboards: MutableList<Leaderboard>) :
        RecyclerView.Adapter<CustomAdapter.CommentViewHolder>() {

        var lbPass = leaderboards

        //Pass data at each position to the viewholder for each row
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            var userName: String = lbPass[position].username
            var timeStr:String =lbPass[position].time.toString()
            holder.bindData(
                userName,
                timeStr
            )
        }

        //Initialise Leaderboard list row xml items and populate them by data passed by the bindviewholder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            var layout = LayoutInflater.from(parent.context)
            val view = layout.inflate(R.layout.listview_leaderboard_items, parent, false)
            return CommentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return lbPass.size
        }

        //Initialise waypoint list row xml items and populate them by data passed by the bindviewholder
        class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var userViewVic = itemView.findViewById(R.id.usernameLB) as TextView
            var timeViewVic = itemView.findViewById(R.id.timeLB) as TextView

            fun bindData(
                username: String, time:String
            ) {
                userViewVic.text = username
                timeViewVic.text = "Seconds: "+ time

            }
        }
    }

    //Return to the main menu
    fun buttonReturnMenu(view: View) {
        val returnIntent : Intent = Intent(this, MenuActivity::class.java)
        startActivity(returnIntent)
        finish()
    }


    class LeaderboardServices {

        //Publicly accessible function to return list of leaderboard from async function
        fun getAllLeaderboard(): MutableList<Leaderboard> {
            return GetLBAsync().execute().get() as MutableList<Leaderboard>
        }

        //Use dao method to retrieve the leaderboard entries for the hunt by searching with the hunt id
        private class GetLBAsync : AsyncTask<Void, Void, MutableList<Leaderboard>>() {
            override fun doInBackground(vararg url: Void): MutableList<Leaderboard> {
                return leaderboardDB!!.leaderboardDao().searchByHuntId(Global.huntId)
            }
        }

        //Publicly accessible function to insert a leaderboard entry from async function
        fun insertLeaderboard(l: Leaderboard){
            InsertLeaderboard(VictoryActivity(), l).execute()
        }

        //Use a thread to insert the item into the database
        private class InsertLeaderboard(var context: VictoryActivity, var l: Leaderboard) :
            AsyncTask<Void, Void, Boolean>() {

            override fun doInBackground(vararg params: Void?): Boolean {
                leaderboardDB!!.leaderboardDao().insert(l)
                return true
            }
        }
    }
}