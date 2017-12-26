package com.tanka.accessories.todoroom.views;

import android.app.Activity;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.Toast;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;
import com.tanka.accessories.todoroom.data.room.AppDataBase;
import com.tanka.accessories.todoroom.utility.Utils;
import com.tanka.accessories.todoroom.widget.NoteWidget;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity {

    private NotesAdapter adapter;
    private AppDataBase db;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private List<Note> noteList;
    private Bitmap bmProfile;
    private static int RESULT_LOAD_IMAGE = 1;
    private boolean isSearch;
    FloatingActionButton fab, fabSearch;

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

        fab = findViewById(R.id.fab);
        fabSearch = findViewById(R.id.fabSearch);
        fab.setOnClickListener(view -> showAddDialog());
        fabSearch.setOnClickListener((View view) -> {

            String textInfo = "Click on the toolbar search bitch!!!";

            showCustomToast(textInfo);

            fabSearch.setVisibility(View.GONE);

        });
    }

    public void showCustomToast(String textInfo) {
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.custom_toast_layout,
                findViewById(R.id.custom_toast_container));

        TextView text = layout.findViewById(R.id.text);
        text.setText(textInfo);

        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.setDuration(Toast.LENGTH_LONG);
        toast.setView(layout);
        toast.show();
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

    private void showSearchDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_search_note);

        final EditText etTitle = dialog.findViewById(R.id.etTitle);
        Button btnSend = dialog.findViewById(R.id.btnAdd);
        btnSend.setOnClickListener(v -> {
            final String title = "%" + etTitle.getText().toString() + "%";
            if (isEmpty(title) || title.equalsIgnoreCase("%%")) {

                dialog.dismiss();
            } else {

                isSearch = true;
                fabSearch.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_delete));
                searchNote(title);
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

    private void searchNote(String title) {
        new AsyncTask<Void, Void, List<Note>>() {
            @Override
            protected List doInBackground(Void... params) {
                return db.getNotesDao().getNote(title);
            }

            @Override
            protected void onPostExecute(List notes) {
                if (noteList != null)
                    noteList.clear();

                noteList = notes;
                adapter = new NotesAdapter(MainActivity.this, noteList);
                recyclerView.setAdapter(adapter);

            }
        }.execute();
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
        updateWidget(noteList, null);

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

                if (noteList != null)
                    noteList.clear();

                noteList = notes;
                adapter = new NotesAdapter(MainActivity.this, noteList);
                recyclerView.setAdapter(adapter);


                updateWidget(noteList, null);

            }
        }.execute();
    }

    private void updateWidget(List<Note> noteList, Bitmap bitmap) {
        Context context = MainActivity.this;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.note_widget);
        ComponentName thisWidget = new ComponentName(context, NoteWidget.class);
        if (!noteList.isEmpty())
            remoteViews.setTextViewText(R.id.appwidget_text, noteList.get(0).getTitle());
        if (bitmap == null) {
            remoteViews.setImageViewBitmap(R.id.image_profile, bmProfile);
        } else {
            remoteViews.setImageViewBitmap(R.id.image_profile, bitmap);
        }
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
        } else if (id == R.id.change_pp) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(intent, RESULT_LOAD_IMAGE);
        } else if (id == R.id.search_note) {
            if (isSearch) {

                fabSearch.setImageDrawable(ContextCompat.getDrawable(this, android.R.drawable.ic_menu_search));
                loadNotes();
                isSearch = false;
            } else {
                showSearchDialog();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void deleteNote(Note item) {
        db.getNotesDao().deleteAll(item);
        noteList.remove(item);
        adapter.notifyDataSetChanged();
        updateWidget(noteList, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Uri uri = null;
        if (requestCode == RESULT_LOAD_IMAGE) {
            if (resultCode == RESULT_OK) {
                if (data != null) {
                    Uri imageUri = data.getData();
                    Bitmap bitmap = null;
                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    loadToView(bitmap);

                } else {
                    Toast.makeText(getApplicationContext(), "Cancelled",
                            Toast.LENGTH_SHORT).show();
                }
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Cancelled",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadToView(Bitmap bitmap) {
        updateWidget(noteList, bitmap);
    }

    private String getRealPathFromUri(Uri tempUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = this.getContentResolver().query(tempUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    private Uri getImageUri(Activity youractivity, Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String path = MediaStore.Images.Media.insertImage(youractivity.getContentResolver(), bitmap, "Title", null);
        return Uri.parse(path);
    }


    public void editNote(Note note) {

        showEditDialog(note);

    }

    private void showEditDialog(Note note) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);

        final EditText etTitle = dialog.findViewById(R.id.etTitle);
        final EditText etDate = dialog.findViewById(R.id.etDate);
        final EditText etBody = dialog.findViewById(R.id.etBody);
        final EditText etType = dialog.findViewById(R.id.etType);
        Button btnSend = dialog.findViewById(R.id.btnAdd);

        etTitle.setText(note.getTitle());
        btnSend.setOnClickListener(v -> {
            final String title = etTitle.getText().toString();
            final String date = etDate.getText().toString();
            final String body = etBody.getText().toString();
            final String type = etType.getText().toString();
            if (isEmpty(title) || title.equalsIgnoreCase("")) {

            } else {

                updateNoteInDb(title, date, body, type);
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

    private void updateNoteInDb(String title, String date, String body, String type) {

        Note note = new Note(title,date,body,type);

        db.getNotesDao().editNote(note);


    }
}
