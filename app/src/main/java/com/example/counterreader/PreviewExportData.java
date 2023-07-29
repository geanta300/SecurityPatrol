package com.example.counterreader;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.counterreader.Adapters.ItemAdapter;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

public class PreviewExportData extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Cursor cursor;

    Button exportButton, editDataButton;

    private final String directoryPathOfFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/CounterReader";
    private final String excelFileName ="CounterData_" + getDateInfo().previousMonthName + "_" + getDateInfo().currentYear + ".xlsx";
    private final String pdfFileName = "CounterData_" + getDateInfo().previousMonthName + "_" + getDateInfo().currentYear + ".pdf";

    SharedPreferences sharedPreferences;

    LoadingAlertDialog loadingAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_data);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        RecyclerView recyclerPreviewForExport = findViewById(R.id.recyclerView);
        recyclerPreviewForExport.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        cursor = databaseHelper.getAllData();

        ItemAdapter itemAdapter = new ItemAdapter(cursor);
        recyclerPreviewForExport.setAdapter(itemAdapter);

        loadingAlertDialog = new LoadingAlertDialog(this);

        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            if (cursor != null) {
                exportData();
            }
        });

        editDataButton = findViewById(R.id.editButton);
        editDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreviewExportData.this, QRScan.class);
            SharedPreferences.Editor edit = sharedPreferences.edit();
            edit.putBoolean("editData", true);
            edit.apply();
            startActivity(intent);
        });
    }


    private void exportToPdf() {
        File pdfFile = new File(directoryPathOfFiles, pdfFileName);

        if (pdfFile.getParentFile() != null && !pdfFile.getParentFile().exists()) {
            boolean directoriesCreated = pdfFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                showToast("The folder cannot be created");
                return;
            }
        }

        // Create a new PDF document
        PdfWriter writer;
        try {
            writer = new PdfWriter(pdfFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast("Failed to create PDF");
            return;
        }

        // Create a new PDF document
        PdfDocument pdfDoc = new PdfDocument(writer);

        // Create a new page
        PageSize pageSize = PageSize.A4;
        PdfPage page = pdfDoc.addNewPage(pageSize);

        // Create a document renderer
        Document doc = new Document(pdfDoc, pageSize);

        // Set up the document layout
        doc.setMargins(50, 50, 50, 50);
        doc.setFontSize(12);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String chirias = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHIRIAS));
                String locatie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
                String felContor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEL_CONTOR));
                String serie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERIE));
                double indexVechi = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_VECHI));
                double indexNou = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));

                // Add the data to the PDF document
                Paragraph paragraph = new Paragraph();
                paragraph.add(new Text("Chirias: " + chirias + "\n"));
                paragraph.add(new Text("Locatie: " + locatie + "\n"));
                paragraph.add(new Text("Fel Contor: " + felContor + "\n"));
                paragraph.add(new Text("Serie: " + serie + "\n"));
                paragraph.add(new Text("Index Vechi: " + indexVechi + "\n"));
                paragraph.add(new Text("Index Nou: " + indexNou + "\n"));
                paragraph.setMarginBottom(20);

                doc.add(paragraph);

                if (!TextUtils.isEmpty(photoUri)) {
                    Uri imageUri = Uri.parse(photoUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (bitmap != null) {
                            int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                            int maxHeight = 500;
                            bitmap = scaleBitmap(bitmap, maxWidth, maxHeight);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            ImageData imageData = ImageDataFactory.create(stream.toByteArray());
                            Image image = new Image(imageData);

                            doc.add(new Paragraph().add(image));
                        }
                    } catch (IOException | java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            } while (cursor.moveToNext());
        }

        // Close the PDF document
        doc.close();
    }

    public void exportToExcel() {
        File excelFile = new File(directoryPathOfFiles, excelFileName);
        if (excelFile.getParentFile() != null && !excelFile.getParentFile().exists()) {
            boolean directoriesCreated = excelFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                showToast("The folder cannot be created");
                return;
            }
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Counters Data");

        if (cursor != null && cursor.moveToFirst()) {
            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.createCell(0).setCellValue("Nr. crt.");
            headerRow.createCell(1).setCellValue("Chirias");
            headerRow.createCell(2).setCellValue("Locatie");
            headerRow.createCell(3).setCellValue("Fel Contor");
            headerRow.createCell(4).setCellValue("Serie");
            headerRow.createCell(5).setCellValue("Index Vechi");
            headerRow.createCell(6).setCellValue("Index Nou");

            do {
                String chirias = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHIRIAS));
                String locatie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
                String felContor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEL_CONTOR));
                String serie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERIE));
                double indexVechi = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_VECHI));
                double indexNou = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));

                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(rowIndex-1);
                dataRow.createCell(1).setCellValue(chirias);
                dataRow.createCell(2).setCellValue(locatie);
                dataRow.createCell(3).setCellValue(felContor);
                dataRow.createCell(4).setCellValue(serie);
                dataRow.createCell(5).setCellValue(indexVechi);
                dataRow.createCell(6).setCellValue(indexNou);
            } while (cursor.moveToNext());
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast("File not found");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Failed to export Excel. The document may be open in another application.");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        } finally {
            try {
                workbook.close();
            } catch (IOException | java.io.IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Bitmap scaleBitmap(Bitmap bitmap, int maxWidth, int maxHeight) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        float scaleFactor = Math.min((float) maxWidth / width, (float) maxHeight / height);
        return Bitmap.createScaledBitmap(bitmap, (int) (width * scaleFactor), (int) (height * scaleFactor), true);
    }

    public static class DateInfo {
        public int currentYear;
        public int currentMonth;
        public int previousYear;
        public int previousMonth;
        public String currentMonthName;
        public String previousMonthName;
    }

    public static DateInfo getDateInfo() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);

        // Subtract 1 month
        calendar.add(Calendar.MONTH, -1);
        int previousYear = calendar.get(Calendar.YEAR);
        int previousMonth = calendar.get(Calendar.MONTH);

        String currentMonthName = new DateFormatSymbols().getMonths()[currentMonth];
        String previousMonthName = new DateFormatSymbols().getMonths()[previousMonth];

        DateInfo dateInfo = new DateInfo();
        dateInfo.currentYear = currentYear;
        dateInfo.currentMonth = currentMonth;
        dateInfo.previousYear = previousYear;
        dateInfo.previousMonth = previousMonth;
        dateInfo.currentMonthName = currentMonthName;
        dateInfo.previousMonthName = previousMonthName;

        return dateInfo;
    }

    private void exportData() {
        ExportDataTask exportTask = new ExportDataTask();
        exportTask.execute();
    }

    @SuppressWarnings("deprecation")
    @SuppressLint("StaticFieldLeak")
    public class ExportDataTask extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            loadingAlertDialog.startAlertDialog();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                exportToPdf();
                exportToExcel();
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        @Override
        protected void onPostExecute(Boolean exportSuccessful) {
            super.onPostExecute(exportSuccessful);

            if (exportSuccessful) {
                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        // Retrieve the values of the last index
                        double newIndex = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));

                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_INDEX_VECHI, newIndex);
                        values.put(DatabaseHelper.COLUMN_INDEX_NOU, 0);
                        values.put(DatabaseHelper.COLUMN_IMAGE_URI, " ");

                        String qrCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COD_QR));
                        String whereClause = DatabaseHelper.COLUMN_COD_QR + "=?";
                        String[] whereArgs = {qrCode};
                        databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

                    } while (cursor.moveToNext());

                    cursor.close();
                }

                showToast("Data exported successfully.");
                shareFiles();
                startActivity(new Intent(PreviewExportData.this, QRScan.class));
            }
            loadingAlertDialog.closeAlertDialog();
        }

        @Override
        protected void onCancelled() {
            loadingAlertDialog.closeAlertDialog();
        }
    }

    private void shareFiles() {
        File excelFile = new File(directoryPathOfFiles, excelFileName);
        File pdfFile = new File(directoryPathOfFiles, pdfFileName);

        // Create Uri objects for the files
        Uri excelFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider", excelFile);
        Uri pdfFileUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".provider", pdfFile);

        // Create an ArrayList to hold the file Uris
        ArrayList<Uri> fileUris = new ArrayList<>();
        fileUris.add(excelFileUri);
        fileUris.add(pdfFileUri);

        // Create the intent to share the files
        Intent intent = new Intent(Intent.ACTION_SEND_MULTIPLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_SUBJECT, "Raport contoare");
        intent.putExtra(Intent.EXTRA_TEXT, "Here are the exported files.");
        intent.putExtra(Intent.EXTRA_EMAIL,new String[] {"geanta300@gmail.com"});
        intent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, fileUris);

        // Start the activity to share the files
        startActivity(Intent.createChooser(intent, "Share Files"));
    }

    private void showToast(final String message) {
        runOnUiThread(() -> Toast.makeText(PreviewExportData.this, message, Toast.LENGTH_SHORT).show());
    }

}