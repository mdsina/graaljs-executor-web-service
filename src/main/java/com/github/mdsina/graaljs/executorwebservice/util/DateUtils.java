package com.github.mdsina.graaljs.executorwebservice.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.commons.lang3.StringUtils;

public class DateUtils {

    private static final Map<String, DateTimeFormatter> CACHED_FORMATTERS = new ConcurrentHashMap<>();
    private static final DateTimeFormatter DEFAULT_DATE_FORMATTER;

    // for compatibility with https://www.npmjs.com/package/dateformat
    private static final String[] TOKENS_TO_REPLCE = new String[]{"mm", "MM", "'", "\""};
    private static final String[] REPLACEMENT_TOKENS = new String[]{"MM", "mm", "''", "'"};

    static {
        DEFAULT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        CACHED_FORMATTERS.put("dd.MM.yyyy", DEFAULT_DATE_FORMATTER);
    }

    public static String formatDate(ZonedDateTime dateTime, String format) {
        String newFormat = StringUtils.replaceEach(format, TOKENS_TO_REPLCE, REPLACEMENT_TOKENS);
        return CACHED_FORMATTERS.computeIfAbsent(newFormat, DateTimeFormatter::ofPattern)
            .format(dateTime);
    }

    public static String formatDate(ZonedDateTime dateTime) {
        return DEFAULT_DATE_FORMATTER.format(dateTime);
    }
}
