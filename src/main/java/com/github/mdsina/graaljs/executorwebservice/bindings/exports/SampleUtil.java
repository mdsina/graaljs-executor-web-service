package com.github.mdsina.graaljs.executorwebservice.bindings.exports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mdsina.graaljs.executorwebservice.execution.ScriptDataBridge;
import com.github.mdsina.graaljs.executorwebservice.util.DateUtils;
import com.github.mdsina.graaljs.executorwebservice.util.TransliterationUtil;
import java.time.ZonedDateTime;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SampleUtil {

    private final ScriptDataBridge scriptDataBridge;

    public String transliterate(String str) {
        return TransliterationUtil.transliterate(str);
    }

    public Object input(String key) throws JsonProcessingException {
        return scriptDataBridge.input(key);
    }

    public Object input(String key, Object defaultValue) throws JsonProcessingException {
        return scriptDataBridge.input(key, defaultValue);
    }

    public void output(String key, Object value) {
        scriptDataBridge.output(key, value);
    }

    public String formatDate(ZonedDateTime dateTime) {
        return DateUtils.formatDate(dateTime);
    }

    public String formatDate(ZonedDateTime dateTime, String format) {
        return DateUtils.formatDate(dateTime, format);
    }
}
