package com.example.dissap

import android.content.Intent
import android.app.Activity
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room.databaseBuilder
import androidx.recyclerview.widget.LinearLayoutManager
import RecyclerItemClickListener

//Initialise variables to be set later
private const val TAG = "ShowActivity"
var huntNames: MutableList<String> = mutableListOf("")

class ShowActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_hunt)
        //Load database and retrieve all current available hunts, use hunts to retrieve titles and
        //display them using the adapter for the recyclerview
        Global.huntDB = databaseBuilder(
            this,
            HuntDB::class.java, "dissap.hunt.db"
        ).build()
        var huntServices = HuntServices()
        var hunt = huntServices.getAllHunts()
        val huntLength = hunt.size - 1
        Global.huntsList = hunt
        if (!hunt.isNullOrEmpty()) {
            for(x in 0..huntLength){
                huntNames.add(x, hunt.get(x).title)
            }
        }
        val customAdaptor = CustomAdaptor(this, huntNames)
        val listView = findViewById<RecyclerView>(R.id.listviewhunt) as RecyclerView
        //Handle onclicks by loading hunt individual hunt object at that position
        listView.layoutManager = LinearLayoutManager(this)
        listView.addOnItemTouchListener(
            RecyclerItemClickListener(
                this,
                listView,
                object : RecyclerItemClickListener.OnItemClickListener {
                    override fun onItemClick(view: View, position: Int) {
                        val huntToLoad: Hunt? = Global.huntsList?.get(position)
                        val gameIntent = Intent(this@ShowActivity, GameActivity::class.java)
                        gameIntent.putExtra("huntId", huntToLoad?.hId)
                        gameIntent.putExtra("title",huntToLoad?.title)
                        gameIntent.putExtra("victory",huntToLoad?.victory)
                        gameIntent.putExtra("names",huntToLoad?.names)
                        gameIntent.putExtra("hints",huntToLoad?.hints)
                        gameIntent.putExtra("infos",huntToLoad?.infos)
                        gameIntent.putExtra("lats",huntToLoad?.lats)
                        gameIntent.putExtra("longs",huntToLoad?.longs)
                        startActivity(gameIntent)
                    }

                    override fun onLongItemClick(view: View?, position: Int) {
                        // Do nothing
                    }
                })
        )
        listView.adapter = customAdaptor
    }

    class HuntServices {

        //Publicly accessible function to return list of comments from async function
        fun getAllHunts(): MutableList<Hunt> {
            return GetHuntsAsync().execute().get() as MutableList<Hunt>
        }

        //Use dao method to retrieve the waypoint by searching for the waypoint with matching id
        private class GetHuntsAsync : AsyncTask<Void, Void, MutableList<Hunt>>() {
            override fun doInBackground(vararg url: Void): MutableList<Hunt> {
                return Global.huntDB!!.huntDao().getAll()
            }
        }
    }

    class CustomAdaptor(
        private val context: Activity, hunts: List<String>
    ) : RecyclerView.Adapter<ShowActivity.CustomAdaptor.HuntViewHolder>() {

        var mOnHuntListener: HuntListener? = null

        interface HuntListener {
            fun onClicked(title: Int){
            }
        }

        //Initialise Hunt list row xml items and populate them by data passed by the bindviewholder
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HuntViewHolder {
            var layout = LayoutInflater.from(parent.context)
            val view = layout.inflate(R.layout.listview_hunt_items, parent, false)
            return HuntViewHolder(view, mOnHuntListener)
        }

        //Pass data at each position to the viewholder for each row
        override fun onBindViewHolder(holder: HuntViewHolder, position: Int) {
            var huntName: String = huntNames.get(position)
            holder.bindData(
                huntName, mOnHuntListener
            )
        }

        override fun getItemCount(): Int {
            return Global.huntsList!!.size
        }

        //Initialise hunt list row xml items and populate them by data passed by the bindviewholder
        //handle onclicks by loading hunt individual hunt object at that position, splitting the variables
        //that make it up and launching the game with the necessary variables added to the intent
        class HuntViewHolder(itemView: View, internal var mOnHuntListener: HuntListener?) :
            RecyclerView.ViewHolder(itemView), View.OnClickListener {

            override fun onClick(v: View?) {
                val huntToLoad: Hunt? = Global.huntsList?.get(adapterPosition)
                val gameIntent = Intent(v?.context, GameActivity::class.java)
                gameIntent.putExtra("huntId", huntToLoad?.hId)
                gameIntent.putExtra("title",huntToLoad?.title)
                gameIntent.putExtra("victory",huntToLoad?.victory)
                gameIntent.putExtra("names",huntToLoad?.names)
                gameIntent.putExtra("hints",huntToLoad?.hints)
                gameIntent.putExtra("infos",huntToLoad?.infos)
                gameIntent.putExtra("lats",huntToLoad?.lats)
                gameIntent.putExtra("longs",huntToLoad?.longs)
                v?.context?.startActivity(gameIntent)

            }

            var row = itemView.findViewById(R.id.list_hunt_item_data) as LinearLayout
            var titleView = itemView.findViewById(R.id.title) as TextView

            fun bindData(
                title: String, listener: HuntListener?
            ) {
                titleView.text = title
                row.setOnClickListener(this)
            }
        }
    }
}
