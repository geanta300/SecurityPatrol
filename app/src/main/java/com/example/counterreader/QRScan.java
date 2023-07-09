package com.example.counterreader;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScan extends AppCompatActivity {

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan the QR code");
        options.setCaptureActivity(CameraZX.class);
        barcodeLauncher.launch(options);
    }

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
        result -> {
            if(result.getContents() != null) {
                //Toast.makeText(QRScan.this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(QRScan.this, CameraActivity.class);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("scannedQRCode", result.getContents());
                editor.apply();
                startActivity(intent);
            }
        });
}