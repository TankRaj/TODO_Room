package com.tanka.accessories.todoroom.data.room

import androidx.room.Database
import androidx.room.RoomDatabase
import com.tanka.accessories.todoroom.data.model.Note

/**
 * Created by access-tanka on 11/16/17.
 */
@Database(entities = [Note::class], version = 2)
abstract class AppDataBase : RoomDatabase() {

    abstract fun userDao(): NoteDao

}