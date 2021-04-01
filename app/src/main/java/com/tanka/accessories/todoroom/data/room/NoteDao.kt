package com.tanka.accessories.todoroom.data.room


import androidx.room.*
import com.tanka.accessories.todoroom.data.model.Note

/**
 * Created by access-tanka on 11/16/17.
 */
@Dao
interface NoteDao {
    /*
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<User>

    @Insert
    suspend fun insertAll(users: List<User>)

    @Delete
    suspend fun delete(user: User)*/
    @Insert
    fun insertAll(notes: List<Note>)

    @Update
    fun updateAll(vararg notes: Note?)

    @get:Query("SELECT * FROM note")
    val allNotes: List<Note>

    @Query("SELECT * FROM note where title LIKE :keyword")
    fun getNote(keyword: String?): List<Note?>?

    @Delete
    fun deleteAll(vararg notes: Note?)

    @Update
    fun editNote(note: Note?)

    @Query("UPDATE Note SET `title` = :txTitle, `body` = :txBody, `date` = :txDate, `type` = :txType   WHERE id = :tid")
    fun updateNote(tid: Long, txTitle: String?, txBody: String?, txDate: String?, txType: String?)

}