package com.example.ds.daily_memo_reader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by DS on 2/1/2016.
 */
public class EntriesDatabase extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "daiy_memo_reader.db";
    private static final int DATABASE_VERSION = 1;

    public EntriesDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + EntriesProvider.Tables.ENTRIES + " ("
                + EntriesContract.EntriesColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + EntriesContract.EntriesColumns.ENTRY_ID + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.TITLE + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.AUTHOR + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.BODY + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.THUMB_URL + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.PHOTO_URL + " TEXT NOT NULL,"
                + EntriesContract.EntriesColumns.PUBLISHED_DATE + " INTEGER NOT NULL DEFAULT 0,"
                + EntriesContract.EntriesColumns.FAVORITE + " INTEGER NOT NULL DEFAULT 0," +
                "UNIQUE (" + EntriesContract.EntriesColumns.ENTRY_ID + ") ON CONFLICT IGNORE"
                + ")" );

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + EntriesProvider.Tables.ENTRIES);
        onCreate(db);
    }
}
