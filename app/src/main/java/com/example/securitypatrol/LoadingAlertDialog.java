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
        builder.setView(inflater.inflate(R.layout.loading_dialog_activity,null));

        builder.setCancelable(true);

        dialog=builder.create();
        dialog.show();
    }

    void closeAlertDialog(){
        dialog.dismiss();
    }
}
