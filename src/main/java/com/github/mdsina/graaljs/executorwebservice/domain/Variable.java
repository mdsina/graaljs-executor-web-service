package com.github.mdsina.graaljs.executorwebservice.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "type",
    visible = true
)
@JsonSubTypes({
    @JsonSubTypes.Type(value = TextVariable.class, name = "TEXT"),
})
@JsonPropertyOrder({"name", "type", "value"})
@JsonInclude(Include.NON_NULL)
public abstract class Variable<T> implements Cloneable {

    private DataType type;
    private T value;
    private String name;

    public Variable() {
    }

    public Variable(DataType type) {
        this.type = type;
    }

    public Variable(DataType type, String name) {
        this.type = type;
        this.name = name;
    }

    public Variable(DataType type, String name, T value) {
        this.type = type;
        this.value = value;
        this.name = name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void setFromString(String value);

    public DataType getType() {
        return type;
    }

    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public Variable clone() {
        try {
            return (Variable) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }
}