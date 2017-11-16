package com.tanka.accessories.todoroom.utility;

import android.arch.persistence.room.TypeConverter;

import java.util.Date;

/**
 * Created by access-tanka on 11/16/17.
 */

class DateConverter {

    @TypeConverter
    public static Date toDate(Long timestamp) {
        return timestamp == null ? null : new Date(timestamp);
    }

    @TypeConverter
    public static Long toTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
