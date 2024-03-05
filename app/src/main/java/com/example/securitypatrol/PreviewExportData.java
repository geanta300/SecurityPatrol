package com.example.securitypatrol;

import android.annotation.SuppressLint;
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.ItemAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.FileShareHelper;
import com.example.securitypatrol.Models.ObjectiveModel;
import com.example.securitypatrol.Models.ScanatModel;
import com.example.securitypatrol.Models.VerificationModel;
import com.example.securitypatrol.Services.StepCounterService;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.itextpdf.io.exceptions.IOException;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

public class PreviewExportData extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Cursor cursor;

    Button exportButton, editDataButton;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;
    private final String pdfFileName = ConstantsHelper.PDF_DIRECTORY_PATH;

    LoadingAlertDialog loadingAlertDialog;
    SharedPreferences stepsTechnologySharedPref;

    Bitmap signaturePhoto;
    String numePompier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview_data);

        RecyclerView recyclerPreviewForExport = findViewById(R.id.recyclerView);
        recyclerPreviewForExport.setLayoutManager(new LinearLayoutManager(this));

        databaseHelper = new DatabaseHelper(this);
        cursor = databaseHelper.getAllData();

        ItemAdapter itemAdapter = new ItemAdapter(cursor, this);
        recyclerPreviewForExport.setAdapter(itemAdapter);

        loadingAlertDialog = new LoadingAlertDialog(this);

        exportButton = findViewById(R.id.exportButton);
        exportButton.setOnClickListener(v -> {
            if (cursor != null && cursor.moveToFirst()) {
                guardSignatures();
                Log.d("ExportDataTask", "Exporting data");
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
                showToast();
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
        pdfDoc.addNewPage(pageSize);

        Document doc = new Document(pdfDoc, pageSize);

        // Set up the document layout
        doc.setMargins(25, 25, 25, 25);
        doc.setFontSize(14);


        Paragraph title = new Paragraph("\nRaport verificare" + "\n\n\n\n").setBold().setFontSize(20).setTextAlignment(TextAlignment.CENTER);

        doc.add(title);

        stepsTechnologySharedPref = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        int pasiSesiune = stepsTechnologySharedPref.getInt("stepCount", 0);

        Paragraph p1 = new Paragraph();
        p1.add("Pasi sesiune: " + pasiSesiune);
        doc.add(p1);

        //Inserare obiective
        List<ObjectiveModel> objectives = databaseHelper.getAllObjectives();
        for (ObjectiveModel objective : objectives) {
            doc.add(new Paragraph("Obiectivul: " + objective.getDescriere())
                    .setBold()
                    .setFontSize(16));
            doc.add(new Paragraph("Locatia: " + objective.getLocatie())
                    .setBold()
                    .setFontSize(16));

            ScanatModel scanatModel = databaseHelper.getAllScansData(objective.getUniqueId());
            doc.add(new Paragraph("Data si ora: " + scanatModel.getDataTime())
                    .setBold()
                    .setFontSize(16));

            //Inserare verificari
            List<VerificationModel> verifications = databaseHelper.getVerificationsByObjectiveId(objective.getUniqueId());
            for (VerificationModel verification : verifications) {
                doc.add(new Paragraph("Verificare: " + verification.getDescriereVerificare()));
                doc.add(new Paragraph("Comentariu: " + verification.getRaspunsVerificare()));
            }
            //Inserare poza
            List<String> photoUris = databaseHelper.getPhotoUris(objective.getUniqueId());
            if (!photoUris.isEmpty()) {
                Paragraph photoParagraph = new Paragraph();

                for (String photoUri : photoUris) {
                    Log.d("ExportDataTask", "5. The image: " + photoUri + " is being added to the PDF");
                    Uri imageUri = Uri.parse(photoUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (bitmap != null) {
                            int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                            int maxHeight = 300;
                            bitmap = scaleBitmap(bitmap, maxWidth, maxHeight);

                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                            byte[] byteArray = stream.toByteArray();

                            ImageData imageData = ImageDataFactory.create(byteArray);
                            Image image = new Image(imageData);

                            image.setMargins(5, 5, 5, 5);

                            photoParagraph.add(image);
                        }
                    } catch (IOException | java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
                doc.add(photoParagraph);
            }
        }
        if (!numePompier.isEmpty() && signaturePhoto.getWidth() > 0 && signaturePhoto.getHeight() > 0) {
            Bitmap signatureBitmap = signaturePhoto;
            Paragraph paragraph = new Paragraph();

            doc.add(new Paragraph());
            doc.add(new Paragraph());

            paragraph.add("Document realizat de " + numePompier +"\n" + "Semnatura ");

            try {
                if (signatureBitmap != null) {
                    int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                    int maxHeight = 300;
                    signatureBitmap = scaleBitmap(signatureBitmap, maxWidth, maxHeight);

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    signatureBitmap.compress(Bitmap.CompressFormat.PNG, 50, stream);
                    byte[] byteArray = stream.toByteArray();

                    ImageData imageData = ImageDataFactory.create(byteArray);
                    Image image = new Image(imageData);

                    image.setMargins(5, 5, 5, 5);

                    paragraph.add(image);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            doc.add(paragraph);
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

    public void guardSignatures() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.popup_guard_signatures, null);

        EditText numepompier_ET = dialogLayout.findViewById(R.id.numepompier_ET);
        SignaturePad mSignaturePad = dialogLayout.findViewById(R.id.signature_pad);
        Button clearSignatureButton = dialogLayout.findViewById(R.id.clearSignature);
        Button exportButton = dialogLayout.findViewById(R.id.exportButton);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);

        AlertDialog dialog = builder.show();

        clearSignatureButton.setOnClickListener(v -> {
            mSignaturePad.clear();
        });

        exportButton.setOnClickListener(v -> {
            if (mSignaturePad.isEmpty()) {
                Toast.makeText(this, "Va rugam sa semnati!", Toast.LENGTH_SHORT).show();
            } else if (numepompier_ET.getText().toString().isEmpty()) {
                numepompier_ET.setError("Numele este necesar");
            } else {
                Intent stopIntent = new Intent(PreviewExportData.this, StepCounterService.class);
                stopIntent.setAction(ConstantsHelper.STOP_FOREGROUND_ACTION);
                startService(stopIntent);

                signaturePhoto = mSignaturePad.getSignatureBitmap();
                numePompier = numepompier_ET.getText().toString();

                dialog.dismiss();

                ExportDataTask exportTask = new ExportDataTask();
                exportTask.execute();
            }
        });
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
                        databaseHelper.deletePhotosPath();
                        databaseHelper.resetScannedData();
                        databaseHelper.resetRaspunsuriVerificari();

                    } while (cursor.moveToNext());
                }

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
            Log.d("ExportDataTask", "onCancelled: " + "ExportDataTask cancelled");
        }

    }

    private void shareFiles() {
        FileShareHelper fileShareHelper = new FileShareHelper(this, directoryPathOfFiles, pdfFileName);
        fileShareHelper.shareFiles();
        if (fileShareHelper.sharedFilesFinished) {
            finish();
        }
    }

    private void showToast() {
        runOnUiThread(() -> Toast.makeText(PreviewExportData.this, "Folderul nu poate fi creat", Toast.LENGTH_SHORT).show());
    }

}