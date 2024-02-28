package com.example.securitypatrol.Helpers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.example.securitypatrol.Models.ObjectiveModel;
import com.example.securitypatrol.Models.ScanatModel;
import com.example.securitypatrol.Models.VerificationModel;
import com.example.securitypatrol.Services.DatabaseStructure;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends DatabaseStructure {

    public DatabaseHelper(Context context) {
        super(context);
    }

    // GET DATA
    public Cursor getAllData() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_OBIECTIVE + " LEFT JOIN " + TABLE_POMPIERI_IN_TURA + " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID + " = " + TABLE_POMPIERI_IN_TURA + "." + COLUMN_UNIQUE_ID + " LEFT JOIN " + TABLE_SCANAT + " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID + " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV + " LEFT JOIN " + TABLE_VERIFICARI + " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID + " = " + TABLE_VERIFICARI + "." + COLUMN_ID_OBIECTIV;

        return db.rawQuery(query, null);
    }

    public Cursor getDataByNFC(String nfcTag) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_OBIECTIVE + " LEFT JOIN " + TABLE_SCANAT + " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID + " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV + " WHERE " + TABLE_OBIECTIVE + "." + COLUMN_NFC_CODE + " = ?";

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

    public Cursor getAllVerificariWithObjectiveIDForPreview(int objectiveId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_DESCRIERE_VERIFICARI + ", " + COLUMN_TIP_VERIFICARE + " FROM " + TABLE_VERIFICARI + " WHERE " + COLUMN_ID_OBIECTIV + " = ?";
        return db.rawQuery(query, new String[]{String.valueOf(objectiveId)});
    }

    public List<ObjectiveModel> getAllObjectives() {
        List<ObjectiveModel> objectives = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_OBIECTIVE, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                ObjectiveModel objective = new ObjectiveModel();
                objective.setUniqueId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
                objective.setDescriere(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIERE_OBIECTIV)));
                objective.setLocatie(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATIE)));
                objectives.add(objective);
            }
            cursor.close();
        }
        return objectives;
    }

    public List<VerificationModel> getVerificationsByObjectiveId(int objectiveId) {
        List<VerificationModel> verifications = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_VERIFICARI + " WHERE " + COLUMN_ID_OBIECTIV + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(objectiveId)});

        if (cursor != null) {
            while (cursor.moveToNext()) {
                VerificationModel verification = new VerificationModel();
                verification.setUniqueId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
                verification.setDescriereVerificare(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIERE_VERIFICARI)));
                verification.setRaspunsVerificare(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RASPUNS_VERIFICARE)));

                verifications.add(verification);
            }
            cursor.close();
        }
        return verifications;
    }

    public ScanatModel getAllScansData(int objectiveId) {
        ScanatModel scanatModel = new ScanatModel();
        SQLiteDatabase db = getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_SCANAT + " WHERE " + COLUMN_ID_OBIECTIV + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(objectiveId)});
        if (cursor != null) {
            while (cursor.moveToNext()) {
                scanatModel.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID)));
                scanatModel.setDataTime(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DTIME)));

            }
            cursor.close();
        }
        return scanatModel;
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

        String query = "SELECT * FROM " + TABLE_OBIECTIVE + " LEFT JOIN " + TABLE_SCANAT + " ON " + TABLE_OBIECTIVE + "." + COLUMN_UNIQUE_ID + " = " + TABLE_SCANAT + "." + COLUMN_ID_OBIECTIV + " WHERE " + TABLE_SCANAT + "." + COLUMN_SCANAT + " = '0'";

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

    public List<Integer> getVerificationIDs(int objectiveID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_UNIQUE_ID + " FROM " + TABLE_VERIFICARI + " WHERE " + COLUMN_ID_OBIECTIV + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(objectiveID)});

        List<Integer> verificationIDs = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_UNIQUE_ID));
                verificationIDs.add(id);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return verificationIDs;
    }

    public Boolean getIfPhotoVerficationExists(int objectiveID, int photoID) {
        boolean exists = false;
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PHOTOBUTTON_ID + " FROM " + TABLE_PHOTOS_URIS + " WHERE " + COLUMN_ID_OBIECTIV + " = ? AND " + COLUMN_PHOTOBUTTON_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(objectiveID), String.valueOf(photoID)});

        if (cursor != null && cursor.moveToFirst()) {
            exists = true;
            cursor.close();
        }

        return exists;
    }

    public List<String> getPhotoUris(int objectiveID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COLUMN_PHOTO_URI + " FROM " + TABLE_PHOTOS_URIS + " WHERE " + COLUMN_ID_OBIECTIV + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(objectiveID)});

        List<String> photoUris = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String uri = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHOTO_URI));
                photoUris.add(uri);
            } while (cursor.moveToNext());

            cursor.close();
        }
        return photoUris;
    }

    /*-------------------------------------------------------------------------------------------*/

    // INSERT DATA

    public void verificationDataToDatabase(int verificationID, String answer) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_RASPUNS_VERIFICARE, answer);
        String whereClause = COLUMN_UNIQUE_ID + " = ?";
        String[] whereArgs = {String.valueOf(verificationID)};

        db.update(TABLE_VERIFICARI, values, whereClause, whereArgs);
    }

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

    public void updatePhotoPath(int objectiveID, String imageUri, String photoButtonID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_URI, imageUri);

        String whereClause = COLUMN_ID_OBIECTIV + " = ? AND " + COLUMN_PHOTOBUTTON_ID + " = ?";
        String[] whereArgs = {String.valueOf(objectiveID), photoButtonID};

        db.update(TABLE_PHOTOS_URIS, values, whereClause, whereArgs);

        db.close();
    }

    public void addPhotoPath(int objectiveID, String imageUri, String photoButtonID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_PHOTO_URI, imageUri);
        values.put(COLUMN_ID_OBIECTIV, objectiveID);
        values.put(COLUMN_PHOTOBUTTON_ID, photoButtonID);
        db.insert(TABLE_PHOTOS_URIS, null, values);
        db.close();
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

    // DELETE/RESET DATA

    public void deletePhotosPath() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTOS_URIS, null, null);
        db.close();
    }

    public void resetScannedData() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_SCANAT, 0);
        values.put(COLUMN_DTIME, "99:99:99");

        db.update(TABLE_SCANAT, values, null, null);
        db.close();
    }

    public void resetRaspunsuriVerificari() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_RASPUNS_VERIFICARE, "");

        db.update(TABLE_VERIFICARI, values, null, null);
        db.close();
    }

}
