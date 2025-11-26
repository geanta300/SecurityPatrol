package com.example.securitypatrol;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;

public class LoadingAlertDialog {

    private final Activity activity;
    private AlertDialog dialog;

    LoadingAlertDialog(PreviewExportData myActivity){
        activity=myActivity;
    }
    void startAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        LayoutInflater inflater = activity.getLayoutInflater();
        android.view.View root = activity.findViewById(android.R.id.content);
        android.view.View dialogView = inflater.inflate(R.layout.popup_loading_dialog, (android.view.ViewGroup) root, false);
        builder.setView(dialogView);

        builder.setCancelable(true);

        dialog=builder.create();
        dialog.show();
    }

    void closeAlertDialog(){
        dialog.dismiss();
    }
}
