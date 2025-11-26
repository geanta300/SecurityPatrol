package com.example.securitypatrol;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.Menu;
import android.view.MenuItem;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.SquareAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Models.SquareItem;
import com.example.securitypatrol.Services.StepCounterService;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int PERMISSIONS_REQUESTS_CODE= 777;
    private LinkedList<String> permissionQueue = new LinkedList<>();

    private List<SquareItem> squareItems;
    private SquareAdapter squareAdapter;
    StepCounterService stepCounterService;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;

    private long backPressedTime = 0;
    private static final int TIME_INTERVAL = 2000;

    SharedPreferences sharedPref_Steps;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MaterialToolbar toolbar = findViewById(R.id.topAppBar);
        setSupportActionBar(toolbar);

        sharedPref_Steps = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        if (sharedPref_Steps.getBoolean("isShiftActive", false)) {
            startActivity(new Intent(MainActivity.this, NFCScan.class).setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP));
        }

        databaseHelper = new DatabaseHelper(this);

        RecyclerView recyclerView = findViewById(R.id.mainRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        squareItems = new ArrayList<>();
        squareAdapter = new SquareAdapter(squareItems);
        recyclerView.setAdapter(squareAdapter);

        loadFilesFromFolder();
        stepCounterService = new StepCounterService();



        Button startShiftButton = findViewById(R.id.startShift);
        startShiftButton.setOnClickListener(v -> {

            Intent startIntent = new Intent(MainActivity.this, StepCounterService.class);
            startIntent.setAction(ConstantsHelper.START_FOREGROUND_ACTION);
            stepCounterService.startShift();
            startService(startIntent);

            startActivity(new Intent(this, NFCScan.class));

        });



        Intent intent = new Intent(this, StepCounterService.class);
        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);

        permissionQueue.add(Manifest.permission.ACTIVITY_RECOGNITION);
        permissionQueue.add(Manifest.permission.CAMERA);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            permissionQueue.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        requestPermissionsFromQueue();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.menu_admin) {
            openAdminDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void requestPermissionsFromQueue() {
        if (!permissionQueue.isEmpty()) {
            String permission = permissionQueue.poll();
            if (permission != null) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSIONS_REQUESTS_CODE);
                } else {
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
                    return name.toLowerCase(java.util.Locale.ROOT).endsWith(".pdf");
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

    private void openAdminDialog() {
        MaterialAlertDialogBuilder dialogBuilder = new MaterialAlertDialogBuilder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.popup_admin_dialog, null);
        dialogBuilder.setView(dialogView);

        final EditText editTextPassword = dialogView.findViewById(R.id.editTextUniqueCode);
        editTextPassword.requestFocus();

        dialogBuilder.setTitle("Introdu parola");
        dialogBuilder.setPositiveButton("Verifica", (dialog, whichButton) -> {

            String enteredPassword = editTextPassword.getText().toString();

            if (enteredPassword.equals(ConstantsHelper.getAdminPassword(this))) {
                startActivity(new Intent(this, AdminActivity.class));
                finish();
            } else {
                Toast.makeText(this, R.string.wrongPass, Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog alertDialog = dialogBuilder.create();
        alertDialog.show();
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
