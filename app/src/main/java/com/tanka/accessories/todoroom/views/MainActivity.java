package com.tanka.accessories.todoroom.views;

import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RemoteViews;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;
import com.tanka.accessories.todoroom.data.room.AppDataBase;
import com.tanka.accessories.todoroom.utility.Utils;
import com.tanka.accessories.todoroom.widget.NoteWidget;

import java.util.List;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter adapter;
    private AppDataBase db;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<Note> noteList;
    private Bitmap bmProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        db = AppDataBase.getDatabase(this);

        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                R.drawable.tanks_suited);
        bmProfile = Utils.getCircleBitmap(bm);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showAddDialog();
            }
        });
    }

    private void showAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);

        final EditText etTitle = dialog.findViewById(R.id.etTitle);
        final EditText etDate = dialog.findViewById(R.id.etDate);
        final EditText etBody = dialog.findViewById(R.id.etBody);
        final EditText etType = dialog.findViewById(R.id.etType);
        Button btnSend = dialog.findViewById(R.id.btnAdd);
        btnSend.setOnClickListener(v -> {
            final String title = etTitle.getText().toString();
            final String date = etDate.getText().toString();
            final String body = etBody.getText().toString();
            final String type = etType.getText().toString();
            if (isEmpty(title) || title.equalsIgnoreCase("")) {

            } else {

                addNote(title, date, body, type);
                dialog.dismiss();
            }

        });
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.CENTER;
        lp.dimAmount = 0.6f;
        dialog.getWindow().setAttributes(lp);
        dialog.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

    }

    private void addNote(String title, String body, String date, String type) {
        Note note = new Note();
        note.setTitle(title);
        note.setBody(body);
        note.setDate(date);
        note.setType(type);

        db.getNotesDao().insertAll(note);
        noteList.add(note);
        adapter.notifyDataSetChanged();
        updateWidget(noteList);

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
    }

    private void loadNotes() {
        new AsyncTask<Void, Void, List<Note>>() {
            @Override
            protected List doInBackground(Void... params) {
                return db.getNotesDao().getAllNotes();
            }

            @Override
            protected void onPostExecute(List notes) {
                noteList = notes;
                adapter = new NotesAdapter(MainActivity.this, noteList);
                recyclerView.setAdapter(adapter);


                updateWidget(noteList);

            }
        }.execute();
    }

    private void updateWidget(List<Note> noteList) {
        Context context = MainActivity.this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        ComponentName thisWidget = new ComponentName(context, NoteWidget.class);
        if (!noteList.isEmpty())
        remoteViews.setTextViewText(R.id.appwidget_text, noteList.get(0).getTitle());
        remoteViews.setImageViewBitmap(R.id.image_profile, bmProfile);
        appWidgetManager.updateAppWidget(thisWidget, remoteViews);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteNote(Note item) {
        db.getNotesDao().deleteAll(item);
        noteList.remove(item);
        adapter.notifyDataSetChanged();
        updateWidget(noteList);
    }
}
