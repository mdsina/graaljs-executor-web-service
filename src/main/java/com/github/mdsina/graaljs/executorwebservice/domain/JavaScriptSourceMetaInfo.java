package com.github.mdsina.graaljs.executorwebservice.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;

public class JavaScriptSourceMetaInfo {

    private String description;

    @JsonUnwrapped
    private InvocationInfo invocationInfo;

    private List<ExampleInfo> examples;


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public InvocationInfo getInvocationInfo() {
        return invocationInfo;
    }

    public void setInvocationInfo(InvocationInfo invocationInfo) {
        this.invocationInfo = invocationInfo;
    }

    public List<ExampleInfo> getExamples() {
        return examples;
    }

    public void setExamples(List<ExampleInfo> examples) {
        this.examples = examples;
    }

    public static class ExampleInfo {

        private String name;

        @JsonUnwrapped
        private InvocationInfo invocationInfo;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public InvocationInfo getInvocationInfo() {
            return invocationInfo;
        }

        public void setInvocationInfo(InvocationInfo invocationInfo) {
            this.invocationInfo = invocationInfo;
        }

    }
}
