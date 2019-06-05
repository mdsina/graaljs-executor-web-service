package com.github.mdsina.graaljs.executorwebservice.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DateUtils {

    private static final Map<String, DateTimeFormatter> CACHED_FORMATTERS = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER;

    static {
        DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        CACHED_FORMATTERS.put("dd.MM.yyyy", DEFAULT_DATE_FORMATTER);
    }

    public static String formatDate(long timestamp, String format) {
        return CACHED_FORMATTERS.computeIfAbsent(format, DateTimeFormatter::ofPattern)
            .format(getDate(timestamp));
    }

    public static String formatDate(long timestamp) {
        return DEFAULT_DATE_FORMATTER.format(getDate(timestamp));
    }

    public static String formatDate(Instant instant, String format) {
        return CACHED_FORMATTERS.computeIfAbsent(format, DateTimeFormatter::ofPattern)
            .format(instant);
    }

    public static String formatDate(Instant instant) {
        return DEFAULT_DATE_FORMATTER.format(instant);
    }

    private static LocalDateTime getDate(long timestamp) {
        return Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDateTime();
    }
}
