package com.github.mdsina.graaljs.executorwebservice.interop;

import com.github.mdsina.graaljs.executorwebservice.spring.context.annotation.ScriptExecutionScope;
import org.springframework.stereotype.Component;

@Component
@ScriptExecutionScope
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
