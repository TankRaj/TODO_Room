package com.tanka.accessories.todoroom.views;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.gordonwong.materialsheetfab.MaterialSheetFab;
import com.gordonwong.materialsheetfab.MaterialSheetFabEventListener;
import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;
import com.tanka.accessories.todoroom.utility.Fab;
import com.tanka.accessories.todoroom.utility.custom_text.VioletEyeTextView;

/**
 * Created by access-tanka on 12/28/17.
 */

public class NoteDetail extends AppCompatActivity {

    VioletEyeTextView title;
    TextView date;
    TextView body;
    ImageView ivClose;
    FrameLayout contentLayout;

    private Note note;
    private MaterialSheetFab materialSheetFab;
    private int statusBarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Intent intent = getIntent();
        note = (Note) intent.getSerializableExtra("note");

        title = findViewById(R.id.tvTitle);
        date = findViewById(R.id.tvDate);
        body = findViewById(R.id.tvBody);
        ivClose = findViewById(R.id.ivClose);
        contentLayout = findViewById(R.id.contentLayout);

        title.setText(note.getTitle());
        body.setText(note.getBody());
        date.setText(note.getDate());

        ivClose.setOnClickListener(v -> finish());


        setupFab();

        ivClose.setOnClickListener(v -> finish());


    }

    /**
     * Sets up the Floating action button.
     */
    private void setupFab() {

        Fab fab = findViewById(R.id.fab);
        View sheetView = findViewById(R.id.fab_sheet);
        View overlay = findViewById(R.id.overlay);
        int sheetColor = getResources().getColor(R.color.caldroid_white);
        int fabColor = getResources().getColor(R.color.caldroid_holo_blue_dark);

        // Create material sheet FAB
        materialSheetFab = new MaterialSheetFab<>(fab, sheetView, overlay, sheetColor, fabColor);

        // Set material sheet event listener
        materialSheetFab.setEventListener(new MaterialSheetFabEventListener() {
            @Override
            public void onShowSheet() {
                // Save current status bar color
                statusBarColor = getStatusBarColor();
                // Set darker status bar color to match the dim overlay
                setStatusBarColor(getResources().getColor(R.color.caldroid_white));
            }

            @Override
            public void onHideSheet() {
                // Restore status bar color
                setStatusBarColor(statusBarColor);
            }
        });

        // Set material sheet item click listeners
        /*findViewById(R.id.fab_sheet_item_recording).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_reminder).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_photo).setOnClickListener(this);
        findViewById(R.id.fab_sheet_item_note).setOnClickListener(this);*/

    }


    @Override
    public void onBackPressed() {
        if (materialSheetFab.isSheetVisible()) {
            materialSheetFab.hideSheet();
        } else {
            super.onBackPressed();
        }
    }

    private int getStatusBarColor() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            return getWindow().getStatusBarColor();
        }
        return 0;
    }

    private void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(color);
        }
    }
}
