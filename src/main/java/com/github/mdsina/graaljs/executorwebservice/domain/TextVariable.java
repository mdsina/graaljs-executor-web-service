package com.github.mdsina.graaljs.executorwebservice.domain;

public class TextVariable extends Variable<String> {

    public TextVariable() {
        setType(DataType.TEXT);
    }

    public TextVariable(String name, String value) {
        super(DataType.TEXT, name, value);
    }

    public TextVariable(String name) {
        super(DataType.TEXT, name);
    }

    @Override
    public void setFromString(String value) {
        setValue(value);
    }
}
