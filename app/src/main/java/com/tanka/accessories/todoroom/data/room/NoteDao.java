package com.tanka.accessories.todoroom.data.room;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.tanka.accessories.todoroom.data.model.Note;

import java.util.List;

/**
 * Created by access-tanka on 11/16/17.
 */

@Dao
public interface NoteDao {

    @Insert
    void insertAll(Note... notes);

    @Update
    void updateAll(Note... notes);

    @Query("SELECT * FROM note")
    List<Note> getAllNotes();

    @Delete
    void deleteAll(Note... notes);
}
