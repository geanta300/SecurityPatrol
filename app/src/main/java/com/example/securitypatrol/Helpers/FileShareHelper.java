package com.example.securitypatrol.Helpers;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.content.SharedPreferences;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.example.securitypatrol.BuildConfig;
import com.example.securitypatrol.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class FileShareHelper {
    private final Context context;
    private final String directoryPathOfFiles;
    private final String pdfFileName;
    public boolean sharedFilesFinished = false;
    Uri pdfFileUri;

    public FileShareHelper(Context context, String directoryPathOfFiles, String pdfFileName) {
        this.context = context;
        this.directoryPathOfFiles = directoryPathOfFiles;
        this.pdfFileName = pdfFileName;
    }

    public void shareFiles() {
        File pdfFile = new File(directoryPathOfFiles, pdfFileName);
        pdfFileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", pdfFile);

        Intent whatsappIntent = new Intent(Intent.ACTION_SEND);
        whatsappIntent.setPackage("com.whatsapp");
        whatsappIntent.setType("application/pdf");
        whatsappIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);
        whatsappIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setDataAndType(Uri.parse("mailto:"), "application/pdf");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Raport patrulare");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"cosmin.geanta@anatower.ro"});
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Atasat regasiti fisierul cu datele despre patrulare.");
        emailIntent.putExtra(Intent.EXTRA_STREAM, pdfFileUri);
        emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        AlertDialog dialog;

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.popup_sharebuttons_dialog, null);
        builder.setView(dialogView);

        Button emailButton = dialogView.findViewById(R.id.email_button);
        Button whatsappButton = dialogView.findViewById(R.id.whatsapp_button);

        builder.setCancelable(false);

        builder.create();

        dialog = builder.show();

        emailButton.setOnClickListener(v -> {
            SharedPreferences sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            sp.edit().putBoolean("navigateToMainAfterShare", true).apply();
            context.startActivity(emailIntent);
            sharedFilesFinished = true;
            Toast.makeText(context, "Datele au fost exportate cu succes.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        whatsappButton.setOnClickListener(v -> {
            SharedPreferences sp = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
            sp.edit().putBoolean("navigateToMainAfterShare", true).apply();
            context.startActivity(whatsappIntent);
            sharedFilesFinished = true;
            Toast.makeText(context, "Datele au fost exportate cu succes.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }
}
