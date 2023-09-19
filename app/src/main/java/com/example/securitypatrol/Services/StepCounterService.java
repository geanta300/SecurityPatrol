package com.example.securitypatrol.Services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.securitypatrol.Helpers.ConstantsHelper;
import com.example.securitypatrol.MainActivity;
import com.example.securitypatrol.R;

public class StepCounterService extends Service {

    private static final String CHANNEL_ID = "StepCounterServiceChannel";

    private final IBinder binder = new LocalBinder();
    private boolean isShiftActive = false;

    private int initialStepCount = 0;
    boolean isInitialStepCountSet = false;

    private SensorManager sensorManager;
    private Sensor stepCounterSensor;

    SharedPreferences sharedPreferences;

    public class LocalBinder extends Binder {
        public StepCounterService getService() {
            return StepCounterService.this;
        }
    }

        @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sharedPreferences = getSharedPreferences("Steps_technology", MODE_PRIVATE);

        if (intent.getAction().equals(ConstantsHelper.START_FOREGROUND_ACTION)) {
            Log.d("StepCounterService", "Received Start Foreground Intent ");
            startForegroundService();
        }
        else if (intent.getAction().equals(ConstantsHelper.STOP_FOREGROUND_ACTION)) {
            Log.d("StepCounterService", "Received Stop Foreground Intent");

            stopForeground(true);
            stopSelfResult(startId);
        }
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d("StepCounterService", "Service created");
        // Initialize the sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        // Get the step counter sensor
        stepCounterSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
    }

    @Override
    public void onDestroy() {
        Log.d("StepCounterService", "Service destroyed");
        stopSelf();
        super.onDestroy();
    }

    public void startShift() {
        Log.d("StepCounterService", "Shift started");
        isShiftActive = true;
        addSharedPreference("isShiftActive", isShiftActive);
        // Register the step counter sensor listener
        sensorManager.registerListener(stepCounterListener, stepCounterSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }
        //TODO: return the step count so that it can be displayed on the PDF
    private final SensorEventListener stepCounterListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            int stepsTakenSinceStart = 0;
            int currentStepCount;
            if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                if (isShiftActive) {
                    isInitialStepCountSet = sharedPreferences.getBoolean("isInitialStepCountSet", false);
                    if (!isInitialStepCountSet) {
                        initialStepCount = (int) event.values[0];
                        isInitialStepCountSet = true;
                        addSharedPreference("isInitialStepCountSet", isInitialStepCountSet);
                    }
                    currentStepCount = (int) event.values[0];
                    stepsTakenSinceStart = currentStepCount - initialStepCount;
                    broadcastStepCount(stepsTakenSinceStart);
                }
                Log.d("StepCounterService", "Step count: " + stepsTakenSinceStart);
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // Handle accuracy changes if needed
        }
    };

    private void broadcastStepCount(int steps) {
        Intent intent = new Intent("StepCountUpdate");
        intent.putExtra("stepCount", steps);
        sendBroadcast(intent);
    }

    private void startForegroundService() {
        Log.d("StepCounterService", "Starting foreground service");
        createNotificationChannel();

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Step Counter Service")
                .setContentText("Tracking steps...")
                .setSmallIcon(R.drawable.notification_steps_icon)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);
    }

    private void createNotificationChannel() {
        Log.d("StepCounterService", "Creating notification channel");
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Step Counter Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );

        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(serviceChannel);
    }

    public void addSharedPreference(String key, Boolean value) {
        sharedPreferences = getSharedPreferences("Steps_technology", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

}