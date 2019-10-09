package com.github.mdsina.graaljs.executorwebservice.execution;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mdsina.graaljs.executorwebservice.interop.JsonConverter;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.graalvm.polyglot.Value;
import org.springframework.util.ClassUtils;

@RequiredArgsConstructor
public class ScriptDataBridge {

    private final JsonConverter jsonConverter;
    private final ObjectMapper objectMapper;

    private Map<String, Object> inputs = new HashMap<>();
    private List<Map<String, Object>> outputs = new ArrayList<>();

    public Object input(String key) throws JsonProcessingException {
        Object value = inputs.get(key);

        if (value == null) {
            return null;
        }

        if (value instanceof List || value instanceof TreeNode) {
            // graal\nashorn cannot handle List from java into Array in JS
            // see https://github.com/graalvm/graaljs/issues/88
            // TODO: remove condition when implemented in graal.js
            // Also JsonVariable value used as object in JS calls, so need to parse them as JS objects
            return jsonConverter.parse(objectMapper.writeValueAsString(value));
        }

        if (!ClassUtils.isPrimitiveOrWrapper(value.getClass())) {
            if (value instanceof BigDecimal) {
                return ((BigDecimal) value).doubleValue();
            }
            if (value instanceof BigInteger) {
                return ((BigInteger) value).longValue();
            }
            return value.toString();
        }

        return value;
    }

    public Object input(String key, Object defaultValue) throws JsonProcessingException {
        if (!inputs.containsKey(key)) {
            return defaultValue;
        }
        return input(key);
    }

    public void output(String key, Object value) {
        Value polyglotValue = (value instanceof Value)
            ? (Value) value
            : Value.asValue(value);

        Object result = value;
        if (polyglotValue.isDate()) {
            result = polyglotValue.asInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().toString();
        }

        Map<String, Object> output = new HashMap<>();
        output.put("name", key);
        output.put("value", result);
        outputs.add(output);
    }

    public void setInputs(List<Variable> inputs) {
        this.inputs = inputs
            .stream()
            .filter(o -> o.getValue() != null)
            .collect(Collectors.toMap(
                Variable::getName,
                Variable::getValue
            ));
    }

    public List<Map<String, Object>> getOutputs() {
        return outputs;
    }
}
