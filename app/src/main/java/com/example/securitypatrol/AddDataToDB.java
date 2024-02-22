package com.example.securitypatrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.ModularCameraActivity;
import com.example.securitypatrol.Interfaces.ConfirmationDialogCallback;
import com.example.securitypatrol.Interfaces.UIComponentCreator;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_Edittext;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_RadioButtons;

import java.util.ArrayList;
import java.util.List;

public class AddDataToDB extends AppCompatActivity {

    TextView obiectivTitle;
    Button saveButton;
    ImageView obiectivOKButton, obiectivNotOKButton;

    private DatabaseHelper databaseHelper;

    SharedPreferences sharedPref;
    String scannedNFCTag;

    String imageURI;
    int readedNFC = 0;

    LinearLayout groupOfEditData;


    private final List<View> uiElements = new ArrayList<>();
    List<String> verificari;
    List<Integer> tipuriVerificare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_db);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);

        imageURI = getIntent().getStringExtra("imagePath");

        obiectivTitle = findViewById(R.id.obiectivTitle);
        saveButton = findViewById(R.id.saveButton);

        databaseHelper = new DatabaseHelper(this);

        saveButton.setOnClickListener(v -> {
            try {
                int columnID = (int) getSQLData(DatabaseHelper.COLUMN_UNIQUE_ID);
                showConfirmationDialog(() -> {
                    //databaseHelper.addOptionalComm(columnID, optionalComm);
                    //databaseHelper.addPhotoPath(columnID, imageURI);
                    databaseHelper.addScannedData(columnID, 1, ConstantsHelper.dateTimeScanned);
                    checkAndSetObjectiveData();
                    Intent intent = new Intent(AddDataToDB.this, NFCScan.class);
                    startActivity(intent);
                }, "Esti sigur ca toate datele sunt ok?", 1000);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
        checkAndSetObjectiveData();

        groupOfEditData = findViewById(R.id.groupIfNotOK);
        setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);

        obiectivOKButton = findViewById(R.id.obiectivOK);
        obiectivOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);
//            saveAllValuesToDatabase();
        });
        obiectivNotOKButton = findViewById(R.id.obiectivNotOK);
        obiectivNotOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, true, 1);
        });

        setImageViewClickListener(R.id.addPhotoButton1);
        setImageViewClickListener(R.id.addPhotoButton2);
        setImageViewClickListener(R.id.addPhotoButton3);
        setImageViewClickListener(R.id.addPhotoButton4);


        getVerificationsFromDatabase();
    }

    public void getVerificationsFromDatabase() {
        int objectiveID = (int) getSQLData(DatabaseHelper.COLUMN_UNIQUE_ID);

        Cursor cursor = databaseHelper.getAllVerificariForObjective(objectiveID);

        verificari = new ArrayList<>();
        tipuriVerificare = new ArrayList<>();

        if (cursor != null && cursor.moveToFirst()) {
            int descriereIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_DESCRIERE_VERIFICARI);
            int tipIndex = cursor.getColumnIndex(DatabaseHelper.COLUMN_TIP_VERIFICARE);

            if (descriereIndex != -1 && tipIndex != -1) {
                do {
                    String verificare = cursor.getString(descriereIndex);
                    int tipVerificare = cursor.getInt(tipIndex);
                    verificari.add(verificare);
                    tipuriVerificare.add(tipVerificare);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        createUIElements(verificari, tipuriVerificare);
    }

    private void createUIElements(List<String> verificari, List<Integer> tipuriVerificare) {
        LinearLayout mainLayout = findViewById(R.id.groupIfNotOK);

        for (int i = 0; i < verificari.size(); i++) {
            String verificare = verificari.get(i);
            int tipVerificare = tipuriVerificare.get(i);

            LinearLayout parentLayout = new LinearLayout(this);
            parentLayout.setOrientation(LinearLayout.VERTICAL);


            TextView verificareTextView = new TextView(this);
            verificareTextView.setText(verificare);
            verificareTextView.setTextColor(Color.BLACK);
            verificareTextView.setTextSize(16);
            parentLayout.addView(verificareTextView);


            UIComponentCreator uiElementCreator = getUIElementCreator(tipVerificare);
            if (uiElementCreator != null) {
                View uiElementView = uiElementCreator.createView(this);
                parentLayout.addView(uiElementView);

                if (uiElementView instanceof EditText || uiElementView instanceof RadioGroup) {
                    uiElementView.setTag("tag_" + i);
                    uiElements.add(uiElementView);
                }
            }

            if (i < verificari.size()) {
                View line = new View(this);
                line.setBackgroundColor(ContextCompat.getColor(this, R.color.teal_200));
                int heightInPixels = 10;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
                layoutParams.topMargin = 50;
                layoutParams.bottomMargin = 50;
                parentLayout.addView(line, layoutParams);
            }
            mainLayout.addView(parentLayout, i);
        }
    }

    private void saveAllValuesToDatabase() {
        int objectiveID = (int) getSQLData(DatabaseHelper.COLUMN_ID_OBIECTIV);
        for (int i = 0; i < uiElements.size(); i++) {
            View uiElement = uiElements.get(i);
            List<Integer> verificationID = databaseHelper.getVerificationIDs(objectiveID);

            if (uiElement instanceof EditText) {
                String enteredText = ((EditText) uiElement).getText().toString();
                databaseHelper.verificationDataToDatabase(verificationID.get(i), enteredText);

            } else if (uiElement instanceof RadioGroup) {
                RadioGroup radioGroup = (RadioGroup) uiElement;
                int selectedRadioButtonId = radioGroup.getCheckedRadioButtonId();

                if (selectedRadioButtonId != -1) {
                    RadioButton selectedRadioButton = findViewById(selectedRadioButtonId);
                    String radioButtonValue = selectedRadioButton.getText().toString();
                    databaseHelper.verificationDataToDatabase(verificationID.get(i), radioButtonValue);
                } else {
                    Log.d("UIElements", "saveAllValuesToDatabase RADIO: No RadioButton selected");
                }
            }
        }
    }

    private UIComponentCreator getUIElementCreator(int tipVerificare) {
        switch (tipVerificare) {
            case 1:
                return new Create_UI_Edittext();
            case 2:
                return new Create_UI_RadioButtons();
            default:
                return null;
        }
    }

    private void setImageViewClickListener(int imageViewId) {
        ImageView imageView = findViewById(imageViewId);
        imageView.setOnClickListener(v -> launchCamera(imageViewId));
    }

    private void launchCamera(int imageViewId) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.activity_objective_capture);
        ImageView takePhotoButton = dialog.findViewById(R.id.takePhoto);
        ImageView activateBlitzButton = dialog.findViewById(R.id.blitz);
        PreviewView cameraPreview = dialog.findViewById(R.id.cameraPreview);

        dialog.show();
        ModularCameraActivity cameraActivity = new ModularCameraActivity();
        cameraActivity.startCamera(cameraActivity.cameraFacing, takePhotoButton, activateBlitzButton, cameraPreview, true, dialog, this);

        dialog.setOnDismissListener(dialog1 -> setupMiniPreviews(imageViewId));
    }

    public void setupMiniPreviews(int imageViewId) {
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        String imageUriString = preferences.getString("popUpImageUri", null);
        if (imageUriString != null) {
            ImageView imageView = findViewById(imageViewId);
            imageView.setImageURI(Uri.parse(imageUriString));

            int objectiveID = (int) getSQLData(DatabaseHelper.COLUMN_UNIQUE_ID);
            databaseHelper.addPhotoPath(objectiveID,imageUriString);
        }
    }

    public void checkAndSetObjectiveData() {
        readedNFC = databaseHelper.getScannedNFCCount();

//        String optionalComm = (String) getSQLData(DatabaseStructure.COLUMN_OPTIONAL_COMM);
//        if(optionalComm != null && !optionalComm.isEmpty()) {
//            optionalComment.setText(optionalComm);
//        }
        String title = (String) getSQLData(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV);
        if (title != null && !title.isEmpty()) {
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
        databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getDataByNFC(scannedNFCTag);

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

        databaseHelper.close();

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
                    saveAllValuesToDatabase();
                }
                alertDialog.dismiss();
            });

            Button negativeButton = alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE);
            negativeButton.setOnClickListener(v -> alertDialog.dismiss());
        });

        alertDialog.show();
    }
}