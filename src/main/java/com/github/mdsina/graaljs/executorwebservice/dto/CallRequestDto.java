package com.github.mdsina.graaljs.executorwebservice.dto;

import com.github.mdsina.graaljs.executorwebservice.domain.Variable;
import java.util.List;

public class CallRequestDto {

    private List<Variable> inputs;
    private List<Variable> outputs;

    public List<Variable> getInputs() {
        return inputs;
    }

    public void setInputs(
        List<Variable> inputs) {
        this.inputs = inputs;
    }

    public List<Variable> getOutputs() {
        return outputs;
    }

    public void setOutputs(
        List<Variable> outputs) {
        this.outputs = outputs;
    }
}
