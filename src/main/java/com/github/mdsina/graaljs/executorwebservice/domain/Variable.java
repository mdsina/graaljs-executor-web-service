package com.github.mdsina.graaljs.executorwebservice.domain;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import lombok.Builder;
import lombok.Getter;

@JsonPropertyOrder({"name", "value"})
@JsonInclude(Include.NON_NULL)
@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Variable.Builder.class)
public class Variable implements Cloneable {

    private final Object value;
    private final String name;

    @Override
    public Variable clone() {
        try {
            return (Variable) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }
}