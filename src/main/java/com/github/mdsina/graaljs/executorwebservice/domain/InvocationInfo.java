package com.github.mdsina.graaljs.executorwebservice.domain;

import java.util.ArrayList;
import java.util.List;

public class InvocationInfo {

    private List<Variable> inputs;
    private List<Variable> outputs;

    public InvocationInfo() {
    }

    public InvocationInfo(List<Variable> inputs, List<Variable> outputs) {
        this.inputs = new ArrayList<>(inputs);
        this.outputs = new ArrayList<>(outputs);
    }

    public List<Variable> getInputs() {
        return inputs;
    }

    public void setInputs(List<Variable> inputs) {
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
