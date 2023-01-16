package com.example.foresight.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//Class de gestion de la bd SQLite
public class ProfileDatabase extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "profile_database";
    public static final int DATABASE_VERSION = 1;

    public ProfileDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Creation de la bd SQLite
        //Init de la table profile
        String createTableQuery = "CREATE TABLE profile (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT, age INTEGER, size INTEGER, weight INTEGER, image_data BLOB);";
        db.execSQL(createTableQuery);

        ContentValues values = new ContentValues();
        values.put("name", "Hugo Picard");
        values.put("age", "18");
        values.put("size", "155");
        values.put("weight", "45");
        values.put("image_data", "");
        values.put("id", "1");
        db.insert("profile", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}