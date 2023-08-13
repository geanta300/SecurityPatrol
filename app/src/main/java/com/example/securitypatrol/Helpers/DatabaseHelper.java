package com.example.securitypatrol.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.apache.xmlbeans.impl.store.Cur;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Security.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "security_patrol";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_USER_NAME = "user_name";
    public static final String COLUMN_DATATIME = "datatime";
    public static final String COLUMN_DESCRPIPTION = "description";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_NFC_TAG = "nfc_tag";
    public static final String COLUMN_SCANNED = "scanned";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER_NAME + " TEXT, " +
                COLUMN_DATATIME + " TEXT, " +
                COLUMN_DESCRPIPTION + " TEXT, " +
                COLUMN_IMAGE_URI + " TEXT, " +
                COLUMN_LOCATION + " TEXT, " +
                COLUMN_NFC_TAG + " TEXT, " +
                COLUMN_SCANNED + " INTEGER DEFAULT 0)";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_USER_NAME,
                COLUMN_DATATIME,
                COLUMN_DESCRPIPTION,
                COLUMN_IMAGE_URI,
                COLUMN_LOCATION,
                COLUMN_NFC_TAG,
                COLUMN_SCANNED};

        return db.query(TABLE_NAME, columns, null, null, null, null, null);
    }

    public int getRowCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        Log.d("getRowCount", String.valueOf(count));
        return count;
    }

    public int getScannedNFCCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + COLUMN_SCANNED + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{"1"});
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }
        Log.d("getScannedNFCCount", String.valueOf(count));
        return count;
    }


    public void addPhotoPath(int id, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_URI, imageUri);

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        db.update(TABLE_NAME, values, whereClause, whereArgs);
    }

    public void insertData(String description, String location, String nfc_tag) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_DESCRPIPTION, description);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_NFC_TAG, nfc_tag);

        db.insert(TABLE_NAME, null, values);
    }

    public Cursor getDataByNFC(String nfc) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_NFC_TAG + " = ?";
        String[] selectionArgs = {nfc};

        return db.query(
                TABLE_NAME,
                null,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
    }

    public Cursor getNFCTagsLeft() {
        SQLiteDatabase db = getReadableDatabase();

        String[] columns = {
                COLUMN_DESCRPIPTION,
                COLUMN_LOCATION
        };

        String selection = COLUMN_SCANNED + "=?";
        String[] selectionArgs = {"0"};

        return db.query(TABLE_NAME, columns, selection, selectionArgs, null, null, null);
    }

    public boolean qrCodeExists(String qrCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{COLUMN_ID},
                COLUMN_NFC_TAG + " = ?",
                new String[]{qrCode},
                null,
                null,
                null)
        ) {
            return cursor != null && cursor.moveToFirst();
        }
    }
}
