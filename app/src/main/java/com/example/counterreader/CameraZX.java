package com.example.counterreader;

import android.os.Handler;
import android.widget.Toast;

import com.journeyapps.barcodescanner.CaptureActivity;

public class CameraZX extends CaptureActivity {

    private int backButtonPressCount = 0;

    @Override
    public void onBackPressed() {
        if (backButtonPressCount == 0) {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            backButtonPressCount++;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    backButtonPressCount = 0;
                }
            }, 2000);
        } else {
            super.onBackPressed();
            finishAffinity();
        }
    }
}
