package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
@ScriptExecutionScope
public class ExecutionScopeDataBridge {

    private Map<String, Object> inputs = new HashMap<>();
    private List<Map<String, Object>> outputs = new ArrayList<>();

    public Object input(String key) {
        String keyInUppercase = key.toUpperCase();

        return inputs.get(keyInUppercase);
    }

    public Object input(String key, Object defaultValue) {
        return Optional.ofNullable(input(key)).orElse(defaultValue);
    }

    public void output(String key, Object value) {
        Map<String, Object> output = new HashMap<>();
        output.put("name", key);
        output.put("value", value);
        outputs.add(output);
    }

    public void setInputs(List<Variable> inputs) {
        this.inputs = inputs
            .stream()
            .filter(o -> o.getValue() != null)
            .collect(Collectors.toMap(o -> o.getName().toUpperCase(), o -> o.getValue().toString()));
    }

    public List<Map<String, Object>> getOutputs() {
        return outputs;
    }
}
