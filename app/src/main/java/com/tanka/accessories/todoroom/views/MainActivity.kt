package com.tanka.accessories.todoroom.views

import android.app.*
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog.OnTimeSetListener
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.tanka.accessories.todoroom.R
import com.tanka.accessories.todoroom.data.model.Note
import com.tanka.accessories.todoroom.data.room.AppDataBase
import com.tanka.accessories.todoroom.utility.Utils
import com.tanka.accessories.todoroom.views.MainActivity
import com.tanka.accessories.todoroom.widget.NoteWidget
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {
    private var adapter: NotesAdapter? = null
    private var db: AppDataBase? = null
    private var recyclerView: RecyclerView? = null
    private var layoutManager: LinearLayoutManager? = null
    private var noteList: MutableList<Note>? = null
    private var bmProfile: Bitmap? = null
    private var reminderCal: Calendar? = null
    private var isSearch = false
    var fab: FloatingActionButton? = null
    var fabSearch: FloatingActionButton? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        reminderCal = Calendar.getInstance()
        db = AppDataBase.getDatabase(this)
        val bm = BitmapFactory.decodeResource(resources,
                R.drawable.tanks_suited)
        bmProfile = Utils.getCircleBitmap(bm)
        recyclerView = findViewById(R.id.my_recycler_view)
        recyclerView.setHasFixedSize(true)
        layoutManager = LinearLayoutManager(this)
        recyclerView.setLayoutManager(layoutManager)
        recyclerView.setItemAnimator(DefaultItemAnimator())
        fab = findViewById(R.id.fab)
        fabSearch = findViewById(R.id.fabSearch)
        fab.setOnClickListener(View.OnClickListener { view: View? -> showAddDialog() })
        val csl = ColorStateList(arrayOf(IntArray(0)), intArrayOf(-0xe16f01))
        fab.setBackgroundTintList(csl)
        fabSearch.setOnClickListener(View.OnClickListener { view: View? ->
            loadNotes()
            fabSearch.setVisibility(View.GONE)
        })
    }

    fun showCustomToast(textInfo: String?) {
        val inflater = layoutInflater
        val layout = inflater.inflate(R.layout.custom_toast_layout,
                findViewById(R.id.custom_toast_container))
        val text = layout.findViewById<TextView>(R.id.text)
        text.text = textInfo
        val toast = Toast(applicationContext)
        toast.setGravity(Gravity.CENTER, 0, 0)
        toast.duration = Toast.LENGTH_LONG
        toast.view = layout
        toast.show()
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
        val etType = dialog.findViewById<EditText>(R.id.etType)
        val btnSend = dialog.findViewById<Button>(R.id.btnAdd)
        tvTime.setOnClickListener { v: View? ->
            // TODO Auto-generated method stub
            val hour = calendar[Calendar.HOUR_OF_DAY]
            val minute = calendar[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(this@MainActivity, OnTimeSetListener { timePicker, selectedHour, selectedMinute ->
                tvTime.text = "$selectedHour:$selectedMinute"

//                    reminderCal.add(Calendar.HOUR_OF_DAY,selectedHour);
//                    reminderCal.add(Calendar.MINUTE,selectedMinute);
                reminderCal!!.add(Calendar.SECOND, 5)
            }, hour, minute, true) //Yes 24 hour time
            mTimePicker.setTitle("Select Time")
            mTimePicker.show()
        }
        tvDate.setOnClickListener { v: View? ->
            val day = calendar[Calendar.DAY_OF_MONTH]
            val month = calendar[Calendar.MONTH]
            val year = calendar[Calendar.YEAR]
            val mDatePicker: DatePickerDialog
            mDatePicker = DatePickerDialog(this@MainActivity, object : OnDateSetListener {
                override fun onDateSet(view: DatePicker, selectedYear: Int, selectedMonth: Int, dayOfMonth: Int) {
                    tvDate.text = dayOfMonth.toString() + ":" + (selectedMonth + 1) + ":" + selectedYear

//                    reminderCal.add(Calendar.YEAR,year);
//                    reminderCal.add(Calendar.MONTH,month);
//                    reminderCal.add(Calendar.DAY_OF_MONTH,dayOfMonth);
                }
            }, year, month, day)
            mDatePicker.setTitle("Select Date")
            mDatePicker.show()
        }
        btnSend.setOnClickListener { v: View? ->
            val title = etTitle.text.toString()
            val date = tvDate.text.toString()
            val body = etBody.text.toString()
            val type = etType.text.toString()
            val intent = Intent(this, AlarmReceiverActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT)
            val am = getSystemService(ALARM_SERVICE) as AlarmManager
            am[AlarmManager.RTC_WAKEUP, reminderCal!!.timeInMillis] = pendingIntent
            if (TextUtils.isEmpty(title) || title.equals("", ignoreCase = true)) {
            } else {
                addNote(title, body, date, type)
                dialog.dismiss()
            }
        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.dimAmount = 0.6f
        dialog.window.attributes = lp
        dialog.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun showSearchDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_search_note)
        val etTitle = dialog.findViewById<EditText>(R.id.etTitle)
        val btnSend = dialog.findViewById<ImageButton>(R.id.btnAdd)
        btnSend.setOnClickListener { v: View? ->
            val title = "%" + etTitle.text.toString() + "%"
            if (TextUtils.isEmpty(title) || title.equals("%%", ignoreCase = true)) {
                dialog.dismiss()
            } else {
                isSearch = true
                searchNote(title)
                dialog.dismiss()
            }
        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.TOP
        lp.dimAmount = 0.6f
        dialog.window.attributes = lp
        dialog.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun searchNote(title: String) {
        object : AsyncTask<Void?, Void?, MutableList<Note?>>() {
            protected override fun doInBackground(vararg params: Void): MutableList<*> {
                return db.getNotesDao().getNote(title)
            }

            protected override fun onPostExecute(notes: MutableList<*>) {
                if (noteList != null) noteList!!.clear()
                noteList = notes
                adapter = NotesAdapter(this@MainActivity, noteList)
                recyclerView!!.setAdapter(adapter)
                fabSearch!!.visibility = View.VISIBLE
            }
        }.execute()
    }

    private fun addNote(title: String, body: String, date: String, type: String) {
        val note = Note()
        note.setTitle(title)
        note.setBody(body)
        note.setDate(date)
        note.setType(type)
        db.getNotesDao().insertAll(note)
        noteList!!.add(note)
        adapter.notifyDataSetChanged()
        updateWidget(noteList, null)
    }

    override fun onResume() {
        super.onResume()
        loadNotes()
    }

    private fun loadNotes() {
        object : AsyncTask<Void?, Void?, MutableList<Note?>>() {
            protected override fun doInBackground(vararg params: Void): MutableList<*> {
                return db.getNotesDao().getAllNotes()
            }

            protected override fun onPostExecute(notes: MutableList<*>) {
                if (noteList != null) noteList!!.clear()
                noteList = notes
                adapter = NotesAdapter(this@MainActivity, noteList)
                recyclerView!!.setAdapter(adapter)
                updateWidget(noteList, null)
            }
        }.execute()
    }

    private fun updateWidget(noteList: List<Note>?, bitmap: Bitmap?) {
        val context: Context = this@MainActivity
        val appWidgetManager = AppWidgetManager.getInstance(context)
        val remoteViews = RemoteViews(context.packageName, R.layout.note_widget)
        val thisWidget = ComponentName(context, NoteWidget::class.java)
        if (!noteList!!.isEmpty()) remoteViews.setTextViewText(R.id.appwidget_text, noteList[0].title)
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
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, RESULT_LOAD_IMAGE)
        } else if (id == R.id.search_note) {
            if (isSearch) {
                loadNotes()
                isSearch = false
            } else {
                showSearchDialog()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    fun deleteNote(item: Note?) {
        db.getNotesDao().deleteAll(item)
        noteList!!.remove(item)
        adapter.notifyDataSetChanged()
        updateWidget(noteList, null)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val uri: Uri? = null
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
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
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(applicationContext, "Cancelled",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadToView(bitmap: Bitmap?) {
        updateWidget(noteList, bitmap)
    }

    private fun getRealPathFromUri(tempUri: Uri): String {
        var cursor: Cursor? = null
        return try {
            val proj = arrayOf(MediaStore.Images.Media.DATA)
            cursor = this.contentResolver.query(tempUri, proj, null, null, null)
            val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            cursor.moveToFirst()
            cursor.getString(column_index)
        } finally {
            cursor?.close()
        }
    }

    private fun getImageUri(youractivity: Activity, bitmap: Bitmap): Uri {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val path = MediaStore.Images.Media.insertImage(youractivity.contentResolver, bitmap, "Title", null)
        return Uri.parse(path)
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
        val etType = dialog.findViewById<EditText>(R.id.etType)
        val btnSend = dialog.findViewById<Button>(R.id.btnAdd)
        etTitle.setText(note.title)
        btnSend.setOnClickListener { v: View? ->
            val title = etTitle.text.toString()
            val date = tvDate.text.toString()
            val body = etBody.text.toString()
            val type = etType.text.toString()
            if (TextUtils.isEmpty(title) || title.equals("", ignoreCase = true)) {
            } else {
                val note1 = Note()
                note1.setId(note.id)
                note1.setTitle(title)
                note1.setBody(body)
                note1.setDate(date)
                note1.setType(type)
                db.getNotesDao().updateAll(note1)
                loadNotes()
                dialog.dismiss()
            }
        }
        dialog.show()
        val lp = WindowManager.LayoutParams()
        lp.copyFrom(dialog.window.attributes)
        lp.width = WindowManager.LayoutParams.MATCH_PARENT
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT
        lp.gravity = Gravity.CENTER
        lp.dimAmount = 0.6f
        dialog.window.attributes = lp
        dialog.window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
    }

    private fun updateNoteInDb(note: Note, title: String, date: String, body: String, type: String) {

//        Note note = new Note(title,date,body,type);
//        db.getNotesDao().editNote(note);
        db.getNotesDao().updateNote(note.id, title, body, date, type)
    }

    companion object {
        private const val RESULT_LOAD_IMAGE = 1
        fun calculateInSampleSize(
                options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
            // Raw height and width of image
            val height = options.outHeight
            val width = options.outWidth
            var inSampleSize = 1
            if (height > reqHeight || width > reqWidth) {
                val halfHeight = height / 2
                val halfWidth = width / 2

                // Calculate the largest inSampleSize value that is a power of 2 and keeps both
                // height and width larger than the requested height and width.
                while (halfHeight / inSampleSize > reqHeight
                        && halfWidth / inSampleSize > reqWidth) {
                    inSampleSize *= 2
                }
            }
            return inSampleSize
        }
    }
}