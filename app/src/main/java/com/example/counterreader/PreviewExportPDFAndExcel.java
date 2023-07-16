package com.example.counterreader;

import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

public class PreviewExportPDFAndExcel extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ItemAdapter itemAdapter;
    private DatabaseHelper databaseHelper;

    Button exportButton,editDataButton;

    String directoryPathOfFiles = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/CounterReader";;
    Boolean excelExported,pdfExported;
    Cursor cursor;

    SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_pdf);
        excelExported=false;
        pdfExported=false;

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        cursor = databaseHelper.getAllData();

        itemAdapter = new ItemAdapter(cursor);
        recyclerView.setAdapter(itemAdapter);

        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            if (cursor != null) {
                exportToPdf();
                exportToExcel();
            }
           if(excelExported && pdfExported){
               if (cursor != null && cursor.moveToFirst()) {
                   do {
                       // Retrieve the values of the last index
                       double newIndex = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));

                       ContentValues values = new ContentValues();
                       values.put(DatabaseHelper.COLUMN_INDEX_VECHI, newIndex);
                       values.put(DatabaseHelper.COLUMN_INDEX_NOU, 0);

                       String qrCode = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_COD_QR));
                       String whereClause = DatabaseHelper.COLUMN_COD_QR + "=?";
                       String[] whereArgs = {qrCode};
                       databaseHelper.getWritableDatabase().update(DatabaseHelper.TABLE_NAME, values, whereClause, whereArgs);

                   } while (cursor.moveToNext());

                   cursor.close();
               }
           }
        });

        editDataButton = findViewById(R.id.editButton);
        editDataButton.setOnClickListener(v -> {
            Intent intent = new Intent(PreviewExportPDFAndExcel.this, QRScan.class);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("editData", true);
            editor.apply();
            startActivity(intent);
        });
    }



    private void exportToPdf() {
        String fileName = "CounterData_" + getDateInfo().previousMonthName +"_"+ getDateInfo().currentYear+ ".pdf";
        File pdfFile = new File(directoryPathOfFiles, fileName);

        // Ensure that the directory exists before saving the PDF file
        if (pdfFile.getParentFile() != null && !pdfFile.getParentFile().exists()) {
            boolean directoriesCreated = pdfFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                Toast.makeText(this, "The folder cannot be created", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create a new PDF document
        PdfWriter writer;
        try {
            writer = new PdfWriter(pdfFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to create PDF", Toast.LENGTH_SHORT).show();
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


        if (cursor.moveToFirst()) {
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
                            // Scale down the bitmap if needed
                            int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                            int maxHeight = 400;
                            bitmap = scaleBitmap(bitmap, maxWidth, maxHeight);

                            // Convert the bitmap to an Image
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG,50, stream);
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

        Toast.makeText(this, "PDF exported successfully", Toast.LENGTH_SHORT).show();
        pdfExported=true;
    }

    public void exportToExcel() {
        String fileName = "CounterData_" + getDateInfo().previousMonthName + "_" + getDateInfo().currentYear + ".xlsx";
        File excelFile = new File(directoryPathOfFiles, fileName);

        // Ensure that the directory exists before saving the Excel file
        if (excelFile.getParentFile() != null && !excelFile.getParentFile().exists()) {
            boolean directoriesCreated = excelFile.getParentFile().mkdirs();
            if (!directoriesCreated) {
                Toast.makeText(this, "The folder cannot be created", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Create a new Excel workbook
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Data");

        if (cursor != null && cursor.moveToFirst()) {
            int rowIndex = 0;
            Row headerRow = sheet.createRow(rowIndex++);
            headerRow.createCell(0).setCellValue("Chirias");
            headerRow.createCell(1).setCellValue("Locatie");
            headerRow.createCell(2).setCellValue("Fel Contor");
            headerRow.createCell(3).setCellValue("Serie");
            headerRow.createCell(4).setCellValue("Index Vechi");
            headerRow.createCell(5).setCellValue("Index Nou");

            do {
                String chirias = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_CHIRIAS));
                String locatie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));
                String felContor = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_FEL_CONTOR));
                String serie = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_SERIE));
                double indexVechi = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_VECHI));
                double indexNou = cursor.getDouble(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_INDEX_NOU));

                Row dataRow = sheet.createRow(rowIndex++);
                dataRow.createCell(0).setCellValue(chirias);
                dataRow.createCell(1).setCellValue(locatie);
                dataRow.createCell(2).setCellValue(felContor);
                dataRow.createCell(3).setCellValue(serie);
                dataRow.createCell(4).setCellValue(indexVechi);
                dataRow.createCell(5).setCellValue(indexNou);
            } while (cursor.moveToNext());
        }

        try {
            FileOutputStream fileOutputStream = new FileOutputStream(excelFile);
            workbook.write(fileOutputStream);
            Toast.makeText(this, "Excel exported successfully", Toast.LENGTH_SHORT).show();
            excelExported=true;
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to export Excel", Toast.LENGTH_SHORT).show();
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
}