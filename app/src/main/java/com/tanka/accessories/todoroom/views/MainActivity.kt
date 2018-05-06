package com.tanka.accessories.todoroom.views

import android.app.*
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.support.design.widget.FloatingActionButton
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.support.v7.widget.Toolbar
import android.support.v7.widget.helper.ItemTouchHelper
import android.text.TextUtils.isEmpty
import android.view.*
import android.widget.*
import com.github.clans.fab.FloatingActionMenu
import com.tanka.accessories.todoroom.R
import com.tanka.accessories.todoroom.data.model.Note
import com.tanka.accessories.todoroom.data.room.AppDataBase
import com.tanka.accessories.todoroom.utility.Utils
import com.tanka.accessories.todoroom.views.helper.OnStartDragListener
import com.tanka.accessories.todoroom.views.helper.SimpleItemTouchHelperCallback
import com.tanka.accessories.todoroom.widget.NoteWidget
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), OnStartDragListener {

    private lateinit var adapter: NotesAdapter
    private var db: AppDataBase? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var gridLayoutManager: GridLayoutManager? = null
    private var mItemTouchHelper: ItemTouchHelper? = null
    private var noteList: MutableList<Note>? = ArrayList()
    private var bmProfile: Bitmap? = null
    private var reminderCal: Calendar? = null
    private var isSearch: Boolean = false
    private var isGridLayout: Boolean = false
    private val layoutFlag = "isLinear"

    private lateinit var fabSearch: FloatingActionButton
    private lateinit var famOpt: FloatingActionMenu
    private lateinit var fabNote: com.github.clans.fab.FloatingActionButton
    private lateinit var fabStory: com.github.clans.fab.FloatingActionButton
    private lateinit var prefs: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        prefs = this.getSharedPreferences(
                "com.tanka.accessories.todoroom", Context.MODE_PRIVATE)
        db = AppDataBase.getDatabase(this)
        reminderCal = Calendar.getInstance()
        val bm = BitmapFactory.decodeResource(resources,
                R.drawable.tanks_suited)
        bmProfile = Utils.getCircleBitmap(bm)

        recyclerView = findViewById(R.id.my_recycler_view)
        fabSearch = findViewById(R.id.fabSearch)
        famOpt = findViewById(R.id.fabMenuOptions)
        fabNote = findViewById(R.id.fabNew)
        fabStory = findViewById(R.id.fabStory)
        famOpt.setClosedOnTouchOutside(true)

        setUpViews()


        fabNote.setOnClickListener { v ->
            famOpt.close(true)
            showAddDialog()
        }

        fabSearch.setOnClickListener { view: View ->
            loadNotes()
            fabSearch.visibility = View.GONE
        }
    }

    private fun setUpViews() {
        recyclerView!!.setHasFixedSize(true)

        adapter = NotesAdapter(this, noteList, this)

        layoutManager = LinearLayoutManager(this)
        gridLayoutManager = GridLayoutManager(this, 2)

        if (!prefs.getBoolean(layoutFlag, false))
            recyclerView!!.layoutManager = layoutManager
        else
            recyclerView!!.layoutManager = gridLayoutManager

        recyclerView!!.itemAnimator = DefaultItemAnimator()
        recyclerView!!.adapter = adapter;


    }


    private fun showAddDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_note)

        val calendar = Calendar.getInstance()

        val etTitle = dialog.findViewById<EditText>(R.id.etTitle)
        val tvDate = dialog.findViewById<TextView>(R.id.tvDate)
        val tvTime = dialog.findViewById<TextView>(R.id.tvTime)
        val etBody = dialog.findViewById<EditText>(R.id.etBody)
        val btnSend = dialog.findViewById<Button>(R.id.btnAdd)

        tvTime.setOnClickListener { v ->

            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)


            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@MainActivity, TimePickerDialog.OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                tvTime.text = selectedHour.toString() + ":" + selectedMinute

                reminderCal!!.add(Calendar.SECOND, 5)
            }, hour, minute, true)//Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()

        }

        tvDate.setOnClickListener { v ->
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            val month = calendar.get(Calendar.MONTH)
            val year = calendar.get(Calendar.YEAR)

            val mDatePicker: DatePickerDialog
            mDatePicker = DatePickerDialog(this@MainActivity, DatePickerDialog.OnDateSetListener { view, selectedYear, selectedMonth, dayOfMonth ->
                tvDate.text = dayOfMonth.toString() + ":" + (selectedMonth + 1) + ":" + selectedYear

            }, year, month, day)
            mDatePicker.setTitle("Select Date")
            mDatePicker.show()

        }
        btnSend.setOnClickListener { v ->
            val title = etTitle.text.toString()
            val date = tvDate.text.toString()
            val body = etBody.text.toString()

            val intent = Intent(this, AlarmReceiverActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            val am = getSystemService(Activity.ALARM_SERVICE) as AlarmManager
            am.set(AlarmManager.RTC_WAKEUP, reminderCal!!.timeInMillis,
                    pendingIntent)


            if (isEmpty(title) || title.equals("")) {

            } else {

                setUpViews()
                addNote(title, body, date, "")
                dialog.dismiss()
            }

        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.dimAmount = 0.6f
        dialog.window!!.attributes = lp
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

    }

    private fun showSearchDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_search_note)

        val etTitle = dialog.findViewById<EditText>(R.id.etTitle)
        val btnSend = dialog.findViewById<ImageButton>(R.id.btnAdd)
        btnSend.setOnClickListener { v ->
            val title = "%" + etTitle.text.toString() + "%"
            if (isEmpty(title) || title.equals("%%")) {

                dialog.dismiss()
            } else {

                isSearch = true
                searchNote(title)
                dialog.dismiss()
            }

        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        lp.dimAmount = 0.6f
        dialog.window!!.attributes = lp
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)

    }

    private fun searchNote(title: String) {
        object : AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg params: Void): MutableList<Note>? {
                return db!!.notesDao.getNote(title)
            }

            protected fun onPostExecute(notes: MutableList<Note>) {
                if (noteList != null)
                    noteList!!.clear()

                noteList = notes
                adapter = NotesAdapter(this@MainActivity, noteList, this@MainActivity)

                val callback = SimpleItemTouchHelperCallback(adapter)
                mItemTouchHelper = ItemTouchHelper(callback)
                mItemTouchHelper!!.attachToRecyclerView(recyclerView)

                recyclerView!!.adapter = adapter
                fabSearch.visibility = View.VISIBLE

            }
        }.execute()
    }

    private fun addNote(title: String, body: String, date: String, type: String) {
        val note = Note()
        note.title = title
        note.body = body
        note.date = date
        note.type = type

        db!!.notesDao.insertAll(note)
        noteList!!.add(note)
        adapter.notifyDataSetChanged()

        updateWidget(noteList!!, null)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        object : AsyncTask<Void, Void, List<Note>>() {
            override fun doInBackground(vararg params: Void): MutableList<Note> {
                return db!!.notesDao.allNotes
            }

            protected fun onPostExecute(notes: MutableList<Note>) {

                if (noteList != null)
                    noteList!!.clear()

                noteList = notes
                adapter = NotesAdapter(this@MainActivity, noteList, this@MainActivity)

                val callback = SimpleItemTouchHelperCallback(adapter)
                mItemTouchHelper = ItemTouchHelper(callback)
                mItemTouchHelper!!.attachToRecyclerView(recyclerView)

                recyclerView!!.adapter = adapter


                updateWidget(noteList!!, null)


            }
        }.execute()
    }

    private fun updateWidget(noteList: List<Note>, bitmap: Bitmap?) {
        val context = this@MainActivity
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.note_widget)
        val thisWidget = ComponentName(context, NoteWidget::class.java)
        if (!noteList.isEmpty())
            remoteViews.setTextViewText(R.id.appwidget_text, noteList[0].title)
        if (bitmap == null) {
            remoteViews.setImageViewBitmap(R.id.image_profile, bmProfile)
        } else {
            remoteViews.setImageViewBitmap(R.id.image_profile, bitmap)
        }
        appWidgetManager.updateAppWidget(thisWidget, remoteViews)

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == R.id.action_settings) {
            return true
        } else if (id == R.id.change_pp) {
            val intent = Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

            startActivityForResult(intent, RESULT_LOAD_IMAGE)
        } else if (id == R.id.search_note) {
            if (isSearch) {

                loadNotes()
                isSearch = false

            } else {
                showSearchDialog()
            }
        } else if (id == R.id.change_layout) {

            if (!isGridLayout) {
                recyclerView!!.layoutManager = gridLayoutManager
                recyclerView!!.animate()
                isGridLayout = true
                prefs.edit().putBoolean(layoutFlag, isGridLayout)
            } else {
                recyclerView!!.layoutManager = layoutManager
                recyclerView!!.animate()
                isGridLayout = false
                prefs.edit().putBoolean(layoutFlag, isGridLayout)
            }
        }

        return super.onOptionsItemSelected(item)
    }

    fun deleteNote(item: Note) {
        db!!.notesDao.deleteAll(item)
        noteList!!.remove(item)
        adapter.notifyDataSetChanged()
        updateWidget(noteList!!, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                if (data != null) {
                    val imageUri = data.data
                    var bitmap: Bitmap? = null
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    loadToView(bitmap)

                } else {
                    Toast.makeText(applicationContext, "Cancelled",
                            Toast.LENGTH_SHORT).show()
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(applicationContext, "Cancelled",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadToView(bitmap: Bitmap?) {
        updateWidget(noteList!!, bitmap)
    }


    fun editNote(note: Note) {

        showEditDialog(note)

    }

    private fun showEditDialog(note: Note) {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_add_note)

        val etTitle = dialog.findViewById<EditText>(R.id.etTitle)
        val tvDate = dialog.findViewById<TextView>(R.id.tvDate)
        val etBody = dialog.findViewById<EditText>(R.id.etBody)
        val btnSend = dialog.findViewById<Button>(R.id.btnAdd)

        etTitle.setText(note.title)
        btnSend.setOnClickListener { v ->
            val title = etTitle.text.toString()
            val date = tvDate.text.toString()
            val body = etBody.text.toString()
            if (isEmpty(title) || title.equals("")) {

            } else {

                val note1 = Note()
                note1.id = note.id
                note1.title = title
                note1.body = body
                note1.date = date

                db!!.notesDao.updateAll(note1)
                loadNotes()

                dialog.dismiss()
            }

        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window!!.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.dimAmount = 0.6f
        dialog.window!!.attributes = lp
        dialog.window!!.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)


    }

    override fun onStartDrag(viewHolder: RecyclerView.ViewHolder) {
        mItemTouchHelper!!.startDrag(viewHolder)
    }

    companion object {
        private val RESULT_LOAD_IMAGE = 1
    }
}
