package com.example.securitypatrol.Helpers;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import java.util.Calendar;
import java.util.Locale;

public class ConstantsHelper {
    private static final String folderName = "SecurityPatrol";
    private static final String photosFolder = "SecurityPatrolPhotos";
    public static final String START_FOREGROUND_ACTION = "com.example.securitypatrol.Services.START_FOREGROUND_ACTION";
    public static final String STOP_FOREGROUND_ACTION = "com.example.securitypatrol.Services.STOP_FOREGROUND_ACTION";

    static SharedPreferences sharedPreferences;

    public static String getAdminPassword(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        return sharedPreferences.getString("adminPass", "admin");
    }

    public static void setAdminPassword(String adminPass, Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("adminPass", adminPass);
        editor.apply();
    }

    public static final String DOCUMENTS_DIRECTORY_PATH =
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS) +
                    "/" + folderName;

    public static final String PDF_DIRECTORY_PATH =
            folderName + "_" +
                    getDateInfo().currentYear + "-" +
                    getDateInfo().currentMonth + "-" +
                    getDateInfo().currentDay + "_" +
                    getDateInfo().currentHour + "." +
                    getDateInfo().currentMinute +
                    ".pdf";

    public static final String PHOTOS_DIRECTORY_PATH = DOCUMENTS_DIRECTORY_PATH + "/" + photosFolder;

    public String dateTimeScanned =
                    getDateInfo().formattedDay + "/" +
                    getDateInfo().formattedMonth + "_" +
                    getDateInfo().currentHour + ":" +
                    getDateInfo().currentMinute + ":" +
                    getDateInfo().currentSecond;


    public static class DateInfo {
        public int currentYear;
        public int currentMonth;
        public int currentDay;
        public String currentHour;
        public String currentMinute;
        public String currentSecond;
        public String formattedMonth;
        public String formattedDay;
    }

    public static DateInfo getDateInfo() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH) + 1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        int currentSecond = calendar.get(Calendar.SECOND);

        String formattedHour = String.format(Locale.getDefault(), "%02d", currentHour);
        String formattedMinute = String.format(Locale.getDefault(), "%02d", currentMinute);
        String formattedSecond = String.format(Locale.getDefault(), "%02d", currentSecond);
        String formattedMonth = String.format(Locale.getDefault(), "%02d", currentMonth);
        String formattedDay = String.format(Locale.getDefault(), "%02d", currentDay);

        DateInfo dateInfo = new DateInfo();
        dateInfo.currentYear = currentYear;
        dateInfo.currentMonth = currentMonth;
        dateInfo.currentDay = currentDay;
        dateInfo.currentHour = formattedHour;
        dateInfo.currentMinute = formattedMinute;
        dateInfo.currentSecond = formattedSecond;
        dateInfo.formattedMonth = formattedMonth;
        dateInfo.formattedDay = formattedDay;

        return dateInfo;
    }

}
