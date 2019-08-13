package com.github.mdsina.graaljs.executorwebservice.execution;

import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder(builderClassName = "Builder")
@Getter
public class JsExecutionResult {

    private final List<Variable> outputs;
    private final String stdout;
    private final String stderr;
}
