package com.example.securitypatrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Services.DatabaseStructure;

public class AddDataToDB extends AppCompatActivity {
    MultiAutoCompleteTextView optionalComment;
    TextView obiectivTitle;
    Button saveButton;

    private DatabaseHelper myDB;

    SharedPreferences sharedPref;
    String scannedNFCTag;

    String imageURI;
    int readedNFC = 0;

    LinearLayout groupOfEditData;

    ImageView obiectivOKButton, obiectivNotOKButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_db);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);

        imageURI = getIntent().getStringExtra("imagePath");

        optionalComment = findViewById(R.id.optionalComment);
        obiectivTitle = findViewById(R.id.obiectivTitle);

        saveButton = findViewById(R.id.saveButton);

        myDB = new DatabaseHelper(this);

        saveButton.setOnClickListener(v -> {
            String optionalComm = optionalComment.getText().toString();
            try {
                int columnID = (int) getSQLData(DatabaseStructure.COLUMN_UNIQUE_ID);
                showConfirmationDialog(() -> {
                    //myDB.addOptionalComm(columnID, optionalComm);
                    //myDB.addPhotoPath(columnID, imageURI);
//                    myDB.addScannedBool(columnID, 1);
//                    myDB.addScannedDateTime(columnID, ConstantsHelper.dateTimeScanned);
                    checkAndSetCounterData();
                    Intent intent = new Intent(AddDataToDB.this, NFCScan.class);
                    startActivity(intent);
                }, "Esti sigur ca toate datele sunt ok?", 1000);
            } catch (NumberFormatException e) {
                optionalComment.setError("Format invalid");
                optionalComment.requestFocus();
            }
        });
        checkAndSetCounterData();

        groupOfEditData = findViewById(R.id.groupIfNotOK);
        setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);

        obiectivOKButton= findViewById(R.id.obiectivOK);
        obiectivOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);
        });
        obiectivNotOKButton = findViewById(R.id.obiectivNotOK);
        obiectivNotOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, true, 1);
        });
    }

    public void checkAndSetCounterData() {
        readedNFC = myDB.getScannedNFCCount();

//        String optionalComm = (String) getSQLData(DatabaseStructure.COLUMN_OPTIONAL_COMM);
//        if(optionalComm != null && !optionalComm.isEmpty()) {
//            optionalComment.setText(optionalComm);
//        }
        String title = (String) getSQLData(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV);
        if(title != null && !title.isEmpty()) {
            obiectivTitle.setText("Obiectivul: " + title);
        }
    }

    private static void setViewAndChildrenEnabled(View view, boolean b, float alpha) {
        view.setEnabled(b);
        view.setAlpha(alpha);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                setViewAndChildrenEnabled(child, b, alpha);
            }
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