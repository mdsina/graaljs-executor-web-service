package com.github.mdsina.graaljs.executorwebservice.script;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder(builderClassName = "Builder")
@JsonDeserialize(builder = Script.Builder.class)
public class Script {

    private final String id;
    private final String body;
    private final LocalDateTime modifyDate;

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder {

    }
}
