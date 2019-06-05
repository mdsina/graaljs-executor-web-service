package com.github.mdsina.graaljs.executorwebservice.exports;

import com.github.mdsina.graaljs.executorwebservice.execution.ExecutionScopeDataBridge;
import com.github.mdsina.graaljs.executorwebservice.util.DateUtils;
import com.github.mdsina.graaljs.executorwebservice.util.TransliterationUtil;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SampleUtil {

    private final ExecutionScopeDataBridge executionScopeDataBridge;

    public SampleUtil(ExecutionScopeDataBridge executionScopeDataBridge) {
        this.executionScopeDataBridge = executionScopeDataBridge;
    }

    public String transliterate(String str) {
        return TransliterationUtil.transliterate(str);
    }

    public Object input(String key) {
        return executionScopeDataBridge.input(key);
    }

    public Object input(String key, Object defaultValue) {
        return executionScopeDataBridge.input(key, defaultValue);
    }

    public void output(String key, Object value) {
        executionScopeDataBridge.output(key, value);
    }

    public String formatDate(long timestamp, String format) {
        return DateUtils.formatDate(timestamp, format);
    }

    public String formatDate(long timestamp) {
        return DateUtils.formatDate(timestamp);
    }

    public String formatDate(Instant instant, String format) {
        return DateUtils.formatDate(instant, format);
    }

    public String formatDate(Instant instant) {
        return DateUtils.formatDate(instant);
    }
}
