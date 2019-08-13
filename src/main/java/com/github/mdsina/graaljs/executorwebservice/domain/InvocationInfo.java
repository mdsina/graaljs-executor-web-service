package com.github.mdsina.graaljs.executorwebservice.domain;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public class InvocationInfo {

    private final List<Variable> inputs;
    private final List<Variable> outputs;
}
