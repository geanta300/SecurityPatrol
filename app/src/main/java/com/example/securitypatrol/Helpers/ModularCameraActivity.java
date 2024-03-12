package com.example.securitypatrol.Helpers;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

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
import androidx.lifecycle.LifecycleOwner;

import com.example.securitypatrol.Interfaces.PhotoTakenCallback;
import com.google.common.util.concurrent.ListenableFuture;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;

public class ModularCameraActivity extends AppCompatActivity{

    public ProcessCameraProvider cameraProvider;

    private PhotoTakenCallback photoTakenCallback;

    public int cameraFacing = CameraSelector.LENS_FACING_BACK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    public void startCamera(int cameraFacing, ImageView takePhotoButt, ImageView activateBlitzButt, PreviewView previewView, Dialog popUpDialog, Context context) {
        ListenableFuture<ProcessCameraProvider> listenableFuture = ProcessCameraProvider.getInstance(context);

        listenableFuture.addListener(() -> {
            try {
                cameraProvider = listenableFuture.get();

                Preview preview = new Preview.Builder()
                        .setTargetAspectRatio(AspectRatio.RATIO_4_3)
                        .build();

                ImageCapture imageCapture = new ImageCapture.Builder()
                        .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                        .build();

                CameraSelector cameraSelector = new CameraSelector.Builder()
                        .requireLensFacing(cameraFacing)
                        .build();

                cameraProvider.unbindAll();

                Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) context, cameraSelector, preview, imageCapture);

                takePhotoButt.setOnClickListener(view -> {
                    Toast.makeText(context, "Se salveaza imaginea", Toast.LENGTH_SHORT).show();
                    takePicture(imageCapture, popUpDialog, context);
                });

                activateBlitzButt.setOnClickListener(view -> setFlashIcon(camera, context));

                preview.setSurfaceProvider(previewView.getSurfaceProvider());
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(context));
    }


    public void takePicture(ImageCapture imageCapture, Dialog popUpdialog, Context context) {
        final String directoryPathOfFiles = ConstantsHelper.PHOTOS_DIRECTORY_PATH;
        File file = new File(directoryPathOfFiles, System.currentTimeMillis() + ".jpg");

        if (file.getParentFile() != null && !file.getParentFile().exists()) {
            boolean directoriesCreated = file.getParentFile().mkdirs();
            if (!directoriesCreated) {
                Toast.makeText(context, "Folderul nu poate fi creat", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();
        imageCapture.takePicture(outputFileOptions, Executors.newCachedThreadPool(), new ImageCapture.OnImageSavedCallback() {
            @Override
            public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                Bitmap imageBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                if (imageBitmap != null) {
                    ContentResolver contentResolver = context.getContentResolver();
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

                                resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                                outputStream.close();

                                onPhotoTaken(String.valueOf(imageUri));
                                popUpdialog.dismiss();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onError(@NonNull ImageCaptureException exception) {
                runOnUiThread(() -> Toast.makeText(context, "Failed to save: " + exception.getMessage(), Toast.LENGTH_SHORT).show());
            }
        });
    }

    public void setPhotoTakenCallback(PhotoTakenCallback callback) {
        this.photoTakenCallback = callback;
    }

    private void onPhotoTaken(String imageUriString) {
        if (photoTakenCallback != null) {
            photoTakenCallback.onPhotoTaken(imageUriString);
        }
    }

    private void setFlashIcon(Camera camera, Context context) {
        if (camera.getCameraInfo().hasFlashUnit()) {
            camera.getCameraControl().enableTorch(camera.getCameraInfo().getTorchState().getValue() == 0);
        } else {
            runOnUiThread(() -> Toast.makeText(context, "Blitzul nu este disponibil", Toast.LENGTH_SHORT).show());
        }
    }
}