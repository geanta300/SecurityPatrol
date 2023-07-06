package com.example.counterreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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

    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        counterAlreadyMade = findViewById(R.id.counterAlreadyMade);
        counterMax = findViewById(R.id.counterMax);
        imageView = findViewById(R.id.imageView);
        newIndex = findViewById(R.id.indexInput);
        scanButton = findViewById(R.id.scanButton);
        saveButton = findViewById(R.id.saveButton);
        exportButton = findViewById(R.id.exportButton);

        String imagePath = getIntent().getStringExtra("imagePath");
        String scannedQRCode = getIntent().getStringExtra("QRCODE");

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
        dbHelper = new DatabaseHelper(MainActivity.this);
        DatabaseInitialize(dbHelper);


        // Retrieve data by serie value
        Cursor cursor = dbHelper.getDataBySerie("72083310");

        // Check if the cursor contains any data
        if (cursor != null && cursor.moveToFirst()) {
            do {
                // Read the data from the cursor
                int id = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_ID));
                String chirias = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_CHIRIAS));
                String locatie = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_LOCATIE));
                String felContor = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_FEL_CONTOR));
                String serie = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_SERIE));
                double indexVechi = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_INDEX_VECHI));
                double indexNou = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_INDEX_NOU));

                // Perform any necessary operations with the data
                Log.d("MainActivity", "ID: " + id);
                Log.d("MainActivity", "Chirias: " + chirias);
                Log.d("MainActivity", "Locatie: " + locatie);
                Log.d("MainActivity", "Fel Contor: " + felContor);
                Log.d("MainActivity", "Serie: " + serie);
                Log.d("MainActivity", "Index Vechi: " + indexVechi);
                Log.d("MainActivity", "Index Nou: " + indexNou);

            } while (cursor.moveToNext());
        }

        // Close the cursor and database connection
        if (cursor != null) {
            cursor.close();
        }
        dbHelper.close();
    }

    public void DatabaseInitialize(DatabaseHelper dbHelper){
        SQLiteDatabase database;
        database = dbHelper.getWritableDatabase();

        // Perform search operations or other database operations here
        Cursor cursor = database.rawQuery("SELECT * FROM contors", null);
        // Process the cursor data or perform other operations
        cursor.close();
    }
}