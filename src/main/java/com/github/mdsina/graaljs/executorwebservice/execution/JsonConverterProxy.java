package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.context.annotation.ScriptExecutionScope;
import com.github.mdsina.graaljs.executorwebservice.util.JsonConverter;
import org.springframework.stereotype.Component;

@Component
//@ScriptExecutionScope
public class JsonConverterProxy {

    private JsonConverter jsonConverter;

    public void setJsonConverter(JsonConverter jsonConverter) {
        this.jsonConverter = jsonConverter;
    }

    public Object parse(String json) {
        return jsonConverter.parse(json);
    }

    public String stringify(Object value) {
        return jsonConverter.stringify(value);
    }
}

