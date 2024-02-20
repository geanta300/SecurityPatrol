package com.example.securitypatrol.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.securitypatrol.Services.DatabaseStructure;

public class DatabaseHelper extends DatabaseStructure {

    public DatabaseHelper(Context context) {
        super(context);
    }

    // CHECK DATA

    public boolean NFCCodeExists(String NFCCode) {
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(
                TABLE_OBIECTIVE,
                new String[]{COLUMN_UNIQUE_ID},
                COLUMN_NFC_CODE + " = ?",
                new String[]{NFCCode},
                null,
                null,
                null)
        ) {
            return cursor != null && cursor.moveToFirst();
        }
    }

    /*-------------------------------------------------------------------------------------------*/

    // GET DATA
    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_OBIECTIVE +
                " LEFT JOIN " + TABLE_POMPIERI_IN_TURA +
                " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID +
                " = " + TABLE_POMPIERI_IN_TURA + "." + COLUMN_UNIQUE_ID +
                " LEFT JOIN " + TABLE_SCANAT +
                " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID +
                " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV +
                " LEFT JOIN " + TABLE_VERIFICARI +
                " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID +
                " = " + TABLE_VERIFICARI + "." + COLUMN_ID_OBIECTIV;

        return db.rawQuery(query, null);
    }

    public Cursor getDataByNFC(String nfcTag) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_OBIECTIVE +
                " LEFT JOIN " + TABLE_SCANAT +
                " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID +
                " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV +
                " WHERE " + TABLE_OBIECTIVE + "." + COLUMN_NFC_CODE + " = ?";

        String[] selectionArgs = {nfcTag};

        return db.rawQuery(query, selectionArgs);
    }

    public int getScannedNFCCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_SCANAT + " WHERE " + COLUMN_SCANAT + " = '1'";
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    public int getCountOfObjectives() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_OBIECTIVE;
        Cursor cursor = db.rawQuery(query, null);
        int count = 0;

        if (cursor != null && cursor.moveToFirst()) {
            count = cursor.getInt(0);
            cursor.close();
        }

        return count;
    }

    public Cursor getNFCTagsLeft() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_OBIECTIVE +
                " LEFT JOIN " + TABLE_SCANAT +
                " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID +
                " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV +
                " WHERE " + TABLE_SCANAT + "." + COLUMN_SCANAT + " = '0'";

        return db.rawQuery(query, null);
    }


    public String[] getUserNames() {
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT " + COLUMN_NUME_POMPIER + " FROM " + TABLE_POMPIERI_IN_TURA;
        Cursor cursor = db.rawQuery(query, null);

        String[] userNames;

        if (cursor != null) {
            int columnIndex = cursor.getColumnIndex(COLUMN_NUME_POMPIER);

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

    /*-------------------------------------------------------------------------------------------*/

    // INSERT DATA

    public void insertObiectiv(String descriere, String locatie, int nfcCode) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Insert into Obiective table
        ContentValues obiectivValues = new ContentValues();
        obiectivValues.put(COLUMN_DESCRIERE_OBIECTIV, descriere);
        obiectivValues.put(COLUMN_LOCATIE, locatie);
        obiectivValues.put(COLUMN_NFC_CODE, nfcCode);
        long obiectivId = db.insert(TABLE_OBIECTIVE, null, obiectivValues);

        // If the insertion into Obiective was successful, insert into Scanat table
        if (obiectivId != -1) {
            ContentValues scanatValues = new ContentValues();
            scanatValues.put(COLUMN_ID_OBIECTIV, obiectivId);

            db.insert(TABLE_SCANAT, null, scanatValues);
        }
    }

    public void insertVerificare(String descriereVerificari, int idObiectiv, int tipVerificare) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues verificareValues = new ContentValues();
        verificareValues.put(COLUMN_DESCRIERE_VERIFICARI, descriereVerificari);
        verificareValues.put(COLUMN_ID_OBIECTIV, idObiectiv);
        verificareValues.put(COLUMN_TIP_VERIFICARE, tipVerificare);

        db.insert(TABLE_VERIFICARI, null, verificareValues);
        db.close();
    }

    public void addUser(String username) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NUME_POMPIER, username);
        db.insert(TABLE_POMPIERI_IN_TURA, null, values);
        db.close();
    }

    public void addPhotoPath(int id, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_URI, imageUri);

        String whereClause = COLUMN_UNIQUE_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        db.update(TABLE_PHOTOS_URIS, values, whereClause, whereArgs);
    }

    public void addScannedData(int id, int scanned, String data) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_SCANAT, scanned);
        values.put(COLUMN_DTIME, data);

        String whereClause = COLUMN_UNIQUE_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        db.update(TABLE_SCANAT, values, whereClause, whereArgs);
    }

    /*-------------------------------------------------------------------------------------------*/
}
