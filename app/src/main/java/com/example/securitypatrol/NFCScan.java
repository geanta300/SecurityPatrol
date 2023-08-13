package com.example.securitypatrol;


import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.NFCTagsLeftAdapter;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;
import android.database.Cursor;

public class NFCScan extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static final int CAMERA_PERMISSION_REQUEST = 123;
    private final String adminPassword = "1234";

    SharedPreferences sharedPreferences;

    private ZXingScannerView scannerView;

    ImageView flashButton,adminButton,nfcTagsLeft;
    Button backToExport;

    DatabaseHelper databaseHelper;
    Cursor cursor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_scan_activity);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            initScannerView();
        }

        flashButton = findViewById(R.id.flashLightButton);
        flashButton.setOnClickListener(v-> scannerView.toggleFlash());

        backToExport = findViewById(R.id.backToExportButt);
        databaseHelper = new DatabaseHelper(this);
        int nfcTags = databaseHelper.getScannedNFCCount();
        int maxnfcTags= databaseHelper.getRowCount();
        if (nfcTags == maxnfcTags){
            backToExport.setVisibility(View.VISIBLE);
            backToExport.setOnClickListener(v -> {
                Intent intent = new Intent(this, PreviewExportData.class);
                startActivity(intent);
            });
        }

        adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(v -> openAdminDialog());

        nfcTagsLeft = findViewById(R.id.nfcTagsLeft);
        nfcTagsLeft.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.item_nfc_tags, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(popupView);

            RecyclerView recyclerView = popupView.findViewById(R.id.recyclerViewNfcTags);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            Cursor cursor = databaseHelper.getNFCTagsLeft();

            NFCTagsLeftAdapter itemAdapter = new NFCTagsLeftAdapter(cursor);
            recyclerView.setAdapter(itemAdapter);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(popupView)
                    .setTitle("Tag-uri de scanat:")
                    .setPositiveButton("OK", null)
                    .create();

            alertDialog.show();
        });
    }

    private void openAdminDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.admin_dialog_activity, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextPassword = dialogView.findViewById(R.id.editTextPassword);
        editTextPassword.requestFocus();

        dialogBuilder.setTitle("Introdu parola");
        dialogBuilder.setPositiveButton("Verifica", (dialog, whichButton) -> {
            // Check the entered password here
            String enteredPassword = editTextPassword.getText().toString();

            if (enteredPassword.equals(adminPassword)) {
                startActivity(new Intent(NFCScan.this, PreviewExportData.class));
                finish();
            } else {
                Toast.makeText(NFCScan.this, R.string.wrongPass, Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

    private void initScannerView() {
        scannerView = findViewById(R.id.zxscan);
        scannerView.setResultHandler(this);
        scannerView.startCamera();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (scannerView != null) {
            scannerView.setResultHandler(this);
            scannerView.startCamera();
        }
    }
    @Override
    public void onPause() {
        super.onPause();
        if (scannerView != null) {
            scannerView.stopCamera();
        }
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
    }

    @Override
    public void handleResult(Result result) {
        cursor = databaseHelper.getDataByNFC(String.valueOf(result));
        if (cursor != null && cursor.moveToFirst()) {
            String nfcScanned = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCANNED));
            cursor.close();
            if(nfcScanned.equals("0") || nfcScanned.isEmpty()){
                Intent intent = new Intent(NFCScan.this, CameraActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("scannedNFCTag", result.toString());
                editor.apply();
                startActivity(intent);
            }else{
                showConfirmationDialog("Acest tag a fost deja scanat, sigur doriti sa continuati?", result);
            }
        }else {
            Toast.makeText(this, R.string.nfcInvalid, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, NFCScan.class));
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initScannerView();
            } else {
                Toast.makeText(this, R.string.cameraPermissionNeeded, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showConfirmationDialog(String message, Object result) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.nfcScannedAlready)
                .setMessage(message)
                .setPositiveButton("OK", null)
                .setNegativeButton("Cancel", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {
                Intent intent = new Intent(NFCScan.this, CameraActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("scannedNFCTag", result.toString());
                editor.apply();
                alertDialog.dismiss();
                startActivity(intent);
            });

            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> startActivity(new Intent(this, NFCScan.class)));
        });

        alertDialog.show();
    }
}