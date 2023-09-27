package com.example.securitypatrol.Helpers;

import android.os.CountDownTimer;
import android.widget.TextView;


public class ShiftTimer {
    private TextView textView;
    private CountDownTimer countDownTimer;

    public ShiftTimer(TextView textView) {
        this.textView = textView;
    }

    public void startTimer(long endTimeMillis, final String onFinishText) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        long currentTimeMillis = System.currentTimeMillis();

        countDownTimer = new CountDownTimer(endTimeMillis - currentTimeMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                long secondsRemaining = millisUntilFinished / 1000;
                int hours = (int) (secondsRemaining / 3600);
                int minutes = (int) ((secondsRemaining % 3600) / 60);
                int seconds = (int) (secondsRemaining % 60);

                String timeLeft = "Mai ai la dispozitie: " + hours + "h " + minutes + "m ";
                textView.setText(timeLeft);
            }

            @Override
            public void onFinish() {
                textView.setText(onFinishText);
            }
        };

        countDownTimer.start();
    }

    public void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}

