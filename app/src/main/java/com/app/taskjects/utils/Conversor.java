package com.app.taskjects.utils;

import android.text.format.DateFormat;

import androidx.core.os.ConfigurationCompat;

import com.google.firebase.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public class Conversor {

    public static String timestampToString(Locale locale, Timestamp timestamp) {
        Calendar cal = Calendar.getInstance(locale);
        cal.setTimeInMillis(timestamp.getSeconds() * 1000L);
        return DateFormat.format("dd MMMM yyyy hh:mm:ss", cal).toString();
    }

}
