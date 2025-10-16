package com.example.securitypatrol;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitypatrol.Adapters.ItemAdapter;
import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.example.securitypatrol.Helpers.FileShareHelper;
import com.example.securitypatrol.Models.GuardsSignatures;
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
import com.itextpdf.kernel.pdf.canvas.draw.SolidLine;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.LineSeparator;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PreviewExportData extends AppCompatActivity {
    private DatabaseHelper databaseHelper;
    private Cursor cursor;

    Button exportButtonMain, editDataButton;

    private final String directoryPathOfFiles = ConstantsHelper.DOCUMENTS_DIRECTORY_PATH;
    private final String pdfFileName = ConstantsHelper.getPdfFileName();

    LoadingAlertDialog loadingAlertDialog;
    SharedPreferences stepsTechnologySharedPref;

    List<GuardsSignatures> signatureGuard = new ArrayList<>();

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

        exportButtonMain = findViewById(R.id.exportButtonMain);
        exportButtonMain.setOnClickListener(v -> {
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

        String logoURI = getSharedPreferences("MyPrefs", MODE_PRIVATE).getString("logoURI", null);
        Log.d("ExportDataTask", "Image URI: " + logoURI);

        Paragraph logoParagraph = new Paragraph();
        if (logoURI != null && !logoURI.isEmpty()) {
            ContentResolver contentResolver = getContentResolver();
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(Uri.parse(logoURI));
                if (inputStream != null) {
                    Bitmap logoBitmap = BitmapFactory.decodeStream(inputStream);
                    if (logoBitmap != null) {
                        logoParagraph.add(compressImage(200, 200, 100, logoBitmap));
                        logoParagraph.setTextAlignment(TextAlignment.CENTER);
                        logoParagraph.setHorizontalAlignment(HorizontalAlignment.CENTER);
                    }
                }
            } catch (SecurityException | FileNotFoundException e) {
                Log.w("ExportDataTask", "Logo URI not accessible; skipping logo. " + e.getMessage());
            } catch (java.io.IOException e) {
                Log.w("ExportDataTask", "Error reading logo; skipping logo.", e);
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (java.io.IOException ignored) {}
                }
            }
        }
        doc.add(logoParagraph);

        Paragraph title = new Paragraph("\nRaport verificare" + "\n\n\n\n").setBold().setFontSize(22).setTextAlignment(TextAlignment.CENTER);

        doc.add(title);

        stepsTechnologySharedPref = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        int pasiSesiune = stepsTechnologySharedPref.getInt("stepCount", 0);

        Paragraph p1 = new Paragraph();
        p1.add("Pasi sesiune: " + pasiSesiune);
        doc.add(p1);

        LineSeparator lineSeparator = new LineSeparator(new SolidLine(1f));
        doc.add(lineSeparator);

        //Inserare obiective
        List<ObjectiveModel> objectives = databaseHelper.getAllObjectives();
        for (ObjectiveModel objective : objectives) {
            doc.add(new Paragraph("Obiectivul: " + objective.getDescriere()).setBold().setFontSize(14));
            doc.add(new Paragraph("Locatia: " + objective.getLocatie()).setBold().setFontSize(14));

            ScanatModel scanatModel = databaseHelper.getAllScansData(objective.getUniqueId());
            doc.add(new Paragraph("Data si ora: " + scanatModel.getDataTime()).setBold().setFontSize(14));

            //Inserare verificari
            List<VerificationModel> verifications = databaseHelper.getVerificationsByObjectiveId(objective.getUniqueId());
            for (VerificationModel verification : verifications) {
                doc.add(new Paragraph(verification.getDescriereVerificare()));
                doc.add(new Paragraph("Comentariu: " + verification.getRaspunsVerificare()));

                doc.add(lineSeparator);
            }

            //Inserare poza
            Paragraph photoParagraph = new Paragraph();

            List<String> photoUris = databaseHelper.getPhotoUris(objective.getUniqueId());
            if (!photoUris.isEmpty()) {

                for (String photoUri : photoUris) {
                    Log.d("ExportDataTask", "5. The image: " + photoUri + " is being added to the PDF");
                    Uri imageUri = Uri.parse(photoUri);
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                        if (bitmap != null) {
                            int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                            int maxHeight = 300;

                            photoParagraph.add(compressImage(maxWidth, maxHeight, 80, bitmap));
                        }
                    } catch (IOException | java.io.IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            if (!photoParagraph.isEmpty()) {
                doc.add(photoParagraph);

                doc.add(lineSeparator);
            }
        }

        if (!signatureGuard.isEmpty()) {

            doc.add(new Paragraph("Document realizat de: " + "\n"));
            for (GuardsSignatures guardSignature : signatureGuard) {
                Bitmap signatureBitmap = guardSignature.getSignatureImage();
                String guardName = guardSignature.getGuardName();

                Paragraph paragraph = new Paragraph();
                doc.add(new Paragraph());

                paragraph.add("Nume: " + guardName + "\n" + "Semnatura: " + "\n");

                try {
                    if (signatureBitmap != null) {
                        int maxWidth = (int) (pageSize.getWidth() - doc.getLeftMargin() - doc.getRightMargin());
                        int maxHeight = 100;

                        paragraph.add(compressImage(maxWidth, maxHeight, 80, signatureBitmap));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                doc.add(paragraph);
            }
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

    private Image compressImage(int maxWidth, int maxHeight, int quality, Bitmap imageBitmap) {
        imageBitmap = scaleBitmap(imageBitmap, maxWidth, maxHeight);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        imageBitmap.compress(Bitmap.CompressFormat.PNG, quality, stream);
        byte[] byteArray = stream.toByteArray();

        ImageData imageData = ImageDataFactory.create(byteArray);
        Image image = new Image(imageData);

        image.setMargins(5, 5, 5, 5);

        return image;
    }

    public void guardSignatures() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.popup_guard_signatures, null);

        final AutoCompleteTextView numepompier_ET = dialogLayout.findViewById(R.id.numepompier_ET);
        SignaturePad mSignaturePad = dialogLayout.findViewById(R.id.signature_pad);
        Button clearSignatureButton = dialogLayout.findViewById(R.id.clearSignature);
        Button exportButton = dialogLayout.findViewById(R.id.exportButton);
        Button nextGuard = dialogLayout.findViewById(R.id.nextGuard);

        mSignaturePad.setPenColor(Color.BLUE);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogLayout);

        AlertDialog dialog = builder.show();


        String[] allUserNames = databaseHelper.getUserNames();
        String[] filteredUserNames = Arrays.stream(allUserNames).filter(name -> !name.equals("Admin")).toArray(String[]::new);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, filteredUserNames);
        numepompier_ET.setAdapter(adapter);

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

                signatureGuard.add(new GuardsSignatures(numepompier_ET.getText().toString(), mSignaturePad.getSignatureBitmap()));

                dialog.dismiss();

                ExportDataTask exportTask = new ExportDataTask();
                exportTask.execute();
            }
        });

        nextGuard.setOnClickListener(v -> {
            if (mSignaturePad.isEmpty()) {
                Toast.makeText(this, "Va rugam sa semnati!", Toast.LENGTH_SHORT).show();
            } else if (numepompier_ET.getText().toString().isEmpty()) {
                numepompier_ET.setError("Numele este necesar");
            } else {
                signatureGuard.add(new GuardsSignatures(numepompier_ET.getText().toString(), mSignaturePad.getSignatureBitmap()));
                dialog.dismiss();
                guardSignatures();
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

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        if (sp.getBoolean("navigateToMainAfterShare", false)) {
            sp.edit().putBoolean("navigateToMainAfterShare", false).apply();
            Intent intent = new Intent(PreviewExportData.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        }
    }

}
