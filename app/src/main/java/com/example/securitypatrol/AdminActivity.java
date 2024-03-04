package com.example.securitypatrol;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class AdminActivity extends AppCompatActivity {

    Button btnImportObjectives, btnImportGuards, btnExportData, btnChangePassword, btnDeleteData;
    DatabaseHelper databaseHelper;
    EditText etNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnImportObjectives = findViewById(R.id.admin_panel_objectiveExcel_Btn);
        btnImportGuards = findViewById(R.id.admin_panel_guards_Btn);
        btnExportData = findViewById(R.id.admin_panel_export_Btn);
        btnChangePassword = findViewById(R.id.admin_panel_adminPass_Btn);
        btnDeleteData = findViewById(R.id.admin_panel_deleteData_Btn);

        etNewPassword = findViewById(R.id.admin_panel_adminPass_ET);

        databaseHelper = new DatabaseHelper(this);

        btnImportObjectives.setOnClickListener(v -> chooseObjectiveExcelFile());

        btnImportGuards.setOnClickListener(v -> chooseGuardsExcelFile());

        btnExportData.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, PreviewExportData.class);
            startActivity(intent);
        });

        btnChangePassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString();
            if (!newPassword.isEmpty()) {
                ConstantsHelper.setAdminPassword(newPassword, AdminActivity.this);
                Toast.makeText(AdminActivity.this, "Parola schimbata cu succes!", Toast.LENGTH_SHORT).show();
                etNewPassword.setText("");
            } else {
                etNewPassword.setError("Introduceti o parola valida");
            }
        });

        btnDeleteData.setOnClickListener(v -> showWarningPopup());
    }

    private void chooseGuardsExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityIntentForGuards.launch(intent);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        startActivity(new Intent(AdminActivity.this, MainActivity.class));
    }

    public void readGuardsExcelFile(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
            XSSFSheet sheet = workbook.getSheetAt(0);

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                XSSFRow row = sheet.getRow(i);

                Log.d("ExcelFileImport", "Row: " + row.getRowNum());

                Cell numeGardianCell = row.getCell(0);
                String numeGardian = (numeGardianCell != null) ? numeGardianCell.getStringCellValue() : "";
                if (numeGardian != null && !numeGardian.isEmpty()) {
                    Log.d("ExcelFileImport", "nume gardian adaugat: " + numeGardian);
                    databaseHelper.addUser(numeGardian);
                }
            }
            Toast.makeText(this, "Personal citit cu succes!", Toast.LENGTH_SHORT).show();
            workbook.close();
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la citire Excel!", Toast.LENGTH_SHORT).show();
            Log.e("ExcelFileImport", "Error reading Excel file: " + e.getMessage());
        }
    }

    private void chooseObjectiveExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityIntent.launch(intent);
    }

    public void readObjectiveExcelFile(Uri uri) {
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
            Toast.makeText(this, "Obiectivele au fost citite cu succes!", Toast.LENGTH_SHORT).show();
            workbook.close();
        } catch (Exception e) {
            Toast.makeText(this, "Eroare la citire Excel!", Toast.LENGTH_SHORT).show();
            Log.e("ExcelFileImport", "Error reading Excel file: " + e.getMessage());
        }
    }

    private final ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            readObjectiveExcelFile(uri);
                        }
                    }
                }
            }
    );
    private final ActivityResultLauncher<Intent> startActivityIntentForGuards = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    if (data != null) {
                        Uri uri = data.getData();
                        if (uri != null) {
                            readGuardsExcelFile(uri);
                        }
                    }
                }
            }
    );

    private void showWarningPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("ATENTIE!")
                .setMessage("Stergerea datelor este permanenta si nu pot fi recuperate."+"\n"+"Sunteti sigur ca doriti stergerea acestora?.")
                .setCancelable(true)
                .setPositiveButton("OK", (dialogInterface, i) -> {
                    deleteDatabase(databaseHelper.getDatabaseName());
                    deleteSharedPreferences("MyPrefs");
                    deleteSharedPreferences("Steps_technology");
                    Toast.makeText(AdminActivity.this, "Datele au fost sterse cu succes!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialogInterface, i) -> {

                })
                .show();
    }

}