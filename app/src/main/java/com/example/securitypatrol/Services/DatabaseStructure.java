package com.example.securitypatrol.Services;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseStructure extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "patrol_db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_POMPIERI_IN_TURA = "Pompieri_in_tura";
    public static final String TABLE_SCANAT = "Scanat";
    public static final String TABLE_VERIFICARI = "Verificari";
    public static final String TABLE_OBIECTIVE = "Obiective";
    public static final String TABLE_SECURITY_PATROL = "Security_Patrol";
    public static final String TABLE_PHOTOS_URIS = "Photos_URIS";

    // Common columns
    public static final String COLUMN_UNIQUE_ID = "UniqueID";
    public static final String COLUMN_NUME_POMPIER = "Nume_Pompier";
    public static final String COLUMN_SCANAT = "Scanat";
    public static final String COLUMN_DTIME = "Dtime";
    public static final String COLUMN_ID_OBIECTIV = "ID_obiectiv";
    public static final String COLUMN_DESCRIERE_VERIFICARI = "Descriere";
    public static final String COLUMN_DESCRIERE_OBIECTIV = "Descriere_obiectiv";
    public static final String COLUMN_LOCATIE = "Locatie";
    public static final String COLUMN_NFC_CODE = "NFC_code";
    public static final String COLUMN_UNIQUE_ID_SECURITY_PATROL = "Unique_ID";
    public static final String COLUMN_ID_POMPIER = "ID_Pompier";
    public static final String COLUMN_ID_OBIECTIVE_SECURITY_PATROL = "ID_Obiective";
    public static final String COLUMN_PHOTO_URI = "Photo_URI";
    public static final String COLUMN_SECURITY_PATROL_ID = "Security_Patrol_ID";

    // Create table statements
    private static final String CREATE_TABLE_POMPIERI_IN_TURA =
            "CREATE TABLE " + TABLE_POMPIERI_IN_TURA + " (" +
                    COLUMN_UNIQUE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_NUME_POMPIER + " VARCHAR(255))";

    private static final String CREATE_TABLE_SCANAT =
            "CREATE TABLE " + TABLE_SCANAT + " (" +
                    COLUMN_UNIQUE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_SCANAT + " VARCHAR(255) DEFAULT '0', " +
                    COLUMN_DTIME + " VARCHAR(255), " +
                    COLUMN_ID_OBIECTIV + " INT, " +
                    "FOREIGN KEY (" + COLUMN_ID_OBIECTIV + ") REFERENCES " +
                    TABLE_OBIECTIVE + "(" + COLUMN_UNIQUE_ID + "))";

    private static final String CREATE_TABLE_VERIFICARI =
            "CREATE TABLE " + TABLE_VERIFICARI + " (" +
                    COLUMN_UNIQUE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_DESCRIERE_VERIFICARI + " VARCHAR(255), " +
                    COLUMN_ID_OBIECTIV + " INT, " +
                    "FOREIGN KEY (" + COLUMN_ID_OBIECTIV + ") REFERENCES " +
                    TABLE_OBIECTIVE + "(" + COLUMN_UNIQUE_ID + "))";

    private static final String CREATE_TABLE_OBIECTIVE =
            "CREATE TABLE " + TABLE_OBIECTIVE + " (" +
                    COLUMN_UNIQUE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_DESCRIERE_OBIECTIV + " VARCHAR(255), " +
                    COLUMN_LOCATIE + " VARCHAR(255), " +
                    COLUMN_NFC_CODE + " VARCHAR(255))";

    private static final String CREATE_TABLE_SECURITY_PATROL =
            "CREATE TABLE " + TABLE_SECURITY_PATROL + " (" +
                    COLUMN_UNIQUE_ID_SECURITY_PATROL + " INTEGER PRIMARY KEY, " +
                    COLUMN_ID_POMPIER + " INT, " +
                    COLUMN_ID_OBIECTIVE_SECURITY_PATROL + " INT, " +
                    "FOREIGN KEY (" + COLUMN_ID_POMPIER + ") REFERENCES " +
                    TABLE_POMPIERI_IN_TURA + "(" + COLUMN_UNIQUE_ID + "), " +
                    "FOREIGN KEY (" + COLUMN_ID_OBIECTIVE_SECURITY_PATROL + ") REFERENCES " +
                    TABLE_OBIECTIVE + "(" + COLUMN_UNIQUE_ID + "))";

    private static final String CREATE_TABLE_PHOTOS_URIS =
            "CREATE TABLE " + TABLE_PHOTOS_URIS + " (" +
                    COLUMN_UNIQUE_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_PHOTO_URI + " VARCHAR(255), " +
                    COLUMN_SECURITY_PATROL_ID + " INT, " +
                    "FOREIGN KEY (" + COLUMN_SECURITY_PATROL_ID + ") REFERENCES " +
                    TABLE_SECURITY_PATROL + "(" + COLUMN_UNIQUE_ID_SECURITY_PATROL + "))";


    public DatabaseStructure(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_POMPIERI_IN_TURA);
        db.execSQL(CREATE_TABLE_SCANAT);
        db.execSQL(CREATE_TABLE_VERIFICARI);
        db.execSQL(CREATE_TABLE_OBIECTIVE);
        db.execSQL(CREATE_TABLE_SECURITY_PATROL);
        db.execSQL(CREATE_TABLE_PHOTOS_URIS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_POMPIERI_IN_TURA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCANAT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VERIFICARI);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_OBIECTIVE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SECURITY_PATROL);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTOS_URIS);
        onCreate(db);
    }
}
