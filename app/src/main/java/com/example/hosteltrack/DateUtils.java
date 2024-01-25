package com.example.hosteltrack;
import android.content.Context;
import android.content.SharedPreferences;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final String PREF_FILE_NAME = "MyPrefs";
    private static final String LAST_KNOWN_DATE_KEY = "lastKnownDate";

    public static boolean hasDateChanged(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        String lastKnownDate = prefs.getString(LAST_KNOWN_DATE_KEY, "");

        String currentDate = getCurrentDate();

        // If the last known date is different from the current date, return true
        return !lastKnownDate.equals(currentDate);
    }

    public static void saveCurrentDate(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(LAST_KNOWN_DATE_KEY, getCurrentDate());
        editor.apply();
    }

    private static String getCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        return dateFormat.format(new Date());
    }
}
