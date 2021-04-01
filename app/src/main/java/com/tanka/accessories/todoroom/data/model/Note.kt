package com.tanka.accessories.todoroom.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
public data class Note(
        @PrimaryKey val id: Int,
        @ColumnInfo(name = "title") val title: String?,
        @ColumnInfo(name = "date") val date: String?,
        @ColumnInfo(name = "body") val body: String?,
        @ColumnInfo(name = "type") val type: String?
)
