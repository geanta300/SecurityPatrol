package com.example.securitypatrol;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.Helpers.DatabaseHelper;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity {

    private ImageView takePhoto;
    private ImageView blitz;
    private PreviewView previewView;
    ProcessCameraProvider cameraProvider;

    String scannedNFCTag;
    SharedPreferences sharedPref;

    int cameraFacing = CameraSelector.LENS_FACING_BACK;

    private final ActivityResultLauncher<String> activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
            result -> startCamera(cameraFacing));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        previewView = findViewById(R.id.cameraPreview);
        takePhoto = findViewById(R.id.takePhoto);
        blitz = findViewById(R.id.blitz);


        if (ContextCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }

        sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        scannedNFCTag = sharedPref.getString("scannedNFCTag", scannedNFCTag);
        displayDataInTextView(scannedNFCTag);
    }

    public void displayDataInTextView(String nfcCode) {
        DatabaseHelper databaseHelper = new DatabaseHelper(this);
        Cursor cursor = databaseHelper.getDataByNFC(nfcCode);

        if (cursor != null && cursor.moveToFirst()) {
            StringBuilder displayText = new StringBuilder();

            String datatime = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DTIME));
            String description = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_DESCRIERE_OBIECTIV));
            String location = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_LOCATIE));

            displayText.append("Descriere: ").append(description).append("\n");
            displayText.append("Locatie: ").append(location).append("\n");

//            if( optionalComment != null && !optionalComment.isEmpty() ) {
//                displayText.append("Comentariu: ").append(optionalComment).append("\n");
//            }
            if( datatime != null && !datatime.isEmpty() ) {
                displayText.append("Data si ora: ").append(datatime).append("\n");
            }

            TextView nfcTextView = findViewById(R.id.nfcTextView);
            nfcTextView.setText(displayText.toString());

            cursor.close();
        }
    }

    public void startCamera(int cameraFacing) {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(this);

        listenableFuture.addListener(() -> {
            try {
                cameraProvider = listenableFuture.get();

                Preview preview = new Preview.Builder().setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                ImageCapture imageCapture = new ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .setTargetRotation(getWindowManager().getDefaultDisplay().getRotation())
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle(CameraActivity.this, cameraSelector, preview, imageCapture);

                takePhoto.setOnClickListener(view -> {
                    Toast.makeText(CameraActivity.this, "Se salveaza imaginea", Toast.LENGTH_SHORT).show();
                    takePicture(imageCapture);
                });

                blitz.setOnClickListener(view -> setFlashIcon(camera));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void stopCamera() {
        if (cameraProvider != null) {
            cameraProvider.unbindAll();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(CameraActivity.this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            activityResultLauncher.launch(android.Manifest.permission.CAMERA);
        } else {
            startCamera(cameraFacing);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
    }

    public void takePicture(ImageCapture imageCapture) {
        final String directoryPathOfFiles = ConstantsHelper.PHOTOS_DIRECTORY_PATH;
        File file = new File(directoryPathOfFiles, System.currentTimeMillis() + ".jpg");

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            boolean directoriesCreated = file.getParentFile().mkdirs();
            if (!directoriesCreated) {
                Toast.makeText(CameraActivity.this, "Folderul nu poate fi creat", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (imageBitmap != null) {
                    ContentResolver contentResolver = getContentResolver();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, file.getName());
                    contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                    String albumName = "SecurityPatrol";
                    contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + File.separator + albumName);

                    // Insert the image into MediaStore
                    Uri imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    if (imageUri != null) {
                        try {
                            OutputStream outputStream = contentResolver.openOutputStream(imageUri);
                            if (outputStream != null) {

                                int maxWidth = 1024;
                                int maxHeight = 1024;
                                float scale = Math.min((float) maxWidth / imageBitmap.getWidth(), (float) maxHeight / imageBitmap.getHeight());
                                Matrix matrix = new Matrix();
                                matrix.postRotate(90);
                                matrix.postScale(scale, scale);
                                Bitmap resizedBitmap = Bitmap.createBitmap(imageBitmap, 0, 0, imageBitmap.getWidth(), imageBitmap.getHeight(), matrix, true);

                                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 50, outputStream);

                                outputStream.close();
                                runOnUiThread(() -> {
                                    Toast.makeText(CameraActivity.this, "Imaginea a fost salvata", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(CameraActivity.this, AddDataToDB.class);
                                    intent.putExtra("imagePath", String.valueOf(imageUri));
                                    startActivity(intent);
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

                startCamera(cameraFacing);
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
                startCamera(cameraFacing);
            }
        });
    }

    private void setFlashIcon(Camera camera) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(camera.getCameraInfo().getTorchState().getValue() == 0);
        } else {
            runOnUiThread(() -> Toast.makeText(CameraActivity.this, "Blitzul nu este disponibil", Toast.LENGTH_SHORT).show());
        }
    }
}