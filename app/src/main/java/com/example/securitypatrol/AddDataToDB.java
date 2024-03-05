package com.example.securitypatrol;

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

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.ModularCameraActivity;
import com.example.securitypatrol.Interfaces.ConfirmationDialogCallback;
import com.example.securitypatrol.Interfaces.PhotoTakenCallback;
import com.example.securitypatrol.Interfaces.UIComponentCreator;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_Edittext;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_RadioButtons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddDataToDB extends AppCompatActivity implements PhotoTakenCallback {

    TextView obiectivTitle;
    Button saveButton;
    ImageView obiectivOKButton, obiectivNotOKButton;

    private DatabaseHelper databaseHelper;

    SharedPreferences sharedPref;
    String scannedNFCTag, imageUriString;

    int readedNFC = 0;

    LinearLayout groupOfEditData;

    private int newImageID;

    private final List<View> uiElements = new ArrayList<>();
    List<String> verificari = new ArrayList<>();
    List<Integer> tipuriVerificare= new ArrayList<>();
    Map<Integer,String> photosList = new HashMap<>();
    ImageView[] addPhotoButtons = new ImageView[4];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_data_to_db);

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);

        obiectivTitle = findViewById(R.id.obiectivTitle);
        saveButton = findViewById(R.id.saveButton);

        databaseHelper = new DatabaseHelper(this);

        saveButton.setOnClickListener(v -> {
            try {
                showConfirmationDialog(() -> {
                    int columnID = (int) getSQLData(DatabaseHelper.COLUMN_UNIQUE_ID);
                    ConstantsHelper constantsHelper = new ConstantsHelper();
                    databaseHelper.addScannedData(columnID, 1, constantsHelper.dateTimeScanned);
                    checkAndSetObjectiveData();

                    Intent intent = new Intent(AddDataToDB.this, NFCScan.class);
                    startActivity(intent);

                }, "Esti sigur ca toate datele sunt ok?", 1000);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        });
        checkAndSetObjectiveData();

        getVerificationsFromDatabase();
        groupOfEditData = findViewById(R.id.groupIfNotOK);
        setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);

        obiectivOKButton = findViewById(R.id.obiectivOK);
        obiectivOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, false, 0.5f);
        });
        obiectivNotOKButton = findViewById(R.id.obiectivNotOK);
        obiectivNotOKButton.setOnClickListener(v -> {
            setViewAndChildrenEnabled(groupOfEditData, true, 1);
        });

        addPhotoButtons[0] = findViewById(R.id.addPhotoButton1);
        addPhotoButtons[1] = findViewById(R.id.addPhotoButton2);
        addPhotoButtons[2] = findViewById(R.id.addPhotoButton3);
        addPhotoButtons[3] = findViewById(R.id.addPhotoButton4);

        setImageViewClickListener(R.id.addPhotoButton1);
        setImageViewClickListener(R.id.addPhotoButton2);
        setImageViewClickListener(R.id.addPhotoButton3);
        setImageViewClickListener(R.id.addPhotoButton4);

    }

    public void getVerificationsFromDatabase() {
        int objectiveID = (int) getSQLData(DatabaseHelper.COLUMN_UNIQUE_ID);

        Cursor cursor = databaseHelper.getAllVerificariWithObjectiveIDForPreview(objectiveID);

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
                line.setBackgroundColor(ContextCompat.getColor(this, R.color.black));
                int heightInPixels = 7;
                LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, heightInPixels);
                layoutParams.topMargin = 15;
                layoutParams.bottomMargin = 15;
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

        for (Map.Entry<Integer, String> entry : photosList.entrySet()) {
            int newImageID = entry.getKey();
            String imageUriString = entry.getValue();
            Log.d("Entrymap", "saveAllValuesToDatabase: "+"Key: "+newImageID+" Value: "+imageUriString);
            if (databaseHelper.getIfPhotoVerficationExists(objectiveID,newImageID)) {
                databaseHelper.updatePhotoPath(objectiveID, imageUriString, String.valueOf(newImageID));
            } else {
                databaseHelper.addPhotoPath(objectiveID, imageUriString, String.valueOf(newImageID));
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
        dialog.setContentView(R.layout.popup_objective_capture);
        ImageView takePhotoButton = dialog.findViewById(R.id.takePhoto);
        ImageView activateBlitzButton = dialog.findViewById(R.id.blitz);
        PreviewView cameraPreview = dialog.findViewById(R.id.cameraPreview);

        dialog.show();
        ModularCameraActivity cameraActivity = new ModularCameraActivity();
        cameraActivity.setPhotoTakenCallback(this);
        cameraActivity.startCamera(cameraActivity.cameraFacing, takePhotoButton, activateBlitzButton, cameraPreview, dialog, this);
        newImageID = imageViewId;
    }

    public void setupMiniPreviews(int imageViewId, String imageUriString) {
        runOnUiThread(() -> {
            ImageView imageView = findViewById(imageViewId);
            imageView.setImageURI(Uri.parse(imageUriString));
            photosList.put(imageViewId, imageUriString);
        });
    }

    @Override
    public void onPhotoTaken(String imageUriString) {
        setupMiniPreviews(newImageID, imageUriString);
        this.imageUriString = imageUriString;
        runOnUiThread(()->{
            for (int i = 1; i < addPhotoButtons.length; i++) {
                if(addPhotoButtons[i-1].getVisibility()==View.VISIBLE && addPhotoButtons[i].getVisibility()==View.GONE){
                    addPhotoButtons[i].setVisibility(View.VISIBLE);
                    Log.d("Photo", "onPhotoTaken: " + "Button " + i + " set to visible");
                    break;
                }
            }
        });
    }

    public void checkAndSetObjectiveData() {
        readedNFC = databaseHelper.getScannedNFCCount();

        String title = (String) getSQLData(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV);
        if (title != null && !title.isEmpty()) {
            obiectivTitle.setText("Obiectivul: " + title);
        }
    }

    private static void setViewAndChildrenEnabled(View view, boolean b, float alpha) {
        view.setEnabled(b);
        view.setClickable(b);
        view.setAlpha(alpha);
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                View child = viewGroup.getChildAt(i);
                child.setEnabled(b);
                child.setClickable(b);
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