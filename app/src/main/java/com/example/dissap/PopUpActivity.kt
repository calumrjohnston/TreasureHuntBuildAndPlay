package com.example.dissap

import android.content.Context
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import kotlinx.android.synthetic.main.activity_pop_up.*
import kotlin.random.Random

//Initialise variables to be set later
var thuntId = ""
var currentPosition = ""
var mPopContext: Context? = null

class PopUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pop_up)
        //Set variables and set layout size to be a smaller activity infront of the map
        Global.imgDB = Room.databaseBuilder(
            this,
            ImgDB::class.java, "dissap.img.db"
        ).build()
        var imgServices = ImageServices()
        val imgObjList = imgServices.getAllImages()
        mPopContext = baseContext
        //val imgByte = intent.getByteArrayExtra("imgByte")
        thuntId = Global.huntId
        val name: String = intent.getStringExtra("name")
        val info: String = intent.getStringExtra("info")
        currentPosition = intent.getIntExtra("currentPosition", 0).toString()
        val imgByte  = imgObjList[currentPosition.toInt()].imageStorage
        val dm = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(dm)
        val width: Int = dm.widthPixels
        val height: Int = dm.heightPixels
        window.setLayout((width * .7).toInt(), (height * .7).toInt())
        //Options is use to decrease bitmap quality to prevent the phone from crashing due to data overload
        val options = BitmapFactory.Options()
        options.inSampleSize = 5
        options.inScaled = true
        //Set view item content as input variables from intent
        val imgBitMap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size, options)
        var iconView = findViewById<ImageView>(R.id.iconPop)
        var titleView = findViewById<TextView>(R.id.titlePop)
        var infoView = findViewById<TextView>(R.id.infoPop)
        iconView.setImageBitmap(imgBitMap)
        titleView.text = name
        infoView.text = info
        //Initialise DB and fetch images
        Global.commentDB = Room.databaseBuilder(
            this,
            CommentDB::class.java, "dissap.comment.db"
        ).build()
        var commentServices = CommentServices()
        var comments = commentServices.getAllComments()
        //Initialise Recylerview and input already existing comments
        val customAdaptor = PopUpActivity.CustomAdapter(this, comments)
        val listView = findViewById<RecyclerView>(R.id.listviewcomment) as RecyclerView
        listView.layoutManager = LinearLayoutManager(this)
        listView.adapter = customAdaptor
        //Handle adding comments by validating input, adding it to the comment list, letting
        // the recylcerview know the data has changed inserting it into the comment database, the
        //user is notified if this is unsuccessful through a toast message
        val add = findViewById<Button>(R.id.buttonCommentPop)
        add.setOnClickListener {
            val commentInsert = editTextCommentPop.text.toString()
            val cId = commentInsert + Global.huntId +
                    Random.nextInt().toString() + (commentInsert.length.toDouble() + Random.nextInt() / 2).toString()
            if (commentInsert != "") {
                val commentToDB = Comment(
                    Global.loggedUser?.username.toString(),
                    currentPosition,
                    huntId.toString(),
                    commentInsert,
                    cId
                )
                comments.add(
                    Comment(
                        Global.loggedUser?.username.toString(),
                        currentPosition,
                        huntId.toString(),
                        commentInsert,
                        cId
                    )
                )
                customAdaptor.notifyDataSetChanged()
                commentServices.insertComment(commentToDB)
            }else {
                Toast.makeText(
                    mPopContext,
                    mPopContext!!.resources.getString(R.string.comment_not_posted),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
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


    class CustomAdapter(popUpActivity: PopUpActivity, comments: MutableList<Comment>) :
        RecyclerView.Adapter<CustomAdapter.CommentViewHolder>() {

        var commentsPass = comments

        //Pass data at each position to the viewholder for each row
        override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
            var userName: String = commentsPass[position].username
            var commentContent:String = commentsPass[position].comment
            holder.bindData(
                userName,
                commentContent
            )
        }

        //Initialise viewholder layout from the comment listview xml file
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
            var layout = LayoutInflater.from(parent.context)
            val view = layout.inflate(R.layout.listview_comment_items, parent, false)
            return CommentViewHolder(view)
        }

        override fun getItemCount(): Int {
            return commentsPass.size
        }

        //Initialise comment list row xml items and populate them by data passed by the bindviewholder
        class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

            var userView = itemView.findViewById(R.id.usernamePop) as TextView
            var commentView = itemView.findViewById(R.id.commentPop) as TextView

            fun bindData(
                username: String, commentContent:String
            ) {
                userView.text = username
                commentView.text = commentContent

            }
        }
    }

    class CommentServices {

        //Publicly accessible function to return list of comments from async function
        fun getAllComments(): MutableList<Comment> {
            return GetHuntsAsync().execute().get() as MutableList<Comment>
        }
        //Create an async thread so multiple users can manipulate the thread without collisions
        private class GetHuntsAsync : AsyncTask<Void, Void, MutableList<Comment>>() {
            //Use dao method to retrieve comments by searching for the comments with matching ids
            override fun doInBackground(vararg url: Void): MutableList<Comment> {
                return Global.commentDB!!.commentDao().searchByComment(thuntId, currentPosition)
            }
        }

        //Publicly accessible function to insert the inputted comment from async function
        fun insertComment(c: Comment){
            InsertComment(PopUpActivity(), c).execute()
        }

        //Create an async thread so multiple users can manipulate the thread without collisions
        private class InsertComment(var context: PopUpActivity, var c: Comment) :
            AsyncTask<Void, Void, Boolean>() {

            //Insert comment into the database
            override fun doInBackground(vararg params: Void?): Boolean {
                Global.commentDB!!.commentDao().insert(c)
                return true
            }

            //Notify user it was successful
            override fun onPostExecute(bool: Boolean?) {
                if(bool!!) {
                    Toast.makeText(mPopContext, mPopContext!!.resources.getString(R.string.comment_posted), Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}