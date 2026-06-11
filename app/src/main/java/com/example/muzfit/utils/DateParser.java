package com.example.muzfit.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public final class DateParser {

    private static final String API_DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";
    private static final SimpleDateFormat API_FORMAT =
            new SimpleDateFormat(API_DATE_PATTERN, Locale.US);

    static {
        API_FORMAT.setTimeZone(TimeZone.getDefault());
    }

    private DateParser() {
    }

    public static long parseApiDate(String date) {
        if (date == null || date.isEmpty()) {
            return 0L;
        }
        try {
            Date parsed = API_FORMAT.parse(date);
            return parsed != null ? parsed.getTime() : 0L;
        } catch (ParseException e) {
            return 0L;
        }
    }

    public static boolean isSameDay(long dateMillis, long targetMillis) {
        if (dateMillis == 0L || targetMillis == 0L) {
            return false;
        }
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateMillis);
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(targetMillis);
        return date.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && date.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR);
    }

    public static boolean isSameMonth(long dateMillis, int year, int month) {
        if (dateMillis == 0L) {
            return false;
        }
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dateMillis);
        return date.get(Calendar.YEAR) == year && date.get(Calendar.MONTH) == month;
    }
}
