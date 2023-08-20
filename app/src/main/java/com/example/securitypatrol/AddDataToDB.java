package com.example.securitypatrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.securitypatrol.Adapters.NFCTagsLeftAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;

public class AddDataToDB extends AppCompatActivity {
    TextView counterAlreadyMade, counterMax;
    ImageView imageView;
    MultiAutoCompleteTextView optionalComment;
    Button saveButton;
    ImageView makePhotoButton;

    private DatabaseHelper myDB;

    SharedPreferences sharedPref;
    String scannedNFCTag;

    String imageURI;
    int readedCounters = 0;
    int maxCounters = 1;

    RelativeLayout countersLeftGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_db);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);

        imageURI = getIntent().getStringExtra("imagePath");

        counterAlreadyMade = findViewById(R.id.counterAlreadyMade);
        counterMax = findViewById(R.id.counterMax);
        imageView = findViewById(R.id.imageView);
        optionalComment = findViewById(R.id.optionalComment);
        makePhotoButton = findViewById(R.id.retakePhotoButton);
        saveButton = findViewById(R.id.saveButton);

        myDB = new DatabaseHelper(AddDataToDB.this);

        if (imageURI != null) {
            imageView.setImageURI(Uri.parse(imageURI));
        }
        makePhotoButton.setOnClickListener(v -> {
            Intent intent = new Intent(AddDataToDB.this, CameraActivity.class);
            startActivity(intent);
        });
        saveButton.setOnClickListener(v -> {
            String optionalComm = optionalComment.getText().toString();
            try {
                int columnID = (int) getSQLData(DatabaseHelper.COLUMN_ID);
                showConfirmationDialog(() -> {
                    myDB.addOptionalComm(columnID, optionalComm);
                    myDB.addPhotoPath(columnID, imageURI);
                    myDB.addScannedBool(columnID, 1);
                    myDB.addScannedDateTime(columnID, ConstantsHelper.dateTimeScanned);
                    checkAndSetCounterData();
                    Intent intent = new Intent(AddDataToDB.this, NFCScan.class);
                    startActivity(intent);
                }, "Esti sigur ca poza si comentariul " + "'" + optionalComm + "'" + " sunt ok?", 1000);
            } catch (NumberFormatException e) {
                optionalComment.setError("Format invalid");
                optionalComment.requestFocus();
            }
        });
        checkAndSetCounterData();

        countersLeftGroup = findViewById(R.id.nfcLeftGroup);
        countersLeftGroup.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.item_nfc_tags, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(popupView);

            RecyclerView recyclerView = popupView.findViewById(R.id.recyclerViewNfcTags);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            myDB = new DatabaseHelper(this);
            Cursor cursor = myDB.getNFCTagsLeft();

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

    public void checkAndSetCounterData() {
        maxCounters = myDB.getRowCount();
        counterMax.setText("/ " + maxCounters);
        readedCounters = myDB.getScannedNFCCount();
        counterAlreadyMade.setText(String.valueOf(readedCounters));

        String optionalComm = (String) getSQLData(DatabaseHelper.COLUMN_OPTIONAL_COMM);
        if(optionalComm != null && !optionalComm.isEmpty()) {
            optionalComment.setText(optionalComm);
        }
    }

    public Object getSQLData(String columnName) {
        myDB = new DatabaseHelper(this);
        Cursor cursor = myDB.getDataByNFC(scannedNFCTag);

        Object columnValue = null;

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            int columnType = cursor.getType(columnIndex);

            switch (columnType) {
                case Cursor.FIELD_TYPE_STRING:
                    columnValue = cursor.getString(columnIndex);
                    break;
                case Cursor.FIELD_TYPE_INTEGER:
                    columnValue = cursor.getInt(columnIndex);
                    break;
                case Cursor.FIELD_TYPE_FLOAT:
                    columnValue = cursor.getFloat(columnIndex);
                    break;
                case Cursor.FIELD_TYPE_BLOB:
                    break;
                case Cursor.FIELD_TYPE_NULL:
                    break;
                default:
                    break;
            }
        }

        if (cursor != null) {
            cursor.close();
        }

        myDB.close();

        return columnValue;
    }

    private void showConfirmationDialog(final ConfirmationDialogCallback callback, String message, int delay) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Verificare")
                .setMessage(message)
                .setPositiveButton("Salveaza datele", null)
                .setNegativeButton("Inapoi", null);

        final AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialog -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            new Handler().postDelayed(() -> {
                positiveButton.setEnabled(true);
            }, delay);

            positiveButton.setOnClickListener(v -> {
                if (callback != null) {
                    callback.onButtonOKPressed();
                }
                alertDialog.dismiss();
            });

            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }
}