package com.tanka.accessories.todoroom.data.model

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import android.support.annotation.NonNull
import java.io.Serializable

/**
 * Created by access-tanka on 11/16/17.
 */

@Entity
class Note : Serializable {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "id")
    var id: Int = 0

    @ColumnInfo(name = "title")
    @NonNull
    lateinit var title: String

    @ColumnInfo(name = "date")
    @NonNull
    lateinit var date: String

    @ColumnInfo(name = "body")
    @NonNull
    lateinit var body: String

    @ColumnInfo(name = "type")
    lateinit var type: String
}
