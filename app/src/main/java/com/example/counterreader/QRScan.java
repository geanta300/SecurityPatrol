package com.example.counterreader;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.Result;

import me.dm7.barcodescanner.zxing.ZXingScannerView;

public class QRScan extends AppCompatActivity implements ZXingScannerView.ResultHandler{
    private static final int CAMERA_PERMISSION_REQUEST = 123;

    SharedPreferences sharedPreferences;
    private int backButtonPressCount = 0;

    private ZXingScannerView scannerView;

    Button flashButton,backToExport;
    boolean backToExportBoolean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scan_activity);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        backToExportBoolean = sharedPreferences.getBoolean("editData",backToExportBoolean);

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);
        } else {
            initScannerView();
        }

        flashButton = findViewById(R.id.flashLightButton);
        flashButton.setOnClickListener(v->{
            scannerView.toggleFlash();
        });

        if(backToExportBoolean){
            backToExport = findViewById(R.id.backToExportButt);
            backToExport.setVisibility(View.VISIBLE);
            backToExport.setOnClickListener(v -> {
                Intent intent = new Intent(this,PreviewExportPDFAndExcel.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("editData", false);
                editor.apply();
                startActivity(intent);
            });
        }
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
        if (backButtonPressCount == 0) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backButtonPressCount++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backButtonPressCount = 0;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
    @Override
    public void handleResult(Result result) {
        Intent intent = new Intent(QRScan.this, CameraActivity.class);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("scannedQRCode", result.toString());
        editor.apply();
        startActivity(intent);
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
                Toast.makeText(this, "Camera permission needed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}