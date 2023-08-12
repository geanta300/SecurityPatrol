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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.securitypatrol.Adapters.CountersLeftAdapter;
import com.example.securitypatrol.Helpers.DatabaseHelper;

public class AddDataToDB extends AppCompatActivity {
    TextView counterAlreadyMade, counterMax;
    ImageView imageView;
    EditText newIndex;
    Button makePhoto, saveButton;

    private DatabaseHelper myDB;

    SharedPreferences sharedPref;
    String scannedQRCode;

    String imageURI;
    int readedCounters = 0;
    int maxCounters = 1;

    RelativeLayout countersLeftGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_db);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedQRCode = sharedPref.getString("scannedQRCode", scannedQRCode);

        imageURI = getIntent().getStringExtra("imagePath");

        counterAlreadyMade = findViewById(R.id.counterAlreadyMade);
        counterMax = findViewById(R.id.counterMax);
        imageView = findViewById(R.id.imageView);
        newIndex = findViewById(R.id.indexInput);
        makePhoto = findViewById(R.id.makePhotoB);
        saveButton = findViewById(R.id.saveButton);

        myDB = new DatabaseHelper(AddDataToDB.this);

        if (imageURI != null) {
            imageView.setImageURI(Uri.parse(imageURI));
        }
        makePhoto.setOnClickListener(v -> {
            Intent intent = new Intent(AddDataToDB.this, CameraActivity.class);
            startActivity(intent);
        });
        saveButton.setOnClickListener(v -> {
            String newIndexText = newIndex.getText().toString();
            if (!newIndexText.isEmpty()) {
                try {
                    double newIndexValue = Double.parseDouble(newIndexText);
                    Object columnIndexID = getSQLData(DatabaseHelper.COLUMN_ID);
                    if (columnIndexID instanceof Integer) {
                        int columnID = (int) columnIndexID;

                        showConfirmationDialog(() -> {
                            if (newIndexValue < (double) getSQLData(DatabaseHelper.COLUMN_INDEX_VECHI)) {
                                newIndex.setError("Noul index trebuie sa fie mai mare decat cel de luna trecuta");
                                newIndex.requestFocus();
                            } else {
                                myDB.addNewIndex(columnID, newIndexValue);
                                myDB.addPhotoPath(columnID, imageURI);
                                checkAndSetCounterData();
                                Intent intent = new Intent(AddDataToDB.this, QRScan.class);
                                startActivity(intent);
                            }
                        }, "Esti sigur ca poza si indexul " + newIndexText + " sunt ok?", 1000);
                    } else {
                        Toast.makeText(AddDataToDB.this, "Invalid column ID", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    newIndex.setError("Format invalid");
                    newIndex.requestFocus();
                }
            } else {
                newIndex.setError("Noul index este necesar");
                newIndex.requestFocus();
            }
        });
        checkAndSetCounterData();

        countersLeftGroup = findViewById(R.id.countersLeftGroup);
        countersLeftGroup.setOnClickListener(v -> {
            View popupView = getLayoutInflater().inflate(R.layout.item_counters, null);

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
            alertDialogBuilder.setView(popupView);

            RecyclerView recyclerView = popupView.findViewById(R.id.recyclerViewCounters);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));

            myDB = new DatabaseHelper(this);
            Cursor cursor = myDB.getCountersLeft();

            CountersLeftAdapter itemAdapter = new CountersLeftAdapter(cursor);
            recyclerView.setAdapter(itemAdapter);

            AlertDialog alertDialog = new AlertDialog.Builder(this)
                    .setView(popupView)
                    .setTitle("Contoare de scanat:")
                    .setPositiveButton("OK", null)
                    .create();

            alertDialog.show();
        });
    }

    public void checkAndSetCounterData() {
        maxCounters = myDB.getRowCount();
        counterMax.setText("/ " + maxCounters);
        readedCounters = myDB.getIndexesHigherThanZero();
        counterAlreadyMade.setText(String.valueOf(readedCounters));
    }

    public Object getSQLData(String columnName) {
        myDB = new DatabaseHelper(this);
        Cursor cursor = myDB.getDataByQR(scannedQRCode);

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
                    columnValue = cursor.getDouble(columnIndex);
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