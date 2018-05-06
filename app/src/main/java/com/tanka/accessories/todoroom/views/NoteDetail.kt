package com.tanka.accessories.todoroom.views

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView

import com.gordonwong.materialsheetfab.MaterialSheetFab
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener
import com.tanka.accessories.todoroom.R
import com.tanka.accessories.todoroom.data.model.Note
import com.tanka.accessories.todoroom.utility.Fab
import com.tanka.accessories.todoroom.utility.custom_text.VioletEyeTextView

/**
 * Created by access-tanka on 12/28/17.
 */

class NoteDetail : AppCompatActivity() {

/*    internal var title: VioletEyeTextView = findViewById(R.id.tvTitle)
    internal var date: TextView = findViewById(R.id.tvDate)
    internal var body: TextView=findViewById(R.id.tvBody)
    internal var ivClose: ImageView= findViewById(R.id.ivClose)
    internal var contentLayout: FrameLayout=findViewById(R.id.contentLayout)*/

    private var note: Note? = null
    private var materialSheetFab: MaterialSheetFab<*>? = null
    private var statusBarColor: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_detail)

        val intent = intent
        note = intent.getSerializableExtra("note") as? Note

        val title = findViewById<TextView>(R.id.tvTitle)
        val date = findViewById<TextView>(R.id.tvDate)
        val body = findViewById<TextView>(R.id.tvBody)
        val ivClose = findViewById<ImageView>(R.id.ivClose)
        val contentLayout = findViewById<FrameLayout>(R.id.contentLayout)

        if (note != null) {
            title.text = note!!.title
            body.text = note!!.body
            date.text = note!!.date
        }

        ivClose.setOnClickListener { v -> finish() }

        setupFab()

        ivClose.setOnClickListener { v -> finish() }


    }

    /**
     * Sets up the Floating action button.
     */
    private fun setupFab() {

        val fab = findViewById<Fab>(R.id.fab)
        val sheetView = findViewById<View>(R.id.fab_sheet)
        val overlay = findViewById<View>(R.id.overlay)
        val sheetColor = resources.getColor(R.color.caldroid_white)
        val fabColor = resources.getColor(R.color.caldroid_holo_blue_dark)

        // Create material sheet FAB
        materialSheetFab = MaterialSheetFab(fab, sheetView, overlay, sheetColor, fabColor)

        // Set material sheet event listener
        materialSheetFab!!.setEventListener(object : MaterialSheetFabEventListener() {
            override fun onShowSheet() {
                // Save current status bar color
                statusBarColor = getStatusBarColor()
                // Set darker status bar color to match the dim overlay
                setStatusBarColor(resources.getColor(R.color.caldroid_white))
            }

            override fun onHideSheet() {
                // Restore status bar color
                setStatusBarColor(statusBarColor)
            }
        })

        // Set material sheet item click listeners
        /*findViewById(R.id.fab_sheet_item_recording).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_reminder).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_photo).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_note).setOnClickListener(this);*/

    }


    override fun onBackPressed() {
        if (materialSheetFab!!.isSheetVisible()) {
            materialSheetFab!!.hideSheet()
        } else {
            super.onBackPressed()
        }
    }

    private fun getStatusBarColor(): Int {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor
        } else 0
    }

    private fun setStatusBarColor(color: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = color
        }
    }
}
