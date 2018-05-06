package com.tanka.accessories.todoroom.data.room;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.tanka.accessories.todoroom.data.model.Note;

/**
 * Created by access-tanka on 11/16/17.
 */

@Database(entities = {Note.class}, version = 2)
public abstract class AppDataBase extends RoomDatabase {

    public static final String DB_NAME = "note_db";

    private static AppDataBase INSTANCE;

    public static AppDataBase getDatabase(Context context) {
        if (INSTANCE == null) {
            INSTANCE =
                    Room.databaseBuilder(context.getApplicationContext(), AppDataBase.class, DB_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .allowMainThreadQueries().build();
        }
        return INSTANCE;
    }

    public abstract NoteDao getNotesDao();
    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
            // Since we didn't alter the table, there's nothing else to do here.
        }
    };



}

