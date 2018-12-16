package com.github.mdsina.graaljs.executorwebservice.dto;

import java.time.LocalDateTime;

public class ScriptDto {

    private final String id;
    private final String body;
    private final LocalDateTime modifyDate;

    public ScriptDto(String id, String body, LocalDateTime modifyDate) {
        this.id = id;
        this.body = body;
        this.modifyDate = modifyDate;
    }

    public String getId() {
        return id;
    }

    public String getBody() {
        return body;
    }

    public LocalDateTime getModifyDate() {
        return modifyDate;
    }
}
