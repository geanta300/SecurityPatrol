package com.example.securitypatrol;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.securitypatrol.Helpers.DatabaseHelper;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.InputStream;

public class AdminActivity extends AppCompatActivity {

    Button btnImportObjectives, btnImportGuards, btnExportData;
    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        btnImportObjectives = findViewById(R.id.admin_panel_objectiveExcel_Btn);
        btnImportGuards = findViewById(R.id.admin_panel_guards_Btn);
        btnExportData = findViewById(R.id.admin_panel_export_Btn);

        databaseHelper = new DatabaseHelper(this);

        btnImportObjectives.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseObjectiveExcelFile();
            }
        });

        btnImportGuards.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseGuardsExcelFile();
            }
        });

        btnExportData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AdminActivity.this, PreviewExportData.class);
                startActivity(intent);
            }
        });
    }

    private void chooseGuardsExcelFile() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        startActivityIntentForGuards.launch(intent);
    }

    public void readGuardsExcelFile(Uri uri){
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
            workbook.close();
        } catch (Exception e) {
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
            workbook.close();
        } catch (Exception e) {
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

}