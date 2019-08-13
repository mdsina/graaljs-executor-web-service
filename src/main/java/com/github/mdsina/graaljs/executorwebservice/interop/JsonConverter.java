package com.github.mdsina.graaljs.executorwebservice.interop;

import org.graalvm.polyglot.Value;

/**
 * JSON converter/parser through graal
 */
public class JsonConverter {

    private final Value parseFunc;
    private final Value stringifyFunc;

    public JsonConverter(Value jsonConverter) {
        parseFunc = jsonConverter.getMember("parse");
        stringifyFunc = jsonConverter.getMember("stringify");
    }

    public Object parse(String json) {
        return parseFunc.execute(json);
    }

    /**
     * Use carefully, only Polyglot's objects can be converted to string
     */
    public String stringify(Object value) {
        if (Value.asValue(value).isHostObject()) {
            throw new IllegalArgumentException("Only Polyglot objects are allowed.");
        }
        return stringifyFunc.execute(value).asString();
    }
}
