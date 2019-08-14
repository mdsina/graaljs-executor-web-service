package com.github.mdsina.graaljs.executorwebservice.util;

import org.graalvm.polyglot.Value;

/**
 * JSON converter/parser through graal
 * Problems with jackson deserializing (unknown return type from any json list\map)
 * TODO: remove when solve jackson problem
 */
public class JsonConverter {

    private final Value jsonConverter;
    private final Value stringifyFunc;

    public JsonConverter(Value jsonConverter) {
        this.jsonConverter = jsonConverter.getMember("parse");
        stringifyFunc = jsonConverter.getMember("stringify");
    }

    public Object parse(String json) {
        return jsonConverter.execute(json);
    }

    public String stringify(Object value) {
        if (Value.asValue(value).isHostObject()) {
            throw new IllegalArgumentException("Only Polyglot objects are allowed.");
        }
        return stringifyFunc.execute(value).asString();
    }
}
