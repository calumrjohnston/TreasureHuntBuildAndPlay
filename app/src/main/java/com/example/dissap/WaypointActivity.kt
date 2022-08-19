package com.example.dissap

import android.content.Intent
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import android.os.AsyncTask
import androidx.room.Room
import java.util.*
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T



//Initialisation of variables
private var imgByteList:MutableList<ByteArray> = mutableListOf()
private var imgList:MutableList<Bitmap> = mutableListOf()
private var names:MutableList<String> = mutableListOf()
private var infos:MutableList<String> = mutableListOf()
private var latLongsStrings:MutableList<String> = mutableListOf()
private var contextUse: Context? = null
private var activity: Activity? = null

class WaypointActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_waypoints)
        //Set variables by building database and fetching from Intent
        activity = this
        contextUse = this
        Global.imgDB = Room.databaseBuilder(
            this,
            ImgDB::class.java, "dissap.img.db"
        ).build()
        names= intent.getStringArrayExtra("names").toMutableList()
        infos= intent.getStringArrayExtra("infos").toMutableList()
        //Create a list of latlongs after converting the data type conversion to be displayed in the
        //textviews
        val sizeList = names.size-1
        val latLongs= intent.getParcelableArrayListExtra<LatLng>("latLongs")
        for (pos in 0..sizeList){
            val latLongString:String = latLongs.get(pos).toString()
            latLongsStrings.add(pos, latLongString)
        }
        //Load the images objects from the database and add the corresponding ones to the database
        var imgServices = ImageServices()
        val imgObjList = imgServices.getAllImages()
        for (x in 0..sizeList) {
            imgByteList.add(imgObjList.get(x).imageStorage)
        }
        //Options is use to decrease bitmap quality to prevent the phone from crashing due to data overload
        val options = BitmapFactory.Options()
        options.inSampleSize = 5
        options.inScaled = true
        for (x in 0..sizeList) {
            val bitMapToAdd = BitmapFactory.decodeByteArray(imgByteList[x], 0,
                imgByteList[x].size, options)
            imgList.add(x, bitMapToAdd)
        }
        //Shuffle order of waypoints to prevent cheating
        val seed = System.nanoTime()
        Collections.shuffle(imgList, Random(seed))
        Collections.shuffle(names, Random(seed))
        Collections.shuffle(infos, Random(seed))
        Collections.shuffle(latLongsStrings, Random(seed))
        //Initialise recyclerview and pass waypoints into it using an adapter
        val customAdaptor = CustomAdaptor(this, imgList, names, infos, latLongsStrings)
        val listView = findViewById<RecyclerView>(R.id.listviewwaypoint)
        listView.adapter = customAdaptor
        listView.layoutManager = LinearLayoutManager(this)
    }

    class ImageServices {

        //Public function to call the async thread and return the images for the hunt
        fun getAllImages(): MutableList<Img> {
            return GetImgsAsync().execute().get() as MutableList<Img>
        }

        //Create an async thread so multiple users can manipulate the thread without collisions
        private class GetImgsAsync : AsyncTask<Void, Void, MutableList<Img>>() {
            override fun doInBackground(vararg url: Void): MutableList<Img> {
                return Global.imgDB!!.imgDao().searchByHuntId(Global.huntId)
            }
        }
    }

    class CustomAdaptor(
        private val context: Activity, imgList:List<Bitmap>,
        names: MutableList<String>, infos: MutableList<String>,
        latLongs:MutableList<String>) : RecyclerView.Adapter<CustomAdaptor.WaypointViewHolder>() {

        //Pass data at each position to the viewholder for each row each row of waypoints
        override fun onBindViewHolder(holder: WaypointViewHolder, position: Int) {
            var waypointImg: Bitmap = imgList.get(position)
            var waypointName: String = names.get(position)
            var waypointInfos: String = infos.get(position)
            var waypointLatLongs: String = latLongsStrings.get(position)
            holder.bindData(
                waypointImg, waypointName, waypointInfos, waypointLatLongs, mOnHuntListener
            )
        }

        var mOnHuntListener: HuntListener? = null

        interface HuntListener {
            fun onClicked(title: Int){
            }
        }

        //Initialise viewholder layout from the listview waypoint xml file
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaypointViewHolder {
            var layout = LayoutInflater.from(parent.context)
            val view = layout.inflate(R.layout.listview_waypoint_items, parent, false)
            return WaypointViewHolder(view, mOnHuntListener)
        }

        override fun getItemCount(): Int {
            return names.size
        }

        //Initialise viewholder layout from the listview waypoint xml file
        class WaypointViewHolder(
            itemView: View,
            internal var mOnHuntListener: HuntListener?
        ) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            override fun onClick(v: View?) {
                val returnIntent = Intent(v?.context, GameActivity::class.java)
                returnIntent.putExtra("result", latLongView.text.toString())
                activity?.setResult(Activity.RESULT_OK, returnIntent)
                activity?.finish()
            }

            var iconView = itemView.findViewById(R.id.iconWay) as ImageView
            var titleView = itemView.findViewById(R.id.titleWay) as TextView
            var infoView = itemView.findViewById(R.id.infoWay) as TextView
            var latLongView = itemView.findViewById(R.id.latitudelongitudeWay) as TextView

            fun bindData(
                waypointImg:Bitmap, waypointName:String, waypointInfo:String, waypointLatLong:String,
                listener: HuntListener?
            ) {
                iconView.setImageBitmap(waypointImg)
                titleView.text = waypointName
                infoView.text = waypointInfo
                latLongView.text = waypointLatLong
                itemView.setOnClickListener(this)
                }
            }
        }
    }
