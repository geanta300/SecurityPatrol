package com.example.counterreader;


import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

    ImageView flashButton;
    Button backToExport;
    boolean backToExportBoolean = false;
    Boolean firstTimeDB;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.qr_scan_activity);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        firstTimeDB = sharedPreferences.getBoolean("firstTimeDB", false);
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

        databaseHelper = new DatabaseHelper(this);
        createInitialDatabase();
        int counters = databaseHelper.getIndexesHigherThanZero();
        int maxCounters= databaseHelper.getRowCount();
        if (counters == maxCounters){
            backToExportBoolean=true;
        }
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

    public void createInitialDatabase() {
        if (!firstTimeDB) {
            databaseHelper.insertData("CEC",                      "agentie",          "termic rece",      "72089956",             34.920,     "100001");
            databaseHelper.insertData("CEC",                      "agentie",          "termic cald",      "72083110",             10.100,     "100002");
            databaseHelper.insertData("CEC",                      "2",                "server termic",    "717661501 ET 2",       28.384,     "100003");
            databaseHelper.insertData("CEC",                      "3",                "server termic",    "71706294 ET 3",        19.381,     "100004");
            databaseHelper.insertData("CEC",                      "4",                "server termic",    "71706293 ET 4",        20.581,     "100005");
            databaseHelper.insertData("CEC",                      "ET 4",             "apa",              "46004593",             679.152,    "100006");
            databaseHelper.insertData("CEC",                      "ET 4",             "apa",              "46004614",             198.404,    "100007");
            databaseHelper.insertData("Tabac",                    "parter",           "termic rece",      "71761498",             18.377,     "100008");
            databaseHelper.insertData("Tabac",                    "parter",           "termic cald",      "71761497",             5.329,      "100009");
            databaseHelper.insertData("Ted's",                    "parter",           "termic rece",      "71761500",             33.173,     "100010");
            databaseHelper.insertData("Ted's",                    "parter",           "termic cald",      "71761499",             24.197,     "100011");
            databaseHelper.insertData("Ted's",                    "parter",           "apa",              "",                     439.354,    "100012");
            databaseHelper.insertData("Roche",                    "15",               "server",           "71925161",             55.722,     "100013");
            databaseHelper.insertData("Roche",                    "16",               "server",           "71925152",             53.900,     "100014");
            databaseHelper.insertData("BAI SEVICIU",              "25",               "apa",              "",                     74.863,     "100015");
            databaseHelper.insertData("Parcare",                  "S1",               "electric",         "1019304131",           4212.100,   "100016");
            databaseHelper.insertData("Parcare",                  "S1",               "electric",         "1019304097",           137014.200, "100017");
            databaseHelper.insertData("Parcare",                  "S1",               "electric",         "1020115119",           3719.000,   "100018");
            databaseHelper.insertData("Parcare pwc",              "",                 "electric",         "121177079",            9218.500,   "100019");
            databaseHelper.insertData("Parcare",                  "S1",               "electric",         "1020115112",           85056.100,  "100020");
            databaseHelper.insertData("Parcare",                  "S2",               "electric",         "1019464163",           68722.100,  "100021");
            databaseHelper.insertData("Spalatorie ATO",           "S3",               "electric",         "26-33010",             13162.910,  "100022");
            databaseHelper.insertData("Irigatii parcare 1",       "S1",               "apa",              "8ZRI1914765533",       8241.000,   "100023");
            databaseHelper.insertData("Irigatii parcare 2",       "S1",               "apa",              "8ZRI1915032202",       1682.000,   "100024");
            databaseHelper.insertData("Irigatii curte lumina",    "S2",               "apa",              "8ZRI1915032208",       40.000,     "100025");
            databaseHelper.insertData("Vodafone",                 "S1",               "electric",         "190100000662/P34S02",  30496.000,  "100026");
            databaseHelper.insertData("orange",                   "S3",               "electric",         "0120/SGS0217",         28556.520,  "100027");
            databaseHelper.insertData("MSD server",               "5",                "termic rece",      "71706295",             37.972,     "100028");
            databaseHelper.insertData("Contor parter baie",       "parter",           "apa",              "50007705",             172.487,    "100029");
            databaseHelper.insertData("AV8 Restaurant",           "Et 25",            "gaz",              "5551009/2020",         2788.477,   "100030");
            databaseHelper.insertData("Roche",                    "parter",           "apa",              "39800961",             15.990,     "100031");
            databaseHelper.insertData("Roche",                    "parter",           "termic rece",      "71925154",             0.580,      "100032");
            databaseHelper.insertData("Roche",                    "parter",           "termic rece",      "71925157",             34.644,     "100033");
            databaseHelper.insertData("Roche",                    "parter",           "termic rece",      "71925158",             4.967,      "100034");
            databaseHelper.insertData("Roche",                    "parter",           "termic cald",      "71925153",             48.616,     "100035");
            databaseHelper.insertData("Roche",                    "parter",           "termic cald",      "71925160",             48.117,     "100036");
            databaseHelper.insertData("Roche",                    "parter",           "termic cald",      "71925159",             25.418,     "100037");
            databaseHelper.insertData("P29 ato",                  "parter",           "apa",              "49008663",             18.794,     "100038");
            databaseHelper.insertData("P29 ato",                  "parter",           "termic cald",      "71925156",             2.551,      "100039");
            databaseHelper.insertData("P29 ato",                  "parter",           "termic rece",      "71925155",             5.466,      "100040");
            databaseHelper.insertData("vestiare mentenanta",      "",                 "apa",              "EZRI0250007721",       202.684,    "100041");
            databaseHelper.insertData("vestiar PWC",              "S2",               "electric",         "",                     2720.410,   "100042");
            databaseHelper.insertData("Contor -2 MSD",            "S2",               "electric",         "",                     0,          "100043");
            databaseHelper.insertData("Contor -3 Roche",          "S3",               "electric",         "521236105",            1125,       "100044");
            databaseHelper.insertData("Contor 26 MSD",            "26",               "electric",         "DDS6788",              789.6,      "100045");
            databaseHelper.insertData("Contor E-infra",           "S2",               "electric",         "EN50470-3",            864.75,     "100046");
            databaseHelper.insertData("Contor general de gaz",    "mecanic [m3]",     "gaz",              "3403401178/2017",      380795.92,  "100047");
            databaseHelper.insertData("Contor general de gaz",    "electronic [m3]",  "gaz",              "Corus / Itron",        380846.1,   "100048");
            databaseHelper.insertData("Contor general de gaz",    "convertit [Nm3]",  "gaz",              "Corus / Itron",        455644.053, "100049");

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeDB", true);
            editor.apply();
        }
    }
}