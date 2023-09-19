package com.example.securitypatrol;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.securitypatrol.Adapters.SquareAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.UserDBHelper;
import com.example.securitypatrol.Models.SquareItem;
import com.example.securitypatrol.Models.UserModel;
import com.example.securitypatrol.Services.StepCounterService;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_FOREGROUND_SERVICE_PERMISSION = 9023;
    private static final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 123;
    private static final int REQUEST_POST_NOTIFICATION_PERMISSION = 888;

    private List<SquareItem> squareItems;
    private SquareAdapter squareAdapter;
    StepCounterService stepCounterService;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;

    private long backPressedTime = 0;
    private static final int TIME_INTERVAL = 2000;

    SharedPreferences sharedPreferences,sharedPref_Steps;
    DatabaseHelper databaseHelper;
    Cursor cursor;

    private Boolean firstTimeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPref_Steps = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        if(sharedPref_Steps.getBoolean("isShiftActive", false)){
            startActivity(new Intent(MainActivity.this, NFCScan.class));
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
            openUserDialog();
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.FOREGROUND_SERVICE}, REQUEST_FOREGROUND_SERVICE_PERMISSION);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
            }
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, REQUEST_POST_NOTIFICATION_PERMISSION);
            }
        }

        stepCounterService = new StepCounterService();
        Intent intent = new Intent(this, StepCounterService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void openUserDialog(){
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.selection_user_dialog_activity, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextUniqueCode = dialogView.findViewById(R.id.editTextUniqueCode);
        final AutoCompleteTextView autoCompleteTextView = dialogView.findViewById(R.id.autoCompleteTextView);

        UserDBHelper userDBHelper = new UserDBHelper(this);
        String[] allUserNames = userDBHelper.getUserNames();
        String[] filteredUserNames = Arrays.stream(allUserNames)
                .filter(name -> !name.equals("Admin"))
                .toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, filteredUserNames);

        autoCompleteTextView.setAdapter(adapter);
        dialogBuilder.setTitle("Conectare");

        dialogBuilder.setPositiveButton("Verifica", (dialog, whichButton) -> {
            String inputUniqueCode = editTextUniqueCode.getText().toString();
            String inputUserName = autoCompleteTextView.getText().toString();

            if (inputUserName.isEmpty() || inputUniqueCode.isEmpty()) {
                if(inputUserName.isEmpty()){
                    autoCompleteTextView.setError("Va rugam sa introduceti un nume de utilizator");
                }if(inputUniqueCode.isEmpty()){
                    editTextUniqueCode.setError("Va rugam sa introduceti un cod unic");
                }
            } else {
                UserModel user = userDBHelper.getUser(inputUserName);
                if (user != null && user.getUniqueCode().equals(inputUniqueCode)) {

                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putString("userConnected", user.getUsername());
                    editor.apply();

                    userDBHelper.close();

                    Intent startIntent = new Intent(MainActivity.this, StepCounterService.class);
                    startIntent.setAction(ConstantsHelper.START_FOREGROUND_ACTION);

                    stepCounterService.startShift();
                    startService(startIntent);

                    startActivity(new Intent(this, NFCScan.class));

                } else {
                    Toast.makeText(this, "Datele introduse sunt gresite", Toast.LENGTH_SHORT).show();
                }
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
    }

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
    //TODO: Make this method showing the name of the user that made the report and the date of the report.
    private void loadFilesFromFolder() {
        File folder = new File(directoryPathOfFiles);

        if (folder.exists() && folder.isDirectory()) {
            File[] files = folder.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.toLowerCase().endsWith(".pdf") || name.toLowerCase().endsWith(".xlsx");
                }
            });

            if (files != null) {
                for (int i = 0; i + 1 < files.length; i += 2) {
                    File file1 = files[i];
                    File file2 = files[i + 1];

                    if (file1.isFile() && file2.isFile()) {
                        String fileName1 = file1.getName();
                        String fileName2 = file2.getName();
                        String[] parts = fileName1.split("_");
                        String title = " ";

                        if (parts[parts.length - 1].contains("xlsx")) {
                            if (parts.length >= 2) {
                                title = parts[1] + " " + parts[2].replace(".xlsx", "");
                            }
                        } else {
                            title = parts[1] + " " + parts[2].replace(".pdf", "");
                        }

                        SquareItem squareItem = new SquareItem(title, fileName1, fileName2);
                        squareItems.add(squareItem);

                    }
                }
                squareAdapter.notifyDataSetChanged();
            }
        }
    }

    public void createInitialDatabase() {
        if (!firstTimeDB) {
            databaseHelper.insertData("Bancomat",      "ET 1",        "100001");
            databaseHelper.insertData("Hidrant",       "ET 2",        "100002");
            databaseHelper.insertData("Hidrant",       "ET 2",        "100003");
            databaseHelper.insertData("Masina",        "ET 3",        "100004");
            databaseHelper.insertData("Parcare",       "ET 4",        "100005");
            databaseHelper.insertData("Statuie",       "ET parter",   "100006");

            UserDBHelper userDBHelper = new UserDBHelper(this);
            UserModel admin = new UserModel(    "Admin",    "9999");
            userDBHelper.addUser(admin);
            UserModel newUser = new UserModel(  "Marian",   "0000");
            userDBHelper.addUser(newUser);
            UserModel neUser = new UserModel(   "Marius",   "0001");
            userDBHelper.addUser(neUser);
            UserModel nUser = new UserModel(    "Gigel",    "0002");
            userDBHelper.addUser(nUser);


            cursor = databaseHelper.getAllData();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_DATATIME, "");
                    values.put(DatabaseHelper.COLUMN_IMAGE_URI,"");
                    values.put(DatabaseHelper.COLUMN_USER_NAME,"");
                    values.put(DatabaseHelper.COLUMN_OPTIONAL_COMM,"");

                    String nfcTag = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NFC_TAG));
                    String whereClause = DatabaseHelper.COLUMN_NFC_TAG + "=?";
                    String[] whereArgs = {nfcTag};
                    databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

                } while (cursor.moveToNext());

                cursor.close();
            }

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("firstTimeDB", true);
            editor.apply();
        }
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