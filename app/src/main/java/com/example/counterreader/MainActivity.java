package com.example.counterreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    TextView counterAlreadyMade, counterMax;
    ImageView imageView;
    EditText newIndex;
    Button scanButton, saveButton, exportButton;

    DatabaseHelper myDB;
    Boolean firstTimeDB;

    SharedPreferences sharedPref;
    String scannedQRCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        firstTimeDB = sharedPref.getBoolean("firstTimeDB",false);
        scannedQRCode = sharedPref.getString("scannedQRCode",scannedQRCode);

        counterAlreadyMade = findViewById(R.id.counterAlreadyMade);
        counterMax = findViewById(R.id.counterMax);
        imageView = findViewById(R.id.imageView);
        newIndex = findViewById(R.id.indexInput);
        scanButton = findViewById(R.id.scanButton);
        saveButton = findViewById(R.id.saveButton);
        exportButton = findViewById(R.id.exportButton);

        createInitialDatabase();

        String imagePath = getIntent().getStringExtra("imagePath");

        if (imagePath != null) {
            imageView.setImageURI(Uri.parse(imagePath));
        }
        scanButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });
        saveButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, QRScan.class);
            startActivity(intent);
        });
        exportButton.setOnClickListener(v -> {

        });

        if(scannedQRCode != null){
            myDB = new DatabaseHelper(this);
            Cursor cursor = myDB.getDataByQR(scannedQRCode);

            // Process the cursor to read data from each column
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String chirias = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHIRIAS));
                    String locatie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
                    String felContor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEL_CONTOR));
                    String serie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERIE));
                    double indexVechi = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_VECHI));
                    int codQR = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COD_QR));

                    Log.d("MainActivity", "Chirias: " + chirias);
                    Log.d("MainActivity", "Locatie: " + locatie);
                    Log.d("MainActivity", "Fel Contor: " + felContor);
                    Log.d("MainActivity", "Serie: " + serie);
                    Log.d("MainActivity", "Index Vechi: " + indexVechi);
                    Log.d("MainActivity", "Cod QR: " + codQR);

                } while (cursor.moveToNext());
            }

            // Close the cursor and the database connection
            if (cursor != null) {
                cursor.close();
            }
            myDB.close();
        }


    }

    public void createInitialDatabase(){
        if(!firstTimeDB){
            myDB = new DatabaseHelper(MainActivity.this);
            myDB.insertData("CEC",                  "agentie",  "termic rece",  "72089956",             34.920,     "100001");
            myDB.insertData("CEC",                  "agentie",  "termic cald",  "72083110",             10.100,     "100002");
            myDB.insertData("CEC",                  "2",        "server termic","717661501 ET 2",       28.384,     "100003");
            myDB.insertData("CEC",                  "3",        "server termic","71706294 ET 3",        19.381,     "100004");
            myDB.insertData("CEC",                  "4",        "server termic","71706293 ET 4",        20.581,     "100005");
            myDB.insertData("CEC",                  "ET 4",     "apa",          "46004593",             679.152,    "100006");
            myDB.insertData("CEC",                  "ET 4",     "apa",          "46004614",             198.404,    "100007");
            myDB.insertData("Tabac",                "parter",   "termic rece",  "71761498",             18.377,     "100008");
            myDB.insertData("Tabac",                "parter",   "termic cald",  "71761497",             5.329,      "100009");
            myDB.insertData("Ted's",                "parter",   "termic rece",  "71761500",             33.173,     "100010");
            myDB.insertData("Ted's",                "parter",   "termic cald",  "71761499",             24.197,     "100011");
            myDB.insertData("Ted's",                "parter",   "apa",          "",                     439.354,    "100012");
            myDB.insertData("Roche",                "15",       "server",       "71925161",             55.722,     "100013");
            myDB.insertData("Roche",                "16",       "server",       "71925152",             53.900,     "100014");
            myDB.insertData("BAI SEVICIU",          "25",       "apa",          "",                     74.863,     "100015");
            myDB.insertData("Parcare",              "S1",       "electric",     "1019304131",           4212.100,   "100016");
            myDB.insertData("Parcare",              "S1",       "electric",     "1019304097",           137014.200, "100017");
            myDB.insertData("Parcare",              "S1",       "electric",     "1020115119",           3719.000,   "100018");
            myDB.insertData("Parcare pwc",          "",         "electric",     "121177079",            9218.500,   "100019");
            myDB.insertData("Parcare",              "S1",       "electric",     "1020115112",           5.329,      "100020");
            myDB.insertData("Parcare",              "S2",       "electric",     "1019464163",           5.329,      "100021");
            myDB.insertData("Spalatorie ATO",       "S3",       "electric",     "26-33010",             5.329,      "100022");
            myDB.insertData("Irigatii parcare 1",   "S1",       "apa",          "8ZRI1914765533",       5.329,      "100023");
            myDB.insertData("Irigatii parcare 2",   "S1",       "apa",          "8ZRI1915032202",       5.329,      "100024");
            myDB.insertData("Irigatii curte lumina","S2",       "apa",          "8ZRI1915032208",       5.329,      "100025");
            myDB.insertData("Vodafone",             "S1",       "electric",     "190100000662/P34S02",  30496.000,  "100026");


            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putBoolean("firstTimeDB", true);
            editor.apply();
        }
    }
}