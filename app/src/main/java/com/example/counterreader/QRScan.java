package com.example.counterreader;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

public class QRScan extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ScanOptions options = new ScanOptions();
        options.setPrompt("Scan a QR code");
        options.setCaptureActivity(CameraZX.class);
        barcodeLauncher.launch(options);
    }

    // Register the launcher and result handler
    private final ActivityResultLauncher<ScanOptions> barcodeLauncher = registerForActivityResult(new ScanContract(),
        result -> {
            if(result.getContents() == null) {
                Toast.makeText(QRScan.this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(QRScan.this, "Scanned: " + result.getContents(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(QRScan.this, MainActivity.class);
                intent.putExtra("QRCODE", result.getContents());
                startActivity(intent);
            }
        });
}