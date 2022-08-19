package com.example.dissap

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.os.AsyncTask
import android.os.Build
import android.widget.*
import android.provider.MediaStore
import kotlinx.android.synthetic.main.activity_create_hunt.*
import android.view.*
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.graphics.Bitmap.CompressFormat
import java.io.ByteArrayOutputStream
import android.text.method.DigitsKeyListener
import android.text.InputFilter
import android.text.InputType
import android.text.TextUtils
import java.util.concurrent.ExecutorService
import kotlin.random.Random

//Initialisation of variables, boundary lat and long variables set to only allow waypoints on campus
//itemLength is 14 as the array of locations on campus is 15 initially with the default list
private val executerService: ExecutorService? = null
var waypoint: ArrayList<Waypoint> =  ArrayList()
private const val TAG = "CreateActivity"
var itemLength:Int = 14
var huntId: Long = 0

class CreateActivity : AppCompatActivity() {

    //Initialise databases and the lists needed for the waypoint objects to be created
    private var huntDB: HuntDB? = null
    private var imgDB: ImgDB? = null

    val imgs = mutableListOf<Int>(
        R.drawable.library,
        R.drawable.great_hall,
        R.drawable.beach,
        R.drawable.tesco,
        R.drawable.santander,
        R.drawable.tower,
        R.drawable.lounge,
        R.drawable.student_union,
        R.drawable.gym,
        R.drawable.tafarn_tawe,
        R.drawable.food_court,
        R.drawable.bus_stop,
        R.drawable.cofo,
        R.drawable.courts,
        R.drawable.engineering_block
    )
    var title = mutableListOf<String>(
        "Library",
        "Great Hall",
        "Beach",
        "Tesco",
        "Santander",
        "Tower",
        "Lounge",
        "Student Union",
        "Uni Gym",
        "Tafarn Tawe",
        "Food Court",
        "Bus Stop",
        "CoFo",
        "Courts",
        "Engineering Block"
    )
    val hint = mutableListOf<String>(
        "What building has the most stories?",
        "I'm done to boats, to cargo," +
                " to loads. When indoors I'm, in a way, a narrow road. What am I?",
        "I've sand for feet, you " +
                "sometimes sit with me in the heat",
        "Deli meat, to cook or ready to eat, toiletries, paste for" +
                " your teeth and soap for your knees",
        "When in a hurry, come here to withdraw your money",
        "I go way up high into the sky, look at my face and tell the time",
        "This 6 letter word becomes" +
                " long when the 'u' and 'e' are removed",
        "Come here when in need of help, For anything personal " +
                "or mental health",
        "We've got treadmills where you can run, lift so much that you can carry a tonne",
        "Hot chips and cold beers, Pool tables and sports here",
        "Here you have a choice of deal for hot meals, " +
                "chilli con carne to biryani",
        "Wait here to be transported into town, or anywhere around",
        "I only opened 2 years ago, In here computer scientists are taught what they know",
        "Tennis games here " +
                "and fun, Football games and run",
        "Here is where engineers are created, STEM student are also educated"
    )
    val info = mutableListOf<String>(
        "A building containing collections of books, periodicals, films and recorded music " +
                "for use or borrowing by members of the university",
        "Large building used for events, also there's food here",
        "A sandy shore",
        "A local shop on campus to purchase supplies",
        "A bank on campus",
        "The tower, on the bottom" +
                " there is a security office",
        "A common area to chill out",
        "Place where you can speak to comeone or get advice",
        "Place where you can work out membership is £25.99",
        "An cafe with entertainment facilities",
        "Large hall" +
                " with different food vendors to choose from",
        "Designated stop where buses which take you into town will stop",
        "Building where the majority of lectures for CS take place",
        "Hoops and court area to play ball games",
        "The building where STEM students are taught"
    )
    val latitude = mutableListOf<String>(
        "51.617963",
        "51.618476",
        "51.616871",
        "51.618603",
        "51.618529",
        "51.618749",
        "51.618846",
        "51.618986",
        "51.618786",
        "51.618496",
        "51.618433",
        "51.619152",
        "51.619076",
        "51.617547",
        "51.618603"
    )
    val longitude = mutableListOf<String>(
        "-3.877329",
        "-3.878384",
        "-3.877323",
        "-3.879853",
        "-3.880250",
        "-3.879092",
        "-3.879859",
        "-3.881018",
        "-3.881855",
        "-3.880507",
        "-3.881108",
        "-3.879161",
        "-3.878421",
        "-3.879183",
        "-3.877960"
    )

    private val CAMERA_REQUEST = 1888
    private val imageView: ImageView? = null
    private val MY_CAMERA_PERMISSION_CODE = 100
    

    @RequiresApi(Build.VERSION_CODES.JELLY_BEAN)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_hunt)
        //Set database and set it so the keyboard doesnt move items on the constraint layout
        huntDB = HuntDB.getDatabase(this)!!
        imgDB = ImgDB.getDatabase(this)!!
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
        //Options is use to decrease bitmap quality to prevent the phone from crashing due to data overload
        val options = BitmapFactory.Options()
        options.inSampleSize = 5
        options.inScaled = true
        val bitMap = mutableListOf<Bitmap>(
            BitmapFactory.decodeResource(resources, R.drawable.library, options),
            BitmapFactory.decodeResource(resources, R.drawable.great_hall, options),
            BitmapFactory.decodeResource(resources, R.drawable.beach, options),
            BitmapFactory.decodeResource(resources, R.drawable.tesco, options),
            BitmapFactory.decodeResource(resources, R.drawable.santander, options),
            BitmapFactory.decodeResource(resources, R.drawable.tower, options),
            BitmapFactory.decodeResource(resources, R.drawable.lounge, options),
            BitmapFactory.decodeResource(resources, R.drawable.student_union, options),
            BitmapFactory.decodeResource(resources, R.drawable.gym, options),
            BitmapFactory.decodeResource(resources, R.drawable.tafarn_tawe, options),
            BitmapFactory.decodeResource(resources, R.drawable.food_court, options),
            BitmapFactory.decodeResource(resources, R.drawable.bus_stop, options),
            BitmapFactory.decodeResource(resources, R.drawable.cofo, options),
            BitmapFactory.decodeResource(resources, R.drawable.courts, options),
            BitmapFactory.decodeResource(resources, R.drawable.engineering_block, options)
        )
        //Construct waypoints and add them to the list
        for (i in 0 until imgs.size-1){
            waypoint.add(Waypoint(bitMap[i], title[i], hint[i], info[i], latitude[i], longitude[i]))
        }
        //Initialise recyclerview and pass waypoints into it using an adapter
        val manager = LinearLayoutManager(this)
        val listView = findViewById<RecyclerView>(R.id.listview)
        listView.layoutManager = LinearLayoutManager(this)
        val customAdaptor = CustomAdaptor(this, waypoint)
        listView.adapter = customAdaptor
        //Uses other method to handle drag and drops on the list, pass in 0 to parameter to disable
        //swiping
        val dividerItemDecoration = DividerItemDecoration(this , manager.orientation)
        listView.addItemDecoration(dividerItemDecoration)
        val callback = DragManageAdapter(customAdaptor, this,
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN), 0)
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(listView)
        //Handle waypoint image insert
        imgInput.setOnClickListener {
            var i = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(i, 123)
        }
        //Prevent user from entering locations off of campus or text values (validation)
/*        val editTextLatitude = findViewById<EditText>(R.id.editTextLat)
        val editTextLongitude = findViewById<EditText>(R.id.editTextLong)
        editTextLatitude.inputType = InputType.TYPE_CLASS_NUMBER
        editTextLatitude.filters = arrayOf(
            InputFilterMinMax(minLat, maxLat),
            InputFilter.LengthFilter(9)
        )
        editTextLatitude.keyListener = DigitsKeyListener.getInstance("0123456789.-")
        editTextLongitude.inputType = InputType.TYPE_CLASS_NUMBER
        editTextLongitude.filters = arrayOf(
            InputFilterMinMax(minLong, maxLong),
            InputFilter.LengthFilter(9)
        )
        editTextLongitude.keyListener = DigitsKeyListener.getInstance("0123456789.")*/

        //Use random number generator, user info and waypoint info as a hashing algorithm
        //to generate a unique ID for the hunt
        val randWaypoint = Random.nextDouble() + waypoint.size.toDouble()
        Global.huntId = Global.loggedUser.toString() + Global.loggedUser?.studentNo.toString() +
                Random.nextInt().toString() + (randWaypoint / 2).toString()
        //Handle adding custom waypoint by adding validating input, adding it to the waypoint
        //list , letting the recylcerview know the data has changed and increasing the size of
        //the list variable
        val add = findViewById<Button>(R.id.buttonAddWayPoint)
        add.setOnClickListener{
            val huntImageDrawable:BitmapDrawable = imgInput.drawable as BitmapDrawable
            val huntImg:Bitmap = huntImageDrawable.bitmap
            val huntName = editTextName.text.toString()
            val huntHint = editTextHint.text.toString()
            val huntInfo = editTextInfo.text.toString()
            val huntLat = editTextLat.text.toString()
            val huntLong = editTextLong.text.toString()
            if(TextUtils.isEmpty(huntName) || huntName.length < 5)
            {
                editTextName.error = "You must have a minimum of 5 characters in the waypoint name."
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(huntHint) || huntHint.length < 5)
            {
                editTextHint.error = "You must have a minimum of 5 characters in the waypoint hint."
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(huntInfo) || huntInfo.length < 15)
            {
                editTextInfo.error = "You must have a minimum of 15 characters in the waypoint info."
                return@setOnClickListener
            }
            val maxLat: Double = 51.619716
            val minLat: Double = 51.617345
            val maxLong: Double = -3.875598
            val minLong: Double = -3.883920
            if(TextUtils.isEmpty(huntLat) || huntLat.length < 6 && huntLat.toDouble() > maxLat &&
                huntLat.toDouble() < minLat)
            {
                editTextLat.error = "You must have a minimum of 6 characters in the waypoint latitude."
                return@setOnClickListener
            }
            if(TextUtils.isEmpty(huntLong) || huntLong.length < 6 && huntLong.toDouble() > maxLong
                && huntLong.toDouble() < minLong)
            {
                editTextLong.error = "You must have a minimum of 6 characters in the waypoint longitude."
                return@setOnClickListener
            }
            waypoint.add(Waypoint(huntImg, huntName, huntHint, huntInfo, huntLat, huntLong))
            customAdaptor.notifyDataSetChanged()
            itemLength = itemLength + 1
        }
    }

    //As long as the result from the camera activity is ok then add taken image to the display
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?){
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode==123){
            var bmp = data?.extras?.get("data") as Bitmap
            imgInput.setImageBitmap(bmp)
        }
    }

    class CustomAdaptor(private val context: Activity, waypoint: ArrayList<Waypoint>
                        ) : RecyclerView.Adapter<CustomAdaptor.WaypointViewHolder>() {

        var mOnWaypointListener: WaypointListener? = null

        //Swap items as they are dragged up the list
        fun swapItems(fromPosition: Int, toPosition: Int) {
            if (fromPosition < toPosition) {
                for (i in fromPosition..toPosition - 1) {
                    waypoint.set(i, waypoint.set(i+1, waypoint.get(i)))
                }
            } else {
                for (i in fromPosition..toPosition + 1) {
                    waypoint.set(i, waypoint.set(i-1, waypoint.get(i)))
                }
            }
            //Let adapter know data set order changed
            notifyItemMoved(fromPosition, toPosition)
        }

        //Pass data at each position to the viewholder for each row and handle cell delete clicks
        override fun onBindViewHolder(holder: WaypointViewHolder, position: Int) {
            var waypointIndividual: Waypoint = waypoint[position]
            holder.bindData(waypointIndividual.getImage_drawables(), waypointIndividual.getNames(),
                waypointIndividual.getHints(), waypointIndividual.getInfos(), waypointIndividual.getLats(),
                waypointIndividual.getLongs(), mOnWaypointListener)
            holder.delete.setOnClickListener{
                waypoint.removeAt(position)
                itemLength = itemLength - 1
                notifyDataSetChanged()
            }
        }

        //Initialise list row xml items and populate them by data passed by the bindviewholder
        class WaypointViewHolder(itemView: View, internal var mOnWaypointListener: WaypointListener?)
            : RecyclerView.ViewHolder(itemView), View.OnClickListener {

            //Uses listener for detecting drags of the row
            override fun onClick(v: View?) {
                mOnWaypointListener?.onClicked(adapterPosition)
            }

            var row = itemView.findViewById(R.id.list_item_data) as LinearLayout
            var iconV = itemView.findViewById(R.id.icon) as ImageView
            var titleV = itemView.findViewById(R.id.title) as TextView
            var hintV = itemView.findViewById(R.id.hint) as TextView
            var infoV = itemView.findViewById(R.id.info) as TextView
            var latV = itemView.findViewById(R.id.latitude) as TextView
            var longV = itemView.findViewById(R.id.longitude) as TextView
            var delete = itemView.findViewById(R.id.delete_button) as Button

            fun bindData(
                icon: Bitmap, title: String, hint: String, info: String, lat: String,
                long: String, listener: WaypointListener?
            ) {
                iconV.setImageBitmap(icon)
                titleV.text = title
                hintV.text = hint
                infoV.text = info
                latV.text = lat
                longV.text = long
                row.setOnClickListener { itemView ->
                    listener?.onClicked(position)
                }
            }
        }

        //Initialise viewholder layout from the listview xml file
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaypointViewHolder {
            var layout = LayoutInflater.from(parent.context)
            val view = layout.inflate(R.layout.listview_items, parent, false)
            return WaypointViewHolder(view, mOnWaypointListener)
        }

        override fun getItemCount(): Int {
            return waypoint.size
        }

        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

    }

    //Create indiviual lists from waypoint object list then conjoin into long string to hold all of
    //the hunt data so it can be inseted into the database, then insert create img objects from the
    //list and insert them into the database
    fun buttonCreateHunt(view: View) {
        var titleMap: MutableList<String> = waypoint.map { it.name }.toMutableList()
        var hintMap: MutableList<String> = waypoint.map { it.hint }.toMutableList()
        var infoMap: MutableList<String> = waypoint.map { it.info }.toMutableList()
        var latMap: MutableList<String> = waypoint.map { it.lat }.toMutableList()
        var longMap: MutableList<String> = waypoint.map { it.long }.toMutableList()

        InsertHunt(this, createHuntPass(titleMap.joinToString(
            separator = "#"
        ),         hintMap.joinToString(
            separator = "#"
        ),         infoMap.joinToString(
            separator = "#"
        ),         latMap.joinToString(
            separator = "#"
        ),         longMap.joinToString(
            separator = "#"
        ))).execute()

        for (i in 0 until itemLength){
            InsertImg(this, createImgPass(i)).execute()
        }
    }

    //Another hashing function to generate primary key for each image
    private fun createImgPass(i: Int): Img {
        return Img(
            Global.huntId,bitmapToByte(waypoint[i].image_drawable), (i.toString() + Global.loggedUser?.
                username + Global.loggedUser?.studentNo.toString() +
                    Random.nextInt().toString() + (randImg() / 3).toString())
        )
    }

    //Another round of hashing
    private fun randImg(): Double {
        return waypoint.size.toDouble() + Global.loggedUser?.telNo!!.toDouble() + Random.nextDouble()

    }

    //Convert bitmap to bytearray so it can be stored in database (Room db wont accept bitmaps)
    private fun bitmapToByte(bitmap: Bitmap): ByteArray {
        val blob = ByteArrayOutputStream()
        bitmap.compress(CompressFormat.PNG, 0 /* Ignored for PNGs */, blob)
        val bitmapdata = blob.toByteArray()
        return bitmapdata
    }

    //Create hunt object from joined strings
    private fun createHuntPass(titles:String, hints: String, infos: String, lats: String, longs: String): Hunt {
        return Hunt(
            Global.huntId,getHuntName(), getVictoryMessage(), titles, hints,
            infos, lats, longs
        )
    }

    //Create an async thread so multiple users manipulate the thread without collisions
    private class InsertHunt(var context: CreateActivity, var hunt: Hunt) :
        AsyncTask<Void, Void, Boolean>() {

        //Insert hunt and post a toast message to let the user know it was successful
        override fun doInBackground(vararg params: Void?): Boolean {
            println(hunt.toString())
            context.huntDB!!.huntDao().insert(hunt)
            return true
        }

        override fun onPostExecute(bool: Boolean?) {
            if(bool!!) {
                Toast.makeText(context, context.resources.getString(R.string.hunt_created)  , Toast.LENGTH_LONG).show()
            }
        }
    }

    //Create an async thread so multiple users can manipulate the thread without collisions
    private class InsertImg(var context: CreateActivity, var img: Img) :
        AsyncTask<Void, Void, Boolean>() {

        //Insert image into the database
        override fun doInBackground(vararg params: Void?): Boolean? {
            println(img.toString())
            context.imgDB!!.imgDao().insert(img)
            return true
        }
    }

    //Get hunt name from edit text item and set default if it is empty
    private fun getHuntName():String{
        var huntName: String = editTextHuntName.text.toString()
        if (huntName.trim().isEmpty()){
            huntName = "Campus Hunt"
        }
        return huntName
    }

    //Get victory message from edit text item and set default if it is empty
    private fun getVictoryMessage():String{
        var victoryMessage: String = editTextHuntVictory.text.toString()
        if (victoryMessage.trim().isEmpty()){
            victoryMessage = "You win!"

        }
        return victoryMessage
    }
}