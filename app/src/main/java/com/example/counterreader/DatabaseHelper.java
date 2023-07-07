package com.example.counterreader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "CountersDB.db";
    public static final int DATABASE_VERSION = 1;
    public static final String TABLE_NAME = "contors";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CHIRIAS = "chirias";
    public static final String COLUMN_LOCATIE = "locatie";
    public static final String COLUMN_FEL_CONTOR = "fel_contor";
    public static final String COLUMN_SERIE = "serie";
    public static final String COLUMN_INDEX_VECHI = "index_vechi";
    public static final String COLUMN_INDEX_NOU = "index_nou";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_COD_QR = "cod_qr";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_CHIRIAS + " TEXT, " +
                COLUMN_LOCATIE + " TEXT, " +
                COLUMN_FEL_CONTOR + " TEXT, " +
                COLUMN_SERIE + " TEXT, " +
                COLUMN_INDEX_VECHI + " TEXT, " +
                COLUMN_INDEX_NOU + " TEXT, " +
                COLUMN_IMAGE_URI + " TEXT, " +
                COLUMN_COD_QR + " TEXT)";
        db.execSQL(createTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void addNewIndex(int id, int newIndex) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_INDEX_NOU, newIndex);

        String whereClause = COLUMN_ID + " = ?";
        String[] whereArgs = {String.valueOf(id)};

        int rowsAffected = db.update(TABLE_NAME, values, whereClause, whereArgs);

        // Check if the update was successful
        if (rowsAffected > 0) {
            // Update successful
            Log.println(Log.VERBOSE,"database","Update successful");
        } else {
            // Update failed
            Log.println(Log.VERBOSE,"database","Update failed");
        }

        db.close();
    }

    public void insertData(String chirias, String locatie, String felContor, String serie, double indexVechi, String codQR) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CHIRIAS, chirias);
        values.put(COLUMN_LOCATIE, locatie);
        values.put(COLUMN_FEL_CONTOR, felContor);
        values.put(COLUMN_SERIE, serie);
        values.put(COLUMN_INDEX_VECHI, indexVechi);
        values.put(COLUMN_COD_QR, codQR);

        db.insert(TABLE_NAME, null, values);
    }

    public Cursor getAllDataByQR(String qr){
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM "  + TABLE_NAME +" WHERE " + COLUMN_COD_QR + "= ?",new String[] { qr });
    }

    public Cursor getDataByQR(String qr) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_COD_QR + " = ?";
        String[] selectionArgs = { qr };

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

}
