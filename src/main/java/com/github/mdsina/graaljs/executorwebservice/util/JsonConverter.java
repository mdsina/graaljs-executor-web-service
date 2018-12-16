package com.github.mdsina.graaljs.executorwebservice.util;

import org.graalvm.polyglot.Value;

/**
 * JSON converter/parser through graal
 * Problems with jackson deserializing (unknown return type from any json list\map)
 * TODO: remove when solve jackson problem
 */
public class JsonConverter {

    private final Value jsonConverter;

    public JsonConverter(Value jsonConverter) {
        this.jsonConverter = jsonConverter.getMember("parse");
    }

    public Object parse(String json) {
        return jsonConverter.execute(json);
    }
}
