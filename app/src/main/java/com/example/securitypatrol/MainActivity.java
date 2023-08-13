package com.example.securitypatrol;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import com.example.securitypatrol.Adapters.SquareAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Models.SquareItem;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private List<SquareItem> squareItems;
    private SquareAdapter squareAdapter;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;

    private long backPressedTime = 0;
    private static final int TIME_INTERVAL = 2000;

    SharedPreferences sharedPreferences;
    DatabaseHelper databaseHelper;
    Cursor cursor;

    private Boolean firstTimeDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

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

        Button scanQRCodes = findViewById(R.id.scanNFC);
        scanQRCodes.setOnClickListener(v -> {
            startActivity(new Intent(this, NFCScan.class));
        });
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
        super.onDestroy();
    }

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

            cursor = databaseHelper.getAllData();
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    ContentValues values = new ContentValues();
                    values.put(DatabaseHelper.COLUMN_DATATIME, "");
                    values.put(DatabaseHelper.COLUMN_IMAGE_URI,"");
                    values.put(DatabaseHelper.COLUMN_USER_NAME,"");

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
}