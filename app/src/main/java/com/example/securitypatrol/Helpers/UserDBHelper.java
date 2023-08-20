package com.example.securitypatrol.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.securitypatrol.Models.UserModel;

public class UserDBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "user_database";
    private static final int DATABASE_VERSION = 1;
    private static final String TABLE_NAME = "name";

    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_UNIQUECODE = "unique_code";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
            COLUMN_USERNAME + " TEXT," +
            COLUMN_UNIQUECODE + " TEXT" + ")";

    public UserDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_USERS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addUser(UserModel user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, user.getUsername());
        values.put(COLUMN_UNIQUECODE, user.getUniqueCode());
        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public UserModel getUser(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        UserModel user = null;

        try {
            cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_USERNAME, COLUMN_UNIQUECODE}, COLUMN_USERNAME + "=?", new String[]{username}, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                user = new UserModel(cursor.getString(1), cursor.getString(2));
            }
        } catch (Exception e) {
            // Handle exceptions if needed
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        }

        return user;
    }


    public String[] getUserNames() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_USERNAME + " FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        String[] userNames;

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(COLUMN_USERNAME);

            if (columnIndex >= 0 && cursor.moveToFirst()) {
                userNames = new String[cursor.getCount()];
                int index = 0;
                do {
                    userNames[index++] = cursor.getString(columnIndex);
                } while (cursor.moveToNext());
            } else {
                userNames = new String[0];
            }

            cursor.close();
        } else {
            userNames = new String[0];
        }

        db.close();
        return userNames;
    }

}
