package com.example.securitypatrol;


import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.NFCTagsLeftAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.ShiftTimer;

import org.apache.commons.math3.analysis.function.Add;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Calendar;

public class NFCScan extends AppCompatActivity {
    private final String adminPassword = "1234";

    SharedPreferences sharedPreferences;

    ImageView adminButton;
    Button backToExport;

    TextView textviewTimer, textviewDataCalendaristica;
    private ShiftTimer shiftTimer;

    DatabaseHelper databaseHelper;
    NfcAdapter nfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.nfc_scan_activity);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        backToExport = findViewById(R.id.backToExportButt);
        databaseHelper = new DatabaseHelper(this);

        int nfcTags = databaseHelper.getScannedNFCCount();
        int maxnfcTags = databaseHelper.getRowCount();
        if (nfcTags == maxnfcTags) {
            backToExport.setVisibility(View.VISIBLE);
            backToExport.setOnClickListener(v -> {
                Intent intent = new Intent(this, PreviewExportData.class);
                startActivity(intent);
            });
        }

        adminButton = findViewById(R.id.adminButton);
        adminButton.setOnClickListener(v -> openAdminDialog());

        RecyclerView recyclerView = findViewById(R.id.recyclerViewNfcTags);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Cursor cursor = databaseHelper.getNFCTagsLeft();

        NFCTagsLeftAdapter itemAdapter = new NFCTagsLeftAdapter(cursor);
        recyclerView.setAdapter(itemAdapter);


        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            Toast.makeText(this, "NFC nu este suportat de acest dispozitiv!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            if (!nfcAdapter.isEnabled()) {
                showNFCSettingsDialog();
            }
        }
        Log.d("NFCTAG", "onNewIntent: " + getIntent());
        setTimerAndDateInTitle();

    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        readNFCTag(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            Intent intent = new Intent(
                    this,
                    getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

            PendingIntent pendingIntent;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_MUTABLE);
            }else{
                pendingIntent = PendingIntent.getActivity(
                        this,
                        0,
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT + PendingIntent.FLAG_IMMUTABLE);
            }
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, null, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    private void openAdminDialog() {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.admin_dialog_activity, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextPassword = dialogView.findViewById(R.id.editTextUniqueCode);
        editTextPassword.requestFocus();

        dialogBuilder.setTitle("Introdu parola");
        dialogBuilder.setPositiveButton("Verifica", (dialog, whichButton) -> {

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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, MainActivity.class));
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

    public void showNFCSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("NFC-ul este dezactivat, vrei sa-l activezi?")
                .setPositiveButton("DA", (dialog, id) -> startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS)))
                .setNegativeButton("NU", (dialog, id) -> startActivity(new Intent(NFCScan.this, NFCScan.class)));
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void readNFCTag(Intent intent) {
        Log.d("NFCTAG", "readNFCTag intent: " + intent);

        String action = intent.getAction();
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {
            Parcelable[] rawMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            String nfcContent = null;

            if (rawMessages != null) {
                NdefMessage[] messages = new NdefMessage[rawMessages.length];
                for (int i = 0; i < rawMessages.length; i++) {
                    messages[i] = (NdefMessage) rawMessages[i];

                }
                Log.d("NFCTAG", "messages: " + Arrays.toString(messages));

                StringBuilder contentBuilder = new StringBuilder();
                for (NdefMessage message : messages) {
                    NdefRecord[] records = message.getRecords();
                    for (NdefRecord record : records) {
                        byte[] payload = record.getPayload();
                        String text = new String(payload, StandardCharsets.UTF_8);

                        Log.d("NFCTAG", "NFC message without any processing: " + text);
                        int langCodeIndex = text.indexOf("en");
                        if (langCodeIndex >= 0) {
                            text = text.substring(langCodeIndex + 2);
                        }

                        nfcContent = contentBuilder.append(text.trim()).toString();
                    }
                }
                Log.d("NFCTAG", "NFC message with processing: " + nfcContent);
                databaseHelper = new DatabaseHelper(this);
                Cursor cursor = databaseHelper.getDataByNFC(nfcContent);
                if(cursor !=null && cursor.moveToFirst()){
                    int NFCalreadyScanned = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SCANNED));
                    Log.d("NFCTAG", "NFC is already scanned: " + NFCalreadyScanned);
                    cursor.close();
                    if(NFCalreadyScanned == 0){
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("scannedNFCTag", nfcContent);
                        editor.apply();
                        startActivity(new Intent(NFCScan.this, AddDataToDB.class));
                    }else if (NFCalreadyScanned > 0) {
                        showConfirmationDialog("Acest tag NFC a fost scanat deja. Vrei sa-l scanezi din nou?", nfcContent);
                    }
                }else {
                    Toast.makeText(this, "NFC invalid" + nfcContent, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, NFCScan.class));
                }
            }
        }
    }

    private void setTimerAndDateInTitle() {
        //Set timer
        textviewTimer = findViewById(R.id.textviewTimer);
        shiftTimer = new ShiftTimer(textviewTimer);
        ConstantsHelper.DateInfo dateInfo = ConstantsHelper.getDateInfo();
        int currentHourInt = Integer.parseInt(dateInfo.currentHour);

        long endTimeMillis;
        boolean waitingForSecondShift;
        if (currentHourInt >= 6 && currentHourInt < 8) {
            waitingForSecondShift = false;
            endTimeMillis = calculateShiftEndTimeMillis(8, 0);
        } else if (currentHourInt >= 8 && currentHourInt < 18) {
            waitingForSecondShift = true;
            endTimeMillis = calculateShiftEndTimeMillis(18, 0);
        } else {
            waitingForSecondShift = false;
            endTimeMillis = calculateShiftEndTimeMillis(20, 0);
        }
        shiftTimer.startTimer(endTimeMillis, "Shift Done", waitingForSecondShift);

        //Set date
        textviewDataCalendaristica = findViewById(R.id.textviewDataCalendaristica);
        String todayDate = dateInfo.currentDay + "." + dateInfo.formattedMonth + "." + dateInfo.currentYear;
        textviewDataCalendaristica.setText(todayDate);
    }

    private long calculateShiftEndTimeMillis(int hour, int minute) {
        long currentTimeMillis = System.currentTimeMillis();

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(currentTimeMillis);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, 0);

        long shiftEndTimeMillis = calendar.getTimeInMillis();

        // If the shift end time is before the current time, add one day to it
        if (shiftEndTimeMillis <= currentTimeMillis) {
            shiftEndTimeMillis += 24 * 60 * 60 * 1000; // 1 day
        }

        return shiftEndTimeMillis;
    }
}