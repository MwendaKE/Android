package com.mwendasoft.superme.reminders;

import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for converting date-time strings to milliseconds
 */
public class ConvertTimeToMillis {
    private static final SimpleDateFormat UTC_FORMATTER = 
	new SimpleDateFormat("yyyy-MM-dd HHmm", Locale.getDefault());

    static {
        UTC_FORMATTER.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Safe conversion with default fallback (returns 0 on error)
     */
    public static long convertToMillis(String dateTimeString) {
        try {
            return convertToMillisStrict(dateTimeString);
        } catch (ParseException e) {
            e.printStackTrace();
            return 0L;
        }
    }

    /**
     * Strict conversion that throws exceptions
     * @throws ParseException if format is invalid
     */
    public static long convertToMillisStrict(String dateTimeString) throws ParseException {
        synchronized (UTC_FORMATTER) {
            return UTC_FORMATTER.parse(dateTimeString).getTime();
        }
    }

    /**
     * Validates if a date-time string is in correct format
     */
    public static boolean isValidFormat(String dateTimeString) {
        try {
            UTC_FORMATTER.parse(dateTimeString);
            return true;
        } catch (ParseException e) {
            return false;
        }
    }
}
