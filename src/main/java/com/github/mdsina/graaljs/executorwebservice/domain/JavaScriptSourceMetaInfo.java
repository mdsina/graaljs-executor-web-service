package com.github.mdsina.graaljs.executorwebservice.domain;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
public class JavaScriptSourceMetaInfo {

    private final String description;

    @JsonUnwrapped
    private final InvocationInfo invocationInfo;

    private final List<ExampleInfo> examples;

    @Getter
    @lombok.Builder(builderClassName = "Builder")
    public static class ExampleInfo {

        private final String name;

        @JsonUnwrapped
        private final InvocationInfo invocationInfo;
    }
}
