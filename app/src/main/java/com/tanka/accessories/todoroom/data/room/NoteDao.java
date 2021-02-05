package com.tanka.accessories.todoroom.data.room;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

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

    @Query("SELECT * FROM note where title LIKE :keyword")
    List<Note> getNote(String keyword);

    @Delete
    void deleteAll(Note... notes);

    @Update
    void editNote(Note note);

    @Query("UPDATE Note SET `title` = :txTitle, `body` = :txBody, `date` = :txDate, `type` = :txType   WHERE id = :tid")
    void updateNote(long tid, String txTitle, String txBody, String txDate, String txType);
}
