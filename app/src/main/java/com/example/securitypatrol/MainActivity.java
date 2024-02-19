package com.example.securitypatrol;

import android.Manifest;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.SquareAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Interfaces.UIComponentCreator;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_Edittext;
import com.example.securitypatrol.Interfaces.UIComponents.Create_UI_RadioButtons;
import com.example.securitypatrol.Models.SquareItem;
import com.example.securitypatrol.Models.UserModel;
import com.example.securitypatrol.Services.StepCounterService;
import com.itextpdf.io.exceptions.IOException;

import org.apache.poi.EncryptedDocumentException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUESTS_CODE= 777;
    private LinkedList<String> permissionQueue = new LinkedList<>();

    private List<SquareItem> squareItems;
    private SquareAdapter squareAdapter;
    StepCounterService stepCounterService;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;

    private long backPressedTime = 0;
    private static final int TIME_INTERVAL = 2000;

    SharedPreferences sharedPreferences, sharedPref_Steps;
    DatabaseHelper databaseHelper;
    Cursor cursor;

    private Boolean firstTimeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref_Steps = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        if (sharedPref_Steps.getBoolean("isShiftActive", false)) {
            startActivity(new Intent(MainActivity.this, NFCScan.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        firstTimeDB = sharedPreferences.getBoolean("firstTimeDB", false);

        databaseHelper = new DatabaseHelper(this);
        createInitialDatabase();

        RecyclerView recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        squareItems = new ArrayList<>();
        squareAdapter = new SquareAdapter(squareItems);
        recyclerView.setAdapter(squareAdapter);

        loadFilesFromFolder();

        Button startShiftButton = findViewById(R.id.startShift);
        startShiftButton.setOnClickListener(v -> {
//            openUserDialog();
//            startActivity(new Intent(this, NFCScan.class));
            chooseExcelFile();
        });


        stepCounterService = new StepCounterService();
        Intent intent = new Intent(this, StepCounterService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        permissionQueue.add(Manifest.permission.ACTIVITY_RECOGNITION);
        permissionQueue.add(Manifest.permission.CAMERA);
        permissionQueue.add(Manifest.permission.POST_NOTIFICATIONS);

        requestPermissionsFromQueue();
    }

    private void requestPermissionsFromQueue() {
        if (!permissionQueue.isEmpty()) {
            String permission = permissionQueue.poll();
            if (permission != null) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUESTS_CODE);
                } else {
                    // Permission already granted, proceed to the next one
                    requestPermissionsFromQueue();
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUESTS_CODE) {
            boolean allPermissionsGranted = true;

            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                // Permissions granted, proceed to the next one
                requestPermissionsFromQueue();
            } else {
                // Permissions denied, show a message or take appropriate action
                showToast("Permissions denied!");
            }
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
//    private void openUserDialog(){
//        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
//        LayoutInflater inflater = this.getLayoutInflater();
//        View dialogView = inflater.inflate(R.layout.selection_user_dialog_activity, null);
//        dialogBuilder.setView(dialogView);
//
//        final EditText editTextUniqueCode = dialogView.findViewById(R.id.editTextUniqueCode);
//        final AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteTextView);
//
//        String[] allUserNames = databaseHelper.getUserNames();
//        String[] filteredUserNames = Arrays.stream(allUserNames)
//                .filter(name -> !name.equals("Admin"))
//                .toArray(String[]::new);
//
//        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
//                android.R.layout.simple_dropdown_item_1line, filteredUserNames);
//
//        autoCompleteTextView.setAdapter(adapter);
//        dialogBuilder.setTitle("Conectare");
//
//        dialogBuilder.setPositiveButton("Verifica", (dialog, whichButton) -> {
//            String inputUniqueCode = editTextUniqueCode.getText().toString();
//            String inputUserName = autoCompleteTextView.getText().toString();
//
//            if (inputUserName.isEmpty() || inputUniqueCode.isEmpty()) {
//                if(inputUserName.isEmpty()){
//                    autoCompleteTextView.setError("Va rugam sa introduceti un nume de utilizator");
//                }if(inputUniqueCode.isEmpty()){
//                    editTextUniqueCode.setError("Va rugam sa introduceti un cod unic");
//                }
//            } else {
//                UserModel user = databaseHelper.getUser(inputUserName);
//                if (user != null) {
//
//                    SharedPreferences.Editor editor = sharedPreferences.edit();
//                    editor.putString("userConnected", user.getUsername());
//                    editor.apply();
//
//                    databaseHelper.close();
//
//                    Intent startIntent = new Intent(MainActivity.this, StepCounterService.class);
//                    startIntent.setAction(ConstantsHelper.START_FOREGROUND_ACTION);
//
//                    stepCounterService.startShift();
//                    startService(startIntent);
//
//                    startActivity(new Intent(this, NFCScan.class));
//
//                } else {
//                    Toast.makeText(this, "Datele introduse sunt gresite", Toast.LENGTH_SHORT).show();
//                }
//            }
//        });
//
//        AlertDialog alertDialog = dialogBuilder.create();
//        alertDialog.show();
//    }

    @Override
    public void onBackPressed() {
        if (System.currentTimeMillis() - backPressedTime < TIME_INTERVAL) {
            super.onBackPressed();
        } else {
            backPressedTime = System.currentTimeMillis();
            Toast.makeText(this, "Apasati din nou pentru a iesi", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        backPressedTime = 0;
        unbindService(serviceConnection);
        super.onDestroy();
    }

    private void loadFilesFromFolder() {
        File folder = new File(directoryPathOfFiles);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".pdf");
                }
            });

            if (files != null) {
                for (int i = 0; i + 1 < files.length; i += 2) {
                    File file1 = files[i];

                    if (file1.isFile()) {
                        String fileName1 = file1.getName();
                        String[] parts = fileName1.split("_");
                        String title = " ";

                        title = parts[1] + " " + parts[2].replace(".pdf", "");

                        SquareItem squareItem = new SquareItem(title, fileName1);
                        squareItems.add(squareItem);

                    }
                }
                squareAdapter.notifyDataSetChanged();
            }
        }
    }

    public void createInitialDatabase() {
        if (!firstTimeDB) {

            UserModel admin = new UserModel("Admin");
            UserModel newUser = new UserModel("Marian");
            UserModel neUser = new UserModel("Marius");
            UserModel nUser = new UserModel("Gigel");

            DatabaseHelper dbAd = new DatabaseHelper(this);
            dbAd.addUser(admin);
            dbAd.addUser(newUser);
            dbAd.addUser(neUser);
            dbAd.addUser(nUser);


//            cursor = databaseHelper.getAllData();
//            if (cursor != null && cursor.moveToFirst()) {
//                do {
//                    ContentValues values = new ContentValues();
//                    values.put(DatabaseHelper.COLUMN_DTIME, "");
//                    values.put(DatabaseHelper.COLUMN_PHOTO_URI,"");
//                    values.put(DatabaseHelper.COLUMN_NUME_POMPIER,"");
////                    values.put(DatabaseStructure.COLUMN_OPTIONAL_COMM,"");
//
//                    String nfcTag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NFC_CODE));
//                    String whereClause = DatabaseHelper.COLUMN_NFC_CODE + "=?";
//                    String[] whereArgs = {nfcTag};
//                    databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_OBIECTIVE, values, whereClause, whereArgs);
//
//                } while (cursor.moveToNext());
//
//                cursor.close();
//            }


            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeDB", true);
            editor.apply();
        }
    }

    private void chooseExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityForResult(intent, 1252);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1252 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                readExcelFile(uri);
            }
        }
    }

    public void readExcelFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);
            int lastNFCCode = 0;

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);

                Log.d("ExcelFileImport", "Row: " + row.getRowNum());

                Cell nrObiectivCell = row.getCell(0);
                int nrObiectiv = (nrObiectivCell != null && nrObiectivCell.getCellType() == CellType.NUMERIC) ? (int) nrObiectivCell.getNumericCellValue() : 0;
                Log.d("ExcelFileImport", "nrObiectiv: " + nrObiectiv);

                Cell descriereCell = row.getCell(1);
                String descriere = (descriereCell != null) ? descriereCell.getStringCellValue() : "";
                Log.d("ExcelFileImport", "descriere: " + descriere);

                Cell locatieCell = row.getCell(2);
                String locatie = (locatieCell != null) ? locatieCell.getStringCellValue() : "";
                Log.d("ExcelFileImport", "locatie: " + locatie);

                Cell codNFCCell = row.getCell(3);
                int codNFC = (codNFCCell != null && codNFCCell.getCellType() == CellType.NUMERIC) ? (int) codNFCCell.getNumericCellValue() : 0;
                Log.d("ExcelFileImport", "codNFC: " + codNFC);

                Cell verificareCell = row.getCell(4);
                String verificare = (verificareCell != null) ? verificareCell.getStringCellValue() : "";
                Log.d("ExcelFileImport", "verificare: " + verificare);

                Cell tipCell = row.getCell(5);
                int tip = (tipCell != null && tipCell.getCellType() == CellType.NUMERIC) ? (int) tipCell.getNumericCellValue() : 0;
                Log.d("ExcelFileImport", "tip_verificare: " + tip);

                if (codNFC != lastNFCCode) {
                    databaseHelper.insertObiectiv(descriere, locatie, codNFC);
                    lastNFCCode = codNFC;
                    Log.d("ExcelFileImport", "Obiectiv inserted: " + descriere + " " + locatie + " " + codNFC);
                }

                databaseHelper.insertVerificare(verificare, nrObiectiv, tip);
            }
            workbook.close();
        } catch (Exception e) {
            Log.e("ExcelFileImport", "Error reading Excel file: " + e.getMessage());
        }
    }

    private void createUIElement(String denumire, String verificare, UIComponentCreator uiElementCreator) {
        // Create a parent layout (e.g., LinearLayout)
        LinearLayout parentLayout = new LinearLayout(this);
        parentLayout.setOrientation(LinearLayout.VERTICAL);

        // Create a TextView for denumire
        TextView verificareTextView = new TextView(this);
        verificareTextView.setText(verificare);

        // Add the TextView to the parent layout
        parentLayout.addView(verificareTextView);

        // Use the UIElementCreator to create the specific UI element
        View uiElementView = uiElementCreator.createView(this);

        // Add the created UI element to the parent layout
        parentLayout.addView(uiElementView);

        // Optionally, add the parent layout to the main layout of the activity
//        ViewGroup mainLayout = findViewById(R.id.relativeLayouttest); // Replace with your main layout ID
//        mainLayout.addView(parentLayout);
    }


    private final ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            StepCounterService.LocalBinder binder = (StepCounterService.LocalBinder) iBinder;
            stepCounterService = binder.getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            stepCounterService = null;
        }
    };
}