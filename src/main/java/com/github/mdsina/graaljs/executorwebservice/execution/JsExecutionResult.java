package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.util.List;
import java.util.Map;

public class JsExecutionResult {

    private final List<Map<String, Object>> outputs;
    private final String stdout;
    private final String stderr;

    public JsExecutionResult(List<Map<String, Object>> outputs, String stdout, String stderr) {
        this.outputs = outputs;
        this.stdout = stdout;
        this.stderr = stderr;
    }

    public List<Map<String, Object>> getOutputs() {
        return outputs;
    }

    public String getStdout() {
        return stdout;
    }

    public String getStderr() {
        return stderr;
    }
}
