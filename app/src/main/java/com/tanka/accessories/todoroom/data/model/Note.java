package com.tanka.accessories.todoroom.data.model;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import java.util.Date;

/**
 * Created by access-tanka on 11/16/17.
 */

@Entity
public class Note {

    public Note() {
    }

    public Note(String title, String date, String body, String type) {
        this.title = title;
        this.date = date;
        this.body = body;
        this.type = type;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    public String title;
    public String date;
    public String body;
    public String type;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
