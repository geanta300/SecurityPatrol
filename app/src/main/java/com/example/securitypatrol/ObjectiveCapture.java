package com.example.securitypatrol;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.ModularCameraActivity;

public class ObjectiveCapture extends AppCompatActivity {

    String scannedNFCTag;
    SharedPreferences sharedPref;
    ModularCameraActivity modularCameraActivity = new ModularCameraActivity();

    private ImageView takePhoto;
    private ImageView blitz;
    private PreviewView previewView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_objective_capture);

        previewView = findViewById(R.id.cameraPreview);
        takePhoto = findViewById(R.id.takePhoto);
        blitz = findViewById(R.id.blitz);

        boolean isTagDetailsShown = getIntent().getBooleanExtra("isTagDetailsShown", false);
        if (isTagDetailsShown) {

            sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
            scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);
            displayDataInTextView(scannedNFCTag);
        }
        modularCameraActivity.startCamera(modularCameraActivity.cameraFacing, takePhoto, blitz, previewView,false,null, this);

    }


    @Override
    protected void onResume() {
        super.onResume();
        modularCameraActivity.startCamera(modularCameraActivity.cameraFacing, takePhoto, blitz, previewView,false,null, this);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (modularCameraActivity.cameraProvider != null) {
            modularCameraActivity.cameraProvider.unbindAll();
        }
    }

    public void displayDataInTextView(String nfcCode) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getDataByNFC(nfcCode);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder displayText = new StringBuilder();

            String datatime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DTIME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));

            displayText.append("Descriere: ").append(description).append("\n");
            displayText.append("Locatie: ").append(location).append("\n");

//            if( optionalComment != null && !optionalComment.isEmpty() ) {
//                displayText.append("Comentariu: ").append(optionalComment).append("\n");
//            }
            if (datatime != null && !datatime.isEmpty()) {
                displayText.append("Data si ora: ").append(datatime).append("\n");
            }

            TextView nfcTextView = findViewById(R.id.nfcTextView);
            nfcTextView.setText(displayText.toString());

            cursor.close();
        }
    }
}