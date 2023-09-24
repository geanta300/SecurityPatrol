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
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.ItemAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.FileShareHelper;
import com.example.securitypatrol.Services.StepCounterService;
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

public class PreviewExportData extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Cursor cursor;

    Button exportButton, editDataButton;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;
//    private final String excelFileName = ConstantsHelper.EXCEL_DIRECTORY_PATH;
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
                String comment = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_OPTIONAL_COMM));

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

                            doc.add(new Paragraph().add(image + "\n"));
                        }
                    } catch (IOException | java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
                if (comment != null && !comment.isEmpty()) {
                    doc.add(new Paragraph().add(new Text(getString(R.string.comment_text, comment) + "\n")));
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
                        ContentValues values = new ContentValues();
                        values.put(DatabaseHelper.COLUMN_OPTIONAL_COMM, "");
                        values.put(DatabaseHelper.COLUMN_IMAGE_URI, "");
                        values.put(DatabaseHelper.COLUMN_SCANNED, 0);

                        String nfcCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_NFC_TAG));
                        String whereClause = DatabaseHelper.COLUMN_NFC_TAG + "=?";
                        String[] whereArgs = {nfcCode};
                        databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

                    } while (cursor.moveToNext());
                }

                Intent stopIntent = new Intent(PreviewExportData.this, StepCounterService.class);
                stopIntent.setAction(ConstantsHelper.STOP_FOREGROUND_ACTION);
                startService(stopIntent);

                SharedPreferences sharedPreferences = getSharedPreferences("Steps_technology", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isShiftActive", false);
                editor.putBoolean("isInitialStepCountSet", false);
                editor.apply();

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
        FileShareHelper fileShareHelper = new FileShareHelper(this, directoryPathOfFiles, pdfFileName);
        fileShareHelper.shareFiles();
        if(fileShareHelper.sharedFilesFinished){
            finish();
        }
    }

    private void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(PreviewExportData.this, message, Toast.LENGTH_SHORT).show());
    }

}