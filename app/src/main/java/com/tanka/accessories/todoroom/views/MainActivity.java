package com.tanka.accessories.todoroom.views;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RemoteViews;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionMenu;
import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;
import com.tanka.accessories.todoroom.data.room.AppDataBase;
import com.tanka.accessories.todoroom.utility.Utils;
import com.tanka.accessories.todoroom.views.helper.OnStartDragListener;
import com.tanka.accessories.todoroom.views.helper.SimpleItemTouchHelperCallback;
import com.tanka.accessories.todoroom.widget.NoteWidget;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static android.text.TextUtils.isEmpty;

public class MainActivity extends AppCompatActivity implements OnStartDragListener {

    private NotesAdapter adapter;
    private AppDataBase db;
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private GridLayoutManager gridLayoutManager;
    private ItemTouchHelper mItemTouchHelper;
    private List<Note> noteList = new ArrayList<>();
    private Bitmap bmProfile;
    private static int RESULT_LOAD_IMAGE = 1;
    private Calendar reminderCal;
    private boolean isSearch, isGridLayout;
    private final String layoutFlag = "isLinear";
    SharedPreferences prefs;
    FloatingActionButton fabSearch;
    //    FabSpeedDial fabSpeedDial;
    FloatingActionMenu famOpt;
    com.github.clans.fab.FloatingActionButton fabNote, fabStory;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        prefs = this.getSharedPreferences(
                "com.tanka.accessories.todoroom", Context.MODE_PRIVATE);



        reminderCal = Calendar.getInstance();

        db = AppDataBase.getDatabase(this);

        Bitmap bm = BitmapFactory.decodeResource(getResources(),
                R.drawable.tanks_suited);
        bmProfile = Utils.getCircleBitmap(bm);

        recyclerView = findViewById(R.id.my_recycler_view);
        recyclerView.setHasFixedSize(true);

        recyclerView.setAdapter(adapter);

        layoutManager = new LinearLayoutManager(this);
        gridLayoutManager = new GridLayoutManager(this, 2);

        if (!prefs.getBoolean(layoutFlag, false))
            recyclerView.setLayoutManager(layoutManager);
        else
            recyclerView.setLayoutManager(gridLayoutManager);

        recyclerView.setItemAnimator(new DefaultItemAnimator());


        famOpt = findViewById(R.id.fabMenuOptions);
        fabNote = findViewById(R.id.fabNew);
        fabStory = findViewById(R.id.fabStory);
//        fabSpeedDial = findViewById(R.id.fabNew);
        fabSearch = findViewById(R.id.fabSearch);

        famOpt.setClosedOnTouchOutside(true);

        fabNote.setOnClickListener(v -> {
            famOpt.close(true);
            showAddDialog();
        });

        fabSearch.setOnClickListener((View view) -> {

            loadNotes();
            fabSearch.setVisibility(View.GONE);

        });
    }


    private void showAddDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);

        Calendar calendar = Calendar.getInstance();

        final EditText etTitle = dialog.findViewById(R.id.etTitle);
        final TextView tvDate = dialog.findViewById(R.id.tvDate);
        final TextView tvTime = dialog.findViewById(R.id.tvTime);
        final EditText etBody = dialog.findViewById(R.id.etBody);
        Button btnSend = dialog.findViewById(R.id.btnAdd);

        tvTime.setOnClickListener(v -> {

            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);


            TimePickerDialog mTimePicker;
            mTimePicker = new TimePickerDialog(MainActivity.this, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                    tvTime.setText(selectedHour + ":" + selectedMinute);

//                    reminderCal.add(Calendar.HOUR_OF_DAY,selectedHour);
//                    reminderCal.add(Calendar.MINUTE,selectedMinute);
                    reminderCal.add(Calendar.SECOND, 5);

                }
            }, hour, minute, true);//Yes 24 hour time
            mTimePicker.setTitle("Select Time");
            mTimePicker.show();

        });

        tvDate.setOnClickListener(v -> {
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            int month = calendar.get(Calendar.MONTH);
            int year = calendar.get(Calendar.YEAR);

            DatePickerDialog mDatePicker;
            mDatePicker = new DatePickerDialog(MainActivity.this, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int dayOfMonth) {
                    tvDate.setText(dayOfMonth + ":" + (selectedMonth + 1) + ":" + selectedYear);

//                    reminderCal.add(Calendar.YEAR,year);
//                    reminderCal.add(Calendar.MONTH,month);
//                    reminderCal.add(Calendar.DAY_OF_MONTH,dayOfMonth);
                }

            }, year, month, day);
            mDatePicker.setTitle("Select Date");
            mDatePicker.show();

        });
        btnSend.setOnClickListener(v -> {
            final String title = etTitle.getText().toString();
            final String date = tvDate.getText().toString();
            final String body = etBody.getText().toString();

            Intent intent = new Intent(this, AlarmReceiverActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this,
                    12345, intent, PendingIntent.FLAG_CANCEL_CURRENT);
            AlarmManager am =
                    (AlarmManager) getSystemService(Activity.ALARM_SERVICE);
            am.set(AlarmManager.RTC_WAKEUP, reminderCal.getTimeInMillis(),
                    pendingIntent);


            if (isEmpty(title) || title.equalsIgnoreCase("")) {

            } else {

                addNote(title, body, date, null);
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
        ImageButton btnSend = dialog.findViewById(R.id.btnAdd);
        btnSend.setOnClickListener(v -> {
            final String title = "%" + etTitle.getText().toString() + "%";
            if (isEmpty(title) || title.equalsIgnoreCase("%%")) {

                dialog.dismiss();
            } else {

                isSearch = true;
                searchNote(title);
                dialog.dismiss();
            }

        });
        dialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.copyFrom(dialog.getWindow().getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.TOP;
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
                adapter = new NotesAdapter(MainActivity.this, noteList, MainActivity.this);

                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                mItemTouchHelper = new ItemTouchHelper(callback);
                mItemTouchHelper.attachToRecyclerView(recyclerView);

                recyclerView.setAdapter(adapter);
                fabSearch.setVisibility(View.VISIBLE);

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
                adapter = new NotesAdapter(MainActivity.this, noteList, MainActivity.this);

                ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
                mItemTouchHelper = new ItemTouchHelper(callback);
                mItemTouchHelper.attachToRecyclerView(recyclerView);

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

                loadNotes();
                isSearch = false;

            } else {
                showSearchDialog();
            }
        } else if (id == R.id.change_layout) {

            if (!isGridLayout) {
                recyclerView.setLayoutManager(gridLayoutManager);
                recyclerView.animate();
                isGridLayout = true;
                prefs.edit().putBoolean(layoutFlag,isGridLayout);
            } else {
                recyclerView.setLayoutManager(layoutManager);
                recyclerView.animate();
                isGridLayout = false;
                prefs.edit().putBoolean(layoutFlag,isGridLayout);
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


    public void editNote(Note note) {

        showEditDialog(note);

    }

    private void showEditDialog(Note note) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_add_note);

        final EditText etTitle = dialog.findViewById(R.id.etTitle);
        final TextView tvDate = dialog.findViewById(R.id.tvDate);
        final EditText etBody = dialog.findViewById(R.id.etBody);
        Button btnSend = dialog.findViewById(R.id.btnAdd);

        etTitle.setText(note.getTitle());
        btnSend.setOnClickListener(v -> {
            final String title = etTitle.getText().toString();
            final String date = tvDate.getText().toString();
            final String body = etBody.getText().toString();
            if (isEmpty(title) || title.equalsIgnoreCase("")) {

            } else {

                Note note1 = new Note();
                note1.setId(note.getId());
                note1.setTitle(title);
                note1.setBody(body);
                note1.setDate(date);

                db.getNotesDao().updateAll(note1);
                loadNotes();

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

    @Override
    public void onStartDrag(RecyclerView.ViewHolder viewHolder) {
        mItemTouchHelper.startDrag(viewHolder);
    }
}
