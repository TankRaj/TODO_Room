package com.tanka.accessories.todoroom.views;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;

/**
 * Created by access-tanka on 12/28/17.
 */

public class NoteDetail extends AppCompatActivity {

     TextView title;
     TextView date;
     TextView body;
     TextView type;
     LinearLayout contentLayout;

     private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);

        Intent intent = getIntent();
        note = (Note) intent.getSerializableExtra("note");

        title = findViewById          (R.id.tvTitle);
        date = findViewById(R.id.tvDate);
        body = findViewById(R.id.tvBody);
        type = findViewById(R.id.tvType);
        contentLayout = findViewById(R.id.contentLayout);

        title.setText(note.getTitle());
        body.setText(note.getBody());
        date.setText(note.getDate());
        type.setText(note.getType());
    }
}
