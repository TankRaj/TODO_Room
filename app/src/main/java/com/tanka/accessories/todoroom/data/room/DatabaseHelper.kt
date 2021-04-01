package com.tanka.accessories.todoroom.data.room

import com.tanka.accessories.todoroom.data.model.Note


interface DatabaseHelper {

    suspend fun getUsers(): List<Note>

    suspend fun insertAll(users: List<Note>)

}