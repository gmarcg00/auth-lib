package dev.auth.lib.utils;

import java.util.Date;

public class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Date currentTimePlusSeconds(Long seconds) {
        return new Date(System.currentTimeMillis() + seconds * 1000);
    }
}
