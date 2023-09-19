package com.example.securitypatrol.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.securitypatrol.BuildConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileShareHelper {
    private final Context context;
    private final String directoryPathOfFiles;
    private final String pdfFileName;
    Uri pdfFileUri;

    public FileShareHelper(Context context, String directoryPathOfFiles, String pdfFileName) {
        this.context = context;
        this.directoryPathOfFiles = directoryPathOfFiles;
        this.pdfFileName = pdfFileName;
    }
    public void shareFiles() {
        File pdfFile = new File(directoryPathOfFiles, pdfFileName);
        pdfFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", pdfFile);

        // WhatsApp-specific intent
        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setPackage("com.whatsapp"); // Specify WhatsApp package
        whatsappIntent.setType("application/pdf");
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);

        // Create a generic intent for email
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Raport patrulare");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cosmin.geanta@anatower.ro"});
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Atasat regasiti fisierul cu datele despre patrulare.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);

        // Create a dialog for choosing between email and WhatsApp
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Share File");
        builder.setItems(new CharSequence[]{"Email", "WhatsApp"}, (dialog, which) -> {
            if (which == 0) {
                context.startActivity(emailIntent);
            } else if (which == 1) {
                context.startActivity(whatsappIntent);
            }
        });

        builder.create().show();
    }
}
