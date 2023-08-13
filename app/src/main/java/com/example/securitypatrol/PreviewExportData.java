package com.example.securitypatrol;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.ItemAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.FileShareHelper;
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
import java.util.Calendar;

public class PreviewExportData extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Cursor cursor;

    Button exportButton, editDataButton;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;
    private final String excelFileName = ConstantsHelper.EXCEL_DIRECTORY_PATH;
    private final String pdfFileName = ConstantsHelper.PDF_DIRECTORY_PATH;

    LoadingAlertDialog loadingAlertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_data);

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
            Intent intent = new Intent(PreviewExportData.this, NFCScan.class);
            startActivity(intent);
        });
    }

    private void exportToPdf() {
        File pdfFile = new File(directoryPathOfFiles, pdfFileName);

        if (pdfFile.getParentFile() != null && !pdfFile.getParentFile().exists()) {
            boolean directoriesCreated = pdfFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                showToast("Folderul nu poate fi creat");
                return;
            }
        }

        // Create a new PDF document
        PdfWriter writer;
        try {
            writer = new PdfWriter(pdfFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        PdfDocument pdfDoc = new PdfDocument(writer);

        PageSize pageSize = PageSize.A4;
        PdfPage page = pdfDoc.addNewPage(pageSize);

        Document doc = new Document(pdfDoc, pageSize);

        // Set up the document layout
        doc.setMargins(25, 25, 25, 25);
        doc.setFontSize(12);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
                String datatime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATATIME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRPIPTION));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));
                String photoUri = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_IMAGE_URI));

                // Add the data to the PDF document
                Paragraph paragraph = new Paragraph();
                paragraph.add(new Text(getString(R.string.user_name_text, userName) + "\n"));
                paragraph.add(new Text(getString(R.string.datatime_text, datatime) + "\n"));
                paragraph.add(new Text(getString(R.string.description_text, description) + "\n"));
                paragraph.add(new Text(getString(R.string.location_text, location) + "\n"));
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

        doc.close();
        try {
            writer.close();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }

    public void exportToExcel() {
        File excelFile = new File(directoryPathOfFiles, excelFileName);
        if (excelFile.getParentFile() != null && !excelFile.getParentFile().exists()) {
            boolean directoriesCreated = excelFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                showToast("Folderul nu poate fi creat");
                return;
            }
        }

        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Patrol Data");

        if (cursor != null && cursor.moveToFirst()) {
            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.createCell(0).setCellValue("Nr. crt.");
            headerRow.createCell(1).setCellValue("Nume");
            headerRow.createCell(2).setCellValue("Data si ora: ");
            headerRow.createCell(3).setCellValue("Descriere");
            headerRow.createCell(4).setCellValue("Locatie");

            do {
                String userName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USER_NAME));
                String dateAndTime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DATATIME));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRPIPTION));
                String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATION));

                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(rowIndex - 1);
                dataRow.createCell(1).setCellValue(userName);
                dataRow.createCell(2).setCellValue(dateAndTime);
                dataRow.createCell(3).setCellValue(description);
                dataRow.createCell(4).setCellValue(location);
            } while (cursor.moveToNext());
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            showToast("Fisierul nu a fost gasit");
        } catch (IOException e) {
            e.printStackTrace();
            showToast("Excelul a esuat la export. Documentul nu poate fi deschis in timpul salvarii.");
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
                cursor.close();
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
//                if (cursor != null && cursor.moveToFirst()) {
//                    do {
//                        // Retrieve the values of the last index
//                        double optionalComment = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));
//
//                        ContentValues values = new ContentValues();
//                        values.put(DatabaseHelper.COLUMN_INDEX_VECHI, optionalComment);
//                        values.put(DatabaseHelper.COLUMN_INDEX_NOU, 0);
//                        values.put(DatabaseHelper.COLUMN_IMAGE_URI, " ");
//
//                        String qrCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COD_QR));
//                        String whereClause = DatabaseHelper.COLUMN_COD_QR + "=?";
//                        String[] whereArgs = {qrCode};
//                        databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);
//
//                    } while (cursor.moveToNext());
//                }
                showToast("Datele au fost exportate cu succes.");

                shareFiles();
            }
            cursor.close();
            databaseHelper.close();
            loadingAlertDialog.closeAlertDialog();
        }

        @Override
        protected void onCancelled() {
            loadingAlertDialog.closeAlertDialog();

        }
    }

    private void shareFiles() {
        FileShareHelper fileShareHelper = new FileShareHelper(this, directoryPathOfFiles, excelFileName, pdfFileName);
        fileShareHelper.shareFiles();
        finish();
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(PreviewExportData.this, message, Toast.LENGTH_SHORT).show());
    }

}