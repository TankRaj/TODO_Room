package com.tanka.accessories.todoroom.views.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.tanka.accessories.todoroom.R;
import com.tanka.accessories.todoroom.data.model.Note;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of App Widget functionality.
 */
public class NoteWidgetActivity extends AppWidgetProvider {

    private static List<Note> notes;
    private static CharSequence widgetText;
    private static RemoteViews views;

    public static void setNotesOnWidgetList(List<Note> data) {
        NoteWidgetActivity.notes = data;
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        if (notes != null) {
            widgetText = notes.get(0).getTitle();
        }
        // Construct the RemoteViews object
        views = new RemoteViews(context.getPackageName(), R.layout.note_widget_activity);
        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Construct an Intent object includes web adresss.
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://erenutku.com"));
        // In widget we are not allowing to use intents as usually. We have to use PendingIntent instead of 'startActivity'
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)) {
            int[] ids = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
//            String title = intent.getStringExtra("noteTitle");
//            views.setTextViewText(R.id.appwidget_text, title);

            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

