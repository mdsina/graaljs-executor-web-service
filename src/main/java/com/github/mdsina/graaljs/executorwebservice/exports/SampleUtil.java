package com.github.mdsina.graaljs.executorwebservice.exports;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.mdsina.graaljs.executorwebservice.execution.ExecutionScopeDataBridge;
import com.github.mdsina.graaljs.executorwebservice.util.TransliterationUtil;

public class SampleUtil {

    private final ExecutionScopeDataBridge executionScopeDataBridge;

    public SampleUtil(ExecutionScopeDataBridge executionScopeDataBridge) {
        this.executionScopeDataBridge = executionScopeDataBridge;
    }

    public String transliterate(String str) {
        return TransliterationUtil.transliterate(str);
    }

    public Object input(String key) throws JsonProcessingException {
        return executionScopeDataBridge.input(key);
    }

    public Object input(String key, Object defaultValue) throws JsonProcessingException {
        return executionScopeDataBridge.input(key, defaultValue);
    }

    public void output(String key, Object value) {
        executionScopeDataBridge.output(key, value);
    }
}
