package com.tanka.accessories.todoroom.data.room

import com.tanka.accessories.todoroom.data.model.Note

class DatabaseHelperImpl(private val appDatabase: AppDataBase) : DatabaseHelper {

    override suspend fun getUsers(): List<Note> = appDatabase.userDao().allNotes

    override suspend fun insertAll(users: List<Note>) = appDatabase.userDao().insertAll(users)

}

