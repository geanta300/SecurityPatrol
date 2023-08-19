package com.example.securitypatrol.Helpers;

import android.os.Environment;

import java.util.Calendar;

public class ConstantsHelper {
    private static final String folderName = "SecurityPatrol";
    private static final String photosFolder = "SecurityPatrolPhotos";

    public static final String DOCUMENTS_DIRECTORY_PATH =
            Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOCUMENTS) +
                    "/" + folderName;

    public static final String EXCEL_DIRECTORY_PATH =
            folderName + "_" +
            getDateInfo().currentYear + "-" +
            getDateInfo().currentMonth + "-" +
            getDateInfo().currentDay + "_" +
            getDateInfo().currentHour + "." +
            getDateInfo().currentMinute +
            ".xlsx";

    public static final String PDF_DIRECTORY_PATH =
            folderName + "_" +
            getDateInfo().currentYear + "-" +
            getDateInfo().currentMonth + "-" +
            getDateInfo().currentDay + "_" +
            getDateInfo().currentHour + "." +
            getDateInfo().currentMinute +
            ".pdf";

    public static final String PHOTOS_DIRECTORY_PATH = DOCUMENTS_DIRECTORY_PATH + "/" + photosFolder;


    public static class DateInfo {
        public int currentYear;
        public int currentMonth;
        public int currentDay;
        public int currentHour;
        public int currentMinute;
    }

    public static DateInfo getDateInfo() {
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH)+1;
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);

        // Subtract 1 month
        //calendar.add(Calendar.MONTH, -1);

        DateInfo dateInfo = new DateInfo();
        dateInfo.currentYear = currentYear;
        dateInfo.currentMonth = currentMonth;
        dateInfo.currentDay = currentDay;
        dateInfo.currentHour = currentHour;
        dateInfo.currentMinute = currentMinute;

        return dateInfo;
    }
}
