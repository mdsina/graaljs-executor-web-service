package com.github.mdsina.graaljs.executorwebservice.web.resources;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = CallRequest.Builder.class)
public class CallRequest {

    private final List<Variable> inputs;
    private final List<Variable> outputs;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }
}
