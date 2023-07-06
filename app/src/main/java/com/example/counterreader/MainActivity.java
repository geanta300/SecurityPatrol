package com.example.counterreader;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Camera;
import android.net.Uri;
import android.os.Bundle;
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

        if (scannedQRCode !=null && scannedQRCode.equals("111")){
            Toast.makeText(this, "maria are mere", Toast.LENGTH_SHORT).show();
        }

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
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            startActivity(intent);
        });
    }
}